package ru.job4j.pooh;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueSchema implements Schema {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Receiver>> receivers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlockingQueue<String>> data = new ConcurrentHashMap<>();
    private final Condition condition = new Condition();

    @Override
    public void addReceiver(Receiver receiver) {
        receivers.putIfAbsent(receiver.name(), new CopyOnWriteArrayList<>());
        receivers.get(receiver.name()).add(receiver);
        condition.on();
    }

    @Override
    public void publish(Message message) {
        data.putIfAbsent(message.name(), new LinkedBlockingQueue<>());
        data.get(message.name()).add(message.text());
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (String queueKey : receivers.keySet()) {
                BlockingQueue<String> queue = data.getOrDefault(queueKey, new LinkedBlockingQueue<>());
                CopyOnWriteArrayList<Receiver> receiverByQueue = receivers.get(queueKey);
                Iterator<Receiver> it = receiverByQueue.iterator();
                while (it.hasNext()) {
                    String data = queue.poll();
                    if (data != null) {
                        it.next().receive(data);
                    }
                    if (data == null) {
                        break;
                    }
                    if (!it.hasNext()) {
                        it = receiverByQueue.iterator();
                    }
                }
            }
            condition.off();
            try {
                condition.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
