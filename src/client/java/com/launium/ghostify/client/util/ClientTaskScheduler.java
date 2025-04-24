package com.launium.ghostify.client.util;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.util.concurrent.PriorityBlockingQueue;

public class ClientTaskScheduler {
    public static final PriorityBlockingQueue<AbstractTask> CLIENT_TASKS = new PriorityBlockingQueue<>();

    public abstract static class AbstractTask implements Comparable<AbstractTask> {

        public long scheduledTimeMs;

        public abstract void execute(Minecraft client);

        public AbstractTask(long scheduled) {
            this.scheduledTimeMs = scheduled;
        }

        @Override
        public int compareTo(ClientTaskScheduler.AbstractTask another) {
            // reversed order to prioritize tasks with earlier scheduled time
            // the higher the time value, the lower the priority
            return Long.compare(another.scheduledTimeMs, scheduledTimeMs);
        }

    }

    public static void whenClientStartTick(Minecraft client) {
        long now = Util.getMillis();
        AbstractTask task;
        while ((task = CLIENT_TASKS.peek()) != null && task.scheduledTimeMs < now) {
            CLIENT_TASKS.poll();
            task.execute(client);
        }
    }
}
