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

package fxtalk.ui.login;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.VBox;

/**
 * Advanced setting UI component like server, port, SSL information etc required for logging into
 * and account
 * @author srikalyc
 */
public class AdvancedGroup extends Group {

    Label serverLabel = LabelBuilder.create().translateX(75).translateY(5).mouseTransparent(true).text("Server").build();
    Label portLabel = LabelBuilder.create().translateX(95).translateY(5).mouseTransparent(true).text("Port").build();
    public TextField server = TextFieldBuilder.create().alignment(Pos.CENTER).text("talk.google.com").styleClass("ntTextField").build();
    public TextField port = TextFieldBuilder.create().alignment(Pos.CENTER).text("5222").styleClass("ntTextField").build();
    public CheckBox enableSSLCheckBox = CheckBoxBuilder.create().text("Enable SSL").style("-fx-text-fill: white;").selected(true).build();
    Button resetToDefaultButton = ButtonBuilder.create().text("Reset").id("dark-blue").build();
    VBox box = new VBox(10);

    public AdvancedGroup() {
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(server, port, enableSSLCheckBox, resetToDefaultButton);
        getChildren().add(box);
        resetToDefaultButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                server.setText("talk.google.com");
                port.setText("5222");
                enableSSLCheckBox.setSelected(true);
            }
        });        
    }
}
