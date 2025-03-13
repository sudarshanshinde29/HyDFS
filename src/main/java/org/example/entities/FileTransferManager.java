package org.example.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public class FileTransferManager {
    private static final RequestQueue requestQueue = new RequestQueue();
    private static final List<String> eventLog = new ArrayList<>();
    private static ConcurrentSkipListMap<String, List<String >> fileOperations = new ConcurrentSkipListMap<>();

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public static synchronized void logEvent(String event) {
        eventLog.add(event);
        if(event.contains("File Operation")) {
            String[] parts = event.split(" : ");

            if (parts.length < 4) {
                System.out.println("Invalid format");
                return;
            }

            String operationType = parts[1];
            String status = parts[2];
            String fileName = parts[3];
            if (status.equals("Successful")) {
                if (fileOperations.containsKey(fileName)) {
                    fileOperations.get(fileName).add(event);
                } else {
                    fileOperations.put(fileName, new ArrayList<>());
                    fileOperations.get(fileName).add(event);
                }
            }
        }
        System.out.println("Event: " + event);
    }

    public static List<String> getEventLog() {
        return eventLog;
    }

    public static List<String> getFileOperations(String fileName) {
        return fileOperations.get(fileName);
    }

    public static ConcurrentSkipListMap<String, List<String>> getFileOperations() {
        return fileOperations;
    }

    //TODO compare two file operations
    public static boolean compareLogs( List<String> originalLogs,List<String> replicaLogs){
        int i=0;
        int j=0;
        while(i<originalLogs.size() && j<replicaLogs.size()){
            String originalString =  getOperationFromOriginal(originalLogs.get(i));
            String replicaString = getOperationFromReplicaLog(replicaLogs.get(j));

            if(!originalString.equals(replicaString)) {
                return false;
            }

            if(originalString.equals("Upload") && replicaString.equals("Upload")) {
                break;
            }

            i++;
            j++;

        }

        return true;
    }

    private static String getOperationFromOriginal(String original){
        if(original!=null){
//            System.out.println("in compare logs "+original);
            String[] parts = original.split(" : ");
            return parts[1];
        }else{
            return null;
        }

    }

    private static String getOperationFromReplicaLog(String replica) {
        if (replica != null) {
            String[] parts = replica.split(" : ");
            return  parts[1].split(" ")[0];
        } else {
            return null;
        }
    }

}
