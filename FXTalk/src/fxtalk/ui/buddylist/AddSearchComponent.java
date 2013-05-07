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
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.Util;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

/**
 * A text field with add buddy and search abilities(both use the same textfield
 * input) when user click on add(we add buddy), when user click on search(we
 * search) when user types ENTER (we do search by default)
 *
 * @author srikalyc
 */
public class AddSearchComponent extends StackPane {

    public int WIDTH = 305;
    public int HEIGHT = 38;
    Tooltip addTooltip = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Add Buddy").alignment(Pos.CENTER).build()).build();
    Tooltip searchTooltip = TooltipBuilder.create().graphic(LabelBuilder.create().textFill(Color.WHITE).text("Filter Buddy").alignment(Pos.CENTER).build()).build();
    Rectangle addRectOverlay = RectangleBuilder.create().width(32).height(34).translateX(-WIDTH / 2.0 + 20).fill(Color.TRANSPARENT).cursor(Cursor.HAND).build();
    ImageView addImgView = ImageViewBuilder.create().image(UIutil.getImage("add.png")).smooth(true).cache(true).preserveRatio(true).visible(true).translateX(-WIDTH / 2.0 + 20).build();
    ImageView search = ImageViewBuilder.create().image(UIutil.getImage("search.png")).smooth(true).translateX(WIDTH / 2.0 - 20).build();
    ImageView searchBox = ImageViewBuilder.create().image(UIutil.getImage("searchBox.png")).smooth(true).visible(true).build();
    ImageView searchBoxNoPrompt = ImageViewBuilder.create().image(UIutil.getImage("searchBoxNoPrompt.png")).visible(false).smooth(true).build();
    TextField searchField = TextFieldBuilder.create().minWidth(200).prefWidth(200).maxWidth(200).text("").build();

    public AddSearchComponent() {
        search.setCursor(Cursor.HAND);
        searchBox.visibleProperty().bind(searchBoxNoPrompt.visibleProperty().not());
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: white;");
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldV, String newV) {
                if (newV != null && newV.equals("")) {
                    searchBoxNoPrompt.setVisible(false);
                } else {
                    searchBoxNoPrompt.setVisible(true);
                }
            }
        });
        getChildren().addAll(searchBoxNoPrompt, searchBox, searchField, addImgView, addRectOverlay, search);
        setupActions();
        addTooltip.getStyleClass().add("settings_tt");
        searchTooltip.getStyleClass().add("settings_tt");
        Tooltip.install(addRectOverlay, addTooltip);
        Tooltip.install(search, searchTooltip);

    }

    private void setupActions() {
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldV, String newV) {
                if (newV.equals("")) {
                    FXTalkApp.app.palListPanel.clearFilter();
                }
            }
        });
        searchField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                searchAction();
            }
        });
        search.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                searchAction();
            }
        });
        addRectOverlay.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                addBuddyAction();
            }
        });
    }

    /**
     * Does the search
     */
    private void searchAction() {
        if (!searchField.getText().trim().equals("")) {
            FXTalkApp.app.palListPanel.filter(searchField.getText());
        }
    }

    private void addBuddyAction() {
        System.out.println("Add buddy");
        String myself = FXTalkApp.app.conn.accnt.getUserName();
        String domain = myself.substring(myself.indexOf("@"));
        if (searchField.getText().trim().equals(""))
            return;
        String buddyJid = "";
        if (searchField.getText().endsWith(domain)) {
            //We are good to go
            buddyJid = searchField.getText();
        } else if (!searchField.getText().contains("@")) {//This means the name doesn't end with @domainnameexpected
            buddyJid = searchField.getText() + domain;
        } else {
            FXTalkApp.app.notifPanel.showNotificationMessage("Invalid user " + searchField.getText());
            return;
        }
        PalEntry palEntry = new PalEntry();
        palEntry.nameLabel.setText(buddyJid);
        palEntry.disablePalEntry("Pending approval.");
        FXTalkApp.app.palListPanel.palListItems.add(palEntry);
        Util.DEBUG("pallist items are "+ FXTalkApp.app.palListPanel.palListItems);
        FXTalkApp.app.conn.presenceTracker.palEntries.put(buddyJid, palEntry);
        FXTalkApp.app.notifPanel.showNotificationMessage("Sent friend request to " + buddyJid);
        FXTalkApp.app.conn.sendFriendRequest(buddyJid);
        
    }
}
