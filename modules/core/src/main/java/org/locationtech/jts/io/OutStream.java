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
 * 一种用于提供字节的输出流类接口。
 * 该接口是类似于Java的<code>OutputStream</code>，但较窄的接口，使之更容易实现。
 */
public interface OutStream
{
  void write(byte[] buf, int len) throws IOException;
}
