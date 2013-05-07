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

package fxtalk.utils;

import fxtalk.ui.misc.UIutil;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

/**
 * Generates bubbles for chat logs.
 * @author srikalyanchandrashekar
 */
public class ChatNodeUIUtil {

    private static final int TOP_IMAGE_HEIGHT = 20;//was 12
    private static final int MIDDLE_IMAGE_HEIGHT = 15;//was 14
    private static final int BOTTOM_IMAGE_HEIGHT = 19;//was 18
    private static final int OVERALL_BUFFER_HEIGHT = -5;
    private static final double MAX_IMAGE_WIDTH = 320.0;//Max
    
    
    private static final int TOP_PLUS_BOTTOM_IMAGE_HEIGHT = TOP_IMAGE_HEIGHT + BOTTOM_IMAGE_HEIGHT;
    static Image topLeftBubbleImg = UIutil.getImage("lBubbleTopPart.png");
    static Image middleLeftBubbleImg = UIutil.getImage("lBubbleMiddlePart.png");
    static Image bottomLeftBubbleImg = UIutil.getImage("lBubbleBottomPart.png");
    static Image topRightBubbleImg = UIutil.getImage("rBubbleTopPart.png");
    static Image middleRightBubbleImg = UIutil.getImage("rBubbleMiddlePart.png");
    static Image bottomRightBubbleImg = UIutil.getImage("rBubbleBottomPart.png");
    static Image topLeftArcRectBubbleImg = UIutil.getImage("lTransRectTopPart.png");
    static Image middleLeftArcRectBubbleImg = UIutil.getImage("lTransRectMiddlePart.png");
    static Image bottomLeftArcRectBubbleImg = UIutil.getImage("lTransRectBottomPart.png");
    static Image topRightArcRectBubbleImg = UIutil.getImage("rTransRectTopPart.png");
    static Image middleRightArcRectBubbleImg = UIutil.getImage("rTransRectMiddlePart.png");
    static Image bottomRightArcRectBubbleImg = UIutil.getImage("rTransRectBottomPart.png");

    static Image topLeftNiceImg = UIutil.getImage("lTopNice.png");
    static Image middleLeftNiceImg = UIutil.getImage("lMiddleNice.png");
    static Image bottomLeftNiceImg = UIutil.getImage("lBottomNice.png");
    static Image topRightNiceImg = UIutil.getImage("rTopNice.png");
    static Image middleRightNiceImg = UIutil.getImage("rMiddleNice.png");
    static Image bottomRightNiceImg = UIutil.getImage("rBottomNice.png");

    static Image topRightNiceBigImg = UIutil.getImage("rTopNiceBig.png");//Height is 5 px more
    static Image bottomRightNiceBigImg = UIutil.getImage("rBottomNiceBig.png");//Height is 5 px more
    
    public static Shape getTransparentRectWithBorder(double w, double h, double arcW, double arcH, double borderW, Color borderColor, double opacity) {
        Rectangle outer = RectangleBuilder.create().width(w).height(h).arcHeight(arcH).arcWidth(arcW).fill(borderColor).build();
        Rectangle inner = RectangleBuilder.create().width(w - 2 * borderW).height(h - 2 * borderW).arcHeight(arcH - borderW / 2).arcWidth(arcW - borderW / 2).translateX(borderW).translateY(borderW).build();
        Shape path = Path.subtract(outer, inner);
        path.setOpacity(opacity);
        return path;
    }

