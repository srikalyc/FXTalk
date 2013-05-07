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
import fxtalk.ui.file.FileTransferUtils;
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.PresenceUtil;
import fxtalk.utils.Util;
import java.io.File;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooserBuilder;
import javafx.util.Duration;
import org.jivesoftware.smack.packet.Presence;

/**
 * See CSS class .list-cell redefined in fxtalk.css
 *
 * @author srikalyc
 */
public class PalEntry extends StackPane implements Comparable<PalEntry> {

    public static int WIDTH = 287;
    public static int HEIGHT = 46;//30
    public static int AVATAR_W = 40;//27
    public static int AVATAR_H = 40;//27
    public static int TOOLTIP_W = 260;
    static InnerShadow is = InnerShadowBuilder.create().offsetY(1).radius(1).color(Color.rgb(60, 60, 60)).build();
    static DropShadow ds = DropShadowBuilder.create().build();
    Rectangle outlineStrip = RectangleBuilder.create().width(WIDTH).height(HEIGHT).arcWidth(4).arcHeight(4).fill(Color.BLACK).build();
    Rectangle backGnd = RectangleBuilder.create().width(WIDTH - 4).height(HEIGHT - 2).arcWidth(10).arcHeight(10).fill(Color.WHITE).translateX(2).translateY(0).build();
    //Label nameLabel = LabelBuilder.create().textFill(Color.BLACK).text("Gtalk id  ").font(Font.font("Courier New", FontWeight.BOLD, 16)).translateY(12).build();
    public Label nameLabel = LabelBuilder.create().text("Gtalk Id").textFill(Color.BLACK).font(Font.font(null, FontWeight.BOLD, 14)).effect(is).translateY(7).build();
    public Label optionalLabel = LabelBuilder.create().text("").textFill(Color.GRAY).font(Font.font(null, FontWeight.THIN, FontPosture.ITALIC, 10)).effect(is).translateY(HEIGHT - 18).build();
    Label statusLabel = LabelBuilder.create().textFill(Color.GRAY).text("").build();
    Image defaultAvatarImg = UIutil.getImage("default_avatar.jpg");
    Rectangle avatarClipper = RectangleBuilder.create().width(AVATAR_W).height(AVATAR_H).arcWidth(10).arcHeight(10).build();
    ImageView avatarImgView = ImageViewBuilder.create().smooth(true).cache(true).fitHeight(AVATAR_H).fitWidth(AVATAR_W).preserveRatio(false).image(defaultAvatarImg).clip(avatarClipper).build();
    ImageView statusImgView = ImageViewBuilder.create().smooth(true).cache(true).fitHeight(HEIGHT - 2).fitWidth(6.0).preserveRatio(false).build();
    ImageView fileImgView = ImageViewBuilder.create().smooth(true).cache(true).translateY(18).preserveRatio(true).mouseTransparent(false).build();
    ImageView phoneImgView = ImageViewBuilder.create().smooth(true).cache(true).translateY(12).preserveRatio(true).mouseTransparent(false).build();
    Presence palPresence = new Presence(Presence.Type.unavailable);
    HBox mainHBox = new HBox(5);
    Rectangle overlay = RectangleBuilder.create().width(WIDTH).height(HEIGHT - 1).arcWidth(4).arcHeight(4).fill(Color.TRANSPARENT).build();
    InnerShadow innerShadow = InnerShadowBuilder.create().offsetX(2.0).offsetY(2.0).build();
    Tooltip statusTooltip = TooltipBuilder.create().minWidth(TOOLTIP_W).prefWidth(TOOLTIP_W).build();
    private boolean disable = false;

    public void enablePalEntry() {
        disable = false;
        optionalLabel.setText("");
    }
    public void disablePalEntry(String msg) {
        disable = true;
        optionalLabel.setText(msg);
    }
    
