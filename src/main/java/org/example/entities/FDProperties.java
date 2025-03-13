package org.example.entities;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.config.AppConfig.readPropertiesFile;

public class FDProperties {
    public static ConcurrentHashMap<String, Object> fDProperties = new ConcurrentHashMap<>();

    public static void initialize(){
        Properties prop = null;
        try {
            prop = readPropertiesFile("application.properties");
        }catch (IOException e){
            e.printStackTrace();
        }
        if(prop == null){
            throw new RuntimeException("Could not load application properties file");
        }
        fDProperties.put("isIntroducer",Boolean.parseBoolean(prop.getProperty("isIntroducer")));
        fDProperties.put("protocolPeriod", Integer.parseInt(prop.getProperty("protocolPeriod")));
        fDProperties.put("suspicionProtocolPeriod", Integer.parseInt(prop.getProperty("suspicionProtocolPeriod")));
        fDProperties.put("isSuspicionModeOn", Boolean.parseBoolean(prop.getProperty("isSuspicionModeOn")));
        fDProperties.put("suspicionSwimWaitPeriod", Integer.parseInt(prop.getProperty("suspicionSwimWaitPeriod")));
        fDProperties.put("basicSwimWaitPeriod", Integer.parseInt(prop.getProperty("basicSwimWaitPeriod")));
        fDProperties.put("machineIp", prop.getProperty("machineIp"));
        fDProperties.put("machinePort", Integer.parseInt(prop.getProperty("machinePort")));
        fDProperties.put("introducerAddress", prop.getProperty("introducerAddress"));
        fDProperties.put("introducerPort", prop.getProperty("introducerPort"));
        fDProperties.put("machineName", prop.getProperty("machineName"));
        fDProperties.put("versionNo", 0);
        fDProperties.put("incarnationNo", prop.getProperty("incarnationNo"));
        fDProperties.put("cacheSize", prop.getProperty("cacheSize"));
        fDProperties.put("mergeReq", prop.getProperty("mergeReq"));
//        fDProperties.put("tcpPort", Integer.parseInt(prop.getProperty("tcpPort")));
//        fDProperties.put("filePort", Integer.parseInt(prop.getProperty("filePort")));
        if((Boolean) fDProperties.get("isSuspicionModeOn"))
            fDProperties.put("ackWaitPeriod", fDProperties.get("suspicionSwimWaitPeriod"));
        else
            fDProperties.put("ackWaitPeriod", fDProperties.get("basicSwimWaitPeriod"));
    }

    public static ConcurrentHashMap<String, Object> getFDProperties(){
        return fDProperties;
    }

    public static void printFDProperties(){
        System.out.println("FDProperties:");
        fDProperties.forEach((key, value) -> System.out.println(key + "=" + value));
    }

    public static String generateRandomMessageId(){
        Random random = new Random();
        return String.valueOf(1000000000L + (long)(random.nextDouble() * 9000000000L));
    }
}
