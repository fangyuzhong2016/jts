

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

import java.util.Collection;
import java.util.Comparator;

import org.locationtech.jts.math.MathUtil;


/**
 * 坐标数组实用类
 *
 * @version 1.7
 */
public class CoordinateArrays {
  private final static Coordinate[] coordArrayType = new Coordinate[0];

  private CoordinateArrays() {}
  
  /**
   * 根据{@link Coordinate}的子类确定维度。
   * 
   * @param pts 提供的坐标数组
   * @return 获取坐标维度
   */
  public static int dimension(Coordinate[] pts) {
    if( pts == null || pts.length == 0) {
      return 3; // unknown, assume default
    }
    int dimension = 0;
    for(Coordinate coordinate : pts ) {
      dimension = Math.max(dimension, Coordinates.dimension( coordinate ));
    }
    return dimension;
  }
  /**
   * 根据{@link Coordinate}的子类确定度量数。
   * 
   * @param pts 提供的坐标数组
   * @return 获取坐标的M度量
   */
  public static int measures(Coordinate[] pts) {
    if( pts == null || pts.length == 0) {
      return 0; // unknown, assume default
    }
    int measures = 0;
    for(Coordinate coordinate : pts ) {
      measures = Math.max(measures, Coordinates.measures( coordinate ));
    }
    return measures;
  }
  
  /**
   * 通过检查长度和闭包来测试{@link Coordinate}的数组是否形成一个环。
   * 不检查自相交。
   * 
   * @param pts 坐标数组
   * @return 如果坐标形成一个环，则为true。
   */
  public static boolean isRing(Coordinate[] pts)
  {
    if (pts.length < 4) return false;
    if (! pts[0].equals2D(pts[pts.length -1])) return false;
    return true;
  }
    
  /**
   * 在点列表中查找未包含在另一个点列表中的点
   * @param testPts 用来测试的{@link Coordinate}s 数组
   * @param pts 一组{@link Coordinate}来测试输入点
   * @return 来自 <code>testPts</code>的{@link Coordinate}，它不在<code>pts</code>中 或返回<code>null</code>
   */
  public static Coordinate ptNotInList(Coordinate[] testPts, Coordinate[] pts)
  {
    for (int i = 0; i < testPts.length; i++) {
      Coordinate testPt = testPts[i];
      if (CoordinateArrays.indexOf(testPt, pts) < 0)
          return testPt;
    }
    return null;
  }

  /**
   *使用词典排序在坐标的前进方向上比较两个{@link Coordinate}数组。
   *
   * @param pts1
   * @param pts2
   * @return 表示顺序的整数
   */
  public static int compare(Coordinate[] pts1, Coordinate[] pts2) {
    int i = 0;
    while (i < pts1.length && i < pts2.length) {
      int compare = pts1[i].compareTo(pts2[i]);
      if (compare != 0)
        return compare;
      i++;
    }
    // 处理数组长度不同时的情况
    if (i < pts2.length) return -1;
    if (i < pts1.length) return 1;

    return 0;
  }

  /**
   *{@link Comparator}用于{@link Coordinate}数组的坐标正向排序器，使用词典排序。
   */
  public static class ForwardComparator
      implements Comparator
  {
    public int compare(Object o1, Object o2) {
      Coordinate[] pts1 = (Coordinate[]) o1;
      Coordinate[] pts2 = (Coordinate[]) o2;

      return CoordinateArrays.compare(pts1, pts2);
    }
  }


  /**
   * 确定{@link Coordinate}数组的哪个方向（整体）增加。
   * 换句话说，确定数组的哪一端“更小”（使用{@link Coordinate}上的标准排序）。
   * 返回一个指示增加方向的整数。
   * 如果序列是回文序列，则定义为在正方向上定向。
   *
   * @param pts 要测试的坐标数组
   * @return 如果数组在开始时较小或是回文，返回<code>1</code>，如果结尾较小，返回<code>-1</code>
   */
  public static int increasingDirection(Coordinate[] pts) {
    for (int i = 0; i < pts.length / 2; i++) {
      int j = pts.length - 1 - i;
      // skip equal points on both ends
      int comp = pts[i].compareTo(pts[j]);
      if (comp != 0)
        return comp;
    }
    // array must be a palindrome - defined to be in positive direction
    return 1;
  }

  /**
   * 确定两个相等长度的{@link Coordinate}数组在相反方向上是否相等。
   *
   * @param pts1
   * @param pts2
   * @return 如果两个数组的方向相反，则<code> true </ code>。
   */
  private static boolean isEqualReversed(Coordinate[] pts1, Coordinate[] pts2)
  {
    for (int i = 0; i < pts1.length; i++) {
      Coordinate p1 = pts1[i];
      Coordinate p2 = pts2[pts1.length - i - 1];
      if (p1.compareTo(p2) != 0)
        return false;
    }
    return true;
  }

