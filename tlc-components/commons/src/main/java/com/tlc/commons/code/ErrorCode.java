package com.tlc.commons.code;

import com.tlc.commons.code.impl.DefaultErrorCode;
import com.tlc.commons.code.impl.StackLessErrorCode;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class ErrorCode extends RuntimeException
{
	protected ErrorCode()
	{
	}

	protected ErrorCode(String message)
	{
		super(message);
	}
	protected ErrorCode(String message, Throwable cause)
	{
		super(message, cause);
	}

	public static ErrorCode get(ErrorCodeProvider codeProvider)
	{
		return new DefaultErrorCode(codeProvider);
	}

	public static ErrorCode get(ErrorCodeProvider codeProvider, String message)
	{
		return new DefaultErrorCode(codeProvider, message);
	}

	public static ErrorCode get(ErrorCodeProvider codeProvider, Throwable cause)
	{
		return new DefaultErrorCode(codeProvider, cause);
	}

	public static ErrorCode get(ErrorCodeProvider codeProvider, String message, Throwable cause)
	{
		return new DefaultErrorCode(codeProvider, message, cause);
	}

	public static ErrorCode getLite(ErrorCodeProvider codeProvider)
	{
		return new StackLessErrorCode(codeProvider);
	}

	public static ErrorCode getLite(ErrorCodeProvider codeProvider, Throwable cause)
	{
		return new StackLessErrorCode(codeProvider, cause);
	}

	public static ErrorCode getLite(ErrorCodeProvider codeProvider, String message)
	{
		return new StackLessErrorCode(codeProvider, message);
	}

	public static ErrorCode getLite(ErrorCodeProvider codeProvider, String message, Throwable cause)
	{
		return new StackLessErrorCode(codeProvider, message, cause);
	}

	public abstract long getCode();

	public abstract ErrorCodeProvider getProvider();

	public abstract boolean hasStackTrace();
}
