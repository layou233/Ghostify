package com.launium.ghostify.client.util;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.util.PriorityQueue;

public class ClientTaskScheduler {
    public static final PriorityQueue<AbstractTask> CLIENT_TASKS = new PriorityQueue<>();

    public abstract static class AbstractTask implements Comparable<AbstractTask> {

        public long scheduledTimeMs;

        public abstract void execute(Minecraft client);

        public AbstractTask(long scheduled) {
            this.scheduledTimeMs = scheduled;
        }

        @Override
        public int compareTo(ClientTaskScheduler.AbstractTask another) {
            // prioritize tasks with earlier scheduled time
            return Long.compare(scheduledTimeMs, another.scheduledTimeMs);
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
