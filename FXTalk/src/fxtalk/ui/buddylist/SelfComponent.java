/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fxtalk.ui.buddylist;

import fxtalk.FXTalkApp;
import fxtalk.ui.misc.ImageEditor;
import fxtalk.ui.misc.SwitchComponent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import fxtalk.ui.misc.UIutil;
import fxtalk.ui.settings.PresenceTabletComponent;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.PresenceUtil;
import fxtalk.utils.Util;
import java.io.ByteArrayInputStream;
import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import org.jivesoftware.smack.packet.Presence;

/**
 * User avatar, status message container.
 *
 * @author srikalyc
 */
public class SelfComponent extends StackPane {

    public int WIDTH = 305;
    public int HEIGHT = 45;
    public int AVATAR_W = 40;
    public int AVATAR_H = 40;
    public Node bgnd = null;
    public Group avatar = null;
    public ImageView avatarImgView =null;
    public Group status = null;
    public TextField statusEditable = null;
    public Label statusLabel = null;
    public Label statusPromptLabel = null;

    public Text signoutLabel = null;
    public SwitchComponent signout = null;
    public ImageView presenceImgView = null;
    public ObjectProperty<Presence> currentPresence = new SimpleObjectProperty<Presence>();//We will set this to ONLINE as soon as login succeeds.
    private HBox root = null;
    private DropShadow ds = new DropShadow();
    private InnerShadow is = InnerShadowBuilder.create().offsetY(1).radius(1).color(Color.rgb(60, 60, 60)).build();
    public PresenceTabletComponent presenceTabletComp = null;
    public ImageEditor imageEditorPopup = null;

    
    public SelfComponent() {
        signoutLabel = TextBuilder.create().text("Logout").fill(Color.rgb(84, 73, 71)).font(Font.font(Font.getDefault().getFamily(), 10)).build();
        bgnd = createBackground();
        avatar = createAvatar();
        status = createStatusComponent();
        signout = createSignoutForApp();
        presenceImgView = createPresenceIndicator();
        
        VBox signoutVbox = VBoxBuilder.create().spacing(2).alignment(Pos.CENTER).children(signoutLabel, signout).build();
        VBox statusPresenceVbox = VBoxBuilder.create().spacing(4).alignment(Pos.CENTER).children(status, presenceImgView).build();
        root = HBoxBuilder.create().spacing(15).translateX(10).children(avatar, statusPresenceVbox, signoutVbox).build();
        root.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(bgnd, root);
        initPresenceListener();
    }
/**
 * When current presence changes we update the image
 */
    private void initPresenceListener() {
        currentPresence.addListener(new ChangeListener<Presence>() {
            @Override
            public void changed(ObservableValue<? extends Presence> ov, Presence oldV, Presence newV) {
                Presence presToBeSaved = new Presence(newV.getType(), newV.getStatus(), 128, newV.getMode());
                if (PresenceUtil.isTypeAvailable(newV)) {
                    if (PresenceUtil.isModeNull(newV) || PresenceUtil.isModeAvailable(newV) || PresenceUtil.isModeChat(newV)) {
                        presenceImgView.setImage(UIutil.getImage("online.png"));
                        presenceTabletComp.busyTabletSelected.set(false);
                        presenceTabletComp.availableTabletSelected.set(true);
                        presenceTabletComp.invisibleTabletSelected.set(false);
                    } else if (PresenceUtil.isModeAway(newV) || PresenceUtil.isModeXa(newV)) {
                        presenceImgView.setImage(UIutil.getImage("idle.png"));
                        //Because we dont want to save idle but available
                        presToBeSaved.setMode(Presence.Mode.available);
                        presenceTabletComp.busyTabletSelected.set(false);
                        presenceTabletComp.availableTabletSelected.set(true);
                        presenceTabletComp.invisibleTabletSelected.set(false);
                    } else if (PresenceUtil.isModeDnd(newV)) {
                        presenceImgView.setImage(UIutil.getImage("busy.png"));
                        presenceTabletComp.busyTabletSelected.set(true);
                        presenceTabletComp.availableTabletSelected.set(false);
                        presenceTabletComp.invisibleTabletSelected.set(false);
                    }
                    AccountsDBStore.savePresence(FXTalkApp.app.conn.accnt.getUserName(), presToBeSaved);
                } else {
                    presenceImgView.setImage(UIutil.getImage("offline.png"));
                    presenceTabletComp.busyTabletSelected.set(false);
                    presenceTabletComp.availableTabletSelected.set(false);
                    presenceTabletComp.invisibleTabletSelected.set(true);
                    if (newV.getProperty("invisible") != null && newV.getProperty("invisible").equals("true")) {
                        AccountsDBStore.savePresence(FXTalkApp.app.conn.accnt.getUserName(), presToBeSaved);
                    } else {
                        Util.DEBUG("Signout , we dont save any presence...");
                        //We are going offline(not just invisible so don't save any presence)
                    }
                }
            }
        });
        
    }
/**
 * Call this after each successful login
 */
    public void loadPresenceOrSetDefault() {
        //////////Load Presence if saved or set the default available
        AccountsDBStore.loadKnownPresence(FXTalkApp.app.conn.accnt.getUserName());
        if (AccountsDBStore.savedPresence.containsKey(FXTalkApp.app.conn.accnt.getUserName())) {
            currentPresence.set(AccountsDBStore.savedPresence.get(FXTalkApp.app.conn.accnt.getUserName()));
        } else {
            currentPresence.set(new Presence(Presence.Type.available));
        }
        //This will ensure your presence is sent out as soon as you login
        FXTalkApp.app.conn.connection.sendPacket(currentPresence.get());
    }
    
