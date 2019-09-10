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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 用于处理{@link Collection}的实用程序。
 *
 * @version 1.7
 */
public class CollectionUtil 
{

  public interface Function {
    Object execute(Object obj);
  }

  /**
   * 对{@link Collection}中的每个项执行一个函数，并在新的{@link List}中返回结果
   *
   * @param coll 要处理的集合
   * @param func 要执行的函数
   * @return 已转换对象的列表
   */
  public static List transform(Collection coll, Function func)
  {
    List result = new ArrayList();
    for (Iterator i = coll.iterator(); i.hasNext(); ) {
      result.add(func.execute(i.next()));
    }
    return result;
  }

  /**
   * 对集合中的每个项执行一个函数，但不会返回结果
   *
   * @param coll 要处理的集合
   * @param func 要执行的函数
   */
  public static void apply(Collection coll, Function func)
  {
    for (Iterator i = coll.iterator(); i.hasNext(); ) {
      func.execute(i.next());
    }
  }

  /**
   * 对集合中的每个项执行{@link Function }，
   * 并收集函数结果等于{@link Boolean } <tt>true</tt>的所有条目。
   *
   * @param collection 要处理的集合
   * @param func 要执行的函数
   * @return 函数为true的对象列表
   */
  public static List select(Collection collection, Function func) {
    List result = new ArrayList();
    for (Iterator i = collection.iterator(); i.hasNext();) {
      Object item = i.next();
      if (Boolean.TRUE.equals(func.execute(item))) {
        result.add(item);
      }
    }
    return result;
  }
}
