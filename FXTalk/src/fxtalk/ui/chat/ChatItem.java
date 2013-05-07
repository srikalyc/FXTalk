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
import fxtalk.utils.ChatNodeUIUtil;
import fxtalk.ui.buddylist.PalEntry;
import fxtalk.ui.misc.MyContextMenu;
import fxtalk.ui.misc.MyMenuItem;
import fxtalk.utils.BrowserUtil;
import fxtalk.utils.Util;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.HyperlinkBuilder;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBuilder;

/**
 * Container for Each Bubble in the chat including text and image .
 *
 * @author srikalyanchandrashekar
 */
public class ChatItem extends StackPane {

    static DropShadow ds = new DropShadow();
    static InnerShadow is = InnerShadowBuilder.create().offsetY(1).radius(1).color(Color.rgb(60, 60, 60)).build();

    public String pal;//This is jabber id (with whom the chat is going on, but this chat Item could be posted by either pal/ by myself)
    public String value;
    public Group valuePane = GroupBuilder.create().build();
    public StackPane valuePaneWithBubble = StackPaneBuilder.create().children(valuePane).effect(ds).alignment(Pos.CENTER).build();
    public HBox masterBox = HBoxBuilder.create().spacing(-10).translateX(5).alignment(Pos.CENTER).build();
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
//    private Rectangle itemImgViewClip = RectangleBuilder.create().width(48).height(48).arcHeight(20).arcWidth(20).build();
    private Circle itemImgViewClip = CircleBuilder.create().radius(24).centerX(24).centerY(24).build();
    private ImageView chatItemImgView = ImageViewBuilder.create().cache(true).fitWidth(48).fitHeight(48).smooth(true).clip(itemImgViewClip).build();
    private Group itemImgViewGroup = GroupBuilder.create().children(chatItemImgView).effect(ds).build();
    private VBox bubbleBgnd = null;
    double WRAP_WIDTH = 265;
    private MyContextMenu contextMenu = new MyContextMenu();
    
    public ChatItem(String pal, String val) {
        this.pal = pal;
        this.value = val;
        getStyleClass().add("chat-item");
    }

    public ChatItem(String pal, String val, double maxW, double maxH) {
        this(pal, val);
        setMinSize(maxW, maxH);
        setPrefSize(maxW, maxH);
        setMaxSize(maxW, maxH);
    }

