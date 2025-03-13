package org.example.FileSystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.entities.FileTransferManager.compareLogs;
import static org.example.entities.FileTransferManager.getFileOperations;

public class Sender {
    private ObjectMapper objectMapper;

    public Sender() {
        objectMapper = new ObjectMapper();
    }

    public String sendMessage(String IpAddress, int port, Message message) {
        try (Socket socket = new Socket(IpAddress, port+10);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            //System.out.println(port+10);
            String msg = objectMapper.writeValueAsString(message.getMessageContent());
            out.println(msg);
            System.out.println("Sent to server: " + msg);

            String response = in.readLine();
            System.out.println("Received from server: " + response);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return "Unsuccessful";
        }
    }


    public String getFileRequest(String IpAddress, int port, String localFileName, String hyDFSFileName, int fileReceiverPort) {
        try {
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("messageName", "get_file");
            messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
            messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            messageContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
            messageContent.put("fileReceiverPort", String.valueOf(fileReceiverPort));
            messageContent.put("msgId", FDProperties.generateRandomMessageId());
            messageContent.put("localFileName", localFileName);
            messageContent.put("hyDFSFileName", hyDFSFileName);
            String senderPort = "" + FDProperties.getFDProperties().get("machinePort");
            Message msg = new Message("sending_file",
                    String.valueOf(FDProperties.getFDProperties().get("machineIp")),
                    senderPort,
                    messageContent);
            String response = sendMessage(IpAddress, port, msg);
            return response;
        }catch (Exception e){
            e.printStackTrace();
            return "Unsuccessful";
        }
    }

