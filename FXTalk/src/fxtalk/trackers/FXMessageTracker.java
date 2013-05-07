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

package fxtalk.trackers;

import fxtalk.network.ConnectionHandle;
import fxtalk.FXTalkApp;
import fxtalk.ui.chat.AccordionChatLogPanel;
import fxtalk.utils.Util;
import javafx.application.Platform;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Track messages received, new chats etc. Has methods to support receiving files too.
 * @author srikalyc
 */
public class FXMessageTracker implements MessageListener {

    private Message incomingMsg;
    private String file;
    public ConnectionHandle handle;

    public FXMessageTracker() {
    }

    public FXMessageTracker(ConnectionHandle handle) {
        this.handle = handle;
    }

    @Override
    public void processMessage(final Chat chat, final Message msg) {
        this.setIncomingMsg(msg);
        if (msg == null || msg.getBody() == null || msg.getBody().trim().equals("")) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String displayName = Util.getShortNameFromJid(chat.getParticipant());
                FXTalkApp.app.chatLogPanel.addItemToChatLog(chat.getParticipant(), displayName + ": " + msg.getBody());
                Util.DEBUG(displayName + " : " + msg.getBody());
            }
            
        });
        if (FXTalkApp.app.chatLogPanel instanceof AccordionChatLogPanel) {
            AccordionChatLogPanel chatPanel = (AccordionChatLogPanel)FXTalkApp.app.chatLogPanel;
            if (chatPanel.inputTextArea.isFocused() && Util.getJidStripOffTrailingDetails(chat.getParticipant()).equals(FXTalkApp.app.currentPal)) {
                return;//Do not show incoming message notifications(You are actively chatting with this guy)
            }
        }
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //Play ding sound
                Util.dingSound();
                FXTalkApp.app.notifPanel.showNotification(chat.getParticipant(), msg.getBody());
            }
            
        });
    }

    public Message getIncomingMsg() {
        return incomingMsg;
    }

    public void setIncomingMsg(Message incomingMsg) {
        this.incomingMsg = incomingMsg;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
