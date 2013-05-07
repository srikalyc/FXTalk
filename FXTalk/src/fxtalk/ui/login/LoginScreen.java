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

import fxtalk.beans.Account;
import fxtalk.FXTalkApp;
import fxtalk.utils.AccountsDBStore;
import fxtalk.network.JabberConnectionHandle;
import fxtalk.network.GtalkConnectionHandle;
import fxtalk.ui.misc.SwitchComponent;
import fxtalk.ui.misc.UIutil;
import fxtalk.ui.settings.AccountsTab;
import fxtalk.utils.TalkSettings;
import fxtalk.utils.Util;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Initial login screen, Actions like login , reset login screen etc.
 * @author srikalyc
 */
public class LoginScreen extends Stage {
    public static int WIDTH = 305;
    public static int HEIGHT = 300;
    
    public StackPane root = null;
    Rectangle overlay = RectangleBuilder.create().width(WIDTH).height(HEIGHT).arcWidth(30).arcHeight(30).fill(Color.BLACK).opacity(0.5).visible(false).build();
    InnerShadow is = InnerShadowBuilder.create().offsetY(1).radius(1).color(Color.rgb(60, 60, 60)).build();
    DropShadow ds = new DropShadow();
    
    //Text statusText = new Text("Login to your Account");
    public Text statusText = TextBuilder.create().text("Login to your Account").fontSmoothingType(FontSmoothingType.LCD).fill(Color.LIGHTGRAY).font(Font.font(null, FontWeight.BOLD, 24)).effect(is).textOrigin(VPos.CENTER).build();
    
    String transparentStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: rgb(122,122,122);";
    ImageView idpassImageView = ImageViewBuilder.create().image(UIutil.getImage("idPass.png")).smooth(true).build();
    ImageView idPromptpassImageView = ImageViewBuilder.create().image(UIutil.getImage("idPromptPass.png")).smooth(true).build();
    ImageView idpassPromptImageView = ImageViewBuilder.create().image(UIutil.getImage("idPassPrompt.png")).smooth(true).build();
    ImageView idpassNopromptImageView = ImageViewBuilder.create().image(UIutil.getImage("idPassNoprompt.png")).smooth(true).build();
    public TextField id = TextFieldBuilder.create().alignment(Pos.CENTER_LEFT).translateX(10).translateY(-20).minWidth(WIDTH - 100).maxWidth(WIDTH - 100).prefWidth(WIDTH - 100).style(transparentStyle).build();
    public PasswordField pass = PasswordFieldBuilder.create().alignment(Pos.CENTER_LEFT).translateX(10).translateY(20).minWidth(WIDTH - 100).maxWidth(WIDTH - 100).prefWidth(WIDTH - 100).style(transparentStyle).build();
    StackPane loginBox = StackPaneBuilder.create().children(idpassImageView, idPromptpassImageView, idpassPromptImageView, idpassNopromptImageView, id, pass).build();
    StringProperty idPassString = new SimpleStringProperty();
    
    
    SwitchComponent saveAccntCheckBox = new SwitchComponent("Save Account");
    
//    CheckBox saveAccntCheckBox = CheckBoxBuilder.create().text("Save Account").style("-fx-text-fill: white;").build();
    Button loginButton = ButtonBuilder.create().text("  Log in  ").id("dark-blue").build();
    Button advancedButton = ButtonBuilder.create().text("Advanced").id("dark-blue").build();

    public AdvancedGroup advancedGroup = new AdvancedGroup();
    boolean toggleAdvanced = false;
    
    public Account accnt = null;

