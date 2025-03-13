package org.example.entities;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the message class
 */
public class Message {
    String messageName;
    InetAddress ipAddress;
    String port;
    Map<String,Object> messageContent;

    public Message(String messageName, String ipAddress, String port, Map<String,Object> messageContent) throws UnknownHostException {
        this.messageName = messageName;
        this.ipAddress = InetAddress.getByName(ipAddress);
        this.port = port;
        this.messageContent = messageContent;
    }

    public Message(String messageName, InetAddress ipAddress, String port, Map<String,Object> messageContent) {
        this.messageName = messageName;
        this.ipAddress = ipAddress;
        this.port = port;
        this.messageContent = messageContent;
    }

    public static Message process(InetAddress address, String port, String input) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> messageContent = mapper.readValue(input, Map.class);
            return new Message(String.valueOf(messageContent.get("messageName")), address, port, messageContent);
        }catch (Exception e){
            System.out.println("error while processing the input json format");
            e.printStackTrace();
            throw new RuntimeException("error while processing the input json format");
        }
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) throws UnknownHostException {
        this.ipAddress = InetAddress.getByName(ipAddress);
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Map getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(Map messageContent) {
        this.messageContent = messageContent;
    }
}
