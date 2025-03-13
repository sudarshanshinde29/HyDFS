package org.example.entities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUFileCache {

    // Singleton instance of FileCache
    public static final FileCache FILE_CACHE = new FileCache(100 * 1024 * 1024);

    public static class FileCache {
        private long maxSize;
        private long currentSize;
        private final LinkedHashMap<String, FileCacheData> cacheMap;
        private final long EXPIRY_TIME_MILLIS = 10000;

        private FileCache(long maxSize) {
            this.maxSize = maxSize;
            this.currentSize = 0;
            this.cacheMap = new LinkedHashMap<>(1000, 0.75f, true);
        }

        public void addMaxSize() {
            this.maxSize = Long.parseLong(String.valueOf(FDProperties.getFDProperties().get("cacheSize")));
        }

        public void addFile(String fileName, long fileSize, String filePath) {
            if (fileSize > maxSize) {
                System.out.println("File size exceeds cache size. Cannot add file to cache.");
                return;
            }

            while (currentSize + fileSize > maxSize) {
                removeOldestFile();
            }

            FileCacheData metadata = new FileCacheData(fileName, fileSize, filePath);
            cacheMap.put(fileName, metadata);
            currentSize += fileSize;
        }

        public FileCacheData getFile(String fileName) {
            FileCacheData metadata = cacheMap.get(fileName);
            if (metadata != null) {
                metadata.updateLastAccessed();
                cacheMap.put(fileName, metadata); // Reinsert to update order
            }
            return metadata;
        }

        private void removeOldestFile() {
            Iterator<Map.Entry<String, FileCacheData>> iterator = cacheMap.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, FileCacheData> oldestEntry = iterator.next();
                currentSize -= oldestEntry.getValue().getFileSize();
                iterator.remove();
                System.out.println("Removed file from cache: " + oldestEntry.getKey());
            }
        }

        public long getCurrentSize() {
            return currentSize;
        }

        public boolean isFileInCache(String fileName) {
            return cacheMap.containsKey(fileName);
        }

        public boolean isFileOlder(String fileName) {
            FileCacheData metadata = cacheMap.get(fileName);
            if (metadata == null) {
                return false; // File not in cache
            }
            long currentTime = System.currentTimeMillis();
            long fileTime = metadata.getLastAccessed().getTime();
            return (currentTime - fileTime) > EXPIRY_TIME_MILLIS;
        }

        public void printCache() {
            for (FileCacheData metadata : cacheMap.values()) {
                System.out.println(metadata.getFileName() + " (Size: " + metadata.getFileSize() +
                        ", Last Accessed: " + metadata.getLastAccessed() + ")");
            }
        }
    }
}