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

package fxtalk.ui.settings;

import fxtalk.FXTalkApp;
import fxtalk.utils.ChatNodeUIUtil;
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.Util;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Has 2 components 1) Top bar (Can be clicked to show/hide settings, has a
 * close App button) 2) Bottom bar (Contains settings like network panel,
 * accounts panel etc)
 *
 * @author srikalyanchandrashekar
 */
public class SettingsPanel extends Stage {
    public static int WIDTH = 305;
    public static int HEIGHT = 420;
    

    public Tab ntTab = new Tab("Proxy");
    public Tab accntTab = new Tab("Accnts");
    public Tab chatSettingTab = new Tab("Chat");

    public boolean isShoing = false;
    DoubleProperty stageX = new SimpleDoubleProperty(0);
    DoubleProperty stageY = new SimpleDoubleProperty(0);
    ChangeListener<Number> stageXChangeListener = null;
    ChangeListener<Number> stageYChangeListener = null;
    
    FlowPane root;
    Group topBar;
    public Group bottomBar;
    ImageView topBarImgView = ImageViewBuilder.create().image(UIutil.getImage("settings.png")).smooth(true).cache(true).preserveRatio(true).visible(true).build();
    ImageView topBarPressedImgView = ImageViewBuilder.create().image(UIutil.getImage("settingsPressed.png")).smooth(true).cache(true).preserveRatio(true).visible(false).build();
    Rectangle clipRectBottomBar = RectangleBuilder.create().width(WIDTH).height(HEIGHT - 26).arcWidth(15).arcHeight(15).fill(Color.WHITE).build();
    Shape bottomOutlineStrip = ChatNodeUIUtil.getTransparentRectWithBorder(WIDTH, HEIGHT - 26, 15, 15, 3, Color.DARKGRAY, 0.6);
    Timeline timeline = new Timeline();
    TabPane settingsPane = new TabPane();
    double settingPaneStageStartX = 0;
    double settingPaneStageStartY = 0;
    double chatLogPaneStageStartX = 0;
    double chatLogPaneStageStartY = 0;
    double loginAppPaneStageStartX = 0;
    double loginApprPaneStageStartY = 0;
    double mainAppPaneStageStartX = 0;
    double mainApprPaneStageStartY = 0;
    ////Timings to track click and drag, move etc
    long mousePressRegisterTime = 0;
    long mouseReleaseRegisterTime = 0;
    private final long MAX_DILATION_FOR_ANIMATION = 250;//ms

    public SettingsPanel() {
        initStyle(StageStyle.TRANSPARENT);
        root = new FlowPane(Orientation.VERTICAL);
        topBar = new Group();
        bottomBar = new Group();
        bottomBar.setClip(clipRectBottomBar);

        topBar.getChildren().addAll(topBarPressedImgView, topBarImgView,  createCloseForApp());
        root.getChildren().addAll(topBar, bottomBar);

        initTopBar();
        initOwner(FXTalkApp.app.primaryStage);
    }