  /**
   * {@link Coordinate} 数组以其方向性为模的比较器{@link Comparator} 。
   *例如。如果两个坐标数组相同但反转，则在此排序下它们，将相等。
   * 如果数组不相等，则返回的顺序是正向排序。
   *
   */
  public static class BidirectionalComparator
      implements Comparator
  {
    public int compare(Object o1, Object o2) {
      Coordinate[] pts1 = (Coordinate[]) o1;
      Coordinate[] pts2 = (Coordinate[]) o2;

      if (pts1.length < pts2.length) return -1;
      if (pts1.length > pts2.length) return 1;

      if (pts1.length == 0) return 0;

      int forwardComp = CoordinateArrays.compare(pts1, pts2);
      boolean isEqualRev = isEqualReversed(pts1, pts2);
      if (isEqualRev)
        return 0;
      return forwardComp;
    }

    public int OLDcompare(Object o1, Object o2) {
      Coordinate[] pts1 = (Coordinate[]) o1;
      Coordinate[] pts2 = (Coordinate[]) o2;

      if (pts1.length < pts2.length) return -1;
      if (pts1.length > pts2.length) return 1;

      if (pts1.length == 0) return 0;

      int dir1 = increasingDirection(pts1);
      int dir2 = increasingDirection(pts2);

      int i1 = dir1 > 0 ? 0 : pts1.length - 1;
      int i2 = dir2 > 0 ? 0 : pts1.length - 1;

      for (int i = 0; i < pts1.length; i++) {
        int comparePt = pts1[i1].compareTo(pts2[i2]);
        if (comparePt != 0)
          return comparePt;
        i1 += dir1;
        i2 += dir2;
      }
      return 0;
    }

  }

  /**
   * 创建参数{@link Coordinate}数组的深层副本。
   *
   * @param coordinates 坐标数组
   * @return 返回拷贝的深层副本
   */
  public static Coordinate[] copyDeep(Coordinate[] coordinates) {
    Coordinate[] copy = new Coordinate[coordinates.length];
    for (int i = 0; i < coordinates.length; i++) {
      copy[i] = coordinates[i].copy();
    }
    return copy;
  }

  /**
   * 创建源{@link Coordinate}数组的给定部分的深层副本到目标Coordinate数组中。
   * 目标数组必须是适当的大小才能接收复制的坐标。
   *
   * @param src 源坐标数组
   * @param srcStart 从中开始复制的索引
   * @param dest 目标坐标数组
   * @param destStart 要开始复制到的目标索引
   * @param length 要复制的项目数
   */
  public static void copyDeep(Coordinate[] src, int srcStart, Coordinate[] dest, int destStart, int length) {
    for (int i = 0; i < length; i++) {
      dest[destStart + i] = src[srcStart + i].copy();
    }
  }

  /**
   * 将给定的坐标集转换为坐标数组。
   */
  public static Coordinate[] toCoordinateArray(Collection coordList)
  {
    return (Coordinate[]) coordList.toArray(coordArrayType);
  }

