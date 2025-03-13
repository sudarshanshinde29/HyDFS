package org.example.entities;

import java.sql.Timestamp;

public class FileCacheData {
    private String fileName;
    private long fileSize;
    private Timestamp lastAccessed;
    private String filePath;

    public FileCacheData(String fileName, long fileSize, String filePath) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.lastAccessed = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getLastAccessed() {
        return lastAccessed;
    }

    public void updateLastAccessed() {
        this.lastAccessed = new Timestamp(System.currentTimeMillis()); // Update with current time
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

}
