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

import org.jivesoftware.smack.packet.Presence;

/**
 * Type and Mode utilities methods.
 * @author srikalyanchandrashekar
 */
public class PresenceUtil {
///////////////////////Presesnce.Type utilities////////////////////////////////

    public static boolean isTypeAvailable(Presence prsnc) {
        return prsnc.getType() == Presence.Type.available;
    }

    public static boolean isTypeUnavailable(Presence prsnc) {
        return prsnc.getType().equals(Presence.Type.unavailable);
    }

    public static boolean isTypeError(Presence prsnc) {
        return prsnc.getType().equals(Presence.Type.error);
    }

    public static boolean isTypeSubscribe(Presence prsnc) {
        return prsnc.getType().equals(Presence.Type.subscribe);
    }

    public static boolean isTypeSubscribed(Presence prsnc) {
        return prsnc.getType().equals(Presence.Type.subscribed);
    }

    public static boolean isTypeUnSubscribe(Presence prsnc) {
        return prsnc.getType().equals(Presence.Type.unsubscribe);
    }

    public static boolean isTypeUnSubscribed(Presence prsnc) {
        return prsnc.getType().equals(Presence.Type.unsubscribed);
    }

    public static boolean isTypeNull(Presence prsnc) {
        return prsnc.getType() == null;
    }
    
///////////////////////Presesnce.Mode utilities//////////////////////////////////
    public static boolean isModeAvailable(Presence prsnc) {
        return prsnc.getMode().equals(Presence.Mode.available);
    }

    public static boolean isModeAway(Presence prsnc) {
        return prsnc.getMode().equals(Presence.Mode.away);
    }

    public static boolean isModeChat(Presence prsnc) {
        return prsnc.getMode().equals(Presence.Mode.chat);
    }

    public static boolean isModeDnd(Presence prsnc) {
        return prsnc.getMode().equals(Presence.Mode.dnd);
    }

    public static boolean isModeXa(Presence prsnc) {
        return prsnc.getMode().equals(Presence.Mode.xa);
    }

    public static boolean isModeNull(Presence prsnc) {
        return prsnc.getMode() == null;
    }
}