    private void init() {
        setTranslateX(-7);
        setStyle("-fx-background-color: rgb(102,102,102);");

        Image typeImg = UIutil.getImage("gray_vert.png");
        statusImgView.setImage(typeImg);

        Image fileImg = UIutil.getImage("fileTransfer.png");
        fileImgView.setImage(fileImg);
        fileImgView.addEventHandler(MouseEvent.ANY, new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                t.consume();
            }
        });
        fileImgView.translateXProperty().bind(new SimpleIntegerProperty(140).subtract(nameLabel.widthProperty()));
        Tooltip file_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Send file").alignment(Pos.CENTER).build()).build();
        file_tt.getStyleClass().add("settings_tt");
        Tooltip.install(fileImgView, file_tt);

        Image phoneImg = UIutil.getImage("phone.png");
        phoneImgView.setImage(phoneImg);
        phoneImgView.addEventHandler(MouseEvent.ANY, new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                t.consume();
            }
        });
        phoneImgView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                FXTalkApp.app.notifPanel.showNotificationMessage("Voice Calls currently not possible..");
            }
        });
        phoneImgView.translateXProperty().bind(new SimpleIntegerProperty(150).subtract(nameLabel.widthProperty()));
        Tooltip phone_tt = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Voice Call").alignment(Pos.CENTER).build()).build();
        phone_tt.getStyleClass().add("settings_tt");
        Tooltip.install(phoneImgView, phone_tt);

        Group avatarImgGroup = GroupBuilder.create().children(avatarImgView).effect(ds).cursor(Cursor.HAND).build();

        mainHBox.getChildren().addAll(avatarImgGroup, statusImgView, nameLabel, optionalLabel, fileImgView, phoneImgView);
        mainHBox.setStyle("-fx-base: rgb(102,102,102);");
        HBox.setMargin(avatarImgGroup, new Insets(3, 0, 0, 6));
        HBox.setMargin(statusImgView, new Insets(1, 0, 0, 0));
        HBox.setMargin(nameLabel, new Insets(8, 0, 0, 10));//(top,right,bottom, left) when top =5 and bottom=0 then top is set to 5 but bottom is left to whatever is available.Like wise with left and right

        getChildren().addAll(outlineStrip, backGnd, overlay, mainHBox);

        setMouseActionsForPalEntry();
        setMouseActionsForFileTransfer();
    }

    public PalEntry(Presence prsnc) {
        init();
        setPalPresence(prsnc);
    }

    public PalEntry() {
        init();
    }
    
    private void setMouseActionsForPalEntry() {
        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                overlay.setFill(Color.GRAY);
                overlay.setOpacity(0.5);
                setCursor(Cursor.HAND);
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                overlay.setFill(Color.TRANSPARENT);
                setCursor(Cursor.DEFAULT);
            }
        });
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (!disable) {
                    FXTalkApp.app.currentPal = Util.getJidStripOffTrailingDetails(palPresence.getFrom());
                    FXTalkApp.app.chatLogPanel.addChatLog(FXTalkApp.app.currentPal);
                    FXTalkApp.app.chatLogPanel.showChatLog(FXTalkApp.app.currentPal);
                    FXTalkApp.app.chatLogPanel.toFront();
                    FXTalkApp.app.settingsPanel.toFront();
                    FXTalkApp.app.primaryStage.toFront();
                }
            }
        });
    }

    /**
     * Mouse actions for file transfer.
     */
    private void setMouseActionsForFileTransfer() {
        fileImgView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                FileChooser fileChooser = FileChooserBuilder.create().title("Choose file to transfer..").initialDirectory(new File(System.getProperty("user.home"))).build();
                File choosenFile = fileChooser.showOpenDialog(FXTalkApp.app.primaryStage);
                if (choosenFile != null) {
                    FileTransferUtils.sendFile(palPresence.getFrom(), choosenFile.getAbsolutePath());
                    Util.DEBUG("Choosen file " + choosenFile.getAbsoluteFile());
                }
                me.consume();//Do not let this seep down
            }
        });

    }

    public Presence getPalPresence() {
        return palPresence;
    }

    public void setPalPresence(Presence prsnc) {
        this.palPresence = prsnc;
        //We uninstall and recreate status tooltip because some how the CSS style changes when presence changes are not being effected(File a bug ?)
        Tooltip.uninstall(this, statusTooltip);
        statusTooltip = TooltipBuilder.create().minWidth(TOOLTIP_W).prefWidth(TOOLTIP_W).build();
        if (prsnc != null) {
            //mainHBox.getChildren().clear();
            if (PresenceUtil.isTypeAvailable(prsnc)) {
                if (PresenceUtil.isModeNull(prsnc) || PresenceUtil.isModeAvailable(prsnc) || PresenceUtil.isModeChat(prsnc)) {
                    statusImgView.setImage(UIutil.getImage("green_vert.png"));
                    statusTooltip.getStyleClass().add("available_tt");
                } else if (PresenceUtil.isModeAway(prsnc) || PresenceUtil.isModeXa(prsnc)) {
                    statusImgView.setImage(UIutil.getImage("orange_vert.png"));
                    statusTooltip.getStyleClass().add("idle_tt");
                } else if (PresenceUtil.isModeDnd(prsnc)) {
                    statusImgView.setImage(UIutil.getImage("red_vert.png"));
                    statusTooltip.getStyleClass().add("busy_tt");
                }
            } else {
                statusImgView.setImage(UIutil.getImage("gray_vert.png"));
                statusTooltip.getStyleClass().add("away_tt");
            }

            nameLabel.setText(Util.getShortNameFromJid(prsnc.getFrom()));
            //mainHBox.getChildren().addAll(statusImgView, nameLabel);
            fillStatus(avatarImgView.getImage());
            Tooltip.install(this, statusTooltip);
            //TODO: uncomment the below code if you want effect when presence changes.
            //playEffect();
        }
    }

    public void setAvatar(Image img) {
        avatarImgView.setImage(img);
        fillStatus(avatarImgView.getImage());
    }

    public Image getAvatar() {
        return avatarImgView.getImage();
    }

    /**
     * Fill status of the buddies in the Tooltip(This includes pal image +
     * status text)
     *
     * @param img
     */
    public void fillStatus(Image img) {
        Rectangle clipImage = RectangleBuilder.create().width(56).height(56).arcWidth(10).arcHeight(10).build();
        ImageView statusImgViewInTooltip = ImageViewBuilder.create().smooth(true).cache(true).fitWidth(56).effect(new DropShadow()).image(img).preserveRatio(true).clip(clipImage).translateX(-4).build();
        HBox statusHBox = new HBox(5);
        statusHBox.setPrefWidth(TOOLTIP_W - 10);
        statusHBox.setMinWidth(TOOLTIP_W - 10);
        statusHBox.setMaxWidth(TOOLTIP_W - 10);
        Label statusLbl = LabelBuilder.create().textFill(Color.WHITE).text(palPresence.getStatus() + "\n(" + Util.getJidStripOffTrailingDetails(palPresence.getFrom()) + ")").alignment(Pos.CENTER).build();
        statusLbl.translateYProperty().bind(statusHBox.heightProperty().subtract(statusLbl.heightProperty()).divide(2.0));
        statusLbl.setWrapText(true);
        statusHBox.getChildren().addAll(statusImgViewInTooltip, statusLbl);
        statusTooltip.setGraphic(statusHBox);
    }

