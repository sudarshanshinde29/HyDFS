package org.example.entities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileData {
    public static List<String> ownedFiles = new CopyOnWriteArrayList<>();
    public static ConcurrentSkipListMap<String,Integer> replicaMap = new ConcurrentSkipListMap<>();

    public static List<String> getOwnedFiles() {
        return ownedFiles;
    }

    public static void addOwnedFile(String ownedFile) {
        FileData.ownedFiles.add(ownedFile);
    }

    public static ConcurrentSkipListMap<String, Integer> getReplicaMap() {
        return replicaMap;
    }

    public static void addReplica(String fileName, int ownerId) {
        replicaMap.put(fileName, ownerId);
    }

    public static boolean checkFilePresent(String fileName) {
        return replicaMap.containsKey(fileName) || ownedFiles.contains(fileName);
    }

//    public static boolean isFileReplica(String fileName) {
//        return replicaMap.containsKey(fileName);
//    }

    public static List<String> getAndRemoveReplicasOfANode(int nodeId) {
        List<String> files = new ArrayList<>();
        for(Map.Entry<String,Integer> entry: replicaMap.entrySet()){
                if(entry.getValue() == nodeId){
                    files.add(entry.getKey());
                    replicaMap.remove(entry.getKey());
                }
        }
        return files;
    }

    public static void addOwnedFiles(List<String>ownedFiles) {
        FileData.ownedFiles.addAll(ownedFiles);
    }

    public static String calculateHash(FileChannel fileChannel) throws NoSuchAlgorithmException, IOException {
        // Initialize the MessageDigest with the desired algorithm
        //System.out.println("At line 59  in calculate hash");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);  // 8 KB buffer
        int bytesRead;

        while ((bytesRead = fileChannel.read(buffer)) != -1) {
            buffer.flip();
            digest.update(buffer);
            buffer.clear();
        }

        byte[] hashBytes = digest.digest();
        StringBuilder hashString = new StringBuilder();
        for (byte b : hashBytes) {
            hashString.append(String.format("%02x", b));
        }
        //System.out.println("At line 75  in calculate hash "+hashString.toString());
        return hashString.toString();
    }
}
