package com.metazion.jm.task;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {

	private ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<Task>();
	private ConcurrentHashMap<Integer, Task> taskMap = new ConcurrentHashMap<Integer, Task>();
	private AtomicInteger nextSeq = new AtomicInteger(10000);

	public void tick(long milliseconds) {
		while (!taskQueue.isEmpty()) {
			Task task = taskQueue.poll();
			task.execute();

			putTask(task);
		}

		Iterator<Map.Entry<Integer, Task>> iterator = taskMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Task> entry = iterator.next();
			Task task = entry.getValue();
			task.tick(milliseconds);

			if (task.isFinished()) {
				task.onFinish();
				iterator.remove();
				continue;
			}

			if (task.isTimeout()) {
				task.onTimeout();
				iterator.remove();
				continue;
			}
		}
	}

	public void pushTask(Task task) {
		final int seq = getNextSeq();
		task.setSeq(seq);
		taskQueue.add(task);
	}

	public int getWaitingTaskSize() {
		return taskQueue.size();
	}

	public int getExecutingTaskSize() {
		return taskMap.size();
	}

	private int getNextSeq() {
		return nextSeq.incrementAndGet();
	}

	private void putTask(Task task) {
		taskMap.put(task.getSeq(), task);
	}
}