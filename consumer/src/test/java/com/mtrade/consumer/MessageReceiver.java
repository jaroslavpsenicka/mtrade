package com.mtrade.consumer;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Component
public class MessageReceiver {

    private BlockingQueue<Message> receivedMessages = new ArrayBlockingQueue<Message>(2);

    public void receive(Message message) throws InterruptedException {
        receivedMessages.put(message);
    }

    public Message getLastMessage() throws InterruptedException {
        return receivedMessages.poll(10, TimeUnit.SECONDS);
    }
}
