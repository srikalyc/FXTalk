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

package fxtalk.ui.notification;

import fxtalk.FXTalkApp;
import fxtalk.ui.buddylist.PalEntry;
import fxtalk.ui.chat.AccordionChatLogPanel;
import fxtalk.ui.file.FileTransferUtils;
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.Util;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBuilder;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * A short lived bubble showing notification for user presence changes, new chat
 * etc.
 *
 * @author srikalyanchandrashekar
 */
public class NotifBubble {

    double NOTIF_WIDTH = 331;
    double NOTIF_HEIGHT = 80;
    double NOTIF_SPACING = 5;
    double NOTIF_BEGIN_Y = 30;// On Mac the top menu bar hides a part of notification bubble otherwise
    
    InnerShadow is = InnerShadowBuilder.create().offsetX(2.0).offsetY(2.0).build();
    private static final int SHOW_DURATION = 4000;//ms
    private Image bubble = UIutil.getImage("notification.png");
    private Image alertBubble = UIutil.getImage("notification_alert.png");
    private Image accept = UIutil.getImage("accept.png");
    private Image decline = UIutil.getImage("decline.png");
    private Image close = UIutil.getImage("close.png");
    //The following map has [pal+message, timestamp]. A thread will check the map every configured seconds and removes any duplicate entries.
    private ConcurrentMap<String, Long> statusChangeMap = new ConcurrentHashMap<String, Long>();
    private static int OLD_STATUS_REMOVE_TIME = 3000;
    public AtomicBoolean isChatInputTextAreaFocused = new AtomicBoolean(false);
    //Do not add popup to the following before it is shown(other wise it will be garbage collected)
    public ObservableList<Popup> children = FXCollections.observableArrayList();//All nodes in the pop must be part of this
    //TODO: maintain a pool of popups and reuse them instead of instantiating one everytime(might cause memory bloat)
    public Stage dummyStage ;
    
