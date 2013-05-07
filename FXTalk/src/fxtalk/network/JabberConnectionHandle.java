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
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;

/**
 * ConnectionHandler specific to Jabber . Enabling SSL by user is honored unlike
 * in Gtalk case where we always enable SSL .
 *
 * @author srikalyc
 */
public class JabberConnectionHandle extends ConnectionHandle {

    private static String SERVICE_NAME = "jabber.com";//This can be replaced by the domain name

    public JabberConnectionHandle(Account accnt) {
        super(accnt);
        int indexOfAtTheRate = accnt.getUserName().indexOf("@");
        SERVICE_NAME = accnt.getUserName().substring(indexOfAtTheRate + 1);
    }

    @Override
    public boolean createConnection() {
        FXTalkApp.app.appState.set(FXTalkApp.AppState.AUTH_IN_PROGRESS);
        try {
            Connection.DEBUG_ENABLED = false;//Enabling this we get Illegal argument exception because enhanced debugger uses AWT :(
            ConnectionConfiguration config = new ConnectionConfiguration(
                    accnt.getServer(), accnt.getPort(), SERVICE_NAME);
            SmackConfiguration.setPacketReplyTimeout(30000);//This is needed because sometimes VCard loading takes time
            TalkSettings.setProxyProperties();
            if (TalkSettings.PROXY_SERVER != null && !TalkSettings.PROXY_SERVER.trim().equals("") && TalkSettings.proxyChanged.get()) {
                if (accnt.isEnableSSL()) {
                    config = new ConnectionConfiguration(
                            accnt.getServer(), accnt.getPort(), SERVICE_NAME,
                            ProxyInfo.forHttpsProxy(TalkSettings.PROXY_SERVER, TalkSettings.PROXY_PORT, null, null));
                    config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
                } else {
                    config = new ConnectionConfiguration(
                            accnt.getServer(), accnt.getPort(), SERVICE_NAME,
                            ProxyInfo.forHttpProxy(TalkSettings.PROXY_SERVER, TalkSettings.PROXY_PORT, null, null));
                }

            } else {//If not proxy still check if SSL is enabled(If yes now we take a different route than with SSL+proxy
                if (accnt.isEnableSSL()) {
                    config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
                    config.setSocketFactory(new MySSLSocketFactory());
                }
            }
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
                        Logger.getLogger(JabberConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
                        FXTalkApp.app.appState.set(FXTalkApp.AppState.NETWORK_ISSUE);
                    }
                }
            }, "Jabber.connect").start();
            Util.gotoSleep(15000, "Main thread interrupted from sleep, probably connection succeeded.");
            if (!connection.isConnected()) {
                Util.DEBUG("timeout exeeded");
                FXTalkApp.app.appState.set(FXTalkApp.AppState.CONNECTION_TIMED_OUT);
                return false;//No point , just return
            }

            connection.addConnectionListener(this);
            //For Gtalk we enable SASL by default and don't honor the user settings.
            SASLAuthentication.supportSASLMechanism("PLAIN", 0);//Else conn throws SASL authentication PLAIN failed: invalid-authzid
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
                personalVCard.load(connection);
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
//jid = jabber id

    @Override
    public void sendMessage(String jid, String msg) {
        if (msg == null || msg.trim().equals("")) {
            return;
        }
        Chat chat = saveChatSession(jid);
        try {
            chat.sendMessage(msg);
            FXTalkApp.app.chatLogPanel.addItemToChatLog(jid, "You: " + msg);
            Util.DEBUG("You: " + msg);
        } catch (XMPPException ex) {
            Logger.getLogger(JabberConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Chat saveChatSession(String jid) {
        Chat chat = null;
        if (chatSessions.containsKey(jid)) {
            chat = chatSessions.get(jid);
        } else {
            chat = connection.getChatManager().createChat(jid, new FXMessageTracker(this));
            chatSessions.put(jid, chat);

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
