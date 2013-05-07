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

package fxtalk.ui.chat;

import fxtalk.FXTalkApp;
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.Util;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.TextAlignment;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * An implementation of ChatLogPanel. More themeable ones can be implemented.
 *
 * @author srikalyc
 */
public class AccordionChatLogPanel extends ChatLogPanel {

    double WIDTH = 420;
    double HEIGHT = 380;
    static InnerShadow is = InnerShadowBuilder.create().offsetX(2.0).offsetY(2.0).build();
    static Glow glow = new Glow(0.5);
    DoubleProperty stageX = new SimpleDoubleProperty(0);
    DoubleProperty stageY = new SimpleDoubleProperty(0);
    FlowPane root = null;
    StackPane rootsParent = null;
    public ObservableList<Node> currentChatLogItems;
    public ScrollPane currentChatLog;
    public TextArea inputTextArea;
    public Button sendButton;
    public Accordion chatLogAccordion = new Accordion();
    public Map<String, TitledPane> palChatCacheMap = new HashMap<String, TitledPane>();
    Rectangle backGndRect = RectangleBuilder.create().width(WIDTH + 10).height(HEIGHT + 5).arcWidth(10).arcHeight(10).fill(Color.BLACK).opacity(0.6).build();
    private boolean initDone = false;
    private Timeline timeline = new Timeline();

    public AccordionChatLogPanel() {
        rootsParent = new StackPane();
        root = new FlowPane();
        root.setMaxSize(WIDTH, HEIGHT);
        chatLogAccordion.setPrefSize(WIDTH, HEIGHT - 60);


        sendButton = createChatSendButton();
        inputTextArea = createChatTextInputArea(sendButton);

        root.setOrientation(Orientation.VERTICAL);
        root.setVgap(5);
        //FlowPane.setMargin(sendButton, new Insets(5, 0, 5, 150));
        root.getChildren().addAll(chatLogAccordion, inputTextArea);//, sendButton);
        rootsParent.getChildren().addAll(backGndRect, root);
        if (!initDone) {
            initialize();
        }

    }
    /**
     * Send button with all actions associated with it.
     * @return 
     */
    private Button createChatSendButton() {
        final Button b = ButtonBuilder.create().text("Send").id("dark-blue").build();
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                send();
            }
        });

        b.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                b.setCursor(Cursor.HAND);
            }
        });
        b.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                b.setCursor(Cursor.DEFAULT);
            }
        });      
        return b;
    }
