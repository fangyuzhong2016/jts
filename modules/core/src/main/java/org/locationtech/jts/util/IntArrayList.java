/*
 * Copyright (c) 2019 Martin Davis.
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

import java.util.Arrays;
import java.util.Objects;

/**
 * 一个可扩展的原始<code>int</code>值数组。
 * 
 * @author Martin Davis
 *
 */
public class IntArrayList {
  private int[] data;
  private int size = 0;

  /**
   * 构造一个空列表。
   */
  public IntArrayList() {
    this(10);
  }

  /**
   * 构造具有指定初始容量的空列表
   * 
   * @param initialCapacity 列表的初始容量
   */
  public IntArrayList(int initialCapacity) {
    data = new int[initialCapacity];
  }

  /**
   * 返回此列表中的值的数量。
   * 
   * @return 列表中的值的数量
   */
  public int size() {
    return size;
  }

  /**
   * 如有必要，增加此列表实例的容量，以确保它至少可以保存capacity参数指定的元素数。
   * 
   * @param capacity 所需的容量
   */
  public void ensureCapacity(final int capacity) {
    if (capacity <= data.length) return;
    int newLength  = Math.max(capacity, data.length * 2);
    //System.out.println("IntArrayList: copying " + size + " ints to new array of length " + capacity);
    data = Arrays.copyOf(data, newLength);
  }
  /**
   * 在此列表的末尾添加一个值。
   * 
   * @param value 要添加的值
   */
  public void add(final int value) {
    ensureCapacity(size + 1);
    data[size] = value;
    ++size;
  }
  
  /**
   * 将数组中的所有值添加到此列表的末尾。
   * 
   * @param values an array of values
   */
  public void addAll(final int[] values) {
    if (values == null) return;
    if (values.length == 0) return;
    ensureCapacity(size + values.length);
    System.arraycopy(values, 0, data, size, values.length);
    size += values.length;
   }
  
  /**
   * 返回一个int数组，其中包含此列表中值的副本。
   * 
   * @return 包含此列表中的值的数组
   */
  public int[] toArray() {
    int[] array = new int[size];
    System.arraycopy(data, 0, array, 0, size);
    return array;
  }
}