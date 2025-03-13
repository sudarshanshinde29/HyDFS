package org.example.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.FileSystem.HashFunction;
import org.example.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.MemoryManagerMXBean;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * This is the Membershiplist  entity class
 */
public class MembershipList {

    //For Failure Detection
    public static ConcurrentSkipListMap<String, Member> members = new ConcurrentSkipListMap<>();
    public static ConcurrentSkipListMap<Integer, Member> memberslist = new ConcurrentSkipListMap<>();
    public static List<String> memberNames = new CopyOnWriteArrayList<>();
    public static List<Integer> failedNodes = new CopyOnWriteArrayList<>();
//    public static Set<String> memberNames = new ConcurrentSkipListSet<>();;
    public static int selfId = HashFunction.hash(String.valueOf(FDProperties.getFDProperties().get("machineName")));
    public static int pointer;
    private static final Logger logger = LoggerFactory.getLogger(MembershipList.class);

    public static void addMember(Member member) {
        members.put(member.getName(), member);
        memberslist.put(member.getId(), member);
        if(!memberNames.contains(member.getName()))
            memberNames.add(member.getName());
    }

    public static void addItself(Member member) {
        memberslist.put(member.getId(), member);
    }

    public static void removeMember(String name) {
       // memberslist.remove(members.get(name).getId());
        members.remove(name);
        memberNames.remove(name);
    }

    public static void RemoveFromMembersList(int memberId){
        memberslist.remove(memberId);
    }
    public static void printMembers() {
//        System.out.println("Printing members at :" + FDProperties.getFDProperties().get("machineName"));
        members.forEach((k, v) -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String json = mapper.writeValueAsString(v);
                logger.info(k + ": " + json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public static void printMembersId() {
        System.out.println("Printing members at : " + FDProperties.getFDProperties().get("machineName"));
        members.forEach((k, v) -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String json = mapper.writeValueAsString(v);
                System.out.println(v.getId() + " " +  v.getName() + "_" + v.getIpAddress() + "_" + v.getPort() + "_" + v.getVersionNo() + "_" + v.getIncarnationNo());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static List<Member> getSuspectedMembers() {
        List<Member> suspectedMembers = new ArrayList<>();
        members.forEach((k, v) -> {
            if(v.getStatus().equals("Suspected")) {
                suspectedMembers.add(v);
            }
        });
        return suspectedMembers;
    }

    public static void generateRandomList() {
        pointer = 0;
        Collections.shuffle(memberNames);
    }

    public static Member getRandomMember() {
        Member member = members.get(memberNames.get(pointer));
        pointer++;
        return member;
    }

    public static Boolean isLast(){
//        logger.debug(pointer + " " + memberNames.size() + " " + members.size());
        return pointer < memberNames.size();
    }

    public static List<Member> getKRandomMember(int k, String targetNode) {
        List<Member> memberList = new ArrayList<>();
        // Check if map is not empty
        if (!memberNames.isEmpty()) {
            // Get a random key and the corresponding value
            while (memberList.size() < k && memberList.size() < memberNames.size()) {
                //TODO now send ping to other k nodes
                Member member = members.get(memberNames.get(ThreadLocalRandom.current().nextInt(memberNames.size())));
                if (!memberList.contains(member) && targetNode.equals(member.getName())) {
                    Member newMember = new Member(member.getId(),
                            member.getName(),
                            member.getIpAddress(),
                            member.getPort(),
                            member.getVersionNo(),
                            member.getStatus(),
                            member.getDateTime(),
                            member.getIncarnationNo()
                    );
                    memberList.add(newMember);
                }
            }
        } else {
            logger.info("Map is empty.");
        }
        return memberList;
    }

    public static List<Member> getNextMembers(int id){
        //TODO MP3 change member to memberlist
        List<Member> sortedMembers = memberslist.values().stream()
                .sorted(Comparator.comparingInt(Member::getId))
                .collect(Collectors.toList());
        // Find the starting index where Member.id >= given id
        int startIndex = 0;
        for (int i = 0; i < sortedMembers.size(); i++) {
            if (sortedMembers.get(i).getId() >= id) {
                startIndex = i;
                break;
            }
        }
        // Collect the next two members in a circular fashion
        List<Member> result = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            int index = (startIndex + i) % sortedMembers.size();
            result.add(sortedMembers.get(index));
        }
        return result;
    }

    public static Member getMemberById(int id) {
        Member member = memberslist.get(id);

        if (member != null) {
            return member;
        } else {
            // Get the next entry if the requested id is not found
            Integer nextKey = memberslist.higherKey(id);
            if (nextKey != null) {
                return memberslist.get(nextKey);
            } else {
                // If there's no "next" key, wrap around and return the first entry
                return memberslist.firstEntry().getValue();
            }
        }
    }


}
