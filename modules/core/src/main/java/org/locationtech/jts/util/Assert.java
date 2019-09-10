

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
 *  用于编程断言的实用程序。
 *
 *@version 1.7
 */
public class Assert {

  /**
   * 如果给定的断言是true，则抛出<code>AssertionFailedException</code>。
   *
   *@param  assertion                  一个应该是真实的条件
   *@throws  AssertionFailedException  如果条件是假的
   */
  public static void isTrue(boolean assertion) {
    isTrue(assertion, null);
  }

  /**
   *  如果给定的断言不为真，则使用给定的消息抛出<code>AssertionFailedException</code>。
   *
   *@param  assertion                  一个应该是真实的条件
   *@param  message                    断言的描述
   *@throws  AssertionFailedException  如果条件是假的
   */
  public static void isTrue(boolean assertion, String message) {
    if (!assertion) {
      if (message == null) {
        throw new AssertionFailedException();
      }
      else {
        throw new AssertionFailedException(message);
      }
    }
  }

  /**
   *  根据<code>equal </code>方法，如果给定对象不相等，
   *  则抛出<code>AssertionFailedException </code>。
   *
   *@param  expectedValue              正确的值
   *@param  actualValue                正在检查的值
   *@throws  AssertionFailedException  如果两个对象不相等
   */
  public static void equals(Object expectedValue, Object actualValue) {
    equals(expectedValue, actualValue, null);
  }

  /**
   *  如果给定的对象不相等，则根据<code>equals</code> 方法，
   *  使用给定的消息抛出<code>AssertionFailedException </code>。
   *
   *@param  expectedValue              正确的值
   *@param  actualValue                正在检查的值
   *@param  message                    断言的描述
   *@throws  AssertionFailedException  如果两个对象不相等
   */
  public static void equals(Object expectedValue, Object actualValue, String message) {
    if (!actualValue.equals(expectedValue)) {
      throw new AssertionFailedException("Expected " + expectedValue + " but encountered "
           + actualValue + (message != null ? ": " + message : ""));
    }
  }

  /**
   *  始终抛出<code>AssertionFailedException</code>。
   *
   *@throws  AssertionFailedException  thrown always
   */
  public static void shouldNeverReachHere() {
    shouldNeverReachHere(null);
  }

  /**
   *  始终使用给定的消息抛出<code>AssertionFailedException</code>。
   *
   *@param  message                    断言的描述
   *@throws  AssertionFailedException  thrown always
   */
  public static void shouldNeverReachHere(String message) {
    throw new AssertionFailedException("Should never reach here"
         + (message != null ? ": " + message : ""));
  }
}

