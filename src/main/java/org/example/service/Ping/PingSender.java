package org.example.service.Ping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.FDProperties;
import org.example.entities.Member;
import org.example.entities.MembershipList;
import org.example.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains pingsender logic
 */
public class PingSender extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(PingSender.class);

    private volatile String result;
    private volatile int waitPeriod;
    private Message message;

    public PingSender(Message message, int waitPeriod) {
        this.message = message;
        this.waitPeriod = waitPeriod;
    }

    public PingSender() {}

    public String getResult() {return result; }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            //This node will wait for below timeout for the target node to send the ack
            socket.setSoTimeout(waitPeriod);
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(message.getMessageContent());
//            System.out.println(s);
            byte[] buf = s.getBytes();
            logger.debug("Printing buffer length: " + buf.length);
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, message.getIpAddress(), Integer.parseInt(message.getPort()));
            socket.send(packet);
//            System.out.println("Ping sent to " + message.getIpAddress() + ":" + message.getPort());
            byte[] receivingBuf = new byte[16384];
            packet = new DatagramPacket(receivingBuf, receivingBuf.length);
            socket.receive(packet);
            String received = new String(
                    packet.getData(), 0, packet.getLength());
//            System.out.println(received);
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            Message replyMessage = Message.process(address, String.valueOf(port), received);
            if (replyMessage.getMessageContent().get("messageName").equals("pingAck")) {
                result = "Successful";
            }
        }
        catch (SocketTimeoutException e) {
            // as the message time has timeout we will send an unsuccessful response
            result = "Unsuccessful";
        } catch (IOException e) {
            throw new RuntimeException("IOException");
        }
    }

    public String sendMessage(Message message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            //This node will wait for below timeout for the target node to send the ack
//            socket.setSoTimeout((int)FDProperties.getFDProperties().get("ackWaitPeriod"));
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(message.getMessageContent());
            byte[] buf = s.getBytes();
            logger.debug("Printing buffer length: " + buf.length);
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, message.getIpAddress(), Integer.parseInt(message.getPort()));
            socket.send(packet);
        }
        catch (SocketTimeoutException e) {
            // as the message time has timeout we will send an unsuccessful response
            return "Unsuccessful";
        } catch (IOException e) {
            return "IOException";
        }
        return "Unsuccessful";
    }

    public String sendMembershipList(Message message, String json) {
        try {
            DatagramSocket socket = new DatagramSocket();
            //This node will wait for below timeout for the target node to send the ack
//            socket.setSoTimeout((int)FDProperties.getFDProperties().get("ackWaitPeriod"));
            byte[] buf = json.getBytes();
            logger.debug("Printing buffer length: " + buf.length);
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, message.getIpAddress(), Integer.parseInt(message.getPort()));
            socket.send(packet);
        }
        catch (SocketTimeoutException e) {
            // as the message time has timeout we will send an unsuccessful response
            return "Unsuccessful";
        } catch (IOException e) {
            return "IOException";
        }
        return "Unsuccessful";
    }

    //TODO needs to improve this one
    public String multicast(String messageName, Map<String, Object> messageContent) throws IOException {
        ConcurrentHashMap<String, Member> copiedMap = new ConcurrentHashMap<>(MembershipList.members);
        copiedMap.forEach((key, member) -> {
            try {
                Message message = new Message(messageName, member.getIpAddress(),member.getPort(),
                        messageContent);
                sendMessage(message);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });
        return "Successful";
    }

}
