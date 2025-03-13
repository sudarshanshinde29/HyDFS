package org.example.entities;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestQueue {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    public void addRequest(Runnable request) {
        queue.add(request);
    }

    public Runnable takeRequest() throws InterruptedException {
        return queue.take();
    }
}