    public NotifBubble() {
        dummyStage = new Stage(StageStyle.TRANSPARENT);
        dummyStage.initModality(Modality.APPLICATION_MODAL);
        Scene scne = new Scene(new Group());
        scne.setFill(Color.TRANSPARENT);
        dummyStage.setScene(scne);
        dummyStage.show();
        
        children.addListener(new ListChangeListener<Popup>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Popup> change) {
                for (int i = 0; i < children.size(); i++) {
                    Popup popup = children.get(i);
                    if (popup.isShowing()) {
                        popup.setY(NOTIF_BEGIN_Y + i * (NOTIF_HEIGHT + NOTIF_SPACING));
                    }
                }
            }
        });
    }

    public void initialize() {
        new Thread(new Runnable() {//Duplicate notification remover
            @Override
            public void run() {
                while (true) {
                    Util.gotoSleep(1000);//Come back and check every one second.
                    Iterator<String> iter = statusChangeMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String palMsg = iter.next();
                        if (System.currentTimeMillis() - statusChangeMap.get(palMsg) > OLD_STATUS_REMOVE_TIME) {
                            statusChangeMap.remove(palMsg);
                        }
                    }
                }
            }
        }, "DuplicateNotifiRemover").start();
    }

    /**
     * Animator for the popup.
     *
     * @param popup
     * @return
     */
    private Animation getAnimator(final Popup popup) {
        return new Transition() {
            {
                setCycleDuration(Duration.millis(SHOW_DURATION));
                setInterpolator(Interpolator.LINEAR);
            }

            @Override
            protected void interpolate(double v) {
                if (v <= 0.6) {
                    popup.setX(popup.getX() + 1);
                } else {
                    setInterpolator(Interpolator.EASE_OUT);
                    popup.setX(popup.getX() + 10);//This will make the transition to move exponentially
                }
                if (v == 1.0) {
                    children.remove(popup);
                    popup.hide();
                }
            }
        };
    }

    /**
     * Creates and shows,hides the notification bubble by animating the opacity
     * and at the end of timeline just removes the notification node from
     * scenegraph.
     */
    public void showNotification(String pal, String message) {
        if (!AccountsDBStore.enableNotific) {
            return;
        }
        if (statusChangeMap.containsKey(pal + message)) {//Don't show anything
            Util.DEBUG("Got duplicate presence : " + pal + message);
            return;
        }
        statusChangeMap.put(pal + message, System.currentTimeMillis());
        final StackPane notifPane = generatePalMessageNotificationBubble(pal, message);
        final Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.setAutoHide(false);
        popup.getContent().add(notifPane);
        children.add(popup);
        final Animation animation = getAnimator(popup);
        notifPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                animation.pause();
            }
        });
        notifPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                animation.play();
            }
        });
        notifPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                //TODO:Add code to show the chat panel with appropriate Tab shown
                FXTalkApp.app.settingsPanel.toFront();
                FXTalkApp.app.primaryStage.toFront();
                FXTalkApp.app.chatLogPanel.toFront();
            }
        });
        animation.play();
        //TODO: The following gimmick is to ensure notification is always on top(especially for Mac :( ) for windows we dont have this issue(Popup will always show on top   )
        if (FXTalkApp.app.chatLogPanel instanceof AccordionChatLogPanel) {
            AccordionChatLogPanel chatPanel = (AccordionChatLogPanel)FXTalkApp.app.chatLogPanel;
            if (!chatPanel.inputTextArea.isFocused() && !FXTalkApp.app.primaryStage.isFocused() && !FXTalkApp.app.settingsPanel.isFocused() && !FXTalkApp.app.chatLogPanel.isFocused() && !FXTalkApp.app.loginScreen.isFocused()) {
                dummyStage.toFront();
            }
        }
        popup.show(dummyStage, Screen.getPrimary().getVisualBounds().getWidth() - NOTIF_WIDTH - NOTIF_SPACING, NOTIF_BEGIN_Y + (children.size() - 1) * (NOTIF_HEIGHT + NOTIF_SPACING));
        //show();
    }

    /**
     * Creates and shows,hides the notification bubble by animating the opacity
     * and at the end of timeline just removes the notification node from
     * scenegraph.
     */
    public void showNotificationMessage(String message) {
        //Ignore any user setting for enable/disable notifications.
        final StackPane notifPane = generateMessageNotificationBubble(message);
        final Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.setAutoHide(false);
        popup.getContent().add(notifPane);
        children.add(popup);
        final Animation animation = getAnimator(popup);

        notifPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                animation.pause();
            }
        });
        notifPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                animation.play();
            }
        });
        notifPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                //TODO:Add code to show the chat panel with appropriate Tab shown
                FXTalkApp.app.settingsPanel.toFront();
                FXTalkApp.app.primaryStage.toFront();
                FXTalkApp.app.chatLogPanel.toFront();
            }
        });
        animation.play();
        popup.show(FXTalkApp.app.primaryStage, Screen.getPrimary().getVisualBounds().getWidth() - NOTIF_WIDTH - NOTIF_SPACING, NOTIF_BEGIN_Y + (children.size() - 1) * (NOTIF_HEIGHT + NOTIF_SPACING));
    }

    /**
     * Creates and shows,hides the notification bubble by animating the opacity
     * and at the end of timeline just removes the notification node from
     * scenegraph.
     */
    public void showFriendReqNotificationAlert(String pal, String message) {
        generateFriendRequestNotificationAlert(pal, message);
    }
    /**
     * Creates and shows,hides the notification bubble by animating the opacity
     * and at the end of timeline just removes the notification node from
     * scenegraph.
     */
    public void showIncomingFileNotificationAlert(final FileTransferRequest ftr) {
        generateIncomingFileNotificationAlert(ftr);
    }

    /**
     * This one creates a notification bubble with progressbar
     * @param bytesWrittenHolder (This is must of the FileTransfer object is of type OutgoingFileTransfer)
     * @param ftr 
     */
    public void showFileTransferProgress(final LongProperty bytesWrittenHolder, final FileTransfer ftr) {
        final ProgressBar progressBar = new ProgressBar();
        progressBar.setMinWidth(NOTIF_WIDTH - 40);
        progressBar.setPrefWidth(NOTIF_WIDTH - 40);
        progressBar.setMaxWidth(NOTIF_WIDTH - 40);
        
        ImageView closeImgView = ImageViewBuilder.create().cache(true).smooth(true).cursor(Cursor.HAND).image(close).build();
        closeImgView.setTranslateX(NOTIF_WIDTH/2.0 - 25);
        closeImgView.setTranslateY(-NOTIF_HEIGHT/2.0 + 15);
        closeImgView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                ftr.cancel();//This will ensure the progressTracker thread also dies
                showNotificationMessage(Util.extractFileName(ftr.getFileName()) + " transfer cancelled");
            }
        });
        double percent = ftr.getProgress() * 100;
        DecimalFormat df = new DecimalFormat("#.##");//Retain 2 places
        
        final String filePath = ftr.getFileName();
        
        String size = df.format(ftr.getFileSize()/1024.0);//KB
        String msg = Util.extractFileName(filePath) + "[" + df.format(percent) + "% of " + size + "KB done]";
        String strippedDownMsg = stripMsg(msg);
        
        final Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.setAutoHide(false);

        final StackPane notifPane = new StackPane();
        notifPane.setAlignment(Pos.CENTER);
        VBox rootVbox = new VBox(5);
        rootVbox.setAlignment(Pos.CENTER);
        ImageView bubbleImgView = ImageViewBuilder.create().cache(true).smooth(true).image(alertBubble).build();

        final Text notifText = TextBuilder.create().fill(Color.WHITE).wrappingWidth(280).fontSmoothingType(FontSmoothingType.LCD).textAlignment(TextAlignment.CENTER).text(strippedDownMsg).build();

        rootVbox.getChildren().addAll(notifText, progressBar);
        notifPane.getChildren().addAll(bubbleImgView, rootVbox, closeImgView);

        popup.getContent().add(notifPane);
        children.add(0, popup);//Add to the top always(this needs attention)
        popup.show(FXTalkApp.app.primaryStage, Screen.getPrimary().getVisualBounds().getWidth() - NOTIF_WIDTH - NOTIF_SPACING, NOTIF_BEGIN_Y + (children.size() - 1) * (NOTIF_HEIGHT + NOTIF_SPACING));
        Thread progressTracker = new Thread(new Runnable() {
            @Override
            public void run() {//While not done,not error, not refused
                while (ftr.getProgress() != 1.0 && !ftr.isDone() && !ftr.getStatus().equals(FileTransfer.Status.error) && !ftr.getStatus().equals(FileTransfer.Status.refused) && !ftr.getStatus().equals(FileTransfer.Status.cancelled)) {
                    final DoubleProperty percent = new SimpleDoubleProperty(ftr.getProgress() * 100);
                    if (ftr instanceof OutgoingFileTransfer) {//Reason we treat OugoingFileTransfer different than IncomingFileTransfer is because somehow we are not getting the right value when calling getProgress()
                        percent.set((bytesWrittenHolder.get() * 100.0)/ftr.getFileSize());
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            DecimalFormat df = new DecimalFormat("#.##");//Retain 2 places
                            final String filePath = ftr.getFileName();
                            String size = df.format(ftr.getFileSize()/1024.0);//KB
                            String msg = Util.extractFileName(filePath) + "[" + df.format(percent.get()) + "% of " + size + "KB done]";
                            String strippedDownMsg = stripMsg(msg);
                            
                            notifText.setText(strippedDownMsg);
                            if (ftr instanceof OutgoingFileTransfer) {
                                progressBar.setProgress(percent.get()/100.0);
                            } else {
                                progressBar.setProgress(ftr.getProgress());
                            }
                        }
                    });
                    Util.gotoSleep(500);//.5secs then come back and check
                    if (percent.get() >= 100.0) {
                        break;
                    }
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        children.remove(popup);
                        popup.hide();
                    }
                });
                Util.DEBUG("File transfer for file " + Util.extractFileName(filePath) + " ended with status " + ftr.getStatus());
            }
        }, "FileprogressTracker" + Util.extractFileName(filePath));
        progressTracker.start();
    }
    
    
    /**
     * This one creates a notification bubble with avatar, message and close
     * button.
     */
    private StackPane generatePalMessageNotificationBubble(String pal, String message) {
        String displayName = Util.getShortNameFromJid(pal);

        String strippedDownMsg = stripMsg(message);
        StackPane notifPane = new StackPane();
        notifPane.setAlignment(Pos.CENTER);
        HBox mainHbox = new HBox(25);
        mainHbox.setTranslateX(8);
        mainHbox.setAlignment(Pos.CENTER);
        ImageView bubbleImgView = ImageViewBuilder.create().cache(true).smooth(true).image(bubble).build();

        VBox textContainer = new VBox(2);
        textContainer.setAlignment(Pos.CENTER);
        Text palTxt = TextBuilder.create().textAlignment(TextAlignment.LEFT).fontSmoothingType(FontSmoothingType.LCD).fill(Color.WHITE).wrappingWidth(245).text(displayName).build();
        Text msgTxt = TextBuilder.create().textAlignment(TextAlignment.LEFT).fontSmoothingType(FontSmoothingType.LCD).fill(Color.rgb(163, 156, 163)).wrappingWidth(245).text(strippedDownMsg).build();
        textContainer.getChildren().addAll(palTxt, msgTxt);

        PalEntry palEntry = FXTalkApp.app.conn.presenceTracker.palEntries.get(pal);
        final ImageView itemImageView = ImageViewBuilder.create().cache(true).smooth(true).fitWidth(36).fitHeight(36).preserveRatio(false).image(palEntry.getAvatar()).build();
        mainHbox.getChildren().addAll(itemImageView, textContainer);
        notifPane.getChildren().addAll(bubbleImgView, mainHbox);
        return notifPane;
    }

    /**
     * This one creates a generic notification bubble (not tied down to any
     * particular user).
     */
    private StackPane generateMessageNotificationBubble(String message) {
        String strippedDownMsg = stripMsg(message);
        final StackPane notifPane = new StackPane();
        notifPane.setAlignment(Pos.CENTER);
        ImageView bubbleImgView = ImageViewBuilder.create().cache(true).smooth(true).image(alertBubble).build();
        Text notifText = TextBuilder.create().fill(Color.WHITE).wrappingWidth(280).fontSmoothingType(FontSmoothingType.LCD).textAlignment(TextAlignment.CENTER).text(strippedDownMsg).build();
        notifPane.getChildren().addAll(bubbleImgView, notifText);
        return notifPane;
    }

    /**
     * This one creates a notification bubble with receiveFile reject.
     */
    private void generateFriendRequestNotificationAlert(final String pal, String message) {
        final Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.setAutoHide(false);

        String strippedDownMsg = stripMsg(message);
        final StackPane notifPane = new StackPane();
        notifPane.setAlignment(Pos.CENTER);
        VBox rootVbox = new VBox(5);
        rootVbox.setAlignment(Pos.CENTER);
        HBox acceptDeclineHbox = new HBox(10);
        acceptDeclineHbox.setAlignment(Pos.CENTER);
        ImageView bubbleImgView = ImageViewBuilder.create().cache(true).smooth(true).image(alertBubble).build();

        Text notifText = TextBuilder.create().fill(Color.WHITE).wrappingWidth(280).fontSmoothingType(FontSmoothingType.LCD).textAlignment(TextAlignment.CENTER).text(strippedDownMsg).build();

        final ImageView accepImgView = ImageViewBuilder.create().cache(true).preserveRatio(true).cursor(Cursor.HAND).smooth(true).image(accept).build();
        final ImageView declineImgView = ImageViewBuilder.create().cache(true).preserveRatio(true).cursor(Cursor.HAND).smooth(true).image(decline).build();
        accepImgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                accepImgView.setEffect(is);
            }
        });
        accepImgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                FXTalkApp.app.conn.acceptFriendRequest(pal);
                children.remove(popup);
                popup.hide();
                AccountsDBStore.deleteRxedFriendReqs(FXTalkApp.app.conn.accnt.getUserName(), pal);
                //You do not set palEntry.disble=false (because if your acceptance really went through then you will receive presence of the user whose request you accepted ,and we handle it in presenceTracker's presenceChange() method.
            }
        });
        declineImgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                declineImgView.setEffect(is);
            }
        });
        declineImgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                FXTalkApp.app.conn.declineFriendRequest(pal);
                children.remove(popup);
                popup.hide();
                AccountsDBStore.deleteRxedFriendReqs(FXTalkApp.app.conn.accnt.getUserName(), pal);
            }
        });
        acceptDeclineHbox.getChildren().addAll(accepImgView, declineImgView);
        rootVbox.getChildren().addAll(notifText, acceptDeclineHbox);
        notifPane.getChildren().addAll(bubbleImgView, rootVbox);

        popup.getContent().add(notifPane);
        children.add(0, popup);//Add to the top always(this needs attention)
        popup.show(FXTalkApp.app.primaryStage, Screen.getPrimary().getVisualBounds().getWidth() - NOTIF_WIDTH - NOTIF_SPACING, NOTIF_BEGIN_Y + (children.size() - 1) * (NOTIF_HEIGHT + NOTIF_SPACING));
    }

    /**
     * This one creates a notification bubble with receiveFile reject.
     */
    private void generateIncomingFileNotificationAlert(final FileTransferRequest ftr) {
        final String filePath = ftr.getFileName();
        String pal = Util.getShortNameFromJid(ftr.getRequestor());
        String strippedDownMsg = stripMsg(pal + " wants to share a file " + Util.extractFileName(filePath) + "(" + ftr.getFileSize()/1024 + "KB), allow?");
        
        final Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.setAutoHide(false);

        final StackPane notifPane = new StackPane();
        notifPane.setAlignment(Pos.CENTER);
        VBox rootVbox = new VBox(5);
        rootVbox.setAlignment(Pos.CENTER);
        HBox acceptDeclineHbox = new HBox(10);
        acceptDeclineHbox.setAlignment(Pos.CENTER);
        ImageView bubbleImgView = ImageViewBuilder.create().cache(true).smooth(true).image(alertBubble).build();

        Text notifText = TextBuilder.create().fill(Color.WHITE).wrappingWidth(280).fontSmoothingType(FontSmoothingType.LCD).textAlignment(TextAlignment.CENTER).text(strippedDownMsg).build();

        final ImageView accepImgView = ImageViewBuilder.create().cache(true).preserveRatio(true).cursor(Cursor.HAND).smooth(true).image(accept).build();
        final ImageView declineImgView = ImageViewBuilder.create().cache(true).preserveRatio(true).cursor(Cursor.HAND).smooth(true).image(decline).build();
        accepImgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                accepImgView.setEffect(is);
            }
        });
        accepImgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                FileTransferUtils.receiveFile(ftr);
                children.remove(popup);
                popup.hide();
            }
        });
        declineImgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                declineImgView.setEffect(is);
            }
        });
        declineImgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                ftr.reject();
                children.remove(popup);
                popup.hide();
            }
        });
        acceptDeclineHbox.getChildren().addAll(accepImgView, declineImgView);
        rootVbox.getChildren().addAll(notifText, acceptDeclineHbox);
        notifPane.getChildren().addAll(bubbleImgView, rootVbox);

        popup.getContent().add(notifPane);
        children.add(0, popup);//Add to the top always(this needs attention)
        popup.show(FXTalkApp.app.primaryStage, Screen.getPrimary().getVisualBounds().getWidth() - NOTIF_WIDTH - NOTIF_SPACING, NOTIF_BEGIN_Y + (children.size() - 1) * (NOTIF_HEIGHT + NOTIF_SPACING));
    }
    
    /**
     * Remove all notifications including the ones (like receiveFile
     */
    public void resetPanel() {
        for (int i = 0; i < children.size(); i++) {
            Popup popup = children.get(i);
            if (popup.isShowing()) {
                popup.hide();
            }
        }
        children.clear();
    }

    /**
     * String Strippping utility
     *
     * @param message
     * @return
     */
    private String stripMsg(String message) {
        return (message.length() > 80) ? (message.substring(0, 80) + "...") : (message);
    }
}
