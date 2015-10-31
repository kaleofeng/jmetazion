package com.metazion.jm.task;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Task {

	public static final int SUCCESS = 1; // 成功
	public static final int FAILED = 0; // 失败
	public static final int TIMEOUT = -1; // 超时

	protected int seq = 0; // 序列号

	protected AtomicInteger desire = new AtomicInteger(0); // 引用记数
	protected volatile int result = 0; // 执行结果

	protected long timeout = 10 * 1000; // 超时时间（ms）

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int incDesire() {
		return desire.incrementAndGet();
	}

	public int incDesire(int number) {
		return desire.addAndGet(number);
	}

	public int decDesire() {
		return desire.decrementAndGet();
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public boolean isFinished() {
		return desire.get() < 1;
	}

	public boolean isTimeout() {
		return timeout <= 0;
	}

	public void tick(long milliseconds) {
		timeout -= milliseconds;
		timeout = timeout > 0 ? timeout : 0;
	}

	public abstract void execute();

	public abstract void onFinish();

	public abstract void onTimeout();
}