    /**
     * Call this after successful login
     */
    public void loadStatusOrSetDefault() {
        AccountsDBStore.loadKnownStatus(FXTalkApp.app.conn.accnt.getUserName());
        String initialStatus = AccountsDBStore.savedStatus.get(FXTalkApp.app.conn.accnt.getUserName());
        if (initialStatus != null) {
            statusEditable.setText(initialStatus);
            FXTalkApp.app.conn.changeStatusMessage(initialStatus);
        }
    }
    
    /**
     * Call this after successful login
     */
    public void loadAvatarOrSetDefault() {
        FXTalkApp.app.conn.changeAvatar();
        byte[] imgBytes = FXTalkApp.app.conn.personalVCard.getAvatar();
        if (imgBytes != null) {
            FXTalkApp.app.conn.avatarSelfNonLocal = new Image(new ByteArrayInputStream(imgBytes));
        } else {
            FXTalkApp.app.conn.avatarSelfNonLocal = UIutil.getImage("default_avatar.jpg");
        }
        if (AccountsDBStore.doesAvatarFileExist()) {
            FXTalkApp.app.conn.avatarSelfLocal = UIutil.getImageFullUrl(AccountsDBStore.BASE_LOCATION + File.separator + AccountsDBStore.avatarFile);;
        } else {
            FXTalkApp.app.conn.avatarSelfLocal = UIutil.getImage("default_avatar.jpg");
        }
    }
    /**
     * When a user log ins successfully he needs to call this method.
     */
    public void loadSignoutComponentOrSetDefault() {
       signout.setState(true);
    }
    
    
/**
 * Create background
 * @return 
 */    
    private Node createBackground() {
//        Stop[] stops = new Stop[]{new Stop(0, Color.rgb(249, 248, 246)), new Stop(1.0, Color.rgb(223, 222, 221))};
//        LinearGradient lg = new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, stops);
//        bgnd = RectangleBuilder.create().width(WIDTH - 1).height(HEIGHT - 1).arcWidth(15).arcHeight(15).strokeWidth(1.5).stroke(Color.rgb(144, 133, 131)).fill(lg).effect(ds).build();
        Image img = UIutil.getImage("self.png");
        ImageView imgView = ImageViewBuilder.create().smooth(true).cache(true).preserveRatio(false).image(img).build();
        return imgView;
    }
/**
 * Create status component
 * @return 
 */
    private Group createStatusComponent() {
        String initialStatus = "";
        
        Group gpId = GroupBuilder.create().build();
        statusEditable = TextFieldBuilder.create().text(initialStatus).alignment(Pos.CENTER).translateX(3).translateY(3).styleClass("ntTextField").minWidth(WIDTH - AVATAR_W - 120).maxWidth(WIDTH - AVATAR_W - 120).visible(false).effect(is).build();
        statusLabel = LabelBuilder.create().text(initialStatus).textFill(Color.rgb(114, 103, 101)).minWidth(WIDTH - AVATAR_W - 120).maxWidth(WIDTH - AVATAR_W - 120).translateY(5).alignment(Pos.CENTER).build();
        statusPromptLabel = LabelBuilder.create().text("Custom Message").mouseTransparent(true).textFill(Color.rgb(114, 103, 101)).minWidth(WIDTH - AVATAR_W - 120).maxWidth(WIDTH - AVATAR_W - 120).translateY(5).alignment(Pos.CENTER).build();
        statusPromptLabel.visibleProperty().bind(statusLabel.textProperty().isNull().or(statusLabel.textProperty().isEqualTo("")));
        statusLabel.textProperty().bind(statusEditable.textProperty());
        statusLabel.visibleProperty().bind(statusEditable.visibleProperty().not());
        final StringBuilder previousStatus = new StringBuilder();
        statusLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                statusEditable.setVisible(true);
                previousStatus.delete(0, previousStatus.toString().length());//Delete the entire content
                previousStatus.append(statusLabel.getText());//Put the new content
            }
        });
        Tooltip sl_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Click to change your status").alignment(Pos.CENTER).build()).build();
        sl_tt.getStyleClass().add("settings_tt");
        Tooltip.install(statusLabel, sl_tt);
        
        statusEditable.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ESCAPE) {//Reset to older state(no change in status)
                    statusEditable.setText(previousStatus.toString());
                    statusEditable.setVisible(false);
                }
            }
        });
        statusEditable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                statusEditable.setVisible(false);
                if (statusEditable.getText().equals(previousStatus.toString())) {
                    //Nothing to change in the status
                } else {
                    FXTalkApp.app.conn.changeStatusMessage(statusEditable.getText());
                    AccountsDBStore.saveStatus(FXTalkApp.app.conn.accnt.getUserName(), statusEditable.getText());
                }
            }
        });
        gpId.getChildren().addAll(statusEditable, statusLabel, statusPromptLabel);
        return gpId;
    }