    /**
     * Do not forget to call this immediately after instantiation. When a user sends out 
     * or receives a chat message he has to go through this method because this will
     * do the necessary processing of the text and puts it into the chat panel.
     */
    public void parseChatText() {
        senseForChangesToChatContent();// Sniff like a dog.
        valuePaneWithBubble.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.isSecondaryButtonDown()) {//right click
                    makeContextMenuAndShow(me.getScreenX(), me.getScreenY());
                }
            }
        });
        if (!value.contains(HTTP) && !value.contains(HTTPS)) {//If not http then everything is text
            Text txt = TextBuilder.create().textAlignment(TextAlignment.LEFT).text(value).fontSmoothingType(FontSmoothingType.LCD).fill(value.startsWith("You:")?Color.BLACK:Color.WHITE).font(Font.font("Arial", 14)).build();
            txt.setWrappingWidth(WRAP_WIDTH);
            valuePane.getChildren().add(txt);

        } else {//else break them into tokens and generate text and hyperlinks correspondingly.
            //txt1 http1 txt2 txt3 txt4 http2 txt5 txt6 .... (So fulltext will club together txt2 txt3 txt4 in this given example)
            String tokens[] = value.split(" ");
            StringBuilder fullText = new StringBuilder("");
            VBox complexContainer = VBoxBuilder.create().maxWidth(WRAP_WIDTH).build();
            
            double complexContainerMaxHeight = 0.0;
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].trim().startsWith(HTTP) || tokens[i].trim().startsWith(HTTPS)) {
                    if (!fullText.toString().trim().equals("")) {
                        Text txt = TextBuilder.create().textAlignment(TextAlignment.LEFT).fontSmoothingType(FontSmoothingType.LCD).fill(value.startsWith("You:")?Color.BLACK:Color.WHITE).text(fullText.toString()).font(Font.font("Arial", 13)).build();
                        txt.setWrappingWidth(WRAP_WIDTH);
                        complexContainer.getChildren().add(txt);
                        fullText.delete(0, fullText.length());//Clear the full text
                        complexContainerMaxHeight += txt.getLayoutBounds().getHeight() + 20;//+20 is buffer
                    }
                    final Hyperlink link = HyperlinkBuilder.create().text(tokens[i].trim()).textFill(Color.rgb(63, 138, 208)).font(Font.font("Arial", 14)).build();
                    link.setPrefWidth(WRAP_WIDTH);
                    link.setMaxWidth(WRAP_WIDTH);
                    link.setWrapText(true);
                    complexContainer.getChildren().add(link);

                    link.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent ae) {
                            if (BrowserUtil.isMac()) {
                                BrowserUtil.runInBrowser("cr", link.getText());
                            } else if (BrowserUtil.isWin()) {
                                BrowserUtil.runInBrowser("ff", link.getText());
                            }
                        }
                    });
                    complexContainerMaxHeight += link.getLayoutBounds().getHeight() + 20;//+20 is buffer

                    String imgUrl = BrowserUtil.extractYoutubeDefaultImgUrl(tokens[i].trim());
                    if (imgUrl != null && !imgUrl.equals("")) {
                        ImageView thumbNail = ImageViewBuilder.create().image(new Image(imgUrl)).fitWidth(128).preserveRatio(true).build();
                        complexContainer.getChildren().add(thumbNail);
                        complexContainerMaxHeight += thumbNail.getLayoutBounds().getHeight() + 20;//+20 is buffer
                    }

                } else {
                    fullText.append(tokens[i]);
                    fullText.append(" ");
                }
            }
            if (!fullText.toString().trim().equals("")) {
                Text txt = TextBuilder.create().textAlignment(TextAlignment.LEFT).fontSmoothingType(FontSmoothingType.LCD).fill(value.startsWith("You:")?Color.BLACK:Color.WHITE).text(fullText.toString()).font(Font.font("Arial", 14)).build();
                txt.setWrappingWidth(WRAP_WIDTH);
                complexContainer.getChildren().add(txt);
                fullText.delete(0, fullText.length());//Clear the full text
                complexContainerMaxHeight += txt.getLayoutBounds().getHeight() + 20;//+20 is buffer
            }
            complexContainer.setMinHeight(complexContainerMaxHeight);
            complexContainer.setPrefHeight(complexContainerMaxHeight);
            valuePane.getChildren().add(complexContainer);
        }

        if (value.startsWith("You:")) {//Then i am adding this chat message.
            masterBox.getChildren().addAll(itemImgViewGroup, valuePaneWithBubble);
        } else {//Other wise add bubble first then the image
            masterBox.getChildren().addAll(valuePaneWithBubble, chatItemImgView);
        }
        getChildren().remove(masterBox);
        getChildren().add(masterBox);
    }
    /**
     * 
     * @param x
     * @param y 
     */
    private void makeContextMenuAndShow(double x, double y) {
        //TODO: You don't need one context menu instance per chatitem(revisit this code)
        contextMenu.getChildren().clear();
        contextMenu.addMenuItem(new MyMenuItem("Copy") {
            @Override
            public void action() {
                Util.copyToClipBoard(value);
                contextMenu.hide();
            }
        });
        contextMenu.addMenuItem(new MyMenuItem("CopyAll") {
            @Override
            public void action() {
                //TODO: fix this
                contextMenu.hide();
            }
        });
        contextMenu.show(x, y);
    }
    /**
     * Listens to layout changes to the valuePane which contains the text and 
     * dynamically generates bubble background as needed.
     */
    private void senseForChangesToChatContent() {
        valuePane.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> arg0, Bounds oldV, Bounds newV) {
                if (newV.getHeight() > 0 && (newV.getHeight() != oldV.getHeight())) {
                    if (bubbleBgnd != null) {
                        valuePaneWithBubble.getChildren().remove(bubbleBgnd);
                    }
                    if (value.startsWith("You:")) {//Then i am adding this chat message.
                        bubbleBgnd = ChatNodeUIUtil.getChatBubbleImage(true, valuePane, ChatNodeUIUtil.Bubble.NICE_BUBBLE);
                        valuePaneWithBubble.setAlignment(Pos.CENTER);
                        valuePane.setTranslateY(6);
                        valuePane.setTranslateX(21);
                        chatItemImgView.setImage((FXTalkApp.app.conn.avatarSelfLocal != null) ? FXTalkApp.app.conn.avatarSelfLocal : FXTalkApp.app.conn.avatarSelfNonLocal);
                    } else {//We received this message 
                        bubbleBgnd = ChatNodeUIUtil.getChatBubbleImage(false, valuePane, ChatNodeUIUtil.Bubble.NICE_BUBBLE);
                        valuePaneWithBubble.setAlignment(Pos.CENTER);
                        valuePane.setTranslateY(1);
                        valuePane.setTranslateX(-5);
                        PalEntry palEntry = FXTalkApp.app.conn.presenceTracker.palEntries.get(pal);
                        chatItemImgView.setImage(palEntry.getAvatar());
                        if (bubbleBgnd.getChildren().size() == 2) {//No middle image was added so the message could be short(may need translation)
                            valuePaneWithBubble.setTranslateX(bubbleBgnd.getTranslateX() * -2);//In other direction but 2 times
                        }
                    }
                    valuePaneWithBubble.getChildren().add(0, bubbleBgnd);
                    //The following 2 lines ensures the layout happens correctly(Ugly?)
                    getChildren().remove(masterBox);
                    getChildren().add(masterBox);
                }
            }
        });
    }
}
