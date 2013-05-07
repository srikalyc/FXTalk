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

package fxtalk.network;

import fxtalk.beans.Account;
import fxtalk.FXTalkApp;
import fxtalk.trackers.FXMsgEventReqAndNotifTracker;
import fxtalk.trackers.FXPresenceTracker;
import fxtalk.trackers.FXMessageTracker;
import fxtalk.trackers.FXFileTransferListener;
import fxtalk.trackers.FXPacketTracker;
import fxtalk.utils.TalkSettings;
import fxtalk.utils.Util;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.provider.VCardProvider;

/**
 * ConnectionHandler specific to Google Talk. Special thing about this is we enable
 * SASL mechanism by default and ignore User settings.
 * @author srikalyc
 */
public class GtalkConnectionHandle extends ConnectionHandle{

    private static final String SERVICE_NAME = "gmail.com";
    
    public GtalkConnectionHandle(Account accnt) {
        super(accnt);
    }

    @Override
    public boolean createConnection() {
        FXTalkApp.app.appState.set(FXTalkApp.AppState.AUTH_IN_PROGRESS);
        try {
            Connection.DEBUG_ENABLED = false;//Enabling this we get Illegal argument exception bcause enhanced debugger uses AWT :(
            config = new ConnectionConfiguration(
                    accnt.getServer(), accnt.getPort(), SERVICE_NAME);
            SmackConfiguration.setPacketReplyTimeout(30000);//This is needed because sometimes VCard loading takes time
            // Add vCard with addIQProvider()
            ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new VCardProvider());
    
            TalkSettings.setProxyProperties();
            if (TalkSettings.PROXY_SERVER != null && !TalkSettings.PROXY_SERVER.trim().equals("") && TalkSettings.proxyChanged.get()) {
                config = new ConnectionConfiguration(
                        accnt.getServer(), accnt.getPort(), SERVICE_NAME,
                        ProxyInfo.forHttpProxy(TalkSettings.PROXY_SERVER, TalkSettings.PROXY_PORT, null, null));
            }
            //For Gtalk we enable SASL by default and don't honor the user settings.
            SASLAuthentication.supportSASLMechanism("PLAIN", 0);//Else conn throws SASL authentication PLAIN failed: invalid-authzid
            //config.setSASLAuthenticationEnabled(true);
            connection = new XMPPConnection(config);
            discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
            final Thread mainThread = Thread.currentThread();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connection.connect();
                        mainThread.interrupt();
                    } catch (XMPPException ex) {
                        Logger.getLogger(GtalkConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
                        FXTalkApp.app.appState.set(FXTalkApp.AppState.NETWORK_ISSUE);
                    }
                }
            }, "GTalk.connect").start();
            Util.gotoSleep(CONNECTION_TIMEOUT_INTERVAL, "Main thread interrupted from sleep, probably connection succeeded.");
            if(!connection.isConnected()) {
                Util.DEBUG("timeout exeeded");
                FXTalkApp.app.appState.set(FXTalkApp.AppState.CONNECTION_TIMED_OUT);
                return false;//No point , just return
            }

            connection.addConnectionListener(this);
////////////Create presence listner, message event request and notifications listeners.////
            presenceTracker = new FXPresenceTracker(this);
////////////Create packet listener(this is low level stuff(raw iq,get,set ,result, presence types can be parsed especially subscibe requests etc)            
            packetTracker = new FXPacketTracker();
            //Before logging in we must register the RosterListener(some how required by Smack API :(
            connection.getRoster().addRosterListener(presenceTracker);
            //Packet listener that will track special packets and requests.
            connection.addPacketListener(packetTracker, null);

            connection.login(accnt.getUserName(), accnt.getPassword());//Google needs @gmail.com trailing to login id.
            //Load the personal VCARD and save avatar as javaFX image
            try {
                personalVCard.load(connection);//This is an asynchronous operation though call returns immediately there is no guarantee you will get what you want so give a pause and goto sleep
            } catch (XMPPException ex) {
                Logger.getLogger(FXPresenceTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
            wireUpListeners();
            FXTalkApp.app.appState.set(FXTalkApp.AppState.AUTHENTICATED);

            return (connection.isAuthenticated());//Check if authenticated
        } catch (XMPPException ex) {
            Logger.getLogger(FXTalkApp.class.getName()).log(Level.WARNING, null, ex);
            if (ex.getXMPPError() != null && ex.getXMPPError().toString().contains("remote-server-timeout")) {
                FXTalkApp.app.appState.set(FXTalkApp.AppState.NETWORK_ISSUE);
            } else {//Assume just auth failed
                FXTalkApp.app.appState.set(FXTalkApp.AppState.INVALID_CREDENTIALS);
            }
            Util.DEBUG("xmpp error " + ex.getXMPPError());
            return false;
        }
    }

    @Override
    public void tearDownConnection() {
        connection.getRoster().removeRosterListener(presenceTracker);
        presenceTracker = null;
        connection.disconnect();
        chatSessions.clear();
        
        FXTalkApp.app.appState.set(FXTalkApp.AppState.NOT_AUTHENTICATED);
        System.err.println("Disconnected succefully.." + !connection.isConnected());
    }
//gid stands for conn id.

    @Override
    public void sendMessage(String gid, String msg) {
        if (msg == null || msg.trim().equals("")) {
            return;
        }
        Chat chat = saveChatSession(gid);
        try {
            chat.sendMessage(msg);
            FXTalkApp.app.chatLogPanel.addItemToChatLog(gid, "You: " + msg);
            Util.DEBUG("You: " + msg);
        } catch (XMPPException ex) {
            Logger.getLogger(GtalkConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Chat saveChatSession(String gid) {
        Chat chat = null;
        if (chatSessions.containsKey(gid)) {
            chat = chatSessions.get(gid);
        } else {
            chat = connection.getChatManager().createChat(gid, new FXMessageTracker(this));
            chatSessions.put(gid, chat);
      
        }
        return chat;
    }

    @Override
    public void wireUpListeners() {
        msgEventReqAndNotifTracker = new FXMsgEventReqAndNotifTracker(this);
        msgEventManager = new MessageEventManager(connection);
        msgEventManager.addMessageEventNotificationListener(msgEventReqAndNotifTracker);
        msgEventManager.addMessageEventRequestListener(msgEventReqAndNotifTracker);
        if (TalkSettings.enableFileTransferService) {
            fileTransferMgr = new FileTransferManager(connection);
            FileTransferNegotiator.setServiceEnabled(connection, TalkSettings.enableFileTransferService);
            fileTransferMgr.addFileTransferListener(new FXFileTransferListener());
        }
    }

}
