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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Browser related utilities, Ex: Launch in a browser, process links etc.
 * @author srikalyanchandrashekar
 */
public class BrowserUtil {
    private static final String ffBrowserPath1 = "C:/Program Files (x86)/Mozilla Firefox/firefox.exe";
    private static final String ffBrowserPath2 = "C:/Program Files/Mozilla Firefox/firefox.exe";
    private static final String ieBrowserPath1 = "C:/Program Files (x86)/Internet Explorer/iexplore.exe";
    private static final String ieBrowserPath2 = "C:/Program Files/Internet Explorer/iexplore.exe";

    private static final String ffBrowserMacPath = "/Applications/Firefox.app/Contents/MacOS/firefox";
    private static final String chromeBrowserMacPath = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";

    private static final String WIN_FILE_PROTOCOL = "file:///";
    private static final String NIX_FILE_PROTOCOL = "file://";
    
    
    public static boolean isWin() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").contains("Mac OS X");
    }
    public static void runInBrowser(final String code, final String resource) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Process process = null;
                    String browserPath = "";
                    if (isWin()) {
                        String browserPath2 = "";
                        if (code.equals("ff")) {
                            browserPath = ffBrowserPath1;
                            browserPath2 = ffBrowserPath2;
                            Util.DEBUG("resource obj " + resource);
                        } else if (code.equals("ie")) {
                            browserPath = ieBrowserPath1;
                            browserPath2 = ieBrowserPath2;
                            Util.DEBUG("resource obj " + resource);
                        }
                        if (new File(browserPath).exists()) {
                            process = new ProcessBuilder(new String[]{"\"" + browserPath + "\"", "\"" + resource + "\""}).start();
                        }
                        if (process == null && new File(browserPath2).exists()) {
                            process = new ProcessBuilder(new String[]{"\"" + browserPath2 + "\"", "\"" + resource + "\""}).start();
                        }
                    } else if (isMac()) {
                        if (code.equals("ff")) {
                            browserPath = ffBrowserMacPath;
                            Util.DEBUG("resource obj " + resource);
                        } else if (code.equals("cr")) {
                            browserPath = chromeBrowserMacPath;
                            Util.DEBUG("resource obj " + resource);
                        }
                        if (new File(browserPath).exists()) {
                            List<String> list= Arrays.asList(new String[]{"open", "-a", browserPath, resource});
                            Util.DEBUG("command " + list);
                            process = new ProcessBuilder(list).start();
                            //process = Runtime.getRuntime().exec(new String[]{"\"" + browserPath + "\"", "\"" + NIX_FILE_PROTOCOL + resource + "\""});
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BrowserUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, "RunResourceBrowserThread").start();
    }
    public static void runLocalResourceInBrowser(final String code, final String resource) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Process process = null;
                    String browserPath = "";
                    if (isWin()) {
                        String browserPath2 = "";
                        String rsrcModified = new StringBuilder(resource).toString();
                        if (code.equals("ff")) {
                            browserPath = ffBrowserPath1;
                            browserPath2 = ffBrowserPath2;
                            Util.DEBUG("resource obj " + resource);
                        } else if (code.equals("ie")) {
                            browserPath = ieBrowserPath1;
                            browserPath2 = ieBrowserPath2;
                            Util.DEBUG("resource obj " + resource);
                            rsrcModified = resource.replace('/', '\\');//IE doesnt like forward slashes
                            rsrcModified = rsrcModified.replace("\\\\", "\\");//IE doesnt like double backslashes either
                        }
                        if (new File(browserPath).exists()) {
                            process = new ProcessBuilder(new String[]{"\"" + browserPath + "\"", "\"" + WIN_FILE_PROTOCOL + rsrcModified + "\""}).start();
                        }
                        if (process == null && new File(browserPath2).exists()) {
                            process = new ProcessBuilder(new String[]{"\"" + browserPath2 + "\"", "\"" + WIN_FILE_PROTOCOL + rsrcModified + "\""}).start();
                        }
                    } else if (isMac()) {
                        if (code.equals("ff")) {
                            browserPath = ffBrowserMacPath;
                            Util.DEBUG("resource obj " + resource);
                        } else if (code.equals("cr")) {
                            browserPath = chromeBrowserMacPath;
                            Util.DEBUG("resource obj " + resource);
                        }
                        if (new File(browserPath).exists()) {
                            List<String> list= Arrays.asList(new String[]{"open", "-a", browserPath, NIX_FILE_PROTOCOL + resource});
                            Util.DEBUG("command " + list);
                            process = new ProcessBuilder(list).start();
                            //process = Runtime.getRuntime().exec(new String[]{"\"" + browserPath + "\"", "\"" + NIX_FILE_PROTOCOL + resource + "\""});
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BrowserUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, "RunLocalResourceBrowserThread").start();
    }
    /**
     * Ex: If the video url is http://www.youtube.com/watch?v=bQVoAWSP7k4
     * then its corresponding default image video url is
     * http://img.youtube.com/vi/bQVoAWSP7k4/2.jpg
     */
    public static String extractYoutubeDefaultImgUrl(String videoRsrc) {
        if (videoRsrc.startsWith("http://www.youtube.com/watch?v=")) {
            String arr[] = videoRsrc.split("watch\\?v=");
            return "http://img.youtube.com/vi/" + arr[1] + "/2.jpg";
        } 
        return null;
    }
    
    
}
