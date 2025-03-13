package org.example.FileSystem;

import org.example.entities.FileTransferManager;
import org.example.entities.MembershipList;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileSender implements Runnable {
    String localFileName;
    String hyDFSFileName;
    // IpAddress and port where file is to be sent.
    String IpAddress;
    int port;
    String result;
    String fileType;
    String fileOp;
    String message;

    public FileSender(String localFileName,
                      String hyDFSFileName,
                      String IpAddress,
                      int port,
                      String fileType,
                      String fileOp,
                      String message) {
        this.localFileName = localFileName;
        this.hyDFSFileName = hyDFSFileName;
        this.IpAddress = IpAddress;
        this.port = port + 20;
        this.fileType = fileType;
        this.fileOp = fileOp;
        this.message = message;
    }
    public void run() {
        System.out.println("Connecting to server at " + IpAddress + ":" + port);
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(IpAddress, port));
             FileChannel fileChannel = FileChannel.open(Paths.get(localFileName), StandardOpenOption.READ)) {
            System.out.println("Sending file");

            JSONObject metadataJson = new JSONObject();
            metadataJson.put("FILENAME", hyDFSFileName);
            metadataJson.put("LOCALFILENAME", localFileName.replace("HyDFS/", ""));
            metadataJson.put("SIZE", fileChannel.size());
            metadataJson.put("TYPE", fileType);
            metadataJson.put("OP", fileOp);
            metadataJson.put("SENDERID", MembershipList.selfId);

            byte[] metadataBytes = metadataJson.toString().getBytes();
            ByteBuffer metadataLengthBuffer = ByteBuffer.allocate(5);
            metadataLengthBuffer.putInt(metadataBytes.length);
            metadataLengthBuffer.flip();
            socketChannel.write(metadataLengthBuffer);

            ByteBuffer metadataBuffer = ByteBuffer.wrap(metadataBytes);
            socketChannel.write(metadataBuffer);

            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024); // 1 MB buffer
            while (fileChannel.read(buffer) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
            buffer.clear();
            socketChannel.socket().getOutputStream().flush();
            result = "File sent successfully!";
            FileTransferManager.logEvent("File sent: " + localFileName);
            fileChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
            result = "File not able to be sent!";
            FileTransferManager.logEvent("File sending failed for " + localFileName + ": " + e.getMessage());
        }
    }

}
