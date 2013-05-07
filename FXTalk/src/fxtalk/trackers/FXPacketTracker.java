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

import fxtalk.FXTalkApp;
import fxtalk.ui.buddylist.PalEntry;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.PresenceUtil;
import fxtalk.utils.Util;
import java.util.List;
import javafx.application.Platform;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * A very low level tracker that can listen to message,presence, IQ(info/query) packets.
 * This is not needed we shift a little bit higher and use 
 * 1) MessageListener which  operates above the packets and gives us Message, Chat objects etc..
 * 2) RosterListener which   operates above the packets for presence changes etc.
 * @author srikalyc
 */
public class FXPacketTracker implements PacketListener {
    public FXPacketTracker() {
    }
    
    @Override
    public void processPacket(Packet packet) {
        
        if (packet instanceof Presence) {
            final Presence presence = (Presence)packet;
            if (PresenceUtil.isTypeSubscribe(presence)) {//Incoming subscribe request
                Util.DEBUG("Subscription request received from " + presence.getFrom());
                String myself = FXTalkApp.app.conn.accnt.getUserName();
                final String trimmedUid = Util.getJidStripOffTrailingDetails(presence.getFrom());
                if (FXTalkApp.app.conn.presenceTracker.palEntries.containsKey(trimmedUid)) {
                    //No point this user is already in your friends list
                    Util.DEBUG(trimmedUid + " is already in your Roster, but trying to send you a friend invite!");
                } else {
                    AccountsDBStore.loadKnownReceivedFriendReqs(myself);
                    List<String> listOfIncomingRequs = AccountsDBStore.rxedFriendReqs.get(myself);
                    if (listOfIncomingRequs != null && listOfIncomingRequs.size() > 0 && listOfIncomingRequs.contains(trimmedUid)) {
                        //This user already requested action and is pending approval
                    } else {
                        AccountsDBStore.saveRxedFriendReqs(myself, trimmedUid);
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Util.dingSound();
                            FXTalkApp.app.notifPanel.showFriendReqNotificationAlert(trimmedUid, trimmedUid + " wants to be your friend,ok?");
                            PalEntry palEntry = new PalEntry();
                            palEntry.nameLabel.setText(Util.getShortNameFromJid(trimmedUid));
                            palEntry.disablePalEntry("Pending approval.");
                            FXTalkApp.app.palListPanel.palListItems.add(palEntry);
                            Util.DEBUG("pallist items are " + FXTalkApp.app.palListPanel.palListItems);
                            FXTalkApp.app.conn.presenceTracker.palEntries.put(Util.getJidStripOffTrailingDetails(presence.getFrom()), palEntry);
                            Util.DEBUG("Presence created : user=" + presence.getFrom() + ",status=" + presence.getStatus() + ",type=" + presence.getType() + ",mode=" + presence.getMode());
                            FXTalkApp.app.palListPanel.sort();
                        }
                    });
                }
                return;
            }             
            
            //You may get presence from yourself(if you are logged from another machine/mobile etc)
            if (presence.getFrom().startsWith(FXTalkApp.app.conn.accnt.getUserName())) {//Use startsWith() because getFrom() returns full username Ex: user@gmail.com/Smack23dfg456
                if (presence.isAvailable()) {
                    FXTalkApp.app.conn.selfOtherClients.add(presence.getFrom());//Set(not list), hence no check needed just add.
                    Util.DEBUG("You were added  " + presence.getFrom());
                } else if (presence.isAway()) {
                    FXTalkApp.app.conn.selfOtherClients.remove(presence.getFrom());
                    Util.DEBUG("You were removed " + presence.getFrom());
                }
            }
        }
    }
    
}
