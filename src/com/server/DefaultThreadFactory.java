package com.server;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public DefaultThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public Thread newThread(Runnable runnable) {
        String threadName = namePrefix + " thread-" + threadNumber.getAndIncrement();
        return new Thread(runnable, threadName);
    }
}