/**
 * Text area 
 * @return 
 */
    private TextArea createChatTextInputArea(Button sendButn) {
        TextArea ta = TextAreaBuilder.create().prefRowCount(2).prefWidth(WIDTH).prefHeight(50).wrapText(true).build();
        ta.setSkin(new InputTextAreaSkin(ta, sendButn));
        ta.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke != null) {
                    if (ke.getCode() == KeyCode.ENTER) {
                        send();
                    }
                }
            }
        });
        ta.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldV, Boolean newV) {
                FXTalkApp.app.notifPanel.isChatInputTextAreaFocused.set(newV);
            }
        });
        return ta;
    }

    /**
     * send the message.
     */
    private void send() {
        sendButton.setOpacity(0.7);
        if (FXTalkApp.app.currentPal != null && FXTalkApp.app.currentPal.contains("@")) {
            FXTalkApp.app.conn.sendMessage(FXTalkApp.app.currentPal, inputTextArea.getText());
            inputTextArea.clear();
            inputTextArea.requestFocus();
        }
        sendButton.setOpacity(1.0);
    }

    @Override
    public void addItemToChatLog(String pal, String item) {
        AccountsDBStore.logChat(pal, item);
        if (palChatCacheMap.containsKey(pal)) {

            ChatItem chatItem = new ChatItem(pal, item);
            chatItem.setMinWidth(WIDTH - 20);
            chatItem.setPrefWidth(WIDTH - 20);
            chatItem.setMaxWidth(WIDTH - 20);
            chatItem.parseChatText();//This is must

            ScrollPane sp = (ScrollPane) (palChatCacheMap.get(pal).getContent());
            VBox spVBox = (VBox) sp.getContent();
            spVBox.getChildren().add(chatItem);
            addChatLog(FXTalkApp.app.currentPal);

        } else {
            FXTalkApp.app.currentPal = Util.getJidStripOffTrailingDetails(pal);
            addChatLog(FXTalkApp.app.currentPal);
            showChatLog(FXTalkApp.app.currentPal);
            addItemToChatLog(FXTalkApp.app.currentPal, item);
        }
        if (!isShoing) {
            showHideChatLogPanel(false);
        }
    }

    @Override
    public void clearChatLog(String pal) {
        if (palChatCacheMap.containsKey(pal)) {
            ((ListView<ChatItem>) (palChatCacheMap.get(pal).getContent())).getItems().clear();
        }
    }

    @Override
    public void addChatLog(final String pal) {

        if (isChatLogPanelFull()) {//Then remove the oldest unused page from the accordion.
            chatLogAccordion.getPanes().remove(0);
        }

        if (palChatCacheMap.containsKey(pal)) {//You chatted with this person already so its in the cache
            if (!chatLogAccordion.getPanes().contains(palChatCacheMap.get(pal))) {//Then this is not added to the accordian(but the titledPane was created)
                chatLogAccordion.getPanes().add(palChatCacheMap.get(pal));
            }
        } else {//You never chatted with this guy after the most recent sign in process.
            final ScrollPane chatLog = new ScrollPane();
            chatLog.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            chatLog.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            chatLog.setPrefSize(WIDTH, HEIGHT - 60);
            final VBox box = new VBox(8);
            box.setStyle("-fx-base: rgb(240,247,254);");
            chatLog.setContent(box);
///Try to scroll the chat log to the most recent item in the chat log
            box.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
                @Override
                public void changed(ObservableValue<? extends Bounds> arg0, Bounds oldV, Bounds newV) {
                    if (oldV == newV) {
                        return;
                    }
                    scrollToBottom(chatLog);
                }
            });
////////////////////////Create a TitlePane and set the close button graphic, title text and actual content.
            TitledPane logPage = TitledPaneBuilder.create().text(pal).textFill(Color.WHITESMOKE).textAlignment(TextAlignment.CENTER).content(chatLog).style("-fx-base: rgb(58,58,58);").build();;
            logPage.setGraphic(createCloseForChatLog(logPage));
////////////////////////Add the ChatLog pane to the Accordion panel container//////
            chatLogAccordion.getPanes().addAll(logPage);
