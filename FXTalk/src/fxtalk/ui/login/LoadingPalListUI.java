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

import fxtalk.utils.Util;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 *
 * @author srikalyanchandrashekar
 */
public class LoadingPalListUI extends StackPane {

    LoadingPalListService service = null;
    Region veil = new Region();
    ProgressIndicator progIndic = ProgressIndicatorBuilder.create().style("-fx-base: rgb(58,58,58);").build();

    public LoadingPalListUI() {
        setVisible(true);
        veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        progIndic.setMaxSize(75, 75);
        getChildren().addAll(veil, progIndic);
    }

    public void service() {
        setVisible(true);//Just to make sure
        service = new LoadingPalListService(this);
        progIndic.progressProperty().bind(service.progressProperty());
        veil.visibleProperty().bind(service.runningProperty());
        progIndic.visibleProperty().bind(service.runningProperty());
        service.start();
    }

    class LoadingPalListService extends Service {
        LoadingPalListUI parentUI;
        LoadingPalListService(LoadingPalListUI parentUI) {
            this.parentUI = parentUI;
        }
        
        @Override
        protected Task createTask() {
            return new LoadingPalListTask();
        }

        public class LoadingPalListTask extends Task {

            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < 500; i++) {
                    updateProgress(i, 500);
                    Util.gotoSleep(10);
                }
                parentUI.setVisible(false);
                return null;
            }
        }
    }
}
