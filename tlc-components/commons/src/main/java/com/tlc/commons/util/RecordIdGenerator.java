package com.tlc.commons.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Abishek
 * @version 1.0
 */
public class RecordIdGenerator
{
    private final AtomicLong counter;
    private final String uniqueId;

    private RecordIdGenerator(int uniqueId)
    {
        this.counter = new AtomicLong(0);
        this.uniqueId = Integer.toString(uniqueId);
    }

    public String nextId()
    {
        final long count = counter.incrementAndGet();
        final StringBuilder intBuilder = new StringBuilder(32).append(uniqueId);
        if(count < Short.MAX_VALUE)
        {
            intBuilder.append("_0000");
        }
        else if(count < Integer.MAX_VALUE)
        {
            intBuilder.append("_0000-0000");
        }
        else
        {
            intBuilder.append("_0000-0000-0000-0000");
        }
        final String hex = Long.toHexString(count);
        final int length = hex.length();
        int pos = intBuilder.length();
        int dec = 1;
        for(int index = length-1; index >= 0; index--)
        {
            intBuilder.setCharAt(pos-dec, hex.charAt(index));
            if(++dec % 5 == 0)
            {
                ++dec;
            }
        }
        return intBuilder.toString();
    }
}