    public LoginScreen() {
        idpassImageView.setVisible(true);
        idpassNopromptImageView.setVisible(false);
        idpassPromptImageView.setVisible(false);
        idPromptpassImageView.setVisible(false);
        idPassString.bind(id.textProperty().concat(pass.textProperty()));
        idPassString.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldV, String newV) {
                if (newV == null || newV.equals("")) {
                    idpassImageView.setVisible(true);
                    idpassNopromptImageView.setVisible(false);
                    idpassPromptImageView.setVisible(false);
                    idPromptpassImageView.setVisible(false);
                } else if (newV.equals(id.getText())) {
                    idpassImageView.setVisible(false);
                    idpassNopromptImageView.setVisible(false);
                    idpassPromptImageView.setVisible(true);
                    idPromptpassImageView.setVisible(false);
                } else if (newV.equals(pass.getText())) {
                    idpassImageView.setVisible(false);
                    idpassNopromptImageView.setVisible(false);
                    idpassPromptImageView.setVisible(false);
                    idPromptpassImageView.setVisible(true);
                } else {
                    idpassImageView.setVisible(false);
                    idpassNopromptImageView.setVisible(true);
                    idpassPromptImageView.setVisible(false);
                    idPromptpassImageView.setVisible(false);
                }
            }
        });
        
        final VBox allInOneVBox = new VBox(25);

        pass.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ENTER) {
                    signIn();
                }
            }
        });

/////////////////Advanced and Log in buttons///////////////        
        final HBox gpButton = HBoxBuilder.create().alignment(Pos.CENTER).spacing(10).build();
        gpButton.getChildren().addAll(advancedButton, loginButton );
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                signIn();
            }
        });
        
        advancedButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!toggleAdvanced) {
                    allInOneVBox.getChildren().removeAll(statusText, loginBox, saveAccntCheckBox);
                    gpButton.getChildren().remove(loginButton);
                    allInOneVBox.getChildren().add(advancedGroup);
                    advancedButton.setText("Back To Login");
                } else {
                    gpButton.getChildren().add(loginButton);
                    allInOneVBox.getChildren().remove(advancedGroup);
                    allInOneVBox.getChildren().remove(gpButton);
                    allInOneVBox.getChildren().addAll(statusText, loginBox, saveAccntCheckBox, gpButton);
                    advancedButton.setText("Advanced");
                }
                toggleAdvanced = !toggleAdvanced;
            }
        });


////////////////////All content Vbox//////////////
        allInOneVBox.getChildren().addAll(statusText,loginBox , saveAccntCheckBox, gpButton);
        allInOneVBox.setAlignment(Pos.CENTER);
        allInOneVBox.setPadding(new Insets(30, 0, 0, 10));//top,right,bottom,left

        root = StackPaneBuilder.create().children(createBackGroundCurvedRect() , allInOneVBox, overlay).build();
        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                FXTalkApp.app.settingsPanel.toFront();
                FXTalkApp.app.loginScreen.toFront();
            }
        });
        
        initDefaultValuesIfPossible();
    }
    
    
    private Node createBackGroundCurvedRect() {
//        Stop[] stops = new Stop[]{new Stop(0, Color.DARKGRAY),new Stop(0.3, Color.BLACK), new Stop(1.0, Color.GRAY)};
//        LinearGradient lg = new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, stops);
        Rectangle outline = RectangleBuilder.create().width(WIDTH - 1).height(HEIGHT - 1).arcWidth(30).arcHeight(30).stroke(Color.BLACK).fill(Color.TRANSPARENT).build();
//        Rectangle backGround = RectangleBuilder.create().width(WIDTH).height(HEIGHT).arcWidth(30).arcHeight(30).fill(lg).build();
        Rectangle clip = RectangleBuilder.create().width(WIDTH).height(HEIGHT).arcWidth(30).arcHeight(30).build();
        StackPane all = StackPaneBuilder.create().build();
//        all.getChildren().addAll(backGround, outline);
        all.getChildren().addAll(ImageViewBuilder.create().image(UIutil.getImage("MClogin.png")).effect(new Glow(0.4)).build());
        
        return all;
    }

    private void initDefaultValuesIfPossible() {
        AccountsDBStore.load();
        if (AccountsDBStore.defaultAccount != null) {
            id.setText(AccountsDBStore.defaultAccount.getUserName());
            pass.setText(AccountsDBStore.defaultAccount.getPassword());
            advancedGroup.server.setText(AccountsDBStore.defaultAccount.getServer());
            advancedGroup.port.setText("" + AccountsDBStore.defaultAccount.getPort());
            advancedGroup.enableSSLCheckBox.setSelected(AccountsDBStore.defaultAccount.isEnableSSL());
        }
        
    }
