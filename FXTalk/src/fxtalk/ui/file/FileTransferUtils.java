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

package fxtalk.ui.file;

import fxtalk.FXTalkApp;
import fxtalk.network.ConnectionHandle;
import fxtalk.utils.AccountsDBStore;
import fxtalk.utils.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * IBB (Inband based byte stream transfer for file transfers.((XEP-0047)
 * @author srikalyc
 */
public class FileTransferUtils {
    static {
        FileTransferNegotiator.IBB_ONLY = true;
    }
    public static void receiveFile(final FileTransferRequest ftr) {
        final String filePath = ftr.getFileName();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final IncomingFileTransfer transfer = ftr.accept();
                    transfer.recieveFile(new File(AccountsDBStore.BASE_LOCATION + File.separator + Util.extractFileName(filePath)));
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            FXTalkApp.app.notifPanel.showFileTransferProgress(null, transfer);
                        }
                    });
                } catch (XMPPException ex) {
                    Logger.getLogger(FileTransferUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    
    /**
     * This is by design of XMPP, see RFC-3921, Section 11.1 Rule 4.3 we cannot
     * send file to id@some.com but id@some.com/resource Ex:
     * sri@gmail.com/Smack05B73129
     *
     * @param fullJid
     * @param fileName
     */
    public static void sendFile(final String fullJid, final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long totalBytesWritten = 0;
                    int BUFFER_LEN = 40480;
                    byte[] fileBytesBuff = null;
                    final OutgoingFileTransfer transfer = FXTalkApp.app.conn.fileTransferMgr.createOutgoingFileTransfer(fullJid);
                    File file = new File(fileName);
                    fileBytesBuff = new byte[BUFFER_LEN];
                    FileInputStream fis = new FileInputStream(file);
                    int bytesRead = 0;
                    final LongProperty bytesWrittenHolder = new SimpleLongProperty(totalBytesWritten);
//                    transfer.sendStream(fis, Util.extractFileName(fileName), file.length(), "");
                    OutputStream stream = transfer.sendFile(file.getName(), file.length(), "");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            FXTalkApp.app.notifPanel.showFileTransferProgress(bytesWrittenHolder, transfer);
                        }
                    });
//////Start sending file
                    while ((bytesRead = fis.read(fileBytesBuff)) != -1) {
                        stream.write(fileBytesBuff, 0, bytesRead);
                        stream.flush();
                        totalBytesWritten += bytesRead;
                        bytesWrittenHolder.set(totalBytesWritten);
                        Util.DEBUG("Transferred " + new DecimalFormat("#.##").format(totalBytesWritten/1024) + "KB so far");
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XMPPException ex) {
                    Logger.getLogger(ConnectionHandle.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

}

