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

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.util.Duration;

/**
 * A binary state switch. This component is not designed to work , if you scale it,
 * though it can easily be extended to work by changing the WIDTH, HEIGHT, 
 * minBallTransX and maxBallTransX accordingly when the node has been scaled(listen
 * to scale changes).
 *
 * @author srikalyc
 */
public class SwitchComponent extends HBox {

    public int WIDTH = 72;//Switch's width
    public int HEIGHT = 31;//Switch's height
    private double startGroupDragX = 0.0;
    Image offSwitchImg = UIutil.getImage("offSwitch.png");
    Image onSwitchImg = UIutil.getImage("onSwitch.png");
    ImageView switchImgView = ImageViewBuilder.create().smooth(true).cache(true).image(offSwitchImg).preserveRatio(true).build();
    ImageView ballImgView = ImageViewBuilder.create().smooth(true).cache(true).image(UIutil.getImage("ball.png")).preserveRatio(true).translateY(2.5).cursor(Cursor.HAND).mouseTransparent(false).build();
    private double minBallTransX = 0.0;
    private double maxBallTransX = WIDTH - ballImgView.getImage().getWidth();
    private double toBeTranslatedTo = 0.0;//This is where the user drags and releases the mouses
    private double actualTobeTranslatedTo = 0.0;//This is where the ballImgView actually animates to from (tobeTranslatedTo)
    Timeline timeline = new Timeline();
    private State currentState;
    Group component = GroupBuilder.create().children(switchImgView, ballImgView).build();
    
    public SwitchComponent(String text) {
        Text label = TextBuilder.create().text(text).fill(Color.WHITE).build();
        if (text.equals("")) {
            getChildren().addAll(component);
        } else {
            getChildren().addAll(label, component);
            setSpacing(10);
        }
        setAlignment(Pos.CENTER);
        setMouseActionsForItems();
    }

    /**
     * This is where the actual sauce is
     */
    private void setMouseActionsForItems() {
        switchImgView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (isOn()) {
                    toBeTranslatedTo = minBallTransX + 1;//just increase by one count to enable animation go through successfully.
                } else {
                    toBeTranslatedTo = maxBallTransX - 1;//just decrease by one count to enable animation go through successfully.
                }
                animate();
            }
        });
        
        
        
        ballImgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                ballImgView.setCursor(Cursor.CLOSED_HAND);
                startGroupDragX = me.getX();
            }
        });
        ballImgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                ballImgView.setCursor(Cursor.DEFAULT);
                animate();
            }
        });
        ballImgView.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                toBeTranslatedTo = ballImgView.getTranslateX() + me.getX() - startGroupDragX;
                if (toBeTranslatedTo >= minBallTransX && toBeTranslatedTo <= maxBallTransX) {//toBeTranslated is within the boundary
                    ballImgView.setTranslateX(toBeTranslatedTo);
                } else {//Bring back the toBeTranslated to boundary(min (OR) max) but not beyond the boundaries
                    toBeTranslatedTo = (toBeTranslatedTo < minBallTransX)? minBallTransX:maxBallTransX;
                }
            }
        });
    }

    /**
     * Don't stay where you left instead move to one of the 2 binary states.
     */
    private void animate() {
        if (timeline != null && timeline.getStatus() != Animation.Status.STOPPED) {
            return;//The animation is in progress just ignore.
        }
        if (toBeTranslatedTo == minBallTransX && isOn()) {//Because the user dragged untill the very end to minimum so do not animate just change the state
            setState(false);
            return;//No where to go
        } else if (toBeTranslatedTo == maxBallTransX && !isOn()) {//Because the user dragged untill the very end to maximum so do not animate just change the state
            setState(true);
            return;//No where to go
        }

        timeline = new Timeline();

        final KeyValue kvbegin1 = new KeyValue(ballImgView.translateXProperty(), toBeTranslatedTo, Interpolator.EASE_BOTH);
        final KeyFrame kfbegin1 = new KeyFrame(Duration.millis(10), kvbegin1);

        if (toBeTranslatedTo <= (maxBallTransX - minBallTransX) / 2.0) {
            actualTobeTranslatedTo = minBallTransX;
            setState(false);
        } else {
            actualTobeTranslatedTo = maxBallTransX;
            setState(true);
        }
        switchImgView.setImage(isOn()?onSwitchImg:offSwitchImg);

        final KeyValue kvend1 = new KeyValue(ballImgView.translateXProperty(), actualTobeTranslatedTo, Interpolator.EASE_IN);
        final KeyFrame kfend1 = new KeyFrame(Duration.millis(250), kvend1);

        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(kfbegin1, kfend1);
        timeline.play();

    }

    /**
     * This can be used to set state programmatically
     *
     * @param state
     */
    public void setState(boolean state) {
        if (state && currentState == State.ON) {
            //Nothing to do
        } else if (!state && currentState == State.OFF) {
            //Nothing to do
        } else {
            currentState = state ? State.ON : State.OFF;
            ballImgView.setTranslateX((state) ? maxBallTransX : minBallTransX);
            switchImgView.setImage(state?onSwitchImg:offSwitchImg);
            if (state) {
                performOnAction();
            } else {
                performOffAction();
            }
        }
    }
    /**
     * An opportunity for users of this component to do custom action when state
     * changes to ON. Just override this.
     */
    public void performOnAction() {
    }
    /**
     * An opportunity for users of this component to do custom action when state 
     * changes to OFF. Just override this.
     */
    public void performOffAction() {
    }

    /**
     * If armed
     *
     * @return
     */
    public boolean isOn() {
        if (currentState == State.ON) {
            return true;
        }
        return false;
    }

    static enum State {

        OFF,
        ON
    }
}
