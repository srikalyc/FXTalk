#!/bin/bash
cd ../images/dmg.image/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib
mkdir ext
cp -p /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar ext
cp -p /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/dnsns.jar ext
cp -p /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/localedata.jar ext
cp -p /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/sunec.jar ext
cp -p /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar ext
cp -p /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/zipfs.jar ext

#The following are required to patch the .app package
mkdir /Users/srikchan/NetbeansProjects/FXTalk/dist/bundles/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext
cp /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar /Users/srikchan/NetbeansProjects/FXTalk/dist/bundles/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext
cp /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/dnsns.jar /Users/srikchan/NetbeansProjects/FXTalk/dist/bundles/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext
cp /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/localedata.jar /Users/srikchan/NetbeansProjects/FXTalk/dist/bundles/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext
cp /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/sunec.jar /Users/srikchan/NetbeansProjects/FXTalk/dist/bundles/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext
cp /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar /Users/srikchan/NetbeansProjects/FXTalk/dist/bundles/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext
cp /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext/zipfs.jar /Users/srikchan/NetbeansProjects/FXTalk/dist/bundles/FXTalk.app/Contents/PlugIns/jdk1.7.0_21.jdk/Contents/Home/jre/lib/ext