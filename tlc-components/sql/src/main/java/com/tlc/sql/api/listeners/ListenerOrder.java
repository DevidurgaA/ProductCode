package com.tlc.sql.api.listeners;

/**
 * @author Abishek
 * @version 1.0
 */
public interface ListenerOrder extends Comparable<ListenerOrder.Priority>
{
    enum Priority
    {
        HIGH_PRIORITY(0), REGISTRY_PRIORITY(25), MEDIUM_PRIORITY(50), LOW_PRIORITY(100);
        private final int value;
        Priority(int value)
        {
            this.value = value;
        }
    }

    default Priority getPriority()
    {
        return Priority.MEDIUM_PRIORITY;
    }

    @Override
    default int compareTo(Priority remote)
    {
        return Integer.compare(getPriority().value, remote.value);
    }
}
