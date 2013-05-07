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
import fxtalk.ui.buddylist.PalEntry;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.PresenceUtil;
import fxtalk.utils.Util;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

/**
 * Track buddie's presence changes.
 *
 * @author srikalyanchandrashekar
 */
public class FXPresenceTracker implements RosterListener {

    public ConnectionHandle handle;
    public Map<String, PalEntry> palEntries = new HashMap<String, PalEntry>();

    public FXPresenceTracker() {
        //When someone invites you to become pal you can manually accept/reject.
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
    }

    public FXPresenceTracker(ConnectionHandle handle) {
        this();
        this.handle = handle;
    }

    @Override
    public void entriesAdded(Collection<String> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void entriesUpdated(Collection<String> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void entriesDeleted(Collection<String> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * You wont get Subscribe request to this method because this method is
     * invoked only when presence of a user in the roster changes. If you want
     * to track subscribe request FXPacketTracker(which implements
     * PacketListener)
     *
     * @param prsnc
     */
    @Override
    public void presenceChanged(final Presence prsnc) {
        if (prsnc == null) {
            return;
        }
        if (!FXTalkApp.app.conn.connection.isConnected()) {//The user/network might have disconnected.
            //Nothing to do just return.
            return;
        }
        String myself = FXTalkApp.app.conn.accnt.getUserName();
        final String trimmedUid = Util.getJidStripOffTrailingDetails(prsnc.getFrom());
        if (PresenceUtil.isTypeSubscribed(prsnc)) {//Incoming subscribed request
            //There is no way to check if you sent out subscribe first(It is the job if the XMPP server to see if this was preceded by subscribe, but if you sent this then just delete any saved stuff
            AccountsDBStore.loadKnownSentFriendReqs(myself);
            if (palEntries.containsKey(trimmedUid)) {
                final PalEntry palEntry = palEntries.get(trimmedUid);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        palEntry.enablePalEntry();
                    }
                });
            }
            AccountsDBStore.deleteSentFriendReqs(myself, trimmedUid);
            return;
        } else {//If you initiated friend request but in some other client where you logged in you accepted the request, this client will still have the sent request saved so delete if any 
            if (palEntries.containsKey(trimmedUid)) {
                final PalEntry palEntry = palEntries.get(trimmedUid);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        palEntry.enablePalEntry();
                    }
                });
            }
            AccountsDBStore.deleteSentFriendReqs(myself, trimmedUid);
        }

        final VCard card = new VCard();
        if (!palEntries.containsKey(trimmedUid)) {//We don't load VCard again and again(once saved assume nothing changes for the session, when you login next time you anyways load again)
            try {
                card.load(FXTalkApp.app.conn.connection, trimmedUid);//Load the vcard here and get image later(just give some time because load() is an asynchronous operation)
            } catch (XMPPException ex) {
                Logger.getLogger(FXPresenceTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FXTalkApp.app.conn.saveChatSession(trimmedUid);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PalEntry palEntry = null;
                //For Ex: jid@gmail.com/gmail.3D01B6F3 will become jid@gmail.com and used as key.
                if (palEntries.containsKey(trimmedUid)) {
                    palEntry = palEntries.get(trimmedUid);
                    palEntry.setPalPresence(prsnc);//This will change stuff
                    Util.DEBUG("Presence changed : user=" + prsnc.getFrom() + ",status=" + prsnc.getStatus() + ",type=" + prsnc.getType() + ",mode=" + prsnc.getMode());

                } else {
                    palEntry = new PalEntry(prsnc);
                    FXTalkApp.app.palListPanel.palListItems.add(palEntry);
                    Util.DEBUG("pallist items are " + FXTalkApp.app.palListPanel.palListItems);
                    palEntries.put(Util.getJidStripOffTrailingDetails(prsnc.getFrom()), palEntry);
                    Util.DEBUG("Presence created : user=" + prsnc.getFrom() + ",status=" + prsnc.getStatus() + ",type=" + prsnc.getType() + ",mode=" + prsnc.getMode());
                    byte[] imgBytes = card.getAvatar();//TODO: We save avatar in the first presence itself(If avatar changes track the presence packets with hash and see the difference in hash)
                    if (imgBytes != null) {//If image bytes are null anyways the palEntry's avatar is initiated to default Avatar
                        Image img = new Image(new ByteArrayInputStream(imgBytes));
                        palEntry.setAvatar(img);
                    }
                }
                FXTalkApp.app.palListPanel.sort();
                //palEntry.statusTooltip.setText(prsnc.getStatus());
                if (FXTalkApp.app.startShowingPresenceNotifics) {//When the app loads initially you dont want to bombard with 100 notifications
                    String displayName = Util.getJidStripOffTrailingDetails(prsnc.getFrom());
                    String availabilityMessage = null;
                    if (PresenceUtil.isTypeAvailable(prsnc)) {//Then user is signed into the server
                        if (PresenceUtil.isModeNull(prsnc) || PresenceUtil.isModeAvailable(prsnc) || PresenceUtil.isModeChat(prsnc)) {
                            availabilityMessage = "is available for chat...";
                        } else if (PresenceUtil.isModeAway(prsnc) || PresenceUtil.isModeXa(prsnc)) {
                            availabilityMessage = null;
                        } else if (PresenceUtil.isModeDnd(prsnc)) {
                            availabilityMessage = "is busy...";
                        }
                    } else {//User is not signed into the server.
                        availabilityMessage = "signed off...";
                    }
                    if (availabilityMessage != null) {
                        FXTalkApp.app.notifPanel.showNotification(displayName, availabilityMessage);
                    }
                }
            }
        });

    }
}
