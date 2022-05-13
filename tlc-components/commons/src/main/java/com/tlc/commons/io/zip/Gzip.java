package com.tlc.commons.io.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * @author Abishek
 * @version 1.0
 */
public class Gzip implements Compressor
{
    @Override
    public byte[] compress(byte[] data) throws Exception
    {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length / 2);
        try(GZIPOutputStream zipStream = new GZIPOutputStream(outputStream))
        {
            zipStream.write(data);
            zipStream.finish();
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] deCompress(byte[] data, int orgLength) throws Exception
    {
        try(GZIPInputStream zipStream = new GZIPInputStream(new ByteArrayInputStream(data), orgLength))
        {
            return zipStream.readAllBytes();
        }
    }
}
