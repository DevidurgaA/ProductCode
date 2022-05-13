package com.tlc.web;

import java.io.IOException;


/**
 * @author Abishek
 * @version 1.0
 */
public interface RequestReceiver
{
	byte[] getBody() throws IOException;

	void readBody(FullBytesCallback callback);

	void readBody(PartialBytesCallback callback);
}
