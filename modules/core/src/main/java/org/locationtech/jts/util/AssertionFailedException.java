

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
package org.locationtech.jts.util;

/**
 *  当应用程序处于不一致状态时抛出。表示代码有问题。
 *
 *@version 1.7
 */
public class AssertionFailedException extends RuntimeException {

  /**
   *  创建<code>AssertionFailedException</code>。
   */
  public AssertionFailedException() {
    super();
  }

  /**
   *  使用给定的详细消息创建<code>AssertionFailedException</code>。
   *
   *@param  message  断言的描述
   */
  public AssertionFailedException(String message) {
    super(message);
  }
}