    public static VBox getChatBubbleImage(boolean isLeft, Group parent, Bubble type) {
        
        Bounds textsBoundingBox = parent.getBoundsInLocal();
        VBox box = new VBox(0);
        double vBoxMaxHeight = TOP_PLUS_BOTTOM_IMAGE_HEIGHT + OVERALL_BUFFER_HEIGHT;
        ImageView bubbleTop = ImageViewBuilder.create().cache(true).smooth(true).build();
        ImageView bubbleBottom = ImageViewBuilder.create().cache(true).smooth(true).build();
        if (type == Bubble.YELLOW_BUBBLE) {
            if (isLeft) {
                bubbleTop.setImage(topLeftBubbleImg);
                bubbleBottom.setImage(bottomLeftBubbleImg);
            } else {
                bubbleTop.setImage(topRightBubbleImg);
                bubbleBottom.setImage(bottomRightBubbleImg);
            }
        } else if (type == Bubble.ARC_RECT_BUBBLE) {
            if (isLeft) {
                bubbleTop.setImage(topLeftArcRectBubbleImg);
                bubbleTop.setTranslateX(-7);
                bubbleBottom.setImage(bottomLeftArcRectBubbleImg);
            } else {
                bubbleTop.setImage(topRightArcRectBubbleImg);
                bubbleBottom.setImage(bottomRightArcRectBubbleImg);
            }
        } else if (type == Bubble.NICE_BUBBLE) {
            if (isLeft) {
                bubbleTop.setImage(topLeftNiceImg);
                bubbleBottom.setImage(bottomLeftNiceImg);
                box.setAlignment(Pos.BOTTOM_LEFT);
            } else {
                bubbleTop.setImage(topRightNiceImg);
                bubbleBottom.setImage(bottomRightNiceImg);
                box.setAlignment(Pos.BOTTOM_RIGHT);
            }
        }
        ///////Add the top part of bubble//////
        box.getChildren().add(bubbleTop);
        boolean atleastOneMiddleLayerAdded = false;
        ///////Add the middle part of bubble if needed //////
        if (textsBoundingBox.getHeight() > (TOP_PLUS_BOTTOM_IMAGE_HEIGHT - OVERALL_BUFFER_HEIGHT)) {
            double diff = textsBoundingBox.getHeight() - (TOP_PLUS_BOTTOM_IMAGE_HEIGHT - OVERALL_BUFFER_HEIGHT);
            for (int i = 0; i < Math.ceil(diff / MIDDLE_IMAGE_HEIGHT); i++) {
                vBoxMaxHeight += MIDDLE_IMAGE_HEIGHT;
                atleastOneMiddleLayerAdded = true;
                //TODO:very inefficient way, figure out a way to stitch all middle images into one image.
                if (type == Bubble.YELLOW_BUBBLE) {
                    if (isLeft) {
                        box.getChildren().add(ImageViewBuilder.create().image(middleLeftBubbleImg).cache(true).smooth(true).build());
                    } else {
                        box.getChildren().add(ImageViewBuilder.create().image(middleRightBubbleImg).cache(true).smooth(true).build());
                    }
                } else if (type == Bubble.ARC_RECT_BUBBLE) {
                    if (isLeft) {
                        box.getChildren().add(ImageViewBuilder.create().image(middleLeftArcRectBubbleImg).cache(true).smooth(true).build());
                    } else {
                        box.getChildren().add(ImageViewBuilder.create().image(middleRightArcRectBubbleImg).cache(true).smooth(true).build());
                    }
                } else if (type == Bubble.NICE_BUBBLE) {
                    if (isLeft) {
                        box.getChildren().add(ImageViewBuilder.create().image(middleLeftNiceImg).cache(true).smooth(true).build());
                    } else {
                        box.getChildren().add(ImageViewBuilder.create().image(middleRightNiceImg).cache(true).smooth(true).build());
                    }
                }
            }
        }
        ///////Add the bottom part of bubble //////
        box.getChildren().add(bubbleBottom);

        if (atleastOneMiddleLayerAdded) {
            if (type == Bubble.NICE_BUBBLE) {
                if (!isLeft) {//Right bubbles (if has any middle layer then use large top and bottom images)
                    bubbleTop.setImage(topRightNiceBigImg);
                    bubbleBottom.setImage(bottomRightNiceBigImg);
                }
            }
        } else {//One single liners (OR) similar sized texts
            if (type == Bubble.NICE_BUBBLE) {
                if (parent.getChildren().size() > 0 && parent.getChildren().get(0) instanceof Text) {
//Because the originalText has its wrappingWidth set, we cannot get its actual physical width, so play a trick to get physical width
                    Text originalText = (Text)(parent.getChildren().get(0));
                    final Text text = new Text(originalText.getText());
                    text.snapshot(null, null);
                    double textWidth = text.getLayoutBounds().getWidth();
                    double xScaleFactor = (textWidth + 40)/MAX_IMAGE_WIDTH;
                    if (textWidth * xScaleFactor >= MAX_IMAGE_WIDTH) {
                        //Do not scale we are overdoing
                        Util.DEBUG("We skip scaling textWidth * xScaleFactor = "  +textWidth + " * " +  xScaleFactor + " = " + textWidth * xScaleFactor);
                    } else {
                        box.setScaleX(xScaleFactor);
                        box.setTranslateX(-(MAX_IMAGE_WIDTH - (textWidth + 70))/2.0);
                        Util.DEBUG("Scaling "+ (textWidth + 40)/MAX_IMAGE_WIDTH + " translate X " + (-(MAX_IMAGE_WIDTH - (textWidth + 70))/2.0));
                    }
                }
            }
        }
        box.setPrefHeight(vBoxMaxHeight);
        box.setMaxHeight(vBoxMaxHeight);
        return box;
    }

    public static enum Bubble {

        YELLOW_BUBBLE,
        ARC_RECT_BUBBLE,
        NICE_BUBBLE
    }
}
