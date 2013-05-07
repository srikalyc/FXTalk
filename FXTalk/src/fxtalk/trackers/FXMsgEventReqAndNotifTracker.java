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
import fxtalk.utils.Util;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.MessageEventNotificationListener;
import org.jivesoftware.smackx.MessageEventRequestListener;

/**
 *
 * @author srikalyanchandrashekar
 */
public class FXMsgEventReqAndNotifTracker implements MessageEventNotificationListener, MessageEventRequestListener {
    public ConnectionHandle handle;

    public FXMsgEventReqAndNotifTracker(ConnectionHandle handle) {
        this.handle = handle;
    }

///////////////////All message notification  methods///////////////////////////
    
    @Override
    public void deliveredNotification(String from, String packetId) {
    }

    @Override
    public void displayedNotification(String from, String packetId) {
    }

    @Override
    public void composingNotification(String from, String packetId) {
        Util.DEBUG(from + " is composing " + packetId);
    }

    @Override
    public void offlineNotification(String from, String packetId) {
        Util.DEBUG(from + " is offline");
    }

    @Override
    public void cancelledNotification(String from, String packetId) {
    }
///////////////////All message  request methods////////////////////////////////
    @Override
    public void deliveredNotificationRequested(String string, String string1, MessageEventManager mem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void displayedNotificationRequested(String string, String string1, MessageEventManager mem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void composingNotificationRequested(String string, String string1, MessageEventManager mem) {
        Util.DEBUG("composing notification ");
    }

    @Override
    public void offlineNotificationRequested(String string, String string1, MessageEventManager mem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
