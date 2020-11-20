package com.metazion.jm.task;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Task {

    public static final int STATE_NORMAL = 1;
    public static final int STATE_DESIRE = 2;
    public static final int STATE_COMPLETE = 3;

    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAILED = 0;
    public static final int RESULT_TIMEOUT = -1;

    protected long seq = 0;
    protected int state = STATE_NORMAL;
    protected long duration = 30 * 1000;

    private AtomicInteger desire = new AtomicInteger(0);
    private AtomicInteger result = new AtomicInteger(0);

    public long getSeq() {
        return seq;
    }

    public void setSeq(long value) {
        seq = value;
    }

    public int incDesire(int number) {
        return desire.addAndGet(number);
    }

    public int decDesire() {
        return desire.decrementAndGet();
    }

    public int getResult() {
        return result.get();
    }

    public void setResult(int value) {
        result.set(value);
    }

    public boolean isComplete() {
        return state == STATE_COMPLETE;
    }

    public void tick(long interval) {
        duration -= interval;

        switch (state) {
            case STATE_NORMAL:
                normalTick();
                break;
            case STATE_DESIRE:
                desireTick();
                break;
            default:
                break;
        }
    }

    public abstract void execute();

    public abstract void finish();

    public abstract void destroy();

    private void normalTick() {
        if (desire.get() <= 0) {
            finish();
            state = STATE_COMPLETE;
        }
        else if (duration <= 0) {
            setResult(RESULT_TIMEOUT);
            finish();
            state = STATE_DESIRE;
        }
    }

    private void desireTick() {
        if (desire.get() <= 0) {
            state = STATE_COMPLETE;
        }
    }
}
