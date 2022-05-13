package com.tlc.web;

import java.io.OutputStream;


/**
 * @author Abishek
 * @version 1.0
 */
public interface ResponseSender
{
	OutputStream getOutputStream();

	void send(String contentType, byte[] bytes);
}