//The following method is called whenever presence of a pal changes.
    private void playEffect() {
        DoubleProperty levelVal = new SimpleDoubleProperty(0.3);
        Glow effect = new Glow(0.0);
        effect.levelProperty().bind(levelVal);
        this.setEffect(effect);
        final PalEntry self = this;
        Timeline timeline = new Timeline();
        timeline.setCycleCount(2);
        final KeyValue kvbegin1 = new KeyValue(levelVal, 0, Interpolator.EASE_OUT);
        final KeyFrame kfbegin1 = new KeyFrame(Duration.millis(10), kvbegin1);

        final KeyValue kvmiddle1 = new KeyValue(levelVal, 0.9, Interpolator.EASE_OUT);
        final KeyFrame kfmiddle1 = new KeyFrame(Duration.millis(1000), kvmiddle1);

        final KeyValue kvend1 = new KeyValue(levelVal, 0, Interpolator.EASE_BOTH);
        final KeyFrame kfend1 = new KeyFrame(Duration.millis(2000), kvend1);
        final KeyFrame kfend2 = new KeyFrame(Duration.millis(2000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                self.setEffect(null);//For performance because Glow is not needed after notification
            }
        });

        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(kfbegin1, kfmiddle1, kfend1, kfend2);
        timeline.play();
    }

    /**
     * Following is needed for sorting the collection of PalEntry alphabetically
     *
     * @return
     */
    @Override
    public String toString() {
        if (palPresence != null && PresenceUtil.isTypeAvailable(palPresence)) {
            if (PresenceUtil.isModeNull(palPresence)) {
                return "a" + palPresence.getFrom();
            }
            if (PresenceUtil.isModeAvailable(palPresence)) {
                return "a" + palPresence.getFrom();
            }
            if (PresenceUtil.isModeChat(palPresence)) {
                return "a" + palPresence.getFrom();
            }
            if (PresenceUtil.isModeDnd(palPresence)) {
                return "b" + palPresence.getFrom();
            }
            if (PresenceUtil.isModeAway(palPresence)) {
                return "c" + palPresence.getFrom();
            }
            if (PresenceUtil.isModeXa(palPresence)) {
                return "d" + palPresence.getFrom();
            }
        }
        return "e" + palPresence.getFrom();
    }
//Required for comparison in FXCollections.sort(list of PalEntry)

    @Override
    public int compareTo(PalEntry o) {
        return toString().compareTo(o.toString());
    }
}
