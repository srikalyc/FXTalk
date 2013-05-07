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
import fxtalk.trackers.FXPacketTracker;
import fxtalk.ui.misc.UIutil;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.Util;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.packet.VCard;

/**
 * To send  requests or to receive notifications about message like offline, 
 * delivered, displayed, composing .
 * @author srikalyc
 */
public abstract class ConnectionHandle implements ConnectionListener {
///////////////////////////private feilds //////////////////////////////////////

    private boolean enableSASLAuthentication = true;//By default we enable.
    private FXMessageTracker msgTracker = null;
///////////////////////////protected feilds ////////////////////////////////////
    protected ServiceDiscoveryManager discoManager;
    protected MessageEventManager msgEventManager;
    
    
    protected Collection<RosterEntry> palList;
///////////////////////////public feilds ///////////////////////////////////////
    public FileTransferManager fileTransferMgr;
    public static int CONNECTION_CHECK_INTERVAL = 1000;
    public static int CONNECTION_TIMEOUT_INTERVAL = 15000;
    
    public ConnectionConfiguration config = null;
    public Connection connection;
    public FXPresenceTracker presenceTracker = null;
    public FXPacketTracker packetTracker = null;
    public FXMsgEventReqAndNotifTracker msgEventReqAndNotifTracker = null;

    public Map<String, Chat> chatSessions = new HashMap<String, Chat>();
    public Account accnt;
    public VCard personalVCard = new VCard();
    public String customMessage = "";
    public Set<String> selfOtherClients = new HashSet<String>();//List of yourself as loggein to mobile,other machine etc.[Ex:srikalyan.chandrashekar@gmail.com/gtalk95DEB2BA]This will be loaded from FXPacketTracker
    public Image avatarSelfNonLocal = null;//This is loaded from the VCard you receive about yourself
    public Image avatarSelfLocal = UIutil.getImage("default_avatar.jpg");;//This is created locally by yourself
    
///////////////////////////constructor methods//////////////////////////////////

    public ConnectionHandle(Account info) {
        this.accnt = info;
    }

///////////////////////////abstract methods/////////////////////////////////////    
    public abstract boolean createConnection();

    public abstract void tearDownConnection();

    public abstract void sendMessage(String jid, String msg);//jid is jabber id

    public abstract Chat saveChatSession(String gid);
    
    public abstract void wireUpListeners();

///////////////////////////full methods/////////////////////////////////////////
    public void goOffline() {
        // Create a new presence. Pass in false to indicate we're unavailable.
        Presence presence = new Presence(Presence.Type.unavailable);
        presence.setPriority(128);
        // Send the packet 
        connection.sendPacket(presence);
        //The follwing will also ensure the image changes showing presence changes.(See listener for currentPresence)
        FXTalkApp.app.selfComponent.currentPresence.set(presence);
    }
    public void goInvisible() {
        // Create a new presence. Pass in false to indicate we're unavailable.
        Presence presence = new Presence(Presence.Type.unavailable);
        presence.setProperty("invisible", "true");
        presence.setPriority(128);
        // Send the packet 
        connection.sendPacket(presence);
        //The follwing will also ensure the image changes showing presence changes.(See listener for currentPresence)
        FXTalkApp.app.selfComponent.currentPresence.set(presence);
    }

    public void goOnline() {
        Presence presence = new Presence(Presence.Type.available);
        presence.setPriority(128);
        presence.setMode(Presence.Mode.available);
        // Send the packet 
        connection.sendPacket(presence);
        //The follwing will also ensure the image changes showing presence changes.(See listener for currentPresence)
        FXTalkApp.app.selfComponent.currentPresence.set(presence);
    }

    public void goIdle() {
        // Create a new presence. Pass in false to indicate we're unavailable.
        Presence presence = new Presence(Presence.Type.available);
        presence.setPriority(128);
        presence.setMode(Presence.Mode.away);
        // Send the packet 
        connection.sendPacket(presence);
        //The following will also ensure the image changes showing presence changes.(See listener for currentPresence)
        FXTalkApp.app.selfComponent.currentPresence.set(presence);
    }

