package com.metazion.jm.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class TaskManager {

    private AtomicLong nextSeq = new AtomicLong(0);
    private ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<>();
    private HashMap<Long, Task> taskMap = new HashMap<>();

    public void tick(long interval) {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            if (task != null) {
                task.execute();
                putTask(task);
            }
        }

        Iterator<Map.Entry<Long, Task>> iterator = taskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Task> entry = iterator.next();
            Task task = entry.getValue();
            if (task != null) {
                task.tick(interval);

                if (task.isComplete()) {
                    task.destroy();
                    iterator.remove();;
                }
            }
        }
    }

    public void pushTask(Task task) {
        final long seq = getNextSeq();
        task.setSeq(seq);
        taskQueue.add(task);
    }

    public int getExecutingTaskSize() {
        return taskQueue.size();
    }

    public int getFinishingTaskSize() {
        return taskMap.size();
    }

    private long getNextSeq() {
        return nextSeq.incrementAndGet();
    }

    private void putTask(Task task) {
        taskMap.put(task.getSeq(), task);
    }
}
