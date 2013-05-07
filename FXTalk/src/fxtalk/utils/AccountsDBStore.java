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

import fxtalk.beans.Account;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jivesoftware.smack.packet.Presence;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * If an account is to be saved and it already exists then still it is saved but
 * to the top of the list .
 *
 * @author srikalyc
 */
public class AccountsDBStore {
///////////////Hidden folder and all file within also are hidden////////////////

    public static final String BASE_LOCATION = System.getProperty("user.home") + File.separator + ".fxtalk";
    public static String accntsFile = ".fxtalk_accnts";
    public static String defaultAccntFile = ".fxtalk_default_accnt";
    public static String presenceFile = ".fxtalk_presence";//This is not actually file name but only partial(The name ends with username)
    public static String statusFile = ".fxtalk_status";//This is not actually file name but only partial(The name ends with username)
    public static String friendReqSentFile = ".fxtalk_friend_req_sent";//This is not actually file name but only partial(The name ends with username)
    public static String friendReqRxedFile = ".fxtalk_friend_req_rxed";//This is not actually file name but only partial(The name ends with username)
    public static String proxyFile = ".fxtalk_proxy";
    public static String notifFile = ".fxtalk_notif";
    public static String idleTimeFile = ".fxtalk_idleTime";
    public static String chatLogCountFile = ".fxtalk_chat_log_count";//most recent X number of chat log items to be saved
    public static String chatLogFile = ".fxtalk_chat_log";//This is not actually file name but only partial(The name ends with username)
    public static String srKeyFile = ".fxtalk_srKey";//Secure random key(make sure this file is read only by owner and nothing else for others and group)
    public static String avatarFile = ".fxtalk_avatar.png";
    public static AccountList<Account> savedAccnts = new AccountList<Account>();
    public static Map<String, Presence> savedPresence = new HashMap<String, Presence>();//user,presence Ex: xyz@gmail.com,available
    public static Map<String, String> savedStatus = new HashMap<String, String>();//user,status Ex: xyz@gmail.com,nothing here
    public static List<String> savedProxies = new ArrayList<String>();
    public static Map<String, Account> userNameAccntMap = new HashMap<String, Account>();
    public static Account defaultAccount = null;
    public static boolean enableNotific = true;//Still if there is a friend request will will show it. Any other request will not be shown
    public static AtomicLong idleInMins = new AtomicLong(5);//minutes
    public static AtomicInteger chatLogCount = new AtomicInteger(5);//This number of recent chat items will be saved
    public static Map<String, List<String>> rxedFriendReqs = new HashMap<String, List<String>>();//(user, list of rxved Friend reqs)
    public static Map<String, List<String>> sentFriendReqs = new HashMap<String, List<String>>();//(user, list of sent friend reqs)
    public static Map<String, List<String>> userToChatLogMap = new HashMap<String, List<String>>();//(buddyName,ChatLog)buddy can be xyz@gmail.com , abc@xyz.com etc (i.e all buddies regardless of the domain all go here),There is one instance called palCacheMap in AccordianChatLogPanel as well but its value is TitlePane
    public static byte[] keyBytes = null;//This is a random key generated only once when FXTalk runs the first time.

    static {
        if (!new File(BASE_LOCATION).exists()) {
            new File(BASE_LOCATION).mkdir();
        }
        generateSRKey();
        load();
    }

    public static boolean doesAvatarFileExist() {
        File f = new File(BASE_LOCATION + File.separator + avatarFile);
        if (f.exists() && f.isFile()) {
            return true;
        }
        return false;
    }

    /**
     * Saves Proxy information to the file to the file. If there is already an
     * entry
     *
     * @param newProxyHostPort host:port
     */
    public static void saveProxy(String newProxyHostPort) {
        loadKnownProxiesList();
        File proxyFileObj = new File(BASE_LOCATION + File.separator + proxyFile);
        List<String> newProxiesList = new ArrayList<String>();
        newProxiesList.add(newProxyHostPort);
        for (int i = 0; i < savedProxies.size(); i++) {
            if (savedProxies.get(i).toString().equals(newProxyHostPort)) {
                //Already added as newProxyHostPort, so nothing here
            } else {
                newProxiesList.add(savedProxies.get(i));
            }
        }
        writeListToFile(proxyFileObj, newProxiesList);
        loadKnownProxiesList();
    }

