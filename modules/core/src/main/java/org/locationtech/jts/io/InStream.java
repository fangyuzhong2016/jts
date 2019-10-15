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

import java.io.IOException;

/**
 * 一种用于提供字节的输入流类接口。
 * 此接口类似于Java的<code>InputStream</code>接口，但较窄的接口，使之更容易实现。
 *
 */
public interface InStream
{
  /**
   * 从所提供的输入流读取长度<code>buf.length</code>的字节流，存储到缓冲区中
   * @param buf 接收字节的缓冲区
   *
   * @throws IOException 如果发生I/O错误
   */
  void read(byte[] buf) throws IOException;
}
