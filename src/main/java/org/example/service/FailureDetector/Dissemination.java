package org.example.service.FailureDetector;

import org.example.entities.FDProperties;
import org.example.entities.Member;
import org.example.entities.MembershipList;
import org.example.entities.Message;
import org.example.service.Ping.PingReceiver;
import org.example.service.Ping.PingSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
/**
 * This Class contains Dissemination logic
 */
public class Dissemination {

    private static final Logger logger = LoggerFactory.getLogger(Dissemination.class);

    public void startDisseminatorService() {
        //Start the Ping receiving service
        PingReceiver pingReceiver = null;
        try {
            pingReceiver = new PingReceiver();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        pingReceiver.start();
    }

    /**
     * This methods sends suspect Message
     * @param member
     */
    public void sendSuspectMessage(Member member) {
        try {
            member.setStatus("Suspected");
            member.setDateTime(Member.getLocalDateTime());
            //TODO add the incarnation number
            MembershipList.members.put(member.getName(), member);
            System.out.println("Node is Suspected : "+ member.getName() + "__" + Member.getLocalDateTime());
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("messageName", "suspect");
            messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            messageContent.put("senderPort", FDProperties.getFDProperties().get("machinePort"));
            messageContent.put("msgId", FDProperties.generateRandomMessageId());
            messageContent.put("memberName", member.getName());
            messageContent.put("memberIp", member.getIpAddress());
            messageContent.put("memberPort", member.getPort());
            PingSender removeNode = new PingSender();
            removeNode.multicast("suspect", messageContent);
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * This method sends alive message
     */
    public void sendSelfAliveMessage() {
        try {
            System.out.println("Setting self alive"  + "__" + Member.getLocalDateTime());
            logger.info("Setting self alive");
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("messageName", "alive");
            messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            messageContent.put("senderPort", FDProperties.getFDProperties().get("machinePort"));
            messageContent.put("msgId", FDProperties.generateRandomMessageId());
            messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
            messageContent.put("versionNo", String.valueOf(FDProperties.getFDProperties().get("versionNo")));
            messageContent.put("incarnationNo", String.valueOf(FDProperties.getFDProperties().get("incarnationNo")));
            messageContent.put("isIntroducing", "false");
            PingSender pingSender = new PingSender();
            pingSender.multicast("alive", messageContent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method sends alive message to introducer
     */
    public void sendAliveMessageToIntroducer(){
        PingSender pingSender = new PingSender();
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("messageName", "alive");
        messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
        messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
        messageContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
        messageContent.put("msgId", FDProperties.generateRandomMessageId());
        FDProperties.getFDProperties().put("versionNo", Member.getLocalDateTime());
        messageContent.put("versionNo", String.valueOf(FDProperties.getFDProperties().get("versionNo")));
        messageContent.put("incarnationNo", String.valueOf(FDProperties.getFDProperties().get("incarnationNo")));
        messageContent.put("isIntroducing", "true");
        try {
            logger.info("Sending alive message to introducer");
            Message message = new Message("alive",
                    (String) FDProperties.getFDProperties().get("introducerAddress"),
                    ((String) FDProperties.getFDProperties().get("introducerPort")),
                    messageContent);
            pingSender.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FDProperties.getFDProperties().put("versionNo", Member.getLocalDateTime());
    }

    /**
     * This method sends alive message
     * @param member
     */
    public void sendAliveMessage(Member member) {
        try {
            member.setStatus("Alive");
            member.setDateTime(Member.getLocalDateTime());
            MembershipList.members.put(member.getName(), member);
            System.out.println("Node is Alive : "+ member.getName() + "__" + Member.getLocalDateTime());
            logger.info("Node is Alive : "+ member.getName());
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("messageName", "alive");
            messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            messageContent.put("senderPort", FDProperties.getFDProperties().get("machinePort"));
            messageContent.put("msgId", FDProperties.generateRandomMessageId());
            messageContent.put("memberName", member.getName());
            messageContent.put("memberIp", member.getIpAddress());
            messageContent.put("memberPort", member.getPort());
            messageContent.put("isIntroducing", "false");
            messageContent.put("versionNo", String.valueOf(FDProperties.getFDProperties().get("versionNo")));
            messageContent.put("incarnationNo", String.valueOf(FDProperties.getFDProperties().get("incarnationNo")));
            messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
            PingSender pingSender = new PingSender();
            pingSender.multicast("alive", messageContent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method sends failed message
     * @param removeMember
     */
    public void sendFailedMessage(Member removeMember) {
        try {
            System.out.println("Node has Failed : " + removeMember.getName() + "__" + Member.getLocalDateTime());
            logger.info("Node has Failed : " + removeMember.getName());
            Map<String, Object> removeContent = new HashMap<>();
            removeContent.put("messageName", "failed");
            removeContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            removeContent.put("senderPort", FDProperties.getFDProperties().get("machinePort"));
            removeContent.put("msgId", FDProperties.generateRandomMessageId());
            removeContent.put("memberName", removeMember.getName());
            removeContent.put("memberIp", removeMember.getIpAddress());
            removeContent.put("memberPort", removeMember.getPort());
            PingSender removeNode = new PingSender();
            removeNode.multicast("failed", removeContent);
            MembershipList.removeMember(removeMember.getName());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method sends leave message
     */
    public void sendLeaveMessage() {
        try {
            String  memberName = String.valueOf(FDProperties.getFDProperties().get("machineName"));
            System.out.println("Node is Leaving : " + memberName + "__" + Member.getLocalDateTime());
            logger.info("Node is Leaving : " + memberName);
            Map<String, Object> removeContent = new HashMap<>();
            removeContent.put("messageName", "failed");
            removeContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            removeContent.put("senderPort", FDProperties.getFDProperties().get("machinePort"));
            removeContent.put("msgId", FDProperties.generateRandomMessageId());
            removeContent.put("memberName", FDProperties.getFDProperties().get("machineName"));
            PingSender removeNode = new PingSender();
            removeNode.multicast("failed", removeContent);
            MembershipList.removeMember(memberName);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method sends confirm message
     * @param removeMember
     */
    public void sendConfirmMessage(Member removeMember) {
        try {
            System.out.println("Node was in Suspect state and since there is no Alive message, Node Failed : " + removeMember.getName() + "__" + Member.getLocalDateTime());
            logger.info("Node was in Suspect state and since there is no Alive message, Node Failed : " + removeMember.getName());
            Map<String, Object> removeContent = new HashMap<>();
            removeContent.put("messageName", "confirm");
            removeContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            removeContent.put("senderPort", FDProperties.getFDProperties().get("machinePort"));
            removeContent.put("msgId", FDProperties.generateRandomMessageId());
            removeContent.put("memberName", removeMember.getName());
            removeContent.put("memberIp", removeMember.getIpAddress());
            removeContent.put("memberPort", removeMember.getPort());
            PingSender removeNode = new PingSender();
            removeNode.multicast("confirm", removeContent);
            MembershipList.removeMember(removeMember.getName());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to multicast the isSuspicionModeOn command
     * @return
     */
    public String sendSwitch(){
        try{
            //Switch mode for self
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

            //switch mode for others
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("messageName", "switch");
            messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            messageContent.put("senderPort", FDProperties.getFDProperties().get("machinePort"));
            messageContent.put("msgId", FDProperties.generateRandomMessageId());
            PingSender pingSender = new PingSender();
            pingSender.multicast("switch", messageContent);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "Successful";
    }
}
