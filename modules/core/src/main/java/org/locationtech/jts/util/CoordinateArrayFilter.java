

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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;

/**
 *  一个{@link CoordinateFilter }，它创建一个包含{@link Geometry }中每个坐标的数组。
 *
 *@version 1.7
 */
public class CoordinateArrayFilter implements CoordinateFilter {
  Coordinate[] pts = null;
  int n = 0;

  /**
   *  构造一个<code>CoordinateArrayFilter</code>。
   *
   *@param  size  <code>CoordinateArrayFilter</code>将收集的点数
   */
  public CoordinateArrayFilter(int size) {
    pts = new Coordinate[size];
  }

  /**
   *  返回收集的<code>Coordinate</code>。
   *
   *@return    此<code>CoordinateArrayFilter</code>收集的<code>Coordinate</code>
   */
  public Coordinate[] getCoordinates() {
    return pts;
  }

  public void filter(Coordinate coord) {
    pts[n++] = coord;
  }
}

