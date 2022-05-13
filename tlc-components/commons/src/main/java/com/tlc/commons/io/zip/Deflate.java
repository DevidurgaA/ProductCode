package com.tlc.commons.io.zip;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * @author Abishek
 * @version 1.0
 */
public class Deflate implements Compressor
{
    private final ObjectPool<Holder<Deflater>> deflateObjectPool;
    private final ObjectPool<Inflater> inflateObjectPool;
    public Deflate(int maxObjects, int maxIdle)
    {
        final GenericObjectPoolConfig<Holder<Deflater>> deflateConfig = new GenericObjectPoolConfig<>();
        deflateConfig.setMaxIdle(maxIdle);
        deflateConfig.setMaxTotal(maxObjects);
        this.deflateObjectPool = new GenericObjectPool<>(new DeflaterObjectFactory(), deflateConfig);

        final GenericObjectPoolConfig<Inflater> inflateConfig = new GenericObjectPoolConfig<>();
        inflateConfig.setMaxIdle(maxIdle);
        inflateConfig.setMaxTotal(maxObjects);
        this.inflateObjectPool = new GenericObjectPool<>(new InflaterObjectFactory(), inflateConfig);
    }

    @Override
    public byte[] compress(byte[] data) throws Exception
    {
        final Holder<java.util.zip.Deflater> holder = deflateObjectPool.borrowObject();
        final java.util.zip.Deflater deflater = holder.obj;
        final byte[] buffer = holder.buffer;
        try
        {
            deflater.setInput(data);
            deflater.finish();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream( data.length/ 2);
            while (!deflater.finished())
            {
                final int length = deflater.deflate(buffer);
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
        finally
        {
            try
            {
                deflater.reset();
            }
            finally
            {
                deflateObjectPool.returnObject(holder);
            }
        }
    }

    @Override
    public byte[] deCompress(byte[] data, int orgLength) throws Exception
    {
        final Inflater inflater = inflateObjectPool.borrowObject();
        try
        {
            inflater.setInput(data);
            final byte[] result = new byte[orgLength];
            int offset = 0;
            while (!inflater.finished())
            {
                offset += inflater.inflate(result, offset, orgLength);
            }
            return result;
        }
        finally
        {
            try
            {
                inflater.reset();
            }
            finally
            {
                inflateObjectPool.returnObject(inflater);
            }
        }
    }

    private static class DeflaterObjectFactory extends BasePooledObjectFactory<Holder<Deflater>>
    {
        @Override
        public Holder<java.util.zip.Deflater> create()
        {
            //            deflater.setLevel(Deflater.BEST_COMPRESSION);
            return new Holder<>(new java.util.zip.Deflater(), new byte[4 * 1024]);
        }

        @Override
        public PooledObject<Holder<Deflater>> wrap(Holder<java.util.zip.Deflater> obj)
        {
            return new DefaultPooledObject<>(obj);
        }
    }

    private static class InflaterObjectFactory extends BasePooledObjectFactory<Inflater>
    {
        @Override
        public Inflater create()
        {
            //            deflater.setLevel(Deflater.BEST_COMPRESSION);
            return new Inflater();
        }

        @Override
        public PooledObject<Inflater> wrap(Inflater obj)
        {
            return new DefaultPooledObject<>(obj);
        }
    }

    private static class Holder<T>
    {
        private final T obj;
        private final byte[] buffer;

        private Holder(T obj, byte[] buffer)
        {
            this.obj = Objects.requireNonNull(obj);
            this.buffer = Objects.requireNonNull(buffer);
        }
    }
}