//////////////Set the current pal to that one whose corresponding Chat TitledPane is expanded.
            logPage.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal) {
                    if (newVal) {//Has been expanded
                        FXTalkApp.app.currentPal = pal;
                        Util.DEBUG("Current buddy on chat is " + pal);
                        showChatLog(pal);
                    }
                }
            });
            palChatCacheMap.put(pal, logPage);
            AccountsDBStore.loadKnownChatLog(pal);
            //Load the most recent X messages before the current session.(We remove not just get)
            List<String> mostRecentSavedChats = AccountsDBStore.userToChatLogMap.remove(pal);
            for (int i = 0;mostRecentSavedChats != null && (i < mostRecentSavedChats.size());i++) {
                addItemToChatLog(pal, mostRecentSavedChats.get(i));
            }
        }
        if (chatLogAccordion.getPanes().size() == 1) {//If this is the only pane in the chatlog panel then show it expanded.
            palChatCacheMap.get(pal).setExpanded(true);
        }
    }

    @Override
    public void removeChatLog(String pal) {
        if (palChatCacheMap.containsKey(pal)) {//You chatted with this person already so its in the cache
            if (chatLogAccordion.getPanes().contains(palChatCacheMap.get(pal))) {//If this is already in the accordian, remove it.
                chatLogAccordion.getPanes().remove(palChatCacheMap.get(pal));
            } else {//Nothing To do
            }
        } else {//You never chatted with this guy after the most recent sign in process.
            //Nothing to do
        }
        if (chatLogAccordion.getPanes().size() == 1) {//If this is the only pane in the chatlog panel then show it expanded.
            chatLogAccordion.getPanes().get(0).setExpanded(true);
        }
    }

    /**
     * This will also set the corresponding chat log as current
     */
    @Override
    public void showChatLog(String pal) {
        showHideChatLogPanel(false);
        TitledPane tp = palChatCacheMap.get(pal);
        if (chatLogAccordion.getPanes().contains(tp) && !tp.isExpanded()) {
            chatLogAccordion.setExpandedPane(palChatCacheMap.get(pal));
        }
        currentChatLog = (ScrollPane) (palChatCacheMap.get(pal).getContent());
        currentChatLogItems = ((VBox) currentChatLog.getContent()).getChildren();
    }

    @Override
    public boolean isChatLogPanelFull() {
        if (chatLogAccordion.getPanes().size() >= MAX_PALS_LOG_PAGES_IN_CONTIANER) {
            return true;
        }
        return false;
    }

    @Override
    public void initialize() {
        if (getScene() != null) {
            return;
        }
        initStyle(StageStyle.TRANSPARENT);
        final Scene scene = new Scene(rootsParent, WIDTH + 10, HEIGHT + 5);
        scene.getStylesheets().add(UIutil.getRsrcURI("fxtalk.css").toString());
        setScene(scene);
        scene.setFill(Color.TRANSPARENT);

        stageX.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                setX(stageX.getValue());
            }
        });
        stageY.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                setY(stageY.getValue());
            }
        });
        initDone = true;
    }

    /**
     * This will show/hide the Chat Log Panel(by animating it).
     *
     * @param forceRetract
     */
    private void showHideChatLogPanel(boolean forceRetract) {
        if (isAnimationInProgress()) {
            return;//Go back
        }
        timeline = new Timeline();
        if (!isShoing) {//If the chat panel is not seen then show it
            stageX.set(FXTalkApp.app.primaryStage.getX());
            stageY.set(FXTalkApp.app.primaryStage.getY() + (FXTalkApp.app.primaryStage.getHeight() - HEIGHT) / 2.0);
            root.setVisible(false);

            final KeyValue kvbegin1 = new KeyValue(stageX, FXTalkApp.app.primaryStage.getX(), Interpolator.EASE_BOTH);
            final KeyFrame kfbegin1 = new KeyFrame(Duration.millis(50), kvbegin1);

            final KeyValue kvbegin2 = new KeyValue(opacityProperty(), 0.0, Interpolator.EASE_BOTH);
            final KeyFrame kfbegin2 = new KeyFrame(Duration.millis(50), kvbegin2);

            final KeyValue kvend1 = new KeyValue(stageX, FXTalkApp.app.primaryStage.getX() + FXTalkApp.app.primaryStage.getWidth(), Interpolator.EASE_BOTH);
            final KeyFrame kfend1 = new KeyFrame(Duration.millis(1000), kvend1);

            final KeyValue kvend2 = new KeyValue(opacityProperty(), 1.0, Interpolator.EASE_BOTH);
            final KeyFrame kfend2 = new KeyFrame(Duration.millis(1000), kvend2);

            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(kfbegin1, kfbegin2, kfend1, kfend2);
            timeline.play();

            show();
            root.setVisible(true);
            FXTalkApp.app.primaryStage.toFront();
            isShoing = true;
        } else if (isShoing && forceRetract) {
            double currentChatPanelX = FXTalkApp.app.primaryStage.getX() + FXTalkApp.app.primaryStage.getWidth();
            stageX.set(currentChatPanelX);
            stageY.set(FXTalkApp.app.primaryStage.getY() + (FXTalkApp.app.primaryStage.getHeight() - getHeight()) / 2.0);

            final KeyValue kvbegin1 = new KeyValue(stageX, currentChatPanelX, Interpolator.EASE_BOTH);
            final KeyFrame kfbegin1 = new KeyFrame(Duration.millis(50), kvbegin1);

            final KeyValue kvbegin2 = new KeyValue(opacityProperty(), 1.0, Interpolator.EASE_BOTH);
            final KeyFrame kfbegin2 = new KeyFrame(Duration.millis(50), kvbegin2);

            final KeyValue kvend1 = new KeyValue(stageX, FXTalkApp.app.primaryStage.getX(), Interpolator.EASE_BOTH);
            final KeyFrame kfend1 = new KeyFrame(Duration.millis(1000), kvend1);

            final KeyValue kvend2 = new KeyValue(opacityProperty(), 0.0, Interpolator.EASE_BOTH);
            final KeyFrame kfend2 = new KeyFrame(Duration.millis(1000), kvend2);

            final KeyFrame kfend3 = new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent ae) {
                    root.setVisible(false);
                    hide();
                }
            });
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(kfbegin1, kfbegin2, kfend1, kfend2, kfend3);
            timeline.play();
            FXTalkApp.app.primaryStage.toFront();
            isShoing = false;

        }

    }

    /**
     * Creates an close image(black cross) which on clicked will close the
     * corresponding chat log window
     *
     * @param logPage
     * @return
     */
    private ImageView createCloseForChatLog(final TitledPane logPage) {
        Image close = UIutil.getImage("close.png");
        final ImageView closeImgView = ImageViewBuilder.create().cache(true).smooth(true).opacity(1.0).translateX(WIDTH - 50).image(close).build();
        Tooltip close_tt = new Tooltip("Close Window");
        close_tt.getStyleClass().add("settings_tt");
        Tooltip.install(closeImgView, close_tt);
        closeImgView.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                closeImgView.setEffect(glow);
                closeImgView.setCursor(Cursor.HAND);
            }
        });
        closeImgView.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                closeImgView.setEffect(null);
                closeImgView.setCursor(Cursor.DEFAULT);
            }
        });
        closeImgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                closeImgView.setEffect(is);
            }
        });
        closeImgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (chatLogAccordion.getPanes().size() == 1) {//Just close whole panel
                    showHideChatLogPanel(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Util.gotoSleep(1200);//Goto sleep for 1 sec because the whole chat panel will be animated and closed and we should remove the panel only at end of animation.
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    chatLogAccordion.getPanes().remove(logPage);
                                }
                            });
                        }
                    }, "CloseChatLogPanel").start();
                } else {//Just remove immediately
                    chatLogAccordion.getPanes().remove(logPage);
                }
            }
        });

        return closeImgView;
    }

    /**
     * Simple helper
     *
     * @return
     */
    public boolean isAnimationInProgress() {
        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
            return true;
        }
        return false;

    }

    @Override
    public void resetPanel() {
        showHideChatLogPanel(true);
        palChatCacheMap.clear();
        for (int i = chatLogAccordion.getPanes().size() - 1; i >= 0; i--) {
            chatLogAccordion.getPanes().remove(i);
        }
        palChatCacheMap = new HashMap<String, TitledPane>();
    }

    private void scrollToBottom(final ScrollPane chatLog) {
        pool.submit(new ScrollTask(chatLog));

    }
    //////////The following pool was necessary to offset programmatic scrolling issue in ScrollPane
    ExecutorService pool = Executors.newFixedThreadPool(1);

    private final class ScrollTask implements Runnable {

        final ScrollPane chatLog;

        public ScrollTask(ScrollPane chatLog) {
            this.chatLog = chatLog;
        }

        @Override
        public void run() {
            //Give some time for the chat log item to be visible in the chatLogPanel then scroll down
            Util.gotoSleep(500);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    chatLog.layout();
                    chatLog.setVvalue(chatLog.getVmax());//This is equivalent to scrolling to bottom
                }
            });
        }
    }
}
