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

import java.util.Comparator;

/**
 * 比较两个{@link CoordinateSequence}。
 * 对于相同维度的序列，排序是词典的。
 * 否则，较低的维度在较高之前排序。
 * 比较的维度可以是有限的;如果这样做，则不会比较高于限制的坐标。
 * <p>
 * 如果比较大小，维度或坐标值需要不同的行为，则可以覆盖任何或所有方法。
 *
 */
public class CoordinateSequenceComparator
	implements Comparator
{
  /**
   * 比较两个<code>double</code>，允许NaN值。
   * NaN被视为小于任何有效数字。
   *
   * @param a 一个<code>double</code>
   * @param b 另一个<code>double</code>
   * @return -1,0或1 取决于a是否小于，等于或大于b
   */
  public static int compare(double a, double b)
  {
    if (a < b) return -1;
    if (a > b) return 1;

    if (Double.isNaN(a)) {
      if (Double.isNaN(b)) return 0;
      return -1;
    }

    if (Double.isNaN(b)) return 1;
    return 0;
  }

  /**
   * 要测试的维数
   */
  protected int dimensionLimit;

  /**
   * 创建一个比较器，用于测试所有维度
   */
  public CoordinateSequenceComparator()
  {
    dimensionLimit = Integer.MAX_VALUE;
  }

  /**
   * 创建一个仅测试指定维数的比较器。
   *
   * @param dimensionLimit 要测试的维数
   */
  public CoordinateSequenceComparator(int dimensionLimit)
  {
    this.dimensionLimit = dimensionLimit;
  }

  /**
   * 比较两个{@link CoordinateSequence}的相对顺序。
   *
   * @param o1 一个{@link CoordinateSequence}
   * @param o2 另一个{@link CoordinateSequence}
   * @return -1,0或1取决于o1是否小于，等于或大于o2
   */
  public int compare(Object o1, Object o2)
  {
    CoordinateSequence s1 = (CoordinateSequence) o1;
    CoordinateSequence s2 = (CoordinateSequence) o2;

    int size1 = s1.size();
    int size2 = s2.size();

    int dim1 = s1.getDimension();
    int dim2 = s2.getDimension();

    int minDim = dim1;
    if (dim2 < minDim)
      minDim = dim2;
    boolean dimLimited = false;
    if (dimensionLimit <= minDim) {
      minDim = dimensionLimit;
      dimLimited = true;
    }

    // lower dimension is less than higher
    if (! dimLimited) {
      if (dim1 < dim2) return -1;
      if (dim1 > dim2) return 1;
    }

    // lexicographic ordering of point sequences
    int i = 0;
    while (i < size1 && i < size2) {
      int ptComp = compareCoordinate(s1, s2, i, minDim);
      if (ptComp != 0) return ptComp;
      i++;
    }
    if (i < size1) return 1;
    if (i < size2) return -1;

    return 0;
  }

  /**
   * 比较给定维数的两个{@link CoordinateSequence}的相同坐标。
   *
   * @param s1 一个{@link CoordinateSequence}
   * @param s2 一个{@link CoordinateSequence}
   * @param i 要测试的坐标的索引
   * @param dimension 要测试的维数
   * @return -1,0或1取决于s1[i]是否小于，等于或大于s2[i]
   */
  protected int compareCoordinate(CoordinateSequence s1, CoordinateSequence s2, int i, int dimension)
  {
    for (int d = 0; d < dimension; d++) {
      double ord1 = s1.getOrdinate(i, d);
      double ord2 = s2.getOrdinate(i, d);
      int comp = compare(ord1, ord2);
      if (comp != 0) return comp;
    }
    return 0;
  }
}
