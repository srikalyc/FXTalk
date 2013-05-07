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

import fxtalk.ui.misc.UIutil;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.TalkSettings;
import fxtalk.utils.Util;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.util.Callback;

/**
 * Network settings like proxy host,port.
 * @author srikalyc
 */
public class NetworkTab extends Group {
    InnerShadow is = InnerShadowBuilder.create().offsetY(1).radius(1).color(Color.rgb(60, 60, 60)).build();
    SettingsPanel settingsPanel = null;
    String style = "-fx-base: rgb(10,10,10);-fx-background-color: rgb(240,240,240);";
    ComboBox proxyHost;
    ComboBox proxyPort;
    
    
    public NetworkTab(SettingsPanel parent) {
        this.settingsPanel = parent;
        AccountsDBStore.loadKnownProxiesList();
        ImageView bckGndImgView = ImageViewBuilder.create().image(UIutil.getImage("linen.png")).smooth(true).opacity(0.5).fitWidth(305).fitHeight(359).cache(true).preserveRatio(false).visible(true).build();

        VBox rootBox = new VBox(30);

        Text tabDesc = TextBuilder.create().text("Network Settings").fill(Color.LIGHTGRAY).font(Font.font(null, FontWeight.BOLD, 24)).effect(is).textOrigin(VPos.CENTER).translateX(20).build();

        rootBox.getChildren().addAll(tabDesc, createProxyHost(), createProxyPort(), createSaveButton(), createClearProxies());
        VBox.setMargin(tabDesc, new Insets(5, 10, 0, 0));
        rootBox.setAlignment(Pos.TOP_CENTER);
        rootBox.setTranslateY(20);

        getChildren().addAll(bckGndImgView, rootBox);
        setUserData(proxyHost);//Beacause we want to get this to request focus at later point in time
        setupFocusedListeners();
        if (AccountsDBStore.savedProxies.size() > 0) {//There is some proxy so just use it
            TalkSettings.PROXY_SERVER = proxyHost.getEditor().getText();
            TalkSettings.PROXY_PORT = Integer.valueOf(proxyPort.getEditor().getText());
            //TODO: Validate proxy and port and popup message if invalid
            TalkSettings.proxyChanged.set(true);
        }
    }
    
    /**
     * Creates proxyHost combobox
     * @return 
     */
    private  Group createProxyHost() {
        Rectangle rectHostBgnd = RectangleBuilder.create().width(280).height(34).fill(Color.rgb(166, 166, 166)).stroke(Color.rgb(90, 90, 90)).arcHeight(10).arcWidth(10).build();
        //final TextField proxyHost = TextFieldBuilder.create().translateX(3).translateY(3).promptText("Proxy Host..").styleClass("ntTextField").prefWidth(273).effect(is).build();
        proxyHost = ComboBoxBuilder.create().editable(true).promptText("Proxy Host..").style(style).minWidth(274).minHeight(26).translateX(3).translateY(3).build();
        proxyHost.setPromptText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[0]:"Proxy Host.."));
        proxyHost.getEditor().setText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[0]:""));
        //proxyHost.getStyleClass().add("ntTextField");
        proxyHost.setCellFactory(SimpleFormatCell.createCellFactory());
        proxyHost.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ENTER) {
                    saveAction();
                }
            }
        });
        return GroupBuilder.create().children(rectHostBgnd, proxyHost).translateY(12).translateX(12).build();
        
    }
    /**
     * Creates proxyPort combobox
     * @return 
     */
    private Group createProxyPort() {
        Rectangle rectPassBgnd = RectangleBuilder.create().width(280).height(34).fill(Color.rgb(166, 166, 166)).stroke(Color.rgb(90, 90, 90)).arcHeight(10).arcWidth(10).build();
        //final TextField proxyPort = TextFieldBuilder.create().translateX(3).translateY(3).promptText("Proxy Port..").styleClass("ntTextField").prefWidth(273).effect(is).build();
        proxyPort = ComboBoxBuilder.create().editable(true).style(style).promptText("Proxy Port..").minWidth(274).minHeight(26).translateX(3).translateY(3).build();
        proxyPort.setPromptText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[1]:"Proxy Port.."));
        proxyPort.getEditor().setText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[1]:""));
        //proxyPort.getStyleClass().add("ntTextField");
        proxyPort.setCellFactory(SimpleFormatCell.createCellFactory());
        proxyPort.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ENTER) {
                    saveAction();
                }
            }
        });
        return GroupBuilder.create().children(rectPassBgnd, proxyPort).translateY(12).translateX(12).build();
    }
    /**
     * Creates save Button
     * @return 
     */
    private Button createSaveButton() {
        final Button saveButton = ButtonBuilder.create().text("Save Settings").id("dark-blue").build();
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
     * Clear proxies button
     * @return 
     */
    private Button createClearProxies() {
        Button clearProxiesButton = ButtonBuilder.create().text("Clear Proxies").id("dark-blue").build();
        clearProxiesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                proxyHost.getItems().clear();
                proxyPort.getItems().clear();
                proxyHost.getEditor().clear();
                proxyPort.getEditor().clear();
                AccountsDBStore.savedProxies.clear();
                AccountsDBStore.deleteAllProxies();
                proxyHost.setPromptText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[0]:"Proxy Host.."));
                proxyHost.getEditor().setText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[0]:""));
                proxyPort.setPromptText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[1]:"Proxy Port.."));
                proxyPort.getEditor().setText(((AccountsDBStore.savedProxies.size() > 0)?AccountsDBStore.savedProxies.get(0).split(":")[1]:""));
                saveAction();
            }
        });
        return clearProxiesButton;
    }
    /**
     * When proxy host/port's combo box is focused we load suggestion.
     */
    private void setupFocusedListeners() {
        proxyHost.getEditor().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                if (newVal) {
                    proxyHost.getItems().clear();
                    for (int i = 0;i < AccountsDBStore.savedProxies.size();i++) {
                        proxyHost.getItems().add(AccountsDBStore.savedProxies.get(i).split(":")[0]);
                    }
                }
            }
        });
        proxyPort.getEditor().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                if (newVal) {
                    proxyPort.getItems().clear();
                    for (int i = 0;i < AccountsDBStore.savedProxies.size();i++) {
                        proxyPort.getItems().add(AccountsDBStore.savedProxies.get(i).split(":")[1]);
                    }
                }
            }
        });
    }
/**
 * Save action.
 */
    private void saveAction() {
        if ((proxyHost.getEditor().getText() == null || proxyHost.getEditor().getText().trim().equals("")) && !Util.isNumber(proxyPort.getEditor().getText())) {
            TalkSettings.PROXY_SERVER = "";
            TalkSettings.PROXY_PORT = 0;
            //TODO: Validate proxy and port and popup message if invalid
            TalkSettings.proxyChanged.set(false);
        } else {
            TalkSettings.PROXY_SERVER = proxyHost.getEditor().getText();
            TalkSettings.PROXY_PORT = Integer.valueOf(proxyPort.getEditor().getText());
            //TODO: Validate proxy and port and popup message if invalid
            TalkSettings.proxyChanged.set(true);
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
