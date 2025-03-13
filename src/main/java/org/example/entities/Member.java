package org.example.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This is the member entity class
 */
public class Member {
    int id;
    String name;
    String ipAddress;
    String port;
    String versionNo;
    String status;
    String dateTime;
    String incarnationNo;

    public Member(int id, String name, String ipAddress, String port, String versionNo, String status, String dateTime, String incarnationNo) {
        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        this.versionNo = versionNo;
        this.status = status;
        this.dateTime = dateTime;
        this.incarnationNo = incarnationNo;
    }

    public static String getLocalDateTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:nnnn");
        return LocalDateTime.now().format(formatter);
    }

    public static LocalDateTime getTimeFromString(String dateTimeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:nnnn");
        return LocalDateTime.parse(dateTimeString, formatter);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    public String getIncarnationNo() {
        return incarnationNo;
    }

    public void setIncarnationNo(String incarnationNo) {
        this.incarnationNo = incarnationNo;
    }

}
