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

import fxtalk.FXTalkApp;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

/**
 * Proxy settings, daemons that check if any activity is happening in any of the
 * active stages inorder to set the user idle etc.
 * @author srikalyc
 */
public class TalkSettings {
    public static String PROXY_SERVER = "";
    public static int PROXY_PORT = 0;
    
    public static boolean enableFileTransferService = true;
    public static BooleanProperty proxyChanged = new SimpleBooleanProperty(false);
    public static long lastActivityTimeStamp = System.currentTimeMillis();//When a message/packet is sent out we update this(useful to make the user idle)
    public static AtomicBoolean anyRecentActivity = new AtomicBoolean(false);
    public static AtomicBoolean isCurrentlyIdle = new AtomicBoolean(false);
    
    static {
        new Thread(new IdleTimestampSettingThrottlerThread(), "IdleTimestampSettingThrottler").start();
        new Thread(new IdlerThread(), "MakeIdle").start();
    }
    
    public static void setProxyProperties() {
        System.setProperty("http.proxyHost", PROXY_SERVER + "");
        System.setProperty("http.proxyPort", PROXY_PORT + "");
        System.setProperty("https.proxyHost", PROXY_SERVER + "");
        System.setProperty("https.proxyPort", PROXY_PORT + "");
        System.setProperty("socksProxyHost", PROXY_SERVER + "");
        System.setProperty("socksProxyPort", PROXY_PORT + "");
    }
    /**
     * All the JavaFX stages except Notification stages should be upon this method.
     * @param stage 
     */
    public static void hookupActivityTrackerForStage(Window stage) {
        stage.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                anyRecentActivity.set(true);
            }
        });
        stage.addEventHandler(KeyEvent.ANY, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                anyRecentActivity.set(true);
            }
        });
    }
    
    /**
     * This thread will sleep for a minute and check if any activity happened in the last
     * minute, if yes then it changes the timestamp, if no it doesn't do anything
     * and goes to sleep again. 
     */
    static class IdleTimestampSettingThrottlerThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                Util.gotoSleep(5000);//Every 5 seconds is good enough
                if (FXTalkApp.app == null || FXTalkApp.app.conn == null || 
                        FXTalkApp.app.conn.connection == null || 
                        !FXTalkApp.app.conn.connection.isConnected()) {
                    continue;
                }
                if (anyRecentActivity.getAndSet(false)) {
                    lastActivityTimeStamp = System.currentTimeMillis();
                    if (isCurrentlyIdle.getAndSet(false)) {
                        if (FXTalkApp.app.selfComponent.presenceTabletComp.busyTabletSelected.get()) {
                            FXTalkApp.app.conn.goBusy();
                        } else if (FXTalkApp.app.selfComponent.presenceTabletComp.availableTabletSelected.get()) {
                            FXTalkApp.app.conn.goOnline();
                        } else if (FXTalkApp.app.selfComponent.presenceTabletComp.invisibleTabletSelected.get()) {
                            FXTalkApp.app.conn.goInvisible();
                        }
                    }
                }
            }
        }
        
    }
    
    /**
     * Check if its time to become idle.
     */
    static class IdlerThread implements Runnable {
        @Override
        public void run() {
            while (true) {//We need some better flag to check and get out gracefully
                Util.gotoSleep(60000);//60 seconds is the minimum time(even if user sets idle time to less than a minute we don't honor it.
                if (FXTalkApp.app == null || FXTalkApp.app.conn == null || 
                        FXTalkApp.app.conn.connection == null || 
                        !FXTalkApp.app.conn.connection.isConnected()) {
                    continue;
                }
                if (FXTalkApp.app.selfComponent.presenceTabletComp.invisibleTabletSelected.get()) {
                    continue;//If invisible no point in becoming idle
                }
                long diffTimeMs = System.currentTimeMillis() -  TalkSettings.lastActivityTimeStamp;
                if (diffTimeMs >= (long)(AccountsDBStore.idleInMins.get() * 60 * 1000)) {
                        FXTalkApp.app.conn.goIdle();
                        isCurrentlyIdle.set(true);
                }

            }
        }
        
    }
    
    
}