    public Boolean getFileRequestHash(String IpAddress, int port, String cacheFileName, String hyDFSFileName) {
        try {
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("messageName", "get_file_hash");
            messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
            messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            messageContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
            messageContent.put("msgId", FDProperties.generateRandomMessageId());
            messageContent.put("hyDFSFileName", hyDFSFileName);
            String senderPort = "" + FDProperties.getFDProperties().get("machinePort");
            Message msg = new Message("sending_file",
                    String.valueOf(FDProperties.getFDProperties().get("machineIp")),
                    senderPort,
                    messageContent);
            String response = sendMessage(IpAddress, port, msg);
            System.out.println(response);
            if(response.contains("Unsuccessful")){
                return false;
            }else {
                FileChannel fileChannel = FileChannel.open(Paths.get("local/" + cacheFileName), StandardOpenOption.READ);
                String calculatedHash = FileData.calculateHash(fileChannel);
                return calculatedHash.equals(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //TODO send request to receive a file
    //TODO send a request to upload a file
    public String uploadFile(String localFileName, String hyDFSFileName) throws IOException {
        //TODO check if file present in local
        try {
            int fileNameHash = HashFunction.hash(hyDFSFileName);
            Member member = MembershipList.getMemberById(fileNameHash);
            FileTransferManager.getRequestQueue().addRequest(new FileSender(
                    localFileName,
                    hyDFSFileName,
                    member.getIpAddress(),
                    Integer.parseInt(member.getPort()),
                    "UPLOAD",
                    "CREATE",
                    ""));
        }catch (Exception e){
            e.printStackTrace();
            return "Unable to upload file";
        }
        return "File uploaded successfully";
    }

    public String get_File(String hyDFSFileName, String localFileName) {
        try {
            //TODO need to handle failures
            //If any node is not there then ask for next node
            //If file not present then return File not found, for that need to check in all replicas
            //This request should go by default to Co-ordinator and then it would return the server name with the filepath and then request the file.
            // TODO check if file is present in its own list , also implement file caching in future
//            if(FileData.checkFilePresent(hyDFSFileName)){
//                System.out.println(hyDFSFileName + " exists at the local machine under HyDFS/");
//                return "success";
//            }
            boolean isAbsent = true;
            int fileNameHash = HashFunction.hash(hyDFSFileName);
            Member member = MembershipList.getMemberById(fileNameHash);
            String IpAddress = member.getIpAddress();
            String port = member.getPort();
            if(LRUFileCache.FILE_CACHE.isFileInCache(hyDFSFileName)){
                //System.out.println(" At line no. 140 in sender");
                if(LRUFileCache.FILE_CACHE.isFileOlder(hyDFSFileName)){
                    //check if file has changed with the owner
                    //System.out.println(" At line no. 143 in sender");
                    if(getFileRequestHash(IpAddress, Integer.parseInt(port), localFileName, hyDFSFileName)){
                        System.out.println("The file is already cached and saved at : local/" + hyDFSFileName);
                        isAbsent = false;
                    }
                }else{
                    System.out.println("The file is already cached and saved at : local/" + hyDFSFileName);
                    isAbsent = false;
                }
            }

            if(isAbsent){
                int fileReceiverPort = (int) FDProperties.getFDProperties().get("machinePort");
                //System.out.println(" At line no. 159 in sender");
                String result = getFileRequest(IpAddress, Integer.parseInt(port), localFileName, hyDFSFileName, fileReceiverPort);
                System.out.println("File receive was " + result);
            }
            return "Success";
        }catch (Exception e){
            e.printStackTrace();
            //System.out.println(" At line no. 166 in sender");
            return "Unable to send receive file request";
        }
    }

    //TODO send a request to append a file
    public void append_File(String localFileName, String hyDFSFileName){
        try {
            int fileNameHash = HashFunction.hash(hyDFSFileName);
            Member member = MembershipList.getMemberById(fileNameHash);
            FileTransferManager.getRequestQueue().addRequest(new FileSender(
                    localFileName,
                    hyDFSFileName,
                    member.getIpAddress(),
                    Integer.parseInt(member.getPort()),
                    "UPLOAD",
                    "APPEND",
                    ""));
        } catch (RuntimeException e) {
            System.out.println("File Append was unsuccessful");
//            throw new RuntimeException(e);
        }
    }

    //TODO get a replica from a node
    public String getFileFromReplica(String VMName, String hyDFSFileName, String localFileName) {
        try{
            int hash = HashFunction.hash(VMName);
            Member member = MembershipList.getMemberById(hash);
            String IpAddress = member.getIpAddress();
            String port = member.getPort();
            int fileReceiverPort = (int) FDProperties.getFDProperties().get("machinePort");
            String result = getFileRequest(IpAddress, Integer.parseInt(port), localFileName, hyDFSFileName, fileReceiverPort);
            if (result.equals("Successful")) {
                System.out.println(result);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return "Unable to receive file";
        }
    }
    //TODO send a request to merge files

    //TODO write a code to send multi appends.
    public void sendMultiAppendRequests(String hyDFSFileName, List<String> VMs, List<String> localFileNames) {
        for(int i=0; i<localFileNames.size(); i++) {
            int VMHash = HashFunction.hash(VMs.get(i));
            Member member = MembershipList.getMemberById(VMHash);
            try {
                Map<String, Object> messageContent = new HashMap<>();
                messageContent.put("messageName", "multi_append");
                messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
                messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
                messageContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
                messageContent.put("msgId", FDProperties.generateRandomMessageId());
                messageContent.put("localFileName", localFileNames.get(i));
                messageContent.put("hyDFSFileName", hyDFSFileName);
                String senderPort = "" + FDProperties.getFDProperties().get("machinePort");
                Message msg = new Message("sending_file",
                        String.valueOf(FDProperties.getFDProperties().get("machineIp")),
                        senderPort,
                        messageContent);
                String response = sendMessage(member.getIpAddress(), Integer.parseInt(member.getPort()), msg);
                System.out.println(response);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // request : get_file_details
    public HashMap<String, List<String>> getFileDetails(Member member, String request, String hyDFSFileNames) throws Exception {
        try {
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("messageName", request);
            messageContent.put("senderName", FDProperties.getFDProperties().get("machineName"));
            messageContent.put("senderIp", FDProperties.getFDProperties().get("machineIp"));
            messageContent.put("senderPort", String.valueOf(FDProperties.getFDProperties().get("machinePort")));
            messageContent.put("msgId", FDProperties.generateRandomMessageId());
//        messageContent.put("localFileName", localFileNames);
            //hyDFS Filenames should be comma separated
            messageContent.put("hyDFSFileNames", hyDFSFileNames);
            String senderPort = "" + FDProperties.getFDProperties().get("machinePort");
            Message msg = new Message(request,
                    String.valueOf(FDProperties.getFDProperties().get("machineIp")),
                    senderPort,
                    messageContent);
            String response = sendMessage(member.getIpAddress(), Integer.parseInt(member.getPort()), msg);
            System.out.println(response);
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, List<String>> map = objectMapper.readValue(response, HashMap.class);
            return map;
        }catch (Exception e){
            throw new Exception("Could not able to reconnect");
        }
    }

    // Function is for merge and Re replication after failure
    public void updateReplicas(List<String> fileNames){
        //get two successors and ask them their fileOperation list to compare with ours for merging
        try {
            String files = "";
            for (String fileName : fileNames) {
//                System.out.println(fileName);
                files += fileName + ",";
            }
//            System.out.println(files);
            files = files.substring(0, files.length() - 1);
//            System.out.println(files);
            List<Member> members = MembershipList.getNextMembers(MembershipList.selfId);
            for (Member member : members) {
//                System.out.println(member.getName());
                HashMap<String, List<String>> map = getFileDetails(member, "get_file_details", files);

                //Compare file details and take appropriate action
                for (String fileName : fileNames) {
                    if (map.containsKey(fileName) || map.get(fileName)!=null) {
                        // compare the list of events to check if files are consistent
                        // if not consistent then ask to replace the file
//                        System.out.println("File existed" + member.getName() + fileName);
                        if(compareLogs(map.get(fileName), getFileOperations(fileName))){ // call the compare list function
                            FileTransferManager.getRequestQueue().addRequest(new FileSender(
                                    "HyDFS/" + fileName,
                                    fileName,
                                    member.getIpAddress(),
                                    Integer.parseInt(member.getPort()),
                                    "REPLICA",
                                    "CREATE",
                                    ""));
                        }
                        // if yes then do nothing
                    } else {
                        //Send the file to the node.
//                        System.out.println("File did not existed" + member.getName() + fileName);
                        FileTransferManager.getRequestQueue().addRequest(new FileSender(
                                "HyDFS/" + fileName,
                                fileName,
                                member.getIpAddress(),
                                Integer.parseInt(member.getPort()),
                                "REPLICA",
                                "CREATE",
                                ""));
                    }
                    FileTransferManager.logEvent("File Operation : Upload : Successful : " + fileName);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        //If file not present then ask them to replicate the file. Can also use UPDATE/MERGE

        //If logs are inconsistent then it means appends may be in wrong order or not received any update
        //Send them those files under UPDATE/MERGE TYPE also with the file operations list.

        //whenever you do these operations put a log event of merge successful

    }
}
