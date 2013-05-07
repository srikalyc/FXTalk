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

import fxtalk.ui.misc.SwitchComponent;
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.Util;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.util.Callback;

/**
 * Chat , notification settings etc.
 * @author srikalyc
 */
public class ChatSettingsTab extends Group {
    InnerShadow is = InnerShadowBuilder.create().offsetY(1).radius(1).color(Color.rgb(60, 60, 60)).build();
    SettingsPanel settingsPanel = null;
    String style = "-fx-base: rgb(10,10,10);-fx-background-color: rgb(240,240,240);";
    SwitchComponent notificationCheckBox = new SwitchComponent("Enable Notification");
    //CheckBox notificationCheckBox = CheckBoxBuilder.create().text("Enable Notification").style("-fx-text-fill: white;").build();
    TextField idleTimeBox = TextFieldBuilder.create().translateX(3).translateY(3).styleClass("ntTextField").prefWidth(273).effect(is).build();
    TextField chatLogCountBox = TextFieldBuilder.create().translateX(3).translateY(3).styleClass("ntTextField").prefWidth(273).effect(is).build();
    Button saveButton = ButtonBuilder.create().text("     Save     ").id("dark-blue").build();
    Double currentSavedIdleTime = 0.0;
    
    public ChatSettingsTab(SettingsPanel parent) {
        AccountsDBStore.load();
        this.settingsPanel = parent;
        ImageView bckGndImgView = ImageViewBuilder.create().image(UIutil.getImage("linen.png")).smooth(true).opacity(0.5).fitWidth(305).fitHeight(359).cache(true).preserveRatio(false).visible(true).build();

        VBox rootBox = VBoxBuilder.create().spacing(30).translateY(20).alignment(Pos.TOP_CENTER).build();

        Text tabDesc = TextBuilder.create().text("Chat Settings").fill(Color.LIGHTGRAY).font(Font.font(null, FontWeight.BOLD, 24)).effect(is).textOrigin(VPos.CENTER).translateX(20).build();

        rootBox.getChildren().addAll(tabDesc,  createChatHistory(), createChatLogCount(), createNotificationsEnDis(), createSaveButton());
        VBox.setMargin(tabDesc, new Insets(5, 10, 0, 0));

        getChildren().addAll(bckGndImgView, rootBox);
    }
    
    /**
     * Creates Notification settings
     * @return 
     */
    private  Group createNotificationsEnDis() {
        notificationCheckBox.setState(AccountsDBStore.enableNotific);
        return GroupBuilder.create().children(notificationCheckBox).translateY(12).translateX(12).build();
    }
    
    /**
     * Creates chat history settings
     * @return 
     */
    private Group createChatHistory() {
        Text label = TextBuilder.create().text("mins of inactivity to idle").fill(Color.LIGHTGRAY).font(Font.font(null, FontWeight.NORMAL, 14)).effect(is).textOrigin(VPos.CENTER).translateX(100).translateY(15).build();
        Rectangle rectIdleBgnd = RectangleBuilder.create().width(280).height(34).fill(Color.rgb(166, 166, 166)).stroke(Color.rgb(90, 90, 90)).arcHeight(10).arcWidth(10).build();
        idleTimeBox.setText(Long.toString(AccountsDBStore.idleInMins.get()));
        return GroupBuilder.create().children(rectIdleBgnd, idleTimeBox, label).translateY(12).translateX(12).build();
    }
    /**
     * Creates chat log count
     * @return 
     */
    private Group createChatLogCount() {
        Text label = TextBuilder.create().text("recent messages will be shown").fill(Color.LIGHTGRAY).font(Font.font(null, FontWeight.NORMAL, 14)).effect(is).textOrigin(VPos.CENTER).translateX(60).translateY(15).build();
        Rectangle rectIdleBgnd = RectangleBuilder.create().width(280).height(34).fill(Color.rgb(166, 166, 166)).stroke(Color.rgb(90, 90, 90)).arcHeight(10).arcWidth(10).build();
        chatLogCountBox.setText(Long.toString(AccountsDBStore.chatLogCount.get()));
        return GroupBuilder.create().children(rectIdleBgnd, chatLogCountBox, label).translateY(12).translateX(12).build();
    }
    /**
     * Creates save Button
     * @return 
     */
    private Button createSaveButton() {
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveAction();
            }
        });

        saveButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                saveButton.setCursor(Cursor.HAND);
            }
        });
        saveButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                saveButton.setCursor(Cursor.DEFAULT);
            }
        });
        return saveButton;
    }
    /**
     * Save action.
     */
    private void saveAction() {
        AccountsDBStore.saveNotific("" + notificationCheckBox.isOn());
        if (idleTimeBox.getText() != null && Util.isNumber(idleTimeBox.getText().trim())) {
            AccountsDBStore.saveChatIdleTime(idleTimeBox.getText().trim());
        }
        if (chatLogCountBox.getText() != null && Util.isNumber(chatLogCountBox.getText().trim())) {
            AccountsDBStore.saveChatLogCount(chatLogCountBox.getText().trim());
        }
        settingsPanel.animate();
    }
    /**
     * To control text color layout etc that goes into combobox list.
     */
    private static class SimpleFormatCell extends ListCell<String> {
        public static Callback<ListView<String>, ListCell<String>> createCellFactory() {
            return new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param) {
                    return new SimpleFormatCell();
                }
            };
        }
        
        @Override
        public void updateItem(String item, boolean empty) {
            setStyle("-fx-text-fill: white;");
            setAlignment(Pos.CENTER);
            super.updateItem(item, empty);
            setText(item == null ? "" : item);

        }
    }

}
