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

import fxtalk.FXTalkApp;
import fxtalk.ui.buddylist.SelfComponent;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.FileChooser;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Popup;
import javafx.stage.PopupBuilder;
import javax.imageio.ImageIO;

/**
 * Ability to select square portions of image , move the selection over the
 * image and save the image to file. This is provided as a JavaFX component.
 * Usage : Must call only from JavaFX user thread. ImageEditor ie = new
 * ImageEditor(); ie.browseForImage(); ie.saveImage() (OR) ie.cancelImage();
 *
 * @author srikalyc
 */
public class ImageEditor extends ScrollPane {
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JPG, PNG images", "*.jpg", "*.png");
    FileChooser fileChooser = FileChooserBuilder.create().title("Choose an Avatar").extensionFilters(filter).initialDirectory(new File(System.getProperty("user.home"))).build();

    SourceType sourceType;
    SelfComponent parent;
    File selectedImageFile = null;
    Image img = null;
    ImageView imageView;
    DoubleProperty beginX = new SimpleDoubleProperty(0.0);
    DoubleProperty beginY = new SimpleDoubleProperty(0.0);
    DoubleProperty endX = new SimpleDoubleProperty(0.0);
    DoubleProperty endY = new SimpleDoubleProperty(0.0);
    DoubleProperty squareW = new SimpleDoubleProperty(0.0);
    DoubleProperty squareH = new SimpleDoubleProperty(0.0);
    IntegerProperty squareLen = new SimpleIntegerProperty(0);
    Text selectedImageDims = TextBuilder.create().fill(Color.BLACK).build();
    Circle tLeft = CircleBuilder.create().radius(6.0).build();
    Circle tRight = CircleBuilder.create().radius(6.0).build();
    Circle bLeft = CircleBuilder.create().radius(6.0).build();
    Circle bRight = CircleBuilder.create().radius(6.0).build();
    Rectangle selectSquare = RectangleBuilder.create().fill(Color.WHITE).stroke(Color.BLACK).build();
    Group select = GroupBuilder.create().mouseTransparent(false).children(selectSquare, tLeft, tRight, bLeft, bRight, selectedImageDims).visible(false).opacity(0.4).build();
    Shape imageOverLayClip = null;
    ChangeListener<Number> squareLenListener = null;//This will listen for changes to square len(useful for
    //masking the image with overlay during selection process.)Note that masking the image also depends on the location of the swuare which we deal
    //within the mouseDragged event for select group.
    double startSelectGroupDragX = 0.0;
    double startSelectGroupDragY = 0.0;
    Pane root = new Pane();
   
