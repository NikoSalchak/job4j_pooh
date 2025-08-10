package ru.job4j.pooh;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TopicSchema implements Schema {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Receiver>> receivers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> data = new ConcurrentHashMap<>();
    private final Condition condition = new Condition();

    @Override
    public void addReceiver(Receiver receiver) {
        receivers.putIfAbsent(receiver.name(), new CopyOnWriteArrayList<>());
        receivers.get(receiver.name()).add(receiver);
        condition.on();
    }

    @Override
    public void publish(Message message) {
        data.putIfAbsent(message.name(), new CopyOnWriteArrayList<>());
        data.get(message.name()).add(message.text());
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (String receiverName : receivers.keySet()) {
                CopyOnWriteArrayList<String> dataList = data.getOrDefault(receiverName, new CopyOnWriteArrayList<>());
                CopyOnWriteArrayList<Receiver> receiverList = receivers.get(receiverName);
                Iterator<Receiver> iterator = receiverList.iterator();
                while (iterator.hasNext()) {
                    String data = null;
                    int counter = 0;
                    Receiver receiver = iterator.next();
                    while (counter < dataList.size()) {
                        data = dataList.get(counter);
                        receiver.receive(data);
                        counter++;
                    }
                    if (data == null) {
                        break;
                    }
                }
                dataList.clear();
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
