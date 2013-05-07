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

import fxtalk.ui.misc.UIutil;
import static fxtalk.utils.AccountsDBStore.BASE_LOCATION;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Boiler plate
 *
 * @author srikalyanchandrashekar
 */
public class Util {

    private static final Media dingAudioFile = new Media(UIutil.getRsrcURI("ding.mp3").toString());
    private static final Media clickAudioFile = new Media(UIutil.getRsrcURI("click.mp3").toString());
    private static MediaPlayer ding = new MediaPlayer(dingAudioFile);
    private static MediaPlayer click = new MediaPlayer(clickAudioFile);
    public static boolean debug = true;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static PrintStream ps;
    private static final File logFolder = new File(BASE_LOCATION + File.separator + "logs");
    private static final File logFile = new File(BASE_LOCATION + File.separator + "logs" + File.separator + dateFormatter.format(new Date()) + ".log");

    static {
        try {
            if (!logFolder.exists()) {
                logFolder.mkdir();
            }
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            ps = new PrintStream(logFile);
            System.setOut(ps);
            System.setErr(ps);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String getShortNameFromJid(String jid) {
        if (jid.indexOf("@") >= 0) {
            return jid.substring(0, jid.indexOf("@"));
        }
        return jid;
    }
    //For Ex: jid@gmail.com/gmail.3D01B6F3 will become jid@gmail.com

    public static String getJidStripOffTrailingDetails(String from) {
        if (from.indexOf("/") >= 0) {
            return from.substring(0, from.indexOf("/"));
        }
        return from;
    }

    public static boolean isNumber(String str) {
        try {
            Integer.valueOf(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static void gotoSleep(long ms) {
        gotoSleep(ms, null);
    }

    public static void gotoSleep(long ms, String msg) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            if (msg != null) {
                Util.DEBUG("Main thread interrupted from sleep, probably connection succeeded.");
            } else {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void DEBUG(String msg) {
        if (debug) {
            System.out.println("DEBUG: " + msg);
        }
    }

    public static void copyToClipBoard(String item) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(item);
        clipboard.setContent(content);
    }

    public static String extractFileName(String fullPath) {
        String sep1 = "/";
        String sep2 = "\\";
        if (fullPath.lastIndexOf(sep1) >= 0) {
            return fullPath.substring(fullPath.lastIndexOf(sep1) + 1);
        } else if (fullPath.lastIndexOf(sep2) >= 0) {
            return fullPath.substring(fullPath.lastIndexOf(sep2) + 1);
        }
        return fullPath;
    }

    /**
     * Close the log file, save the chat to DB store etc
     */
    public static void cleanup() {
        if (ps != null) {
            ps.flush();
            ps.close();
        }
        AccountsDBStore.saveChatLog();
    }

    public static void dingSound() {
        ding.seek(Duration.ZERO);
        ding.play();
    }

    public static void clickSound() {
        click.seek(Duration.ZERO);
        click.play();
    }
}
