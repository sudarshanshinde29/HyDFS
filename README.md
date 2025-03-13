# Hybrid Distributed File System (hyDFS)

---

## Overview
The Hybrid Distributed File System (hyDFS) is a robust, scalable, and efficient distributed storage solution implemented. It leverages distributed hash-based file assignment, coordinator-based updates, push-based replication, and client-side caching to achieve high availability and consistency.

## System Components

### 1. **Membership & Ring Formation**
- We utilized the Membership protocol, assigning ring IDs to each member by hashing the ASCII values of the machine name.
- The same hash function is used to allocate files to specific ring IDs, ensuring an even distribution of storage.

### 2. **File Transfer Mechanism**
- A dedicated file transfer service handles all incoming and outgoing files.
- Each transfer request is preceded by a metadata JSON file indicating file type (hyDFS file, upload, append request, or retrieval request).
- A queue-based mechanism ensures smooth and efficient file transfers.

### 3. **Coordinator-Based Approach**
- The node storing the original file acts as the **coordinator**.
- All file update requests are directed to the coordinator, ensuring a single point of truth.

### 4. **Push-Based Replication Process**
- **Node Failure Handling:**
  - If a successor fails, its predecessor pushes replicated files to the next two successors.
  - If a predecessor fails, the first successor claims the replicas and propagates them forward.
- **Node Rejoining Handling:**
  - The node receives missing files from its predecessors.
  - The successor recalculates file assignments and updates ownership lists accordingly.

### 5. **Ensuring Eventual Consistency**
- The file owner verifies file consistency periodically by merging recent updates.
- Logs are maintained for each file and checked against replicas for inconsistencies.
- If inconsistencies are detected, the owner propagates the latest file version to replica nodes.

### 6. **Client-Side File Caching**
- Implemented an **LRU-based caching policy** to improve file read performance.
- Cache size is managed dynamically, evicting the least recently used files.
- Cached files are validated periodically against the owner's hash to ensure consistency.

### 7. **Parallel Service Execution**
- Multiple receiver services run on different ports and threads.
- Used MP1 code for debugging on virtual machines while enhancing MP2 functionality.

---

## Performance Analysis

### 1. **Replication Performance**
- Replication time increases with file size.
- Bandwidth usage scales proportionally to file size, but transfer speeds remain high (~1 sec for 100MB files).

### 2. **Merge Performance**
- Appending a **4KB file** is fastest with one concurrent client, but performance degrades with multiple clients.
- For a **40KB append**, the longest time is recorded with a single client, while multiple clients show steady performance.

### 3. **Cache Performance**
- Read latency decreases as cache size increases.
- A **Zipfian workload** exhibits lower latency than a **uniform workload**.
- Caching reduces latency by **50%** compared to non-cached reads.

### 4. **Cache Performance with Appends**
- With a **90% read and 10% append** workload, caching significantly lowers read latency across both workload distributions.

---

## Installation Instructions

1. Run `run.bat` from your local machine.
   - Edit `hosts`, `ips`, `ports1`, `names`, and `VM_USER` parameters.
   - Modify Git user configurations before running:
     ```bash
     git config user.name 'user netid'
     git config user.email 'User email id'
     ```

2. SSH into all 10 machines.

3. On the machine designated as the **Introducer**, navigate to the repository folder and modify `application.properties`:
   ```bash
   nano application.properties
   ```
   - Set `isIntroducer=true`.

4. Place input files in the `Hybrid-Distributed-File-System/input/` folder.

5. On each machine, navigate to the repository folder and run the system using:
   ```bash
   java -jar mp1-1.jar
   ```

6. Join the node using the command:
   ```bash
   join
   ```

7. Once nodes are part of the network, execute file system commands such as:
   ```bash
   create input/test_file_1MB.txt test_file_1MB.txt
   ```

---

## Conclusion
The **Hybrid Distributed File System (hyDFS)** efficiently manages distributed storage, ensuring **fault tolerance, consistency, and scalability**. With features such as **coordinator-based updates, push-based replication, and LRU caching**, the system achieves **high availability and optimal performance** under varying workloads.

