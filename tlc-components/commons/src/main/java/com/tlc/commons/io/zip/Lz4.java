package com.tlc.commons.io.zip;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.util.Arrays;

/**
 * @author Abishek
 * @version 1.0
 */
public class Lz4 implements Compressor
{
    private final LZ4Compressor compressor;
    private final LZ4FastDecompressor decompressor;
    public Lz4()
    {
        final LZ4Factory factory = LZ4Factory.fastestInstance();
        this.compressor = factory.fastCompressor();
        this.decompressor = factory.fastDecompressor();
    }
    @Override
    public byte[] compress(byte[] data)
    {
        final int maxCompressedLength = compressor.maxCompressedLength(data.length);
        final byte[] compressed = new byte[maxCompressedLength];
        final int size = compressor.compress(data, compressed);
        return Arrays.copyOfRange(compressed, 0, size);
    }

    @Override
    public byte[] deCompress(byte[] data, int orgLength)
    {
        final byte[] restored = new byte[orgLength];
        decompressor.decompress(data, 0, restored, 0, orgLength);
        return restored;
    }
}
