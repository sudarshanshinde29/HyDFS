package org.example.FileSystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.FDProperties;
import org.example.entities.FileData;
import org.example.entities.FileTransferManager;
import org.example.entities.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;

public class Receiver extends Thread {

    public void receiveMessage(){
        int serverPort = (int) FDProperties.getFDProperties().get("machinePort") + 10;
        FileTransferManager fileTransferManager = new FileTransferManager();
        try (var serverSocket = new ServerSocket(serverPort)) { // Server listening on port 5000
            System.out.println("Server is listening on port " + serverPort);

            // Accept client connection
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    System.out.println("Client connected");

                    // Read message from client
                    String received = in.readLine();
                    Message message = Message.process(socket.getInetAddress(), String.valueOf(socket.getPort()), received);
                    System.out.println("Received from client: " + message);
                    String response = "Successful";
                    switch (message.getMessageName()){
                        case "get_file" :
                            //TODO write code for sending the file which is demanded
                            try {
                                String hyDFSFileName = String.valueOf(message.getMessageContent().get("hyDFSFileName"));
                                if(FileData.checkFilePresent(hyDFSFileName)) {
                                    FileTransferManager.getRequestQueue().addRequest(new FileSender(
                                            "HyDFS/" + hyDFSFileName,
                                            hyDFSFileName,
                                            message.getIpAddress().getHostAddress(),
                                            Integer.parseInt(String.valueOf(message.getMessageContent().get("senderPort"))),
                                            "GET",
                                            "CREATE",
                                            "Sending Requested File"
                                    ));
                                    response = "Successful";
                                }else {
                                    response = "Unsuccessful file not found";
                                }
                                out.println(response);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                        case "get_file_from_replica" :
                            try {
                                String hyDFSFileName = String.valueOf(message.getMessageContent().get("hyDFSFileName"));
                                if(FileData.checkFilePresent(hyDFSFileName)){
                                    FileTransferManager.getRequestQueue().addRequest(new FileSender(
                                            "HyDFS/" + hyDFSFileName,
                                            hyDFSFileName,
                                            message.getIpAddress().getHostAddress(),
                                            Integer.parseInt(String.valueOf(message.getMessageContent().get("senderPort"))),
                                            "GET",
                                            "CREATE",
                                            "Sending Requested File"
                                    ));
                                    response = "Successful";
                                }else {
                                    response = "Unsuccessful file not found";
                                }
                                out.println(response);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                        case "get_file_hash":
                            try {
                                String hyDFSFileName = String.valueOf(message.getMessageContent().get("hyDFSFileName"));
                                //System.out.println("At line 92 in receiv");
                                if(FileData.checkFilePresent(hyDFSFileName)){
                                    FileChannel fileChannel = FileChannel.open(Paths.get("HyDFS/" + hyDFSFileName), StandardOpenOption.READ);
                                    response = FileData.calculateHash(fileChannel);
                                    //System.out.println("At line 95 in receiv");
                                }else {
                                    response = "Unsuccessful file not found";
                                    //System.out.println("At line 98 in receiv");
                                }
                                //System.out.println("Printing from 101"+response);
                                out.println(response);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                        case "multi_append":
                            Sender sender = new Sender();
                            String localFileName = String.valueOf(message.getMessageContent().get("localFileName"));
                            String hyDFSFileName = String.valueOf(message.getMessageContent().get("hyDFSFileName"));
                            sender.append_File(localFileName, hyDFSFileName);
                            break;
                        case "get_file_details":
                            String[] requestFiles = String.valueOf(message.getMessageContent().get("hyDFSFileNames")).split(",");
                            HashMap<String, List<String>> map = new HashMap<>();
                            for(String requestFile : requestFiles){
//                                System.out.println(FileTransferManager.getFileOperations(requestFile));
                                if(FileData.checkFilePresent(requestFile)) {
                                    map.put(requestFile, FileTransferManager.getFileOperations(requestFile));
                                }
                            }
                            ObjectMapper objectMapper = new ObjectMapper();
                            out.println(objectMapper.writeValueAsString(map));
                        case "/exit":
                    }
                    // Send response back to client

                    System.out.println("Sent to client: " + response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        receiveMessage();
    }
}
