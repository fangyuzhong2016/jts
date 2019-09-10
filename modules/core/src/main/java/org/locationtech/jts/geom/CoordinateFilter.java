

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


/**
 *  使用{@link Geometry}中坐标值的类的接口。
 * 坐标过滤器可用于实现质心和包络计算以及许多其他功能。
 * <p>
 * <code>CoordinateFilter</code>是Gang-of-Four Visitor模式的一个示例。
 * <p>
 * <b>注意</b>: 不建议使用这些过滤器来改变坐标。
 * 无法保证坐标是存储在源几何中的实际对象。
 * 特别是，如果源Geometry使用非默认的{@link CoordinateSequence}，则可能无法保留修改后的值。
 * 如果需要就地变异，请使用{@link CoordinateSequenceFilter}。
 *  
 * @see Geometry#apply(CoordinateFilter)
 * @see CoordinateSequenceFilter
 *
 *@version 1.7
 */
public interface CoordinateFilter {

  /**
   * 使用提供的<code>coord</code>执行操作。
   * 请注意，无法保证输入坐标是存储在源几何中的实际对象，因此对坐标对象的更改可能不会保存。
   *
   *@param  coord  应用过滤器的<code>Coordinate</code>。
   */
  void filter(Coordinate coord);
}

