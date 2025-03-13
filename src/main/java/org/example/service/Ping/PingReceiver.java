package org.example.service.Ping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.FileSystem.HashFunction;
import org.example.entities.FDProperties;
import org.example.entities.Member;
import org.example.entities.MembershipList;
import org.example.entities.Message;
import org.example.service.FailureDetector.Dissemination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains pingReceiver logic
 */
public class PingReceiver extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(PingReceiver.class);

    private DatagramSocket serverSocket;
    private boolean running;
    private ObjectMapper objectMapper;

    public PingReceiver() throws SocketException {
        objectMapper = new ObjectMapper();
        serverSocket = new DatagramSocket((int)FDProperties.getFDProperties().get("machinePort"));
    }

    public void run() {
        running = true;
        logger.info("Listener service for Dissemination Component started");
        while (running) {
            byte[] buf = new byte[16384];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                serverSocket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            String received = new String(
                    packet.getData(), 0, packet.getLength());
            Message message = Message.process(address, String.valueOf(port), received);

            String messageName = message.getMessageName();
            //TODO based on the message received take the action
            //TODO add the introducer under this class and set a flag of if introducer then only execute the below code
            //TODO same will be for suspicion mode.
            //Add a switch case
            switch (messageName){
                case "introduction":
                    try {
                        logger.info("Introduction successful message received");
                        MembershipList.addMember(
                                new Member(HashFunction.hash((String) message.getMessageContent().get("senderName")),
                                        (String) message.getMessageContent().get("senderName"),
                                        (String) message.getMessageContent().get("senderIp"),
                                        ((String) message.getMessageContent().get("senderPort")),
                                        (String) message.getMessageContent().get("versionNo"),
                                        "alive",
                                        Member.getLocalDateTime(),
                                        (String) message.getMessageContent().get("incarnationNo"))
                        );
                        MembershipList.printMembers();
                        logger.info("Receiving membership list");
                        buf = new byte[16384];
                        packet = new DatagramPacket(buf, buf.length, address, port);
                        serverSocket.receive(packet);
                        String json = new String(
                                packet.getData(), 0, packet.getLength());
                        ConcurrentHashMap<String,Object> temp = objectMapper.readValue(json, ConcurrentHashMap.class);
                        temp.forEach((k,v) -> {
                            try {
                                String t = objectMapper.writeValueAsString(v);
                                Map<String, String> map = objectMapper.readValue(t, Map.class);
                                if(!map.get("name").equals(FDProperties.getFDProperties().get("machineName"))) {
                                    MembershipList.addMember(
                                            new Member(HashFunction.hash(map.get("name")), map.get("name"), map.get("ipAddress"), map.get("port"), map.get("versionNo"), map.get("status"), map.get("dateTime"), map.get("incarnationNo"))
                                    );
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    MembershipList.printMembers();
                    break;
                case "alive":
                    try {
                        if (message.getMessageContent().get("isIntroducing").equals("true") && (Boolean) FDProperties.getFDProperties().get("isIntroducer")) {
                            System.out.println("A node wants to join a group with ip address " + packet.getAddress() + ":" + packet.getPort()
                                    + " with version " + message + "__" + Member.getLocalDateTime());
                            String result = "";
                            //if a node sends an alive message to join the group then multicast that message to everyone
                            try {
                                PingSender pingSender = new PingSender();
                                result = pingSender.multicast(message.getMessageName(), message.getMessageContent());
                                //add this member to its own list
                                MembershipList.addMember(
                                        new Member(HashFunction.hash((String) message.getMessageContent().get("senderName")),
                                                (String) message.getMessageContent().get("senderName"),
                                                (String) message.getMessageContent().get("senderIp"),
                                                ((String) message.getMessageContent().get("senderPort")),
                                                (String) message.getMessageContent().get("versionNo"),
                                                "alive",
                                                Member.getLocalDateTime(),
                                                (String) message.getMessageContent().get("incarnationNo"))
                                );
                                MembershipList.printMembers();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (result.equals("Successful")) {
                                logger.info("Member Introduced in the Group");
                                PingSender pingSender = new PingSender();
                                Map<String, Object> messageContent = new HashMap<>();
                                messageContent.put("messageName", "introduction");
                                messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
                                messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
                                messageContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
                                messageContent.put("msgId", FDProperties.generateRandomMessageId());
                                messageContent.put("versionNo", String.valueOf(FDProperties.getFDProperties().get("versionNo")));
                                messageContent.put("incarnationNo", String.valueOf(FDProperties.getFDProperties().get("incarnationNo")));
                                messageContent.put("isIntroducing", "false");
                                try {
                                    Message introduceBackMessage = new Message("alive",
                                            (String) message.getMessageContent().get("senderIp"),
                                            ((String) message.getMessageContent().get("senderPort")),
                                            messageContent);
                                    //Add introducer to the memberList of the new member
                                    pingSender.sendMessage(introduceBackMessage);
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    String json = objectMapper.writeValueAsString(MembershipList.members);
                                    logger.info("Sending Member List" + json);
                                    //Give the list of other members to the new member
                                    pingSender.sendMembershipList(introduceBackMessage, json);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            logger.info("Got Alive message" + message.getMessageContent().get("incarnationNo"));
                            System.out.println("Alive message received : " + message.getMessageContent().get("senderName") + "__" + Member.getLocalDateTime());
                            if (message.getMessageContent().get("senderName").equals(FDProperties.getFDProperties().get("machineName")))
                                break;
                            String s = "" + message.getMessageContent().get("incarnationNo");
                            String t = "" + message.getMessageContent().get("versionNo");
                            String k = "" + message.getMessageContent().get("senderPort");
                            logger.info(s);
//                            if(message.getMessageContent().get("incarnationNo") instanceof Integer){
//                                logger.info("yes");
//                            }
//                            int incarnationNo = (int) message.getMessageContent().get("incarnationNo");
//                            logger.info("No");
                            MembershipList.addMember(
                                    new Member(HashFunction.hash((String) message.getMessageContent().get("senderName")),
                                            (String) message.getMessageContent().get("senderName"),
                                            (String) message.getMessageContent().get("senderIp"),
                                            k,
                                            t,
                                            "alive",
                                            Member.getLocalDateTime(),
                                            s)
                            );
                            MembershipList.printMembers();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                    break;
                case "ping":
                    try {
                        logger.info("Ping received from : " + packet.getAddress());
                        Map<String, Object> messageContent = new HashMap<>();
                        messageContent.put("messageName", "pingAck");
                        messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
                        messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
                        messageContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
                        messageContent.put("msgId", FDProperties.generateRandomMessageId());
//                        System.out.println("Sending Ping Ack");
                        buf = new byte[1024];
                        buf = objectMapper.writeValueAsString(messageContent).getBytes();
                        DatagramPacket pingAckPacket
                                = new DatagramPacket(buf, buf.length, address, port);
                        serverSocket.send(pingAckPacket);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                    break;
                case "pingReq":
                    try {
                        System.out.println("Ping Required received");
                        Map<String, Object> pingReqContent = new HashMap<>();
                        pingReqContent.put("messageName", "pingReqJ");
                        pingReqContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
                        pingReqContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
                        pingReqContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
                        pingReqContent.put("msgId", message.getMessageContent().get("msgId"));
                        pingReqContent.put("originalSenderIp", address);
                        pingReqContent.put("originalSenderPort", String.valueOf(port));
                        System.out.println("Sending Ping Ack");
                        Message pingReqMessage = new Message("ping", (String) message.getMessageContent().get("targetSenderIp"), String.valueOf(Integer.parseInt((String) message.getMessageContent().get("targetSenderPort"))), pingReqContent);
                        PingSender pingSender = new PingSender();
                        pingSender.sendMessage(pingReqMessage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "failed" :
                    System.out.println("Failed message received" + message.getMessageContent().get("memberName") + "__" + Member.getLocalDateTime());
                    logger.info("Failed message received" + message.getMessageContent().get("memberName"));
                    //TODO MP3 call a function to start re replication process
                    try {
                        MembershipList.failedNodes.add(HashFunction.hash(String.valueOf(message.getMessageContent().get("memberName"))));
                        if(!message.getMessageContent().get("memberName").equals(FDProperties.getFDProperties().get("machineName"))) {
                            MembershipList.removeMember((String) message.getMessageContent().get("memberName"));
                        }else{
                            logger.info("The Node itself has been removed from the group");
                            MembershipList.members.clear();
                            MembershipList.memberNames.clear();
                        }
                    }catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                    break;
                case "suspect" :
                    System.out.println("Suspect message received " + message.getMessageContent().get("memberName") + "__" + Member.getLocalDateTime());
                    logger.info("Suspect message received " + message.getMessageContent().get("memberName"));
                    try{
                        //add a piece of code to set the status of a member to suspect
                        //add a piece of code which will send alive multicast if the suspect node is itself
                        if(message.getMessageContent().get("memberName").equals(FDProperties.getFDProperties().get("machineName"))){
                            Dissemination dissemination = new Dissemination();
                            int inc = Integer.parseInt((String) FDProperties.getFDProperties().get("incarnationNo"));
                            FDProperties.getFDProperties().put("incarnationNo", String.valueOf(inc+1));
                            dissemination.sendSelfAliveMessage();
                        }else {
                            if(!MembershipList.members.get((String) message.getMessageContent().get("memberName")).getStatus().equals("Suspected")) {
                                Member member = MembershipList.members.get((String) message.getMessageContent().get("memberName"));
                                member.setStatus("Suspected");
                                member.setDateTime(Member.getLocalDateTime());
                                //TODO add the incarnation number
                                MembershipList.members.put(member.getName(), member);
                            }
                        }
                    }catch(Exception e){
                        logger.error(e.getMessage());
                    }
                    break;
                case "confirm" :
                    System.out.println("Confirm message received " + message.getMessageContent().get("memberName") + "__" + Member.getLocalDateTime() );
                    logger.info("Confirm message received " + message.getMessageContent().get("memberName") );
                    //TODO MP3 call a function to start re replication process
                    try {
                        //TODO add a piece of code that will remove the member from the list
                        MembershipList.failedNodes.add(HashFunction.hash(String.valueOf(message.getMessageContent().get("memberName"))));
                        if(!message.getMessageContent().get("memberName").equals(FDProperties.getFDProperties().get("machineName"))) {
                            MembershipList.removeMember((String) message.getMessageContent().get("memberName"));
                        }else{
                            logger.info("The Node itself has been removed from the group");
                            MembershipList.members.clear();
                            MembershipList.memberNames.clear();
                        }
                    }catch(Exception e){
                        logger.error(e.getMessage());
                    }
                    break;
                case "switch" :
                    if((Boolean) FDProperties.getFDProperties().get("isSuspicionModeOn")) {
                        System.out.println("Switching to Basic Swim");
                        FDProperties.getFDProperties().put("isSuspicionModeOn", false);
                        FDProperties.getFDProperties().put("ackWaitPeriod", FDProperties.getFDProperties().get("basicSwimWaitPeriod"));
                    }
                    else {
                        System.out.println("Switching to Suspicion Mode");
                        FDProperties.getFDProperties().put("isSuspicionModeOn", true);
                        FDProperties.getFDProperties().put("ackWaitPeriod", FDProperties.getFDProperties().get("suspicionSwimWaitPeriod"));
                    }
                    break;
                default:
            }
        }
        serverSocket.close();
    }

    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