    double WIDTH = 400;
    double HEIGHT = 400;
    Popup imageEditorPopup = null;
   
   
    private void init(SelfComponent parent, SourceType type) {
        setPrefWidth(WIDTH);
        setPrefHeight(HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-base: rgb(58,58,58); -fx-background-color: rgb(102,102,102);");
        //setPannable(true);
        setContent(root);
        if (type == SourceType.WEB_CAM) {
            //NOT SUPPORTED YET
        } else if (type == SourceType.LOCAL_FILE) {
            sourceType = type;
        }
        setEffect(new DropShadow());
        this.parent = parent;
        squareW.bind(new When(beginX.subtract(endX).greaterThan(0.0)).then(beginX.subtract(endX)).otherwise(endX.subtract(beginX)));
        squareH.bind(new When(beginY.subtract(endY).greaterThan(0.0)).then(beginY.subtract(endY)).otherwise(endY.subtract(beginY)));
        squareLen.bind(new When(squareH.greaterThan(squareW).and(squareW.lessThan(96))).then(squareW).otherwise(new When(squareW.greaterThan(squareH).and(squareH.lessThan(96))).then(squareH).otherwise(96)));
        bindSelectGroup();
        imageEditorPopup = PopupBuilder.create().autoHide(false).autoFix(true).content(createImageEditor()).build();
    }

    public ImageEditor(SelfComponent parent) {
        init(parent, SourceType.LOCAL_FILE);
    }

    public ImageEditor(SelfComponent parent, SourceType type) {
        init(parent, type);
    }

    public ImageEditor(SelfComponent parent, double w, double h) {
        WIDTH = w;
        HEIGHT = h;
        init(parent, SourceType.LOCAL_FILE);
    }
   
    public ImageEditor(SelfComponent parent, SourceType type, double w, double h) {
        WIDTH = w;
        HEIGHT = h;
        init(parent, type);
    }
    
    public void showPopup() {
        imageEditorPopup.show(FXTalkApp.app.primaryStage);
    }
    public void showPopup(double x, double y) {
        imageEditorPopup.show(parent, x, y);
    }
    
    
    public void hidePopup() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                imageEditorPopup.hide();
            }
        });
    }
    
    /**
     *
     * @return true if opened an image .
     */
    public boolean browseForImage() {
        //Editor popup is the parent, so when you cancel the editor the filchooser dialogue closes by itself.
        selectedImageFile = fileChooser.showOpenDialog(imageEditorPopup);
        if (selectedImageFile != null) {
            Util.DEBUG("Choosen file " + selectedImageFile.getAbsoluteFile());
            openImage();
            return true;
        }
        return false;
    }

    /**
     * Save image selection to file
     */
    public void saveImage() {
        if (imageOverLayClip != null && root.getChildren().contains(imageOverLayClip)) {
            root.getChildren().remove(imageOverLayClip);
        }
        select.setVisible(false);
        takeSnap();
        bindSelectGroup();
    }

    /**
     * Cancel image selection
     */
    public void cancelImage() {
        if (imageOverLayClip != null && root.getChildren().contains(imageOverLayClip)) {
            root.getChildren().remove(imageOverLayClip);
        }
        select.setVisible(false);
        bindSelectGroup();
    }

    /**
     * Bind the select Group object's positional parameters.
     */
    private void bindSelectGroup() {
        select.translateXProperty().bind(beginX);
        select.translateYProperty().bind(beginY);

        selectSquare.widthProperty().bind(squareLen);
        selectSquare.heightProperty().bind(squareLen);

        tLeft.translateXProperty().bind(selectSquare.xProperty());
        tRight.translateXProperty().bind(selectSquare.xProperty().add(squareLen));
        bLeft.translateYProperty().bind(selectSquare.yProperty().add(squareLen));
        bRight.translateXProperty().bind(selectSquare.xProperty().add(squareLen));
        bRight.translateYProperty().bind(selectSquare.yProperty().add(squareLen));

        selectedImageDims.translateXProperty().bind(selectSquare.xProperty().add(10.0));
        selectedImageDims.translateYProperty().bind(selectSquare.yProperty().subtract(10.0));
        selectedImageDims.textProperty().bind(squareLen.asString().concat(" X ").concat(squareLen.asString()));

        if (squareLenListener == null) {
            squareLenListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                   
                    if (imageOverLayClip != null  && root.getChildren().contains(imageOverLayClip)) {
                        root.getChildren().remove(imageOverLayClip);
                    }
                    imageOverLayClip = Shape.subtract(RectangleBuilder.create().fill(Color.BLACK).width(img.getWidth()).height(img.getHeight()).build(), RectangleBuilder.create().width(squareLen.get()).height(squareLen.get()).x(select.getTranslateX()).y(select.getTranslateY()).build());
                    imageOverLayClip.setMouseTransparent(false);
                    imageOverLayClip.setOpacity(0.3);
                    root.getChildren().add(1, imageOverLayClip);
                }
            };
            squareLen.addListener(squareLenListener);
        }
    }

    /**
     * UnBind the select Group object's positional parameters.
     */
    private void unbindSelectGroup() {
        select.translateXProperty().unbind();
        select.translateYProperty().unbind();

        selectSquare.widthProperty().unbind();
        selectSquare.heightProperty().unbind();

        tLeft.translateXProperty().unbind();
        tRight.translateXProperty().unbind();
        bLeft.translateYProperty().unbind();
        bRight.translateXProperty().unbind();
        bRight.translateYProperty().unbind();

        selectedImageDims.translateXProperty().unbind();
        selectedImageDims.translateYProperty().unbind();
        selectedImageDims.textProperty().unbind();
    }

    /**
     * This enables us to generate select square over the image for selection.
     */
    private void setMouseActionsforImage() {
        imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                beginX.set(me.getX());
                beginY.set(me.getY());
                select.setVisible(true);
            }
        });

        imageView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                endX.set(me.getX());
                endY.set(me.getY());
                unbindSelectGroup();
            }
        });

        imageView.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                endX.set(me.getX());
                endY.set(me.getY());
            }
        });
    }

    /**
     * Enables us to move/drag the select square around on the scene to move
     * selection.
     */
    private void setMouseActionsforSelectGroup() {
        select.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                select.setCursor(Cursor.CLOSED_HAND);
                startSelectGroupDragX = me.getX();
                startSelectGroupDragY = me.getY();
            }
        });
        select.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                select.setCursor(Cursor.DEFAULT);
            }
        });
        select.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                select.setTranslateX(select.getTranslateX() + me.getX() - startSelectGroupDragX);
                select.setTranslateY(select.getTranslateY() + me.getY() - startSelectGroupDragY);

                if (imageOverLayClip != null && root.getChildren().contains(imageOverLayClip)) {
                    root.getChildren().remove(imageOverLayClip);
                }
                imageOverLayClip = Shape.subtract(RectangleBuilder.create().fill(Color.BLACK).width(img.getWidth()).height(img.getHeight()).build(), RectangleBuilder.create().width(squareLen.get()).height(squareLen.get()).x(select.getTranslateX()).y(select.getTranslateY()).build());
                imageOverLayClip.setMouseTransparent(false);
                imageOverLayClip.setOpacity(0.3);
                root.getChildren().add(1, imageOverLayClip);
            }
        });
    }

    /**
     * Open an image in the Editor.
     */
    private void openImage() {
        root.getChildren().clear();
        try {
            img = new Image(new FileInputStream(selectedImageFile.getAbsolutePath()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImageEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        imageView = ImageViewBuilder.create().image(img).cache(false).build();
        root.getChildren().addAll(imageView, select);
        if (img.getWidth() < WIDTH) {//This wil ensure smaller images appear within the box in center
            root.setTranslateX((WIDTH - img.getWidth())/2.0);
        } else {
            root.setTranslateX(0.0);
        }
        if (img.getHeight()< HEIGHT) {//This wil ensure smaller images appear within the box in center
            root.setTranslateY((HEIGHT - img.getHeight())/2.0);
        } else {
            root.setTranslateY(0.0);
        }
       
        setMouseActionsforImage();
        setMouseActionsforSelectGroup();
    }

    /**
     * Does the actual content writing to image file.
     */
    private void takeSnap() {
        //Play click sound
        Util.clickSound();
        
        //Set ViewPort
        imageView.setViewport(new Rectangle2D(select.getTranslateX(), select.getTranslateY(), selectSquare.getWidth(), selectSquare.getHeight()));
        WritableImage nodeSnapshot = imageView.snapshot(new SnapshotParameters(), null);
        try {
            new File(AccountsDBStore.BASE_LOCATION + File.separator + AccountsDBStore.avatarFile).delete();
            ImageIO.write(SwingFXUtils.fromFXImage(nodeSnapshot, null), "png", new File(AccountsDBStore.BASE_LOCATION + File.separator + AccountsDBStore.avatarFile));
        } catch (IOException ex) {
            Logger.getLogger(ImageEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Reset ViewPort
        imageView.setViewport(null);
    }
    /**
     * Create avatar editor
     * @return 
     */
    private  VBox createImageEditor() {
        final Button saveImageButton = ButtonBuilder.create().text("Set As Avatar").id("dark-blue").disable(true).build();
        final Button cancelImageButton = ButtonBuilder.create().text("Cancel").id("dark-blue").build();
        final Button openImageButton = ButtonBuilder.create().text("Open Image").id("dark-blue").build();
        openImageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean selected = browseForImage();
                if (selected) {
                    saveImageButton.setDisable(false);
                }
            }
        });
        saveImageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveImage();//This will save image to local
                hidePopup();
                parent.setToLocalAvatar();// This will set the image to recently saved.
                FXTalkApp.app.conn.changeAvatar();
            }
        });
        
        cancelImageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                cancelImage();
                hidePopup();
            }
        });

        HBox avatarEditorButtonsHbox = HBoxBuilder.create().alignment(Pos.CENTER).children(openImageButton, saveImageButton, cancelImageButton).build();
        VBox avatarEditorAllVbox = VBoxBuilder.create().alignment(Pos.CENTER).children(this, avatarEditorButtonsHbox).build();
        return avatarEditorAllVbox;
    }

    public static enum SourceType {
        LOCAL_FILE,
        WEB_CAM
    }
}