    /**
     * Deletes Proxy file
     */
    public static void deleteAllProxies() {
        File proxyFileObj = new File(BASE_LOCATION + File.separator + proxyFile);
        proxyFileObj.delete();
        loadKnownProxiesList();
    }

    /**
     * Saves Enable/Disable notification information to the file. If there is
     * already an entry it is overriden.
     *
     * @param notific
     */
    public static void saveNotific(String notific) {
        File notificFileObj = new File(BASE_LOCATION + File.separator + notifFile);
        List<String> notificList = new ArrayList<String>();
        notificList.add(notific);
        writeListToFile(notificFileObj, notificList);
        loadKnownNotificSettings();
    }

    /**
     * Saves idle time in minutes to the file. If there is already an entry it
     * is overriden.
     *
     * @param idleTime
     */
    public static void saveChatIdleTime(String idleTime) {
        File idleTimeFileObj = new File(BASE_LOCATION + File.separator + idleTimeFile);
        List<String> idleTimeList = new ArrayList<String>();
        idleTimeList.add(idleTime);
        writeListToFile(idleTimeFileObj, idleTimeList);
        loadKnownChatIdleTimeSettings();
    }

    /**
     * Saves idle time in minutes to the file. If there is already an entry it
     * is overriden.
     *
     * @param chatLogCount
     */
    public static void saveChatLogCount(String chatLogCount) {
        File chatLogCountFileObj = new File(BASE_LOCATION + File.separator + chatLogCountFile);
        List<String> chatLogList = new ArrayList<String>();
        chatLogList.add(chatLogCount);
        writeListToFile(chatLogCountFileObj, chatLogList);
        loadKnownChatLogCount();
    }

    /**
     * Save presence information in following format. type,mode
     *
     * @param user
     * @param presence
     */
    public static void savePresence(String user, Presence presence) {
        File presenceFileObj = new File(BASE_LOCATION + File.separator + presenceFile + user);
        List<String> presenceList = new ArrayList<String>();
        presenceList.add(presence.getType().name() + "," + ((presence.getMode() == null) ? Presence.Mode.available.name() : presence.getMode().name()));
        writeListToFile(presenceFileObj, presenceList);
        loadKnownPresence(user);
    }

    /**
     * Save status
     *
     * @param user
     * @param status
     */
    public static void saveStatus(String user, String status) {
        File statusFileObj = new File(BASE_LOCATION + File.separator + statusFile + user);
        List<String> statusList = new ArrayList<String>();
        statusList.add(status);
        writeListToFile(statusFileObj, statusList);
        loadKnownStatus(user);
    }

    /**
     * Remove account and if it default then delete that too.
     *
     * @param accntToBeRemoved
     */
    public static void deleteAccount(Account accntToBeRemoved) {
        load();//Just in case
        if (accntToBeRemoved.toString().equals(defaultAccount.toString())) {
            defaultAccount = null;
            File defaultAccntsFileObj = new File(BASE_LOCATION + File.separator + defaultAccntFile);
            if (defaultAccntsFileObj.exists()) {
                defaultAccntsFileObj.delete();
            }
        }
        Util.DEBUG("Account being removed :" + accntToBeRemoved);
        Util.DEBUG("Before removal :" + savedAccnts);
        savedAccnts.remove(accntToBeRemoved);
        userNameAccntMap.remove(accntToBeRemoved.getUserName());
        Util.DEBUG("After removal :" + savedAccnts);
        File accntsFileObj = new File(BASE_LOCATION + File.separator + accntsFile);
        accntsFileObj.delete();//This is because we are going to reconsctruct the file again.
        writeListToFile(accntsFileObj, savedAccnts);
    }

