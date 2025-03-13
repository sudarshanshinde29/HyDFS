package org.example.service;

import org.example.entities.FileTransferManager;

public class FileSystem extends Thread {
    public static void main(String[] args) {
        FileTransferManager manager = new FileTransferManager();
//        Receiver receiver = new Receiver(8080, manager);
//        Sender sender = new Sender(manager);
//
//        // Start the receiver thread
//        receiver.start();
//
//        // Example of adding a send task
//        sender.sendFile("example.txt", "127.0.0.1", 8081);

        // Processing tasks from queue
        System.out.println("Starting File Sender Thread");
        while (true) {
            try {
                Runnable request = manager.getRequestQueue().takeRequest();
                new Thread(request).start();  // Start each task as a new thread
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        FileTransferManager manager = new FileTransferManager();
        while (true) {
            try {
                Runnable request = manager.getRequestQueue().takeRequest();
//                new Thread(request).start();  // Start each task as a new thread
                request.run(); // will start a task and wait till it gets end

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
