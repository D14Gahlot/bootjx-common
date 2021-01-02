package com.boot.jx.connectors;

import java.util.LinkedList;
import java.util.List;

import com.boot.jx.postman.model.Message;

public class MessageQueue {

	private List<Message<?>> queue = new LinkedList<Message<?>>();
	private int limit = 10;

	public MessageQueue(int limit) {
		this.limit = limit;
	}

	public synchronized void enqueue(Message<?> item)
			throws InterruptedException {
		while (this.queue.size() == this.limit) {
			wait();
		}
		this.queue.add(item);
		if (this.queue.size() == 1) {
			notifyAll();
		}
	}

	public synchronized Message<?> dequeue()
			throws InterruptedException {
		while (this.queue.size() == 0) {
			wait();
		}
		if (this.queue.size() == this.limit) {
			notifyAll();
		}

		return this.queue.remove(0);
	}
}