    /**
     * Save default account.
     *
     * @param accntToBeSaved
     */
    public static void saveDefaultAccount(Account accntToBeSaved) {
        defaultAccount = accntToBeSaved;
        File defaultAccntsFileObj = new File(BASE_LOCATION + File.separator + defaultAccntFile);
        if (defaultAccntsFileObj.exists()) {
            defaultAccntsFileObj.delete();
        }
        List<Account> def = new ArrayList<Account>();
        def.add(accntToBeSaved);
        writeListToFile(defaultAccntsFileObj, def);
    }

    /**
     * Saves Account to the file to the file. If there is already an entry with
     * same username and server then it is overrwritten. See if there is default
     * account already if no then saveAccount this as default account which
     * makes sense because the user wants to use the most recently logged in
     * account as default (OR) just use existing saved default.
     */
    public static void saveAccount(Account accntToBeSaved) {
        load();
        if (defaultAccount == null) {
            saveDefaultAccount(accntToBeSaved);
        }

        File accntsFileObj = new File(BASE_LOCATION + File.separator + accntsFile);
        List<Account> newAccntsToBeSaved = new ArrayList<Account>();
        newAccntsToBeSaved.add(accntToBeSaved);
        for (int i = 0; i < savedAccnts.size(); i++) {
            Account tmpAccnt = Account.toAccount(savedAccnts.get(i).toString());
            if (tmpAccnt != null) {
                if (tmpAccnt.getServer().equals(accntToBeSaved.getServer()) && tmpAccnt.getUserName().equals(accntToBeSaved.getUserName())) {
                    //This tmpAccnt will not be saved instead we will be saving accntToBeSaved
                } else {
                    newAccntsToBeSaved.add(tmpAccnt);
                }
            }
        }
        writeListToFile(accntsFileObj, newAccntsToBeSaved);
    }

    /**
     *
     * @param myself
     * @param toUser
     */
    public static void saveSentFriendReqs(String myself, String toUser) {
        loadKnownSentFriendReqs(myself);
        File sentFriendReqFileObj = new File(BASE_LOCATION + File.separator + friendReqSentFile + myself);
        if (sentFriendReqFileObj.exists()) {
            sentFriendReqFileObj.delete();
        }
        List<String> reqs = sentFriendReqs.remove(myself);
        if (reqs == null) {
            reqs = new ArrayList<String>();
            reqs.add(toUser);
        } else if (!reqs.contains(toUser)) {
            reqs.add(toUser);
        }
        sentFriendReqs.put(myself, reqs);
        writeListToFile(sentFriendReqFileObj, reqs);
    }

    /**
     *
     * @param myself
     * @param toUser
     */
    public static void deleteSentFriendReqs(String myself, String toUser) {
        loadKnownSentFriendReqs(myself);
        File sentFriendReqFileObj = new File(BASE_LOCATION + File.separator + friendReqSentFile + myself);
        if (sentFriendReqFileObj.exists()) {
            sentFriendReqFileObj.delete();
        }
        List<String> reqs = sentFriendReqs.remove(myself);
        if (reqs == null) {
            reqs = new ArrayList<String>();
        } else if (reqs.contains(toUser)) {
            reqs.remove(toUser);
        }
        sentFriendReqs.put(myself, reqs);
        writeListToFile(sentFriendReqFileObj, reqs);
    }

    /**
     *
     * @param myself
     * @param fromUser
     */
    public static void saveRxedFriendReqs(String myself, String fromUser) {
        loadKnownReceivedFriendReqs(myself);
        File rxvedFriendReqFileObj = new File(BASE_LOCATION + File.separator + friendReqRxedFile + myself);
        if (rxvedFriendReqFileObj.exists()) {
            rxvedFriendReqFileObj.delete();
        }
        List<String> reqs = rxedFriendReqs.remove(myself);
        if (reqs == null) {
            reqs = new ArrayList<String>();
            reqs.add(fromUser);
        } else if (!reqs.contains(fromUser)) {
            reqs.add(fromUser);
        }
        rxedFriendReqs.put(myself, reqs);
        writeListToFile(rxvedFriendReqFileObj, reqs);
    }

