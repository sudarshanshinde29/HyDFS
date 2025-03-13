package org.example.FileSystem;

import org.example.entities.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileReceiver extends Thread {
    int port;
    String hyDFSFileName;
    String HyDFSFilePath = "HyDFS/";
    String localFilePath = "local/";
    int tempCounter;

    public FileReceiver() {
        this.port = (int) FDProperties.getFDProperties().get("machinePort") + 20;
        tempCounter = 0;
    }

    public void run(){
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("Server listening on port " + port);
            while (true) {
                try (SocketChannel socketChannel = serverSocketChannel.accept()) {

                    ByteBuffer metadataLengthBuffer = ByteBuffer.allocate(4);
                    while (metadataLengthBuffer.hasRemaining()) {
                        int bytesRead = socketChannel.read(metadataLengthBuffer);
                        if (bytesRead == -1) {
                            throw new IOException("Channel closed prematurely while reading metadata length");
                        }
                    }

                    metadataLengthBuffer.flip();
                    int metadataLength = metadataLengthBuffer.getInt();

                    ByteBuffer metadataBuffer = ByteBuffer.allocate(metadataLength);
                    while (metadataBuffer.hasRemaining()) {
                        int bytesRead = socketChannel.read(metadataBuffer);
                        if (bytesRead == -1) {
                            throw new IOException("Channel closed prematurely while reading metadata");
                        }
                    }
                    metadataBuffer.flip();
                    String metadataJsonString = new String(metadataBuffer.array(), 0, metadataBuffer.limit());

                    System.out.println("Printing json" + metadataJsonString);
                    JSONObject metadataJson = new JSONObject(metadataJsonString);
                    String hyDFSFileName = metadataJson.getString("FILENAME");
                    String localFileName = metadataJson.getString("LOCALFILENAME");
                    long fileSize = metadataJson.getLong("SIZE");
                    String fileType = metadataJson.getString("TYPE");
                    String fileOp = metadataJson.getString("OP");
                    int senderId = metadataJson.getInt("SENDERID");

                    System.out.println(hyDFSFileName + fileSize + fileOp + fileType);
                    if (hyDFSFileName == null || fileSize == 0 || fileOp == null || fileType == null) {
                        System.out.println("Invalid metadata received");
                        continue; // Skip to the next connection if metadata is invalid
                    }

                    //Based on File Operation choose the option to create or append
                    FileChannel fileChannel = null;
                    FileChannel tempFileChannel = null;
                    boolean isTempFilePresent = false;
                    if(fileType.equals("UPLOAD")) {
                        if (fileOp.equals("CREATE")) {
                            fileChannel = FileChannel.open(Paths.get(HyDFSFilePath + hyDFSFileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        } else if (fileOp.equals("APPEND")) {
                            fileChannel = FileChannel.open(Paths.get(HyDFSFilePath + hyDFSFileName), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                            tempFileChannel = FileChannel.open(Paths.get(localFilePath + "temp" + tempCounter), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

                            isTempFilePresent = true;
                        }
                    }else if(fileType.equals("GET")) {
                        fileChannel = FileChannel.open(Paths.get(localFilePath + localFileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    }else if(fileType.equals("REPLICA")) {
                        if (fileOp.equals("CREATE")) {
                            fileChannel = FileChannel.open(Paths.get(HyDFSFilePath + hyDFSFileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        } else if (fileOp.equals("APPEND")) {
                            fileChannel = FileChannel.open(Paths.get(HyDFSFilePath + hyDFSFileName), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                        }
                    }
                    ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024); // 1 MB buffer
                    while (socketChannel.read(buffer) > 0) {
                        buffer.flip();
                        fileChannel.write(buffer);
                        if(isTempFilePresent) {
                            buffer.rewind(); // Set position back to 0
                            tempFileChannel.write(buffer);
                        }
                        buffer.clear();
                    }
                    buffer.clear();
                    System.out.println("File received successfully! and saved at " + hyDFSFileName);


                    if(fileType.equals("UPLOAD")) {
                        if(fileOp.equals("APPEND")) {
                            StandardOpenOption option = StandardOpenOption.APPEND;
                            //get next 2 nodes and send them the request
                            List<Member> members = MembershipList.getNextMembers(HashFunction.hash(hyDFSFileName));
                            for (Member member : members) {
                                FileTransferManager.getRequestQueue().addRequest(new FileSender(
                                        localFilePath + "temp" + tempCounter,
                                        hyDFSFileName,
                                        member.getIpAddress(),
                                        Integer.parseInt(member.getPort()),
                                        "REPLICA",
                                        "APPEND",
                                        ""));
                            }
                            FileTransferManager.logEvent("File Operation : Append : Successful : " + hyDFSFileName);
                            FileTransferManager.logEvent("File Append to " + hyDFSFileName + " with file " + localFileName);

                        }else{
                            FileData.addOwnedFile(hyDFSFileName);
                            //et next 2 nodes and send them the request
                            List<Member> members = MembershipList.getNextMembers(HashFunction.hash(hyDFSFileName));
                            for (Member member : members) {
                                FileTransferManager.getRequestQueue().addRequest(new FileSender(
                                        HyDFSFilePath + hyDFSFileName,
                                        hyDFSFileName,
                                        member.getIpAddress(),
                                        Integer.parseInt(member.getPort()),
                                        "REPLICA",
                                        "CREATE",
                                        ""));
                            }
                            FileTransferManager.logEvent("File Operation : Upload : Successful : " + hyDFSFileName);
                        }
                    }else if(fileType.equals("REPLICA")) {
                        if (fileOp.equals("APPEND")) {
                            FileTransferManager.logEvent("File Operation : Append Replica : Successful : " + hyDFSFileName);
                            FileTransferManager.logEvent("File Append to " + hyDFSFileName + " with file " + localFileName);
                        }else{
                            FileData.addReplica(hyDFSFileName, senderId);
                            FileTransferManager.logEvent("File Operation : Upload Replica : Successful : " + hyDFSFileName);
                        }
                    } else if (fileType.equals("GET")) {
                        LRUFileCache.FILE_CACHE.addFile(hyDFSFileName, fileChannel.size(), localFilePath + hyDFSFileName);
                    }
                    FileTransferManager.logEvent("File received: " + hyDFSFileName);
                    tempCounter++;

                    if (fileChannel != null) fileChannel.close();
                    if (isTempFilePresent && tempFileChannel != null) tempFileChannel.close();
                } catch (IOException e) {
                    System.out.println(" Operation Completed . Current Time is :- "+Member.getLocalDateTime());
                    e.printStackTrace();
                    FileTransferManager.logEvent("File reception failed for " + hyDFSFileName + ": " + e.getMessage());
                }


                hyDFSFileName = null;
                System.out.println(" Operation Completed . Current Time is :- "+Member.getLocalDateTime());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during Server start: " + e.getMessage());
        }
    }

}