/**
 * This will spin off a thread that connects to collaboration server, if succeeds
 * then logs into it with specified user id and password. If login succeeds then
 * we will let the FXTalk app know about it calling loginSuccessful() else reset
 * login screen and present it to user again.
 */
    private void signIn() {
        FXTalkApp.app.appState.set(FXTalkApp.AppState.AUTH_IN_PROGRESS);
        //Check if username password are valid.
        if (id.getText().trim().equals("") || pass.getText().trim().equals("")) {
            FXTalkApp.app.appState.set(FXTalkApp.AppState.NOT_AUTHENTICATED);
            return;
        }
        //Check valid port number
        if (!Util.isNumber(advancedGroup.port.getText().trim())) {
            FXTalkApp.app.appState.set(FXTalkApp.AppState.NOT_AUTHENTICATED);
            return;
        }
        //It is the login screen's responsibility to setup connection
        accnt = new Account(id.getText(), pass.getText(), advancedGroup.server.getText(), Integer.parseInt(advancedGroup.port.getText().trim()), advancedGroup.enableSSLCheckBox.isSelected());
        if (saveAccntCheckBox.isOn()) {
            AccountsDBStore.saveAccount(accnt);
            ((AccountsTab)(FXTalkApp.app.settingsPanel.accntTab.getContent())).reloadAccounts();
        }
        loginButton.setText("Logging..");
        overlay.setVisible(true);
        new Thread(new Runnable() {//Long running task hence put this in thread.

            @Override
            public void run() {
                if (!id.getText().contains("@")) {//Invalid id
                    FXTalkApp.app.appState.set(FXTalkApp.AppState.INVALID_CREDENTIALS);
                    resetLoginScreen(false);
                    return;
                }
                if (id.getText().endsWith("@gmail.com")) {
                    FXTalkApp.app.conn = new GtalkConnectionHandle(accnt);
                } else {
                    FXTalkApp.app.conn = new JabberConnectionHandle(accnt);
                }
                final boolean isAuthenticated = FXTalkApp.app.conn.createConnection();
                if (isAuthenticated) {
                    if (TalkSettings.PROXY_SERVER != null && !TalkSettings.PROXY_SERVER.trim().equals("")) {
                        AccountsDBStore.saveProxy(TalkSettings.PROXY_SERVER + ":" + TalkSettings.PROXY_PORT);
                    }
                }

                Platform.runLater(new Runnable() {//This is a UI operation outside JavaFX Thread hence ..

                    @Override
                    public void run() {
                        if (isAuthenticated) {
                            FXTalkApp.app.loginSuccessful();
                        } else {
                            //FXTalkApp.app.appState.set(FXTalkApp.AppState.NOT_AUTHENTICATED);;
                            resetLoginScreen(false);
                        }
                    }
                });
            }
        }, "Login").start();
    }

    public void gainFocus() {
        id.requestFocus();
    }

    public void initialize() {
        initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(UIutil.getRsrcURI("fxtalk.css").toString());
        setScene(scene);
        scene.setFill(Color.TRANSPARENT);
//        jidLabel.getStyleClass().add("id-password-label");
//        passcodeLabel.getStyleClass().add("id-password-label");
        show();
    }
/**
 * Change the status of login screen based on the STATE of the App.
 */
    public void resetLoginScreen(final boolean resetEverything) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (resetEverything) {
                    FXTalkApp.app.appState.set(FXTalkApp.AppState.NOT_AUTHENTICATED);
                }
                loginButton.setText("Logg in");
                overlay.setVisible(false);
            }
        });

    }
}