    /**
     *
     * @param myself
     * @param fromUser
     */
    public static void deleteRxedFriendReqs(String myself, String fromUser) {
        loadKnownReceivedFriendReqs(myself);
        File rxvedFriendReqFileObj = new File(BASE_LOCATION + File.separator + friendReqRxedFile + myself);
        if (rxvedFriendReqFileObj.exists()) {
            rxvedFriendReqFileObj.delete();
        }
        List<String> reqs = rxedFriendReqs.remove(myself);
        if (reqs == null) {
            reqs = new ArrayList<String>();
        } else if (reqs.contains(fromUser)) {
            reqs.remove(fromUser);
        }
        rxedFriendReqs.put(myself, reqs);
        writeListToFile(rxvedFriendReqFileObj, reqs);
    }

    /**
     * Load accounts , proxies, notification settings, chat idle time etc if
     * known
     */
    public static void load() {
        loadKnownAccountsList();
        loadKnownProxiesList();
        loadKnownNotificSettings();
        loadKnownChatIdleTimeSettings();
        loadKnownChatLogCount();
    }

    /**
     * If key exists read off the file else generate a new one which will be
     * used to encrypt and store passwords.
     */
    private static void generateSRKey() {
        File srKeyFileObj = new File(BASE_LOCATION + File.separator + srKeyFile);
        if (srKeyFileObj.exists()) {//Read off the file
            List<String> srKeyList = getFileAsListOfStrings(srKeyFileObj);
            if (srKeyList.size() > 0) {
                try {
                    keyBytes = new BASE64Decoder().decodeBuffer(srKeyList.get(0));
                } catch (IOException ex) {
                    Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return;
        }
        SecureRandom sr = new SecureRandom();
        keyBytes = new byte[16];//128 bit key
        sr.nextBytes(keyBytes);
        List<String> srKeyList = new ArrayList<String>();
        BASE64Encoder enc = new BASE64Encoder();
        srKeyList.add(enc.encode(keyBytes));
        writeListToFile(srKeyFileObj, srKeyList);
        srKeyFileObj.setWritable(false);
        srKeyFileObj.setReadable(false);
        srKeyFileObj.setReadable(true, true);
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("r--------");
        try {
            try {
                Files.setPosixFilePermissions(srKeyFileObj.toPath(), perms);
            } catch (UnsupportedOperationException uso) {//Mostly because it is windows OS
                Files.setAttribute(srKeyFileObj.toPath(), "dos:hidden", true);
                Files.setAttribute(srKeyFileObj.toPath(), "dos:readonly", true);
                Files.setAttribute(srKeyFileObj.toPath(), "dos:system", true);
            }
        } catch (IOException ex) {
            Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Load from file into the list of accounts
     */
    public static void loadKnownAccountsList() {
        savedAccnts.clear();
        File accntsFileObj = new File(BASE_LOCATION + File.separator + accntsFile);
        if (accntsFileObj != null && accntsFileObj.exists() && !accntsFileObj.isDirectory()) {
            List<String> res = getFileAsListOfStrings(accntsFileObj);
            for (int i = 0; i < res.size(); i++) {
                Account accnt = Account.toAccount(res.get(i));
                if (accnt != null) {
                    savedAccnts.add(accnt);
                }
            }
            for (int i = 0; i < AccountsDBStore.savedAccnts.size(); i++) {
                Account accnt = AccountsDBStore.savedAccnts.get(i);
                userNameAccntMap.put(accnt.getUserName(), accnt);
            }
        }
        ////////////Load default account if exists
        File defaultAccntsFileObj = new File(BASE_LOCATION + File.separator + defaultAccntFile);
        if (!defaultAccntsFileObj.exists()) {
            return;
        }
        List<String> res = getFileAsListOfStrings(defaultAccntsFileObj);
        if (res.size() > 0) {
            Account accnt = Account.toAccount(res.get(0));
            if (accnt != null) {
                defaultAccount = accnt;
            }
        }
    }

    /**
     * Load known proxies if any from the file.
     */
    public static void loadKnownProxiesList() {
        File proxiesFileObj = new File(BASE_LOCATION + File.separator + proxyFile);
        if (proxiesFileObj != null && proxiesFileObj.exists() && !proxiesFileObj.isDirectory()) {
            savedProxies = getFileAsListOfStrings(proxiesFileObj);
        } else {
            savedProxies = new ArrayList<String>();
        }
    }

    /**
     * Load known notification settings if any from the file.
     */
    public static void loadKnownNotificSettings() {
        File notifFileObj = new File(BASE_LOCATION + File.separator + notifFile);
        if (notifFileObj != null && notifFileObj.exists() && !notifFileObj.isDirectory()) {
            List<String> res = getFileAsListOfStrings(notifFileObj);
            if (res.size() > 0) {
                enableNotific = Boolean.parseBoolean(res.get(0));
            }
        } else {//Save default if the file doesnt exist
            saveNotific("true");
        }
    }

    /**
     * Load known chat idle time settings if any from the file.
     */
    private static void loadKnownChatIdleTimeSettings() {
        File idleTimeFileObj = new File(BASE_LOCATION + File.separator + idleTimeFile);
        if (idleTimeFileObj.exists() && !idleTimeFileObj.isDirectory()) {
            List<String> res = getFileAsListOfStrings(idleTimeFileObj);
            if (res.size() > 0) {
                idleInMins.set((long) (Double.parseDouble(res.get(0))));
            }
        } else {//Save default if the file doesnt exist
            saveChatIdleTime("5");
        }
    }

    /**
     * save chat log to file
     */
    public static void saveChatLog() {
        Iterator<String> iter = userToChatLogMap.keySet().iterator();
        while (iter.hasNext()) {
            String buddy = iter.next();
            File chatLogFileObj = new File(BASE_LOCATION + File.separator + chatLogFile + buddy);
            List<String> logs = userToChatLogMap.get(buddy);
            if (logs.size() < chatLogCount.get()) {
                writeListToFile(chatLogFileObj, logs);
            } else {
                writeListToFile(chatLogFileObj, logs.subList(logs.size() - chatLogCount.get(), logs.size()));
            }
        }
    }

    /**
     * Load number of chat items to be saved.
     */
    private static void loadKnownChatLogCount() {
        File chatItemsCountFileObj = new File(BASE_LOCATION + File.separator + chatLogCountFile);
        if (chatItemsCountFileObj.exists() && !chatItemsCountFileObj.isDirectory()) {
            List<String> res = getFileAsListOfStrings(chatItemsCountFileObj);
            if (res.size() > 0) {
                chatLogCount.set((int) (Double.parseDouble(res.get(0))));
            }
        } else {//Save default if the file doesnt exist
            saveChatLogCount("5");
        }
    }

    /**
     * Load the saved chat Log..
     *
     * @param buddy
     */
    public static void loadKnownChatLog(String buddy) {
        File chatItemsFileObj = new File(BASE_LOCATION + File.separator + chatLogFile + buddy);
        if (chatItemsFileObj.exists() && !chatItemsFileObj.isDirectory()) {
            List<String> res = getFileAsListOfStrings(chatItemsFileObj);
            if (res.size() > 0) {
                userToChatLogMap.put(buddy, res);
            }
        }
    }

    /**
     * Load known friend requests if any, non empty result means that you hav
     * not yet accepted/rejected the request.
     *
     * @param user(yourself)
     */
    public static void loadKnownReceivedFriendReqs(String user) {
        File rxvedFriendReqFileObj = new File(BASE_LOCATION + File.separator + friendReqRxedFile + user);
        if (rxvedFriendReqFileObj.exists() && !rxvedFriendReqFileObj.isDirectory()) {
            rxedFriendReqs.remove(user);
            rxedFriendReqs.put(user, getFileAsListOfStrings(rxvedFriendReqFileObj));
        }
    }

    /**
     * Load known friend requests if any, non empty result means that other user
     * has not yet accepted/rejected your request.
     *
     * @param user(yourself)
     */
    public static void loadKnownSentFriendReqs(String user) {
        File sentFriendReqFileObj = new File(BASE_LOCATION + File.separator + friendReqSentFile + user);
        if (sentFriendReqFileObj.exists() && !sentFriendReqFileObj.isDirectory()) {
            sentFriendReqs.remove(user);
            sentFriendReqs.put(user, getFileAsListOfStrings(sentFriendReqFileObj));
        }
    }

    /**
     * Load known presence information settings if any from the file.
     */
    public static void loadKnownPresence(String user) {
        File presenceFileObj = new File(BASE_LOCATION + File.separator + presenceFile + user);
        if (presenceFileObj.exists() && !presenceFileObj.isDirectory()) {
            List<String> res = getFileAsListOfStrings(presenceFileObj);
            if (res.size() > 0) {
                String[] items = res.get(0).split(",");
                Presence pres = new Presence(Presence.Type.valueOf(items[0]));
                pres.setMode(Presence.Mode.valueOf(items[1]));
                if (pres.getType() == Presence.Type.unavailable) {//Its unavailable and is still being saved then it means user want to be invisible
                    pres.setProperty("invisible", "true");
                }
                savedPresence.remove(user);
                savedPresence.put(user, pres);
            }
        } else {//Save default if the file doesnt exist
            Presence pres = new Presence(Presence.Type.available);
            pres.setMode(Presence.Mode.available);
            savePresence(user, pres);
        }
    }

    /**
     * Load known status information settings if any from the file.
     */
    public static void loadKnownStatus(String user) {
        File statusFileObj = new File(BASE_LOCATION + File.separator + statusFile + user);
        if (statusFileObj.exists() && !statusFileObj.isDirectory()) {
            List<String> res = getFileAsListOfStrings(statusFileObj);
            if (res.size() > 0) {
                savedStatus.remove(user);
                savedStatus.put(user, res.get(0));
            }
        } else {
            //Dont save any defaults because we want the use to set the status explicitly
        }
    }

    /**
     * Log the chat .
     *
     * @param user
     * @param item
     */
    public static void logChat(String user, String item) {
        if (item == null || item.trim().equals("")) {
            return;
        }
        item = item.replaceAll("\\n", "");
        if (userToChatLogMap.containsKey(user)) {
            List<String> chats = userToChatLogMap.get(user);
            chats.add(item);
        } else {
            List<String> chats = new ArrayList<String>();
            chats.add(item);
            userToChatLogMap.put(user, chats);
        }
    }

    /**
     * Just write each item in list to file .
     *
     * @param <T> can be anything
     * @param file
     * @param list
     */
    private static <T> void writeListToFile(File file, List<T> list) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (int i = 0; i < list.size(); i++) {
                bw.append(list.get(i).toString());
                bw.newLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Returns lines of file as list of string objects
     *
     * @param file
     * @return
     */
    private static List<String> getFileAsListOfStrings(File file) {
        List<String> res = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;

            while ((line = br.readLine()) != null) {
                res.add(line);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(AccountsDBStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return res;

    }

    /**
     * List of accounts.
     *
     * @param <T>
     */
    static class AccountList<T> extends ArrayList<T> {

        @Override
        public boolean contains(Object o) {
            for (int i = 0; i < size(); i++) {
                if (((T) get(i)).toString().equals(((T) o).toString())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            for (int i = 0; i < size(); i++) {
                if (((T) get(i)).toString().equals(((T) o).toString())) {
                    super.remove(i);
                    return true;
                }
            }
            return false;
        }
    }
}