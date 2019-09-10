

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

/**
 *  一个{@link CoordinateFilter }，用于计算<code>Geometry</code>中的坐标总数
 *
 *@version 1.7
 */
public class CoordinateCountFilter implements CoordinateFilter {
  private int n = 0;

  public CoordinateCountFilter() { }

  /**
   *  返回过滤的结果。
   *
   *@return    此<code>CoordinateCountFilter</code>找到的点数
   */
  public int getCount() {
    return n;
  }

  public void filter(Coordinate coord) {
    n++;
  }
}

