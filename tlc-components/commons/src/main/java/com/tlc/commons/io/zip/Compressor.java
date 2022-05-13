package com.tlc.commons.io.zip;


/**
 * @author Abishek
 * @version 1.0
 */
public interface Compressor
{
    byte[] compress(byte[] data) throws Exception;

    byte[] deCompress(byte[] data, int orgLength) throws Exception;

    static Compressor gzip()
    {
        return new Gzip();
    }

    static Compressor lz4()
    {
        return new Lz4();
    }

    static Compressor deflate(int maxObjects, int maxIdle)
    {
        return new Deflate(maxObjects, maxIdle);
    }
}
