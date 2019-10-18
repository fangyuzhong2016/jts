/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io;

/**
 * Allows an array of bytes to be used as an {@link InStream}.
 * 为了优化存储器使用，实例可以具有不同的字节数组被重用。
 */
public class ByteArrayInStream
	implements InStream
{
	/*
	 * Implementation improvement suggested by Andrea Aime - Dec 15 2007
	 */
	
  private byte[] buffer;
  private int position;

	/**
	 * 创建基于给定缓冲区的新流。
	 * 
	 * @param buffer 读取字节
	 */
	public ByteArrayInStream(final byte[] buffer) {
		setBytes(buffer);
	}

	/**
	 * 设置此流从给定的缓冲区中读取
	 * 
	 * @param buffer 读取字节
	 */
	public void setBytes(final byte[] buffer) {
		this.buffer = buffer;
		this.position = 0;
	}

	/**
	 * 从所提供的输入流读取长度<code>buf.length</code>的字节流，存储到缓冲区中
	 * 
	 * @param buf the buffer to place the read bytes into
	 */
	@Override
	public void read(final byte[] buf) {
		int numToRead = buf.length;
		// don't try and copy past the end of the input
		if ((position + numToRead) > buffer.length) {
			numToRead = buffer.length - position;
			System.arraycopy(buffer, position, buf, 0, numToRead);
			// zero out the unread bytes
			for (int i = numToRead; i < buf.length; i++) {
				buf[i] = 0;
			}
		}
		else {
			System.arraycopy(buffer, position, buf, 0, numToRead);			
		}
		position += numToRead;
	}
}