  /**
   * 返回给定数组中任何两个连续坐标是否相等。
   */
  public static boolean hasRepeatedPoints(Coordinate[] coord)
  {
    for (int i = 1; i < coord.length; i++) {
      if (coord[i - 1].equals(coord[i]) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * 如果长度大于给定量，则返回给定坐标数组，或返回空坐标数组。
   */
  public static Coordinate[] atLeastNCoordinatesOrNothing(int n, Coordinate[] c) {
      return c.length >= n ? c : new Coordinate[] {  };
  }

  /**
   * 如果坐标数组参数具有重复点，则构造一个不包含重复点的新数组。否则，返回参数。
   * @see #hasRepeatedPoints(Coordinate[])
   */
  public static Coordinate[] removeRepeatedPoints(Coordinate[] coord)
  {
    if (! hasRepeatedPoints(coord)) return coord;
    CoordinateList coordList = new CoordinateList(coord, false);
    return coordList.toCoordinateArray();
  }

  /**
   * 折叠坐标数组以删除所有null元素。
   * 
   * @param coord 要折叠的坐标数组
   * @return 一个只包含非null元素的数组
   */
  public static Coordinate[] removeNull(Coordinate[] coord)
  {
    int nonNull = 0;
    for (int i = 0; i < coord.length; i++) {
      if (coord[i] != null) nonNull++;
    }
    Coordinate[] newCoord = new Coordinate[nonNull];
    // empty case
    if (nonNull == 0) return newCoord;
    
    int j = 0;
    for (int i = 0; i < coord.length; i++) {
      if (coord[i] != null) newCoord[j++] = coord[i];
    }
    return newCoord;
  }
  
  /**
   * 在原地反转数组中的坐标。
   */
  public static void reverse(Coordinate[] coord)
  {
    int last = coord.length - 1;
    int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      Coordinate tmp = coord[i];
      coord[i] = coord[last - i];
      coord[last - i] = tmp;
    }
  }

  /**
   * 如果两个数组相同，返回true，则为null或逐点相等（使用Coordinate＃equals进行比较）
   * @see Coordinate#equals(Object)
   */
  public static boolean equals(
    Coordinate[] coord1,
    Coordinate[] coord2)
  {
    if (coord1 == coord2) return true;
    if (coord1 == null || coord2 == null) return false;
    if (coord1.length != coord2.length) return false;
    for (int i = 0; i < coord1.length; i++) {
      if (! coord1[i].equals(coord2[i])) return false;
    }
    return true;
  }

  /**
   * 如果两个数组相同，均为null或逐点相等，则返回true，使用用户定义的{@link Comparator}进行{@link Coordinate}的比较
   *
   * @param coord1 坐标数组
   * @param coord2 坐标数组
   * @param coordinateComparator 坐标比较器
   */
  public static boolean equals(
    Coordinate[] coord1,
    Coordinate[] coord2,
    Comparator coordinateComparator)
  {
    if (coord1 == coord2) return true;
    if (coord1 == null || coord2 == null) return false;
    if (coord1.length != coord2.length) return false;
    for (int i = 0; i < coord1.length; i++) {
      if (coordinateComparator.compare(coord1[i], coord2[i]) != 0)
          return false;
    }
    return true;
  }

  /**
   * 使用通常的字典比较返回最小坐标。
   *
   *@param  coordinates  要搜索的数组
   *@return              使用 <code>compareTo</code>找到的数组中的最小坐标
   */
  public static Coordinate minCoordinate(Coordinate[] coordinates)
  {
    Coordinate minCoord = null;
    for (int i = 0; i < coordinates.length; i++) {
      if (minCoord == null || minCoord.compareTo(coordinates[i]) > 0) {
        minCoord = coordinates[i];
      }
    }
    return minCoord;
  }
  /**
   * 移动坐标的位置，直到<code>firstCoordinate</code> 是第一个为止。
   *
   *@param  coordinates      要重新排列的数组
   *@param  firstCoordinate  要移动到首位的坐标
   */
  public static void scroll(Coordinate[] coordinates, Coordinate firstCoordinate) {
    int i = indexOf(firstCoordinate, coordinates);
    if (i < 0) return;
    Coordinate[] newCoordinates = new Coordinate[coordinates.length];
    System.arraycopy(coordinates, i, newCoordinates, 0, coordinates.length - i);
    System.arraycopy(coordinates, 0, newCoordinates, coordinates.length - i, i);
    System.arraycopy(newCoordinates, 0, coordinates, 0, coordinates.length);
  }

  /**
   *  返回<code>coordinate</code>对象在坐标数组<code>coordinates</code>中的索引.
   *  第一个位置是0;第二，1;以此类推
   *
   *@param  coordinate   要搜索的<code>Coordinate</code>
   *@param  coordinates  要搜索的数组
   *@return              <code>Coordinate</code>的位置，如果未找到，则返回-1
   */
  public static int indexOf(Coordinate coordinate, Coordinate[] coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      if (coordinate.equals(coordinates[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * 从索引<code>start</code>到<code>end</code>（包括）提取输入{@link Coordinate}数组的子序列。
   * 输入索引被限制为数组大小;
   * 如果结束索引小于起始索引，则提取的数组将为空。
   *
   * @param pts 输入数组
   * @param start 要提取的子序列的开始的索引
   * @param end 提取子序列结束的索引
   * @return 输入数组的子序列
   */
  public static Coordinate[] extract(Coordinate[] pts, int start, int end)
  {
    start = MathUtil.clamp(start, 0, pts.length);
    end = MathUtil.clamp(end, -1, pts.length);
    
    int npts = end - start + 1;
    if (end < 0) npts = 0;
    if (start >= pts.length) npts = 0;
    if (end < start) npts = 0;
    
    Coordinate[] extractPts = new Coordinate[npts];
    if (npts == 0) return extractPts;
    
    int iPts = 0;
    for (int i = start; i <= end; i++) {
      extractPts[iPts++] = pts[i];
    }
    return extractPts;
  }

  /**
   * 计算坐标的包络。
   * 
   * @param coordinates 要扫描的坐标
   * @return 坐标的包络线
   */
  public static Envelope envelope(Coordinate[] coordinates) {
    Envelope env = new Envelope();
    for (int i = 0; i < coordinates.length; i++) {
      env.expandToInclude(coordinates[i]);
    }
    return env;
  }
  
  /**
   * 提取与{@link Envelope}相交的坐标。
   * 
   * @param coordinates 要扫描的坐标
   * @param env 要与之相交的包络
   * @return 与包络相交的坐标数组
   */
  public static Coordinate[] intersection(Coordinate[] coordinates, Envelope env) {
    CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < coordinates.length; i++) {
      if (env.intersects(coordinates[i]))
        coordList.add(coordinates[i], true);
    }
    return coordList.toCoordinateArray();
  }
}