    /**
     * The bar that looks like moving part of a harmonium which can be clicked
     * to show/hide settings.
     */
    private void initTopBar() {
        stageXChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                setX(stageX.getValue());
            }
        };
        stageYChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                setY(stageY.getValue());
            }
        };
        topBar.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                topBar.setCursor(Cursor.HAND);
            }
        });
        topBar.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                topBar.setCursor(Cursor.DEFAULT);
            }
        });
        topBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                topBar.setCursor(Cursor.CLOSED_HAND);
                topBarPressedImgView.setVisible(true);
                topBarImgView.setVisible(false);
                //TODO: uncomment below
                FXTalkApp.app.primaryStage.toFront();//This is must
                setY(getY() + 4);//This is needed cause a smaller image now occupies....
                //Record the following to make the entire app draggable
                mousePressRegisterTime = System.currentTimeMillis();

                settingPaneStageStartX = me.getScreenX() - getX();
                settingPaneStageStartY = me.getScreenY() - getY();

                chatLogPaneStageStartX = me.getScreenX() - FXTalkApp.app.chatLogPanel.getX();
                chatLogPaneStageStartY = me.getScreenY() - FXTalkApp.app.chatLogPanel.getY();

                if (FXTalkApp.app.loginScreen.isShowing()) {
                    loginAppPaneStageStartX = me.getScreenX() - FXTalkApp.app.loginScreen.getX();
                    loginApprPaneStageStartY = me.getScreenY() - FXTalkApp.app.loginScreen.getY();
                }

                mainAppPaneStageStartX = me.getScreenX() - FXTalkApp.app.primaryStage.getX();
                mainApprPaneStageStartY = me.getScreenY() - FXTalkApp.app.primaryStage.getY();

                //TODO: uncomment below
                FXTalkApp.app.chatLogPanel.toFront();
                FXTalkApp.app.loginScreen.toFront();
                FXTalkApp.app.primaryStage.toFront();
            }
        });
        topBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                //Register changelisteners otherwise animation will not work.
                stageX.addListener(stageXChangeListener);
                stageY.addListener(stageYChangeListener);
                
                topBarImgView.setVisible(true);
                topBarPressedImgView.setVisible(false);
                setY(getY() - 4);//This is to offset what we did in the seOnMousePressed.
                mouseReleaseRegisterTime = System.currentTimeMillis();
                //We do not open up the settings panel if the click action is less then MAX_DILATION(This makes sense when app is dragged)
                if (mouseReleaseRegisterTime - mousePressRegisterTime <= MAX_DILATION_FOR_ANIMATION) {
                    animate();
                }
            }
        });

        topBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                //Remove because we change stageX/Y values and yet we dont want to update stage' X,Y because of stageX/Y's change
                stageX.removeListener(stageXChangeListener);
                stageY.removeListener(stageYChangeListener);
                //For draggability of the entire app
                setX(me.getScreenX() - settingPaneStageStartX);
                setY(me.getScreenY() - settingPaneStageStartY);
                
                //StageX/Y needs update because when you drag stage and then click(to animate then stageX will start from old values)
                //This could have been avoided if there was xProperty and yProperty for stage so that we can bind bidirectionally.
                stageX.set(getX());
                stageY.set(getY());
                
                
                FXTalkApp.app.chatLogPanel.setX(me.getScreenX() - chatLogPaneStageStartX);
                FXTalkApp.app.chatLogPanel.setY(me.getScreenY() - chatLogPaneStageStartY);

                if (FXTalkApp.app.loginScreen.isShowing()) {
                    FXTalkApp.app.loginScreen.setX(me.getScreenX() - loginAppPaneStageStartX);
                    FXTalkApp.app.loginScreen.setY(me.getScreenY() - loginApprPaneStageStartY);
                }


                FXTalkApp.app.primaryStage.setX(me.getScreenX() - mainAppPaneStageStartX);
                FXTalkApp.app.primaryStage.setY(me.getScreenY() - mainApprPaneStageStartY);
            }
        });

    }

    /**
     * 1) Create the Network panel, Accounts panel and profile panel etc.. 2)
     * Set dimensions for the panel, create scene etc.
     */
    public void initialize() {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.getStylesheets().add(UIutil.getRsrcURI("fxtalk.css").toString());
        setScene(scene);
        scene.setFill(Color.TRANSPARENT);

        ntTab.setGraphic(ImageViewBuilder.create().image(UIutil.getImage("proxy.png")).scaleX(0.7).scaleY(0.7).build());
        ntTab.setContent(createNtPanel());
        ntTab.setClosable(false);
        Tooltip nt_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Network Settings").alignment(Pos.CENTER).build()).build();
        nt_tt.getStyleClass().add("settings_tt");
        ntTab.setTooltip(nt_tt);


        accntTab.setGraphic(ImageViewBuilder.create().image(UIutil.getImage("accounts.png")).scaleX(0.7).scaleY(0.7).build());
        accntTab.setContent(createAccountsPanel());
        accntTab.setClosable(false);
        Tooltip ja_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Jabber Accounts").alignment(Pos.CENTER).build()).build();
        ja_tt.getStyleClass().add("settings_tt");
        accntTab.setTooltip(ja_tt);



        chatSettingTab.setGraphic(ImageViewBuilder.create().image(UIutil.getImage("chatSettings.png")).scaleX(0.7).scaleY(0.7).build());
        chatSettingTab.setContent(createChatPanel());
        chatSettingTab.setClosable(false);
        Tooltip cs_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Chat Settings").alignment(Pos.CENTER).build()).build();
        cs_tt.getStyleClass().add("settings_tt");
        chatSettingTab.setTooltip(cs_tt);

        settingsPane.setPrefSize(WIDTH, HEIGHT - 24);
        settingsPane.getTabs().addAll(ntTab, accntTab, chatSettingTab);
        settingsPane.setTabMaxWidth(WIDTH/3.5);
        
        bottomBar.getChildren().addAll(settingsPane, bottomOutlineStrip);
        bottomBar.setVisible(false);//Initially this is needed


    }

    /**
     * Called after initialization
     */
    public void postInit() {
        stageX.setValue(getX());//Initialize to the stage's current x val
        stageY.setValue(getY());//Initialize to the stage's current y val

    }

    /**
     * Network settings like proxy host,port.
     *
     * @return
     */
    private Node createNtPanel() {
        return new NetworkTab(this);
    }

    /**
     * Manage accounts like set default account, delete account, see whats the
     * current default account
     *
     * @return
     */
    private Node createAccountsPanel() {
        return new AccountsTab(this);
    }


    /**
     * Change notification settings, save chat history etc
     *
     * @return
     */
    private Node createChatPanel() {
        return new ChatSettingsTab(this);
    }

    /**
     * Component when clicked will close the FXTalk app.
     *
     * @return
     */
    private Node createCloseForApp() {
        Image close = UIutil.getImage("shutdown.png");
        final ImageView closeImgView = ImageViewBuilder.create().cache(true).smooth(true).opacity(1.0).image(close).cursor(Cursor.HAND).build();

        Circle overlay = CircleBuilder.create().radius(9).centerX(close.getWidth()/2.0).centerY(close.getWidth()/2.0).fill(Color.TRANSPARENT).build();
        Group closeRoot = GroupBuilder.create().mouseTransparent(false).children(closeImgView, overlay).translateX(WIDTH - 35).translateY(2).build();
        
        Tooltip close_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Close Application").alignment(Pos.CENTER).build()).build();
        close_tt.getStyleClass().add("settings_tt");
        Tooltip.install(closeRoot, close_tt);
        closeRoot.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (FXTalkApp.app.conn != null) {
                    FXTalkApp.app.conn.connection.disconnect();
                }
                FXTalkApp.app.chatLogPanel.close();
                FXTalkApp.app.loginScreen.close();
                FXTalkApp.app.splashScreen.close();
                FXTalkApp.app.primaryStage.close();
                Util.cleanup();
                Platform.exit();
                System.exit(0);//Platform.exit() is not closing the VM. Needs to investigate which threads are still alive ...
            }
        });
        return closeRoot;
    }

    /**
     * Animates and either shows the settings panel to the side of Main stage or
     * hides it back. This can be used any where which has access to the
     * SettingsPanel object. For Ex: Al the Tabs like NtTab, AccountsTab,
     * StatusTab etc.
     */
    public void animate() {
        if (timeline != null && timeline.getStatus() != Animation.Status.STOPPED) {
            return;//The animation is in progress just ignore.
        }
        timeline = new Timeline();
        if (!isShoing) {//If the settings panel is not seen then show it
            //To begin with make sure we are in right track so set the initial values to correct ones.
            stageX.set(FXTalkApp.app.primaryStage.getX());
            stageY.set(FXTalkApp.app.primaryStage.getY() - 23);
            
            
            final KeyValue kvbegin1 = new KeyValue(stageX, getX(), Interpolator.EASE_BOTH);
            final KeyFrame kfbegin1 = new KeyFrame(Duration.millis(10), kvbegin1);

            final KeyValue kvmiddle1 = new KeyValue(stageX, getX() + getWidth(), Interpolator.EASE_BOTH);
            final KeyFrame kfmiddle1 = new KeyFrame(Duration.millis(500), kvmiddle1);

            final KeyValue kvend1 = new KeyValue(stageY, getY() + 23, Interpolator.EASE_IN);
            final KeyFrame kfend1 = new KeyFrame(Duration.millis(1000), kvend1);


            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(kfbegin1, kfmiddle1, kfend1);
            timeline.play();
            bottomBar.setVisible(true);

            isShoing = true;
        } else {
            //To begin with make sure we are in right track so set the initial values to correct ones.
            stageX.set(FXTalkApp.app.primaryStage.getX() + getWidth());
            stageY.set(FXTalkApp.app.primaryStage.getY());

            final KeyValue kvbegin1 = new KeyValue(stageY, getY(), Interpolator.EASE_OUT);
            final KeyFrame kfbegin1 = new KeyFrame(Duration.millis(10), kvbegin1);

            final KeyValue kvmiddle1 = new KeyValue(stageY, getY() - 23, Interpolator.EASE_OUT);
            final KeyFrame kfmiddle1 = new KeyFrame(Duration.millis(200), kvmiddle1);

            final KeyValue kvend1 = new KeyValue(stageX, getX() - getWidth(), Interpolator.EASE_BOTH);
            final KeyFrame kfend1 = new KeyFrame(Duration.millis(800), kvend1);
            final KeyFrame kfend2 = new KeyFrame(Duration.millis(800), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent arg0) {
                    bottomBar.setVisible(false);
                    //app.settingsPanel.toFront();//Otherwise tooltip for close button and signout buttons will not be visible fully
                    FXTalkApp.app.loginScreen.setX(FXTalkApp.app.settingsPanel.getX());
                    FXTalkApp.app.loginScreen.setY(FXTalkApp.app.settingsPanel.getY() + 23);
                }
            });
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(kfbegin1, kfmiddle1, kfend1, kfend2);
            timeline.play();
            isShoing = false;
        }
        ///The following will ensure the window's z order is maintained
        FXTalkApp.app.chatLogPanel.toFront();
        FXTalkApp.app.settingsPanel.toFront();
        FXTalkApp.app.primaryStage.toFront();
        FXTalkApp.app.loginScreen.toFront();

    }
}