/**
 * Create avatar image
 * @return 
 */
    private Group createAvatar() {
        imageEditorPopup = new ImageEditor(this);
        
        Image initialAvatarImg = null;
        if (AccountsDBStore.doesAvatarFileExist()) {
            initialAvatarImg = UIutil.getImageFullUrl(AccountsDBStore.BASE_LOCATION + File.separator + AccountsDBStore.avatarFile);;
        } else {
            initialAvatarImg = UIutil.getImage("default_avatar.jpg");
        }
        Rectangle avatarClipper = RectangleBuilder.create().width(AVATAR_W).height(AVATAR_H).arcWidth(10).arcHeight(10).build();
        avatarImgView = ImageViewBuilder.create().smooth(true).cache(true).fitHeight(AVATAR_H).fitWidth(AVATAR_W).preserveRatio(false).image(initialAvatarImg).clip(avatarClipper).build();
        Group gp = GroupBuilder.create().children(avatarImgView).effect(ds).cursor(Cursor.HAND).build();
        Tooltip av_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Change Avatar").alignment(Pos.CENTER).build()).build();
        av_tt.getStyleClass().add("settings_tt");
        Tooltip.install(gp, av_tt);
        
        gp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                imageEditorPopup.showPopup(t.getScreenX(), t.getScreenY());
                t.consume();//Should not go below(as the primaryStage's base has mouseClickedHandler and will bring some other stage to front , so this popup will not be visible
            }
        });
        return gp;
    }

    /**
     * Will sign off the user and bring the LoginScreen visible.
     *
     * @return
     */
    private SwitchComponent createSignoutForApp() {
        SwitchComponent logout = new SwitchComponent("") {
            @Override
            public void performOffAction() {
                FXTalkApp.app.appState.set(FXTalkApp.AppState.NOT_AUTHENTICATED);
                
                FXTalkApp.app.conn.goOffline();
                FXTalkApp.app.conn.tearDownConnection();
                FXTalkApp.app.startShowingPresenceNotifics = false;
                
                FXTalkApp.app.palListPanel.resetPanel();
                FXTalkApp.app.chatLogPanel.resetPanel();
                FXTalkApp.app.notifPanel.resetPanel();

                FXTalkApp.app.chatLogPanel.hide();
                FXTalkApp.app.chatLogPanel.isShoing = false;
                
                FXTalkApp.app.primaryStage.hide();//This will take care to hide the pallist panel
                
                
                if (FXTalkApp.app.settingsPanel.isShoing) {
                    FXTalkApp.app.settingsPanel.hide();
                    FXTalkApp.app.settingsPanel.animate();
                    FXTalkApp.app.settingsPanel.show();
                }

                FXTalkApp.app.loginScreen.resetLoginScreen(true);
                FXTalkApp.app.loginScreen.setX(FXTalkApp.app.settingsPanel.getX());
                FXTalkApp.app.loginScreen.setY(FXTalkApp.app.settingsPanel.getY() + 23);
                FXTalkApp.app.loginScreen.show();
                AccountsDBStore.saveChatLog();
            }
            @Override
            public void performOnAction() {
                //TODO: nothing here as of now
            }
        };
        logout.setState(true);
        return logout;
    }
/**
 * Create presence indicator ,and popup to handle user input for presence changes.
 * @return 
 */
    private ImageView createPresenceIndicator() {
        presenceTabletComp = new PresenceTabletComponent();
        
        presenceImgView = ImageViewBuilder.create().smooth(true).cache(true).clip(RectangleBuilder.create().width(58).height(5).arcWidth(3).arcHeight(3).build()).
                image(UIutil.getImage("offline.png")).preserveRatio(false).cursor(Cursor.HAND).build();
        Tooltip presIV_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Change Presence").alignment(Pos.CENTER).build()).build();
        presIV_tt.getStyleClass().add("settings_tt");
        Tooltip.install(presenceImgView, presIV_tt);
        
        presenceImgView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                presenceTabletComp.showPopup(t.getScreenX(), t.getScreenY());
                t.consume();//Should not go below(as the primaryStage's base has mouseClickedHandler and will bring some other stage to front , so this popup will not be visible
            }
        });
        
        return presenceImgView;
    }
    
    public void setToLocalAvatar() {
        String avatarFilePath = AccountsDBStore.BASE_LOCATION + File.separator + AccountsDBStore.avatarFile;
        Image newImage = UIutil.getImageFullUrl(avatarFilePath);
        if (newImage != null) {
            avatarImgView.setImage(newImage);
        } else {
            avatarImgView.setImage(UIutil.getImage("default_avatar.jpg"));
        }
    }
    
}
