package org.example.FileSystem;

import org.example.entities.FDProperties;
import org.example.entities.FileData;
import org.example.entities.Member;
import org.example.entities.MembershipList;
import org.example.service.Log.LogServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLOutput;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Convergence extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Convergence.class);
    int checkMergeCounter = 0;
    public void run() {
        while (true) {
            try {
                Sender s = new Sender();
                if(Boolean.parseBoolean(String.valueOf(FDProperties.getFDProperties().get("mergeReq")))){
                    System.out.println("Executing MergeReq");
                    if(checkMergeCounter > 6) {
                        List<String> ownedFiles = FileData.getOwnedFiles();
                        s.updateReplicas(ownedFiles);
                        checkMergeCounter = 0;
                    }
                }
                List<Integer> sortedKeys = new CopyOnWriteArrayList<>(MembershipList.memberslist.keySet());
                if(MembershipList.failedNodes.size()>0){
                    System.out.println("Some Nodes Failed");
                }
                for (int key : MembershipList.failedNodes) {
                    MembershipList.RemoveFromMembersList(key);
                }
                List<String> ownedFiles = FileData.getOwnedFiles();
                for (Integer failedNodeId : MembershipList.failedNodes) {
                    System.out.println("Starting re-replication of files of node " +failedNodeId+" Current Time:- "+ Member.getLocalDateTime());
                    String status = checkPredecessorOrSuccessor(failedNodeId, MembershipList.selfId,sortedKeys);
                    switch (status) {
                        case "Successor1":
                            System.out.println("Failed Node was successor1 for my node.  my node id is" + MembershipList.selfId);
                            if(!ownedFiles.isEmpty())
                                s.updateReplicas(ownedFiles);
                            break;
                        case "Successor2":
                            System.out.println("Failed Node was successor2 for my node.  my node id is" + MembershipList.selfId);
                            if(!ownedFiles.isEmpty())
                                s.updateReplicas(ownedFiles);
                            break;

                        case "Predecessor":
                            System.out.println("Failed Node was Predecessor for my node.  my node id is" + MembershipList.selfId);
                            List<String> replicaFilesOfFailedNode = FileData.getAndRemoveReplicasOfANode(failedNodeId);
                            FileData.addOwnedFiles(replicaFilesOfFailedNode);
                            List<String> updatedownedFiles = FileData.getOwnedFiles();
                            if(!updatedownedFiles.isEmpty())
                                s.updateReplicas(updatedownedFiles);
                            break;
                        default:
                            break;
                    }
                }
                MembershipList.failedNodes.clear();
                checkMergeCounter++;
//System.out.println("re-replication process completed");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }



    }


    private String checkPredecessorOrSuccessor(int target, int current, List<Integer>sortedKeys) {
        int size = sortedKeys.size();
        int currentIndex = sortedKeys.indexOf(current);


        if (currentIndex == -1) {
            return "NotFound";
        }


        int successor1Index = (currentIndex + 1) % size;
        int successor2Index = (currentIndex + 2) % size;
        int predecessorIndex = (currentIndex - 1 + size) % size;

        if (sortedKeys.get(successor1Index).equals(target)) {
            return "Successor1";
        }


        if (sortedKeys.get(successor2Index).equals(target)) {
            return "Successor2";
        }

        if (sortedKeys.get(predecessorIndex).equals(target)) {
            return "Predecessor";
        }
        return "NotFound";
    }
}
