

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
package org.locationtech.jts.geom;

import org.locationtech.jts.geom.util.GeometryEditor;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 *  JTS的坐标序列过滤器，用于处理{@link CoordinateSequence}中坐标的类的接口。
 *  过滤器可以记录有关每个坐标的信息，也可以更改坐标的值。
 *  过滤器可用于实现诸如坐标变换，质心和包络计算等操作以及许多其他功能。
 *  {@link Geometry}类支持将<code>CoordinateSequenceFilter</code>应用于它们包含的每个{@link CoordinateSequence}的概念。
 *  <p>
 *  为了获得最大效率，可以使用{@link #isDone}方法将过滤器的执行短路。
 *  <p>
 *  <code>CoordinateSequenceFilter</code>是Gang-of-Four Visitor模式的一个示例。
 *  <p> 
 * <b>注意</b>: 通常，优选将Geometrys视为不可变的。
 * 应该通过创建一个新的Geometry对象来执行变换（请参阅{@link GeometryEditor} 和{@link GeometryTransformer}以获得方便的方法）。
 * 此规则的一个例外是通过{@link Geometry＃copy()}创建了一个新的Geometry,在这种情况下，改变几何不会导致别名问题，过滤器是实现坐标转换的便捷方式。
 *  
 * @see Geometry#apply(CoordinateFilter)
 * @see GeometryTransformer
 * @see GeometryEditor
 *
 *@see Geometry#apply(CoordinateSequenceFilter)
 *@author Martin Davis
 *@version 1.7
 */
public interface CoordinateSequenceFilter 
{
  /**
   * 对{@link CoordinateSequence}中的坐标执行操作。
   *
   *@param seq  应用过滤器的<code>CoordinateSequence</code>
   *@param i 要应用过滤器的坐标的索引
   */
  void filter(CoordinateSequence seq, int i);
  
  /**
   * 报告是否可以终止此过滤器的应用程序。
   * 一旦此方法返回<tt>true</tt>，它必须在每次后续调用时继续返回<tt>true</tt>。
   * 
   * @return 如果可以终止此过滤器的应用程序，则为true。
   */
  boolean isDone();
  
  /**
   * 报告此过滤器的执行是否已修改几何的坐标。
   * 如果是这样，在此过滤器执行完毕后将执行{@link Geometry#geometryChanged}。
   * <p>
   * 大多数过滤器只能返回一个常量值，反映它们是否能够更改坐标。
   * 
   * @return 如果此滤镜已更改几何的坐标，则为true
   */
  boolean isGeometryChanged();
}

