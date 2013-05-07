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

package fxtalk.ui.misc;

import fxtalk.FXTalkApp;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBuilder;
import javafx.stage.Popup;

/**
 * Simple context menu implementation
 * @author srikalyc
 */
public class MyContextMenu extends VBox {
    double WIDTH = 130;
    double HEIGHT = 29;
    
    private static Image menuPressed = UIutil.getImage("pressedMenu.png");
    private static Image menuNormal = UIutil.getImage("normalMenu.png");
    private Popup popup = new Popup();
    private static DropShadow ds = new DropShadow(15, 8, 8, Color.BLACK);
    Rectangle clip = RectangleBuilder.create().width(WIDTH).height(HEIGHT).arcWidth(10).arcHeight(10).build();
    
    public MyContextMenu() {
        setEffect(ds);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        setClip(clip);
    }
    
    public void addMenuItem(final MyMenuItem item) {
        final ImageView bgndImgView = ImageViewBuilder.create().cache(true).smooth(true).opacity(0.98).image(menuNormal).build();
        Text menuTxt = TextBuilder.create().textAlignment(TextAlignment.CENTER).fill(Color.BLACK).wrappingWidth(120).text(item.text).build();
        StackPane root = new StackPane();
        root.setCursor(Cursor.HAND);
        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                item.action();
            }
        });
        root.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                bgndImgView.setImage(menuPressed);
            }
        });
        root.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                bgndImgView.setImage(menuNormal);
            }
        });
        root.getChildren().addAll(bgndImgView, menuTxt);
        getChildren().add(root);
        clip.setHeight(HEIGHT * getChildren().size());
        setClip(clip);
    }
    
    public void show(double x, double y) {
        popup.getContent().clear();
        popup.getContent().add(this);
        popup.show(FXTalkApp.app.chatLogPanel, x, y);
    }

    public void hide() {
        popup.hide();
    }
}
