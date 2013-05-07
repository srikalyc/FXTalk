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
import fxtalk.beans.Account;
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.AccountsDBStore;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
 * Manage accounts like set default account,load account, delete account, see
 * whats the current default account
 *
 * @author srikalyc
 */
public class AccountsTab extends Group {

    SettingsPanel settingsPanel = null;
    ComboBox existingAccntsComboBox = null;
    String style = "-fx-base: rgb(10,10,10); -fx-background-color: rgb(240,240,240);";
    public static final double TAB_WIDTH = 305;
    public static final double TAB_HEIGHT = 359;
    
    public AccountsTab(SettingsPanel parent) {
        this.settingsPanel = parent;
        ImageView bckGndImgView = ImageViewBuilder.create().image(UIutil.getImage("linen.png")).smooth(true).opacity(0.5).fitWidth(TAB_WIDTH).fitHeight(TAB_HEIGHT).cache(true).preserveRatio(false).visible(true).build();
        InnerShadow is = InnerShadowBuilder.create().offsetY(1).radius(1).color(Color.rgb(60, 60, 60)).build();

        VBox rootBox = VBoxBuilder.create().spacing(20).translateX(5).translateY(20).alignment(Pos.TOP_CENTER).build();
        Text tabDesc = TextBuilder.create().text("Manage Accounts").fill(Color.LIGHTGRAY).font(Font.font(null, FontWeight.BOLD, 24)).effect(is).textOrigin(VPos.CENTER).build();
        AccountsDBStore.load();

        //existingAccntsComboBox = ComboBoxBuilder.create().maxWidth(TAB_WIDTH - 30).prefWidth(TAB_WIDTH - 30).build();
        Group existingAccounts = createAccountsCombo();
        reloadAccounts();

        Button loadAccountButton =   ButtonBuilder.create().text(" Load Account ").id("dark-blue").build();
        Button deleteAccountButton = ButtonBuilder.create().text("Delete Account").id("dark-blue").build();
        Button setDefaultButton =    ButtonBuilder.create().text("Set As Default").id("dark-blue").build();
        setDefaultButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String selectedAccntUserName = existingAccntsComboBox.getSelectionModel().getSelectedItem().toString();
                Account selectedAccnt = AccountsDBStore.userNameAccntMap.get(selectedAccntUserName);
                AccountsDBStore.saveDefaultAccount(selectedAccnt);
            }
        });
        deleteAccountButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String selectedAccntUserName = existingAccntsComboBox.getSelectionModel().getSelectedItem().toString();
                Account selectedAccnt = AccountsDBStore.userNameAccntMap.get(selectedAccntUserName);
                AccountsDBStore.deleteAccount(selectedAccnt);
                existingAccntsComboBox.getItems().remove(existingAccntsComboBox.getSelectionModel().getSelectedItem());
            }
        });
        loadAccountButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String selectedAccntUserName = existingAccntsComboBox.getSelectionModel().getSelectedItem().toString();
                Account selectedAccnt = AccountsDBStore.userNameAccntMap.get(selectedAccntUserName);
                FXTalkApp.app.loginScreen.id.setText(selectedAccntUserName);
                FXTalkApp.app.loginScreen.pass.setText(selectedAccnt.getPassword());
                FXTalkApp.app.loginScreen.advancedGroup.server.setText(selectedAccnt.getServer());
                FXTalkApp.app.loginScreen.advancedGroup.port.setText(selectedAccnt.getPort() + "");
                FXTalkApp.app.loginScreen.advancedGroup.enableSSLCheckBox.setSelected(selectedAccnt.isEnableSSL());
            }
        });

        rootBox.getChildren().addAll(tabDesc, existingAccounts, loadAccountButton, deleteAccountButton, setDefaultButton);
        VBox.setMargin(existingAccntsComboBox, new Insets(0, 0, 0, 14));
        getChildren().addAll(bckGndImgView, rootBox);
    }

   /**
     * Creates existingAccntsComboBox combobox
     * @return 
     */
    private  Group createAccountsCombo() {
        Rectangle rectHostBgnd = RectangleBuilder.create().width(280).height(34).fill(Color.rgb(166, 166, 166)).stroke(Color.rgb(90, 90, 90)).arcHeight(10).arcWidth(10).build();
        existingAccntsComboBox = ComboBoxBuilder.create().promptText("Existing accounts").style(style).minWidth(274).minHeight(26).translateX(3).translateY(3).build();
        //existingAccntsComboBox.setStyle("-fx-base: rgb(10,10,10);-fx-background-color: rgb(240,240,240);");
        existingAccntsComboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new SimpleFormatCell();
            }
        });
        return GroupBuilder.create().children(rectHostBgnd, existingAccntsComboBox).translateY(12).translateX(12).build();
    }

    /**
     * Called each time when you save a new Account(See loginScreen's login()
     * method)
     */
    public void reloadAccounts() {
        AccountsDBStore.loadKnownAccountsList();
        existingAccntsComboBox.getItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                if (existingAccntsComboBox.getItems().size() == 0) {
                    existingAccntsComboBox.setVisible(false);
                } else {
                    existingAccntsComboBox.setVisible(true);
                }
            }
        });
        existingAccntsComboBox.getItems().clear();
        existingAccntsComboBox.getItems().addAll(AccountsDBStore.userNameAccntMap.keySet());

    }

    /**
     * To control text color layout etc that goes into combobox list.
     */
    private static class SimpleFormatCell extends ListCell<String> {

        @Override
        public void updateItem(String item, boolean empty) {
            setStyle("-fx-text-fill: white;");
            setAlignment(Pos.CENTER);
            super.updateItem(item, empty);
            setText(item == null ? "" : item);

        }
    }
}
