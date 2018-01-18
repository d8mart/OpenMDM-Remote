package com.openmdmremote.nativ.net;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageDispatcher {
    private final String LOGTAG = "WIPC MessageDispacher";

    private BlockingQueue<WIPCProto.Message> msgQueueToNavtie = new ArrayBlockingQueue<WIPCProto.Message>(20);
    private BlockingQueue<byte[]> imageQueue = new ArrayBlockingQueue<byte[]>(2);

    public MessageDispatcher() {
    }

    public void addMesageToBackend(WIPCProto.Message msg) {
        try {
            msgQueueToNavtie.add(msg);
        } catch (IllegalStateException e){
        }
    }

    public void addImage(byte[] img) {
        try {
            imageQueue.add(img);
        } catch (IllegalStateException e) {
        }
    }

    public WIPCProto.Message takeToTheNative() throws InterruptedException {
        // Block until it get msg.
        return msgQueueToNavtie.take();
    }

    public byte[] pollImage() throws InterruptedException {
        return imageQueue.poll(5, TimeUnit.SECONDS);
    }
}