    public void goBusy() {
        Presence presence = new Presence(Presence.Type.available);
        presence.setPriority(128);
        presence.setMode(Presence.Mode.dnd);
        // Send the packet 
        connection.sendPacket(presence);
        //The follwing will also ensure the image changes showing presence changes.(See listener for currentPresence)
        FXTalkApp.app.selfComponent.currentPresence.set(presence);
    }
    public void sendFriendRequest(String to) {
        Presence presence = new Presence(Presence.Type.subscribe);
        presence.setTo(to);
        presence.setPriority(128);
        // Send the packet 
        connection.sendPacket(presence);
    }

    public void acceptFriendRequest(String to) {
        Presence presence = new Presence(Presence.Type.subscribed);
        presence.setTo(to);
        presence.setPriority(128);
        // Send the packet 
        connection.sendPacket(presence);
    }

    public void declineFriendRequest(String to) {
        Presence presence = new Presence(Presence.Type.unsubscribe);
        presence.setTo(to);
        presence.setPriority(128);
        // Send the packet 
        connection.sendPacket(presence);
    }

    public void changeStatusMessage(String msg) {
        Iterator<String> it = selfOtherClients.iterator();
        while (it.hasNext()) {
            if (customMessage == null) {

            }
            // Retain the current presence.
            Presence presence = new Presence(FXTalkApp.app.selfComponent.currentPresence.get().getType());
            presence.setFrom(connection.getUser());
            presence.setTo(it.next());
            presence.setPriority(24);
            presence.setMode(FXTalkApp.app.selfComponent.currentPresence.get().getMode());
            presence.setStatus(msg);
            // Send the packet .
            connection.sendPacket(presence);
        }
        Presence presence = new Presence(FXTalkApp.app.selfComponent.currentPresence.get().getType());
        presence.setFrom(connection.getUser());
        presence.setPriority(24);
        presence.setMode(FXTalkApp.app.selfComponent.currentPresence.get().getMode());
        presence.setStatus(msg);
        // Send the packet .
        connection.sendPacket(presence);
    }
/**
 * We attempt 3 times
 */
    public void changeAvatar() {
        new Thread(new Runnable() {//Threaded because we sleep in between and waste some time.
            @Override
            public void run() {
                int attempts = 3;
                int currentAttemptCount = 0;
                if (AccountsDBStore.doesAvatarFileExist()) {
                    String avatarFilePath = AccountsDBStore.BASE_LOCATION + File.separator + AccountsDBStore.avatarFile;
                    boolean doneSaving = false;
                    while (!doneSaving && currentAttemptCount < attempts) {
                        try {
                            personalVCard.setAvatar(VCard.getBytes(new File(avatarFilePath).toURI().toURL()), "image/png");
                            Util.gotoSleep(2000);//Wait for 2 seconds the reason is VCard.load() must have been called just few ms ago and it is asynchronous in nature and hence give some time for VCard to actually have something meaningful.
                            personalVCard.save(connection);
                            doneSaving = true;
                        } catch (IOException ex) {
                            Logger.getLogger(ConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
                            currentAttemptCount++;
                        } catch (XMPPException ex) {
                            Logger.getLogger(ConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
                            currentAttemptCount++;
                        }
                    }
                }
            }
        }, "ConnectionHandle.changeAvatar").start();
    }
    
    public boolean isEnableSASLAuthentication() {
        return enableSASLAuthentication;
    }

    public void setEnableSASLAuthentication(boolean enableSASLAuthentication) {
        this.enableSASLAuthentication = enableSASLAuthentication;
    }

    public FXMessageTracker getMsgTracker() {
        if (msgTracker == null) {
            msgTracker = new FXMessageTracker();
        }
        return msgTracker;
    }

    /**
     * Ensure connection object exists before this method is called.
     *
     * @param activeLoad if true then we get fresh list from collaboration
     * server.
     * @return
     */
    public Collection<RosterEntry> getPalList(boolean activeLoad) {
        if (palList == null || activeLoad) {
            Roster roster = connection.getRoster();
            palList = roster.getEntries();
        }
        return palList;
    }

    @Override
    public void connectionClosed() {
        FXTalkApp.app.appState.set(FXTalkApp.AppState.CONNECTION_CLOSED_BY_SERVER);
    }

    @Override
    public void connectionClosedOnError(Exception excptn) {
        ;
    }

    @Override
    public void reconnectingIn(int i) {
        ;
    }

    @Override
    public void reconnectionSuccessful() {
        ;
    }

    @Override
    public void reconnectionFailed(Exception excptn) {
        ;
    }
    
    
}
