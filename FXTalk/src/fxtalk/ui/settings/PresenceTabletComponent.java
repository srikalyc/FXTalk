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
import fxtalk.ui.misc.UIutil;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.InnerShadowBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Popup;
import javafx.stage.PopupBuilder;

/**
 * Component for selecting presence of user
 * @author srikalyc
 */
public class PresenceTabletComponent extends FlowPane {
    InnerShadow is = InnerShadowBuilder.create().offsetX(0.0).offsetY(0.0).build();
    InnerShadow nullIs = null;//Null innershadow because bind does not directly accept null

    ImageView busyTabletComp = ImageViewBuilder.create().image(UIutil.getImage("leftTablet.png")).build();
    ImageView availableTabletComp = ImageViewBuilder.create().image(UIutil.getImage("centerTablet.png")).build();
    ImageView invisibleTabletComp = ImageViewBuilder.create().image(UIutil.getImage("rightTablet.png")).build();
    public BooleanProperty busyTabletSelected = new SimpleBooleanProperty(false);
    public BooleanProperty availableTabletSelected = new SimpleBooleanProperty(true);
    public BooleanProperty invisibleTabletSelected = new SimpleBooleanProperty(false);
    Popup presenceChangePopup;
    
    public PresenceTabletComponent() {
        setEffect(new DropShadow());
        getChildren().addAll(busyTabletComp, availableTabletComp, invisibleTabletComp);
        setupBindings();
        setupCommonMouseActions();
        setupComponentMouseActions();
        presenceChangePopup = PopupBuilder.create().autoHide(true).autoFix(true).content(this).build();
    }
    
    public void showPopup() {
        presenceChangePopup.show(FXTalkApp.app.primaryStage);
    }
    public void showPopup(double x, double y) {
        presenceChangePopup.show(FXTalkApp.app.primaryStage, x, y);
    }
    
    public void hidePopup() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                presenceChangePopup.hide();
            }
        });
    }

    private void setupBindings() {
        busyTabletComp.effectProperty().bind(new When(busyTabletSelected).then(is).otherwise(nullIs));
        availableTabletComp.effectProperty().bind(new When(availableTabletSelected).then(is).otherwise(nullIs));
        invisibleTabletComp.effectProperty().bind(new When(invisibleTabletSelected).then(is).otherwise(nullIs));
    }
/**
 * For each of the tablet setup mouse actions.
 */    
    private void setupComponentMouseActions() {
        busyTabletComp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (busyTabletSelected.get())//Nothing to select
                    return;
                FXTalkApp.app.conn.goBusy();
                hidePopup();
            }
        });
        availableTabletComp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (availableTabletSelected.get())//Nothing to select
                    return;
                FXTalkApp.app.conn.goOnline();
                hidePopup();
            }
        });
        invisibleTabletComp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (invisibleTabletSelected.get())//Nothing to select
                    return;
                FXTalkApp.app.conn.goInvisible();
                hidePopup();
            }
        });
    }
/**
 * Mouse actions for the whole component
 */
    private void setupCommonMouseActions() {
        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                setCursor(Cursor.HAND);
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                setCursor(Cursor.DEFAULT);
            }
        });
    }
}
