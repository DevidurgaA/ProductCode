package com.tlc.commons.util;

import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;


/**
 * @author Abishek
 * @version 1.0
 */
public class RandomIdGenerator
{
    private static final RandomIdGenerator INSTANCE;
    static
    {
        INSTANCE = new RandomIdGenerator();
    }

    public static RandomIdGenerator getInstance()
    {
        return INSTANCE;
    }

    private final char[] CHARS = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                                            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                                            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
                                            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                                            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
                                            'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final SecureRandom secureRandom;
    private RandomIdGenerator()
    {
        this.secureRandom = new SecureRandom();
    }

    public String generateUniqueId()
    {
        return UUID.randomUUID().toString();
    }

    public BigInteger generateUniqueBigInteger(int bitLength)
    {
        return new BigInteger(bitLength, secureRandom);
    }

    public String generateUniqueHex(int length)
    {
        final byte[] data = new byte[length * 4];
        secureRandom.nextBytes(data);
        return Hex.encodeHexString(data, true);
    }

    public String generateUniqueASCII(int length)
    {
        final byte[] data = new byte[length * 4];
        secureRandom.nextBytes(data);
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        final StringBuilder builder = new StringBuilder(length);
        while(byteBuffer.hasRemaining())
        {
            final int index = byteBuffer.getInt() % CHARS.length;
            builder.append(CHARS[Math.abs(index)]);
        }
        return builder.toString();
    }

    public String generateUniqueId(Long serverId, String... params)
    {
        final String uuid = generateUniqueId();
        final StringBuilder builder = new StringBuilder();
        builder.append(serverId).append("_").append(uuid);
        if(params != null && params.length > 0)
        {
            for(String param : params)
            {
                builder.append(param).append("_");
            }
        }
        return builder.append(uuid).toString();
    }

}
