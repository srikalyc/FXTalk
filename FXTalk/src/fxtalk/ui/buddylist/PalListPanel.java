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

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.util.Callback;

/**
 * See CSS class .list-cell redefined in fxtalk.css
 *
 * @author srikalyc
 */
public class PalListPanel extends Pane {

    public ObservableList<PalEntry> palListItems = FXCollections.observableArrayList();
    public ObservableList<PalEntry> palListFilteredItems = FXCollections.observableArrayList();//Once the palListItems are filtered  the remaining items are put into this(i.e invisible items are put in here)
    public ListView<PalEntry> palList = new ListView<PalEntry>(palListItems);
    public static int WIDTH = 305;
    public static int HEIGHT = 355;
    private static int BUFFER_HEIGHT = 1;
    DropShadow ds = DropShadowBuilder.create().build();
    Rectangle clipRect = RectangleBuilder.create().width(WIDTH).height(HEIGHT).arcWidth(10).arcHeight(10).fill(Color.WHITE).build();

    //public 
    public PalListPanel() {
        //setEffect(ds);
        //setTranslateX(2.5);
        //setTranslateY(5);
        calculatePalListViewDimensions();

        palList.setStyle("-fx-base: rgb(58,58,58); -fx-background-color: rgb(102,102,102);");
        palList.setOpacity(0.99);
        palListItems.addListener(new ListChangeListener<PalEntry>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends PalEntry> change) {
                calculatePalListViewDimensions();
            }
        });
        palList.setCellFactory(new Callback<ListView<PalEntry>, ListCell<PalEntry>>() {
            @Override
            public ListCell<PalEntry> call(ListView<PalEntry> list) {
                return new PalEntryCell();
            }
        });
        getChildren().add(palList);
    }
/**
 * When an item is added or removed from the buddy list we change its dimensions
 */
    private void calculatePalListViewDimensions() {
        double actualItemsHeight = palListItems.size() * PalEntry.HEIGHT;
        double height = (actualItemsHeight < HEIGHT)? (actualItemsHeight + BUFFER_HEIGHT):HEIGHT;
        if (height == HEIGHT && palList.getMinHeight() == HEIGHT && palList.getPrefHeight() == HEIGHT && palList.getMaxHeight() == HEIGHT) {
            //Nothing to set
        } else {
            palList.setMinSize(WIDTH, height);
            palList.setPrefSize(WIDTH, height);
            palList.setMaxSize(WIDTH, height);
            clipRect.setHeight(height);
            setClip(clipRect);
        }
    }
    
    public void resetPanel() {
        palList.getItems().removeAll(palListItems);
        palListItems.clear();
    }
    /**
     * Remove all the buddies matching partialStr and put in another list.
     * @param partialStr 
     */
    public void filter(String partialStr) {
        for (int i = 0;i < palListItems.size();i++) {
            PalEntry entry = (PalEntry)(palListItems.get(i));
            if (!entry.nameLabel.getText().contains(partialStr)) {
                palListFilteredItems.add(entry);
            }
        }
        palListItems.removeAll(palListFilteredItems);
    }
    /**
     * Add all the filtered out buddies back to the visible list. You also need to 
     * sort the list based on availabiltiy.
     */
    public void clearFilter() {
        palListItems.addAll(palListFilteredItems);
        sort();
        palListFilteredItems.clear();
    }
    /**
     * Sort based on the availability (See the PalEntry class and its inner classes)
     */
    public void sort() {
        FXCollections.sort(palListItems);    
    }
    
/**
 * Needed to have a finer control over the dimensions of the PalEntry .
 */
    static class PalEntryCell extends ListCell<PalEntry> {

        @Override
        public void updateItem(PalEntry item, boolean empty) {
            super.updateItem(item, empty);
            setMinSize(PalEntry.WIDTH, PalEntry.HEIGHT);
            setPrefSize(PalEntry.WIDTH, PalEntry.HEIGHT);
            setMaxSize(PalEntry.WIDTH, PalEntry.HEIGHT);
            setGraphic(item);
        }
    }
}
