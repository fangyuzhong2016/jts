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

import org.locationtech.jts.io.OrdinateFormat;
import org.locationtech.jts.util.NumberUtil;


/**
 * 用于操纵{@link CoordinateSequence}的实用程序函数
 *
 * @version 1.7
 */
public class CoordinateSequences {

  /**
   * 以原位顺序反转序列中的坐标。
   * 
   * @param seq 要反转的坐标序列
   */
  public static void reverse(CoordinateSequence seq)
  {
    int last = seq.size() - 1;
    int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      swap(seq, i, last - i);
    }
  }

  /**
   * 交换序列中的两个坐标。
   *
   * @param seq 要修改的序列
   * @param i 要交换的坐标的索引
   * @param j 要交换的坐标的索引
   */
  public static void swap(CoordinateSequence seq, int i, int j)
  {
    if (i == j) return;
    for (int dim = 0; dim < seq.getDimension(); dim++) {
      double tmp = seq.getOrdinate(i, dim);
      seq.setOrdinate(i, dim, seq.getOrdinate(j, dim));
      seq.setOrdinate(j, dim, tmp);
    }
  }
  
  /**
   * 将{@link CoordinateSequence}的一部分复制到另一个{@link CoordinateSequence}。
   * 序列可能有不同的维度;在这种情况下，只复制公共维度。
   *
   * @param src 从中复制的序列
   * @param srcPos 源序列中的位置开始复制
   * @param dest 要复制的序列
   * @param destPos 要复制到的目标序列中的位置
   * @param length 要复制的坐标数
   */
  public static void copy(CoordinateSequence src, int srcPos, CoordinateSequence dest, int destPos, int length)
  {
  	for (int i = 0; i < length; i++) {
  		copyCoord(src, srcPos + i, dest, destPos + i);
  	}
  }

  /**
   * 将{@link CoordinateSequence}的坐标复制到另一个{@link CoordinateSequence}。
   * 序列可能有不同的维度;在这种情况下，只复制通用维度。
   * 
   * @param src 从中复制的序列
   * @param srcPos 要复制的源坐标
   * @param dest 要复制的序列
   * @param destPos 要复制到的目标坐标
   */
  public static void copyCoord(CoordinateSequence src, int srcPos, CoordinateSequence dest, int destPos)
  {
    int minDim = Math.min(src.getDimension(), dest.getDimension());
		for (int dim = 0; dim < minDim; dim++) {
			dest.setOrdinate(destPos, dim, src.getOrdinate(srcPos, dim));
		}
  }
  
  /**
   * 通过检查序列长度和闭包(2D中的第一个和最后一个点是否相同)来测试{@link CoordinateSequence}是否形成有效的{@link LinearRing}。
   * 不检查自相交。
   * 
   * @param seq 要测试的序列
   * @return 如果序列是一个环，则为true
   * @see LinearRing
   */
  public static boolean isRing(CoordinateSequence seq) 
  {
  	int n = seq.size();
  	if (n == 0) return true;
  	// too few points
  	if (n <= 3) 
  		return false;
  	// test if closed
  	return seq.getOrdinate(0, CoordinateSequence.X) == seq.getOrdinate(n-1, CoordinateSequence.X)
  		&& seq.getOrdinate(0, CoordinateSequence.Y) == seq.getOrdinate(n-1, CoordinateSequence.Y);
  }
  
  /**
   * 确保CoordinateSequence形成有效的环，如果需要，返回正确长度的新闭合序列。
   * 如果输入序列已经是有效的环，则返回它而不进行修改。
   * 如果输入序列太短或未关闭，则使用起始点的一个或多个副本进行扩展。
   * 
   * @param fact CoordinateSequenceFactory用于创建新序列
   * @param seq 要测试的序列
   * @return 原始序列，如果它是有效环，或新序列是有效的。
   */
  public static CoordinateSequence ensureValidRing(CoordinateSequenceFactory fact, CoordinateSequence seq)
  {
  	int n = seq.size();
  	// empty sequence is valid
  	if (n == 0) return seq; 
  	// too short - make a new one
  	if (n <= 3) 
  		return createClosedRing(fact, seq, 4);
  	
  	boolean isClosed = seq.getOrdinate(0, CoordinateSequence.X) == seq.getOrdinate(n-1, CoordinateSequence.X)
		&& seq.getOrdinate(0, CoordinateSequence.Y) == seq.getOrdinate(n-1, CoordinateSequence.Y);
  	if (isClosed) return seq;
  	// make a new closed ring
  	return createClosedRing(fact, seq, n+1);
  }
  
  private static CoordinateSequence createClosedRing(CoordinateSequenceFactory fact, CoordinateSequence seq, int size)
  {
    CoordinateSequence newseq = fact.create(size, seq.getDimension());
    int n = seq.size();
    copy(seq, 0, newseq, 0, n);
    // fill remaining coordinates with start point
    for (int i = n; i < size; i++)
      copy(seq, 0, newseq, i, 1);
    return newseq;
  }
  
  public static CoordinateSequence extend(CoordinateSequenceFactory fact, CoordinateSequence seq, int size)
  {
    CoordinateSequence newseq = fact.create(size, seq.getDimension());
    int n = seq.size();
    copy(seq, 0, newseq, 0, n);
    // fill remaining coordinates with end point, if it exists
    if (n > 0) {
      for (int i = n; i < size; i++)
        copy(seq, n-1, newseq, i, 1);
    }
    return newseq;
  }

  /**
   * 测试两个{@link CoordinateSequence}是否相等。
   * 为了相同，序列必须是相同的长度。
   * 它们不需要具有相同的维度，但两个的最小维度的坐标值必须相等。
   * 两个<code>NaN</code>坐标值被认为是相等的。
   * 
   * @param cs1 CoordinateSequence
   * @param cs2 CoordinateSequence
   * @return 如果序列在公共维度中相等，则为true
   */
  public static boolean isEqual(CoordinateSequence cs1, CoordinateSequence cs2) {
    int cs1Size = cs1.size();
    int cs2Size = cs2.size();
    if (cs1Size != cs2Size) return false;
    int dim = Math.min(cs1.getDimension(), cs2.getDimension());
    for (int i = 0; i < cs1Size; i++) {
      for (int d = 0; d < dim; d++) {
        double v1 = cs1.getOrdinate(i, d);
        double v2 = cs2.getOrdinate(i, d);
        if (cs1.getOrdinate(i, d) == cs2.getOrdinate(i, d))
          continue;
        // special check for NaNs
        if (Double.isNaN(v1) && Double.isNaN(v2))
          continue;
        return false;
      }
    }
    return true;
  }
  
  /**
   * 创建{@link CoordinateSequence}的字符串表示形式。
   * 格式为：
   * <pre>
   *   ( ord0,ord1.. ord0,ord1,...  ... )
   * </pre>
   * 
   * @param cs 要输出的序列
   * @return 序列的字符串表示
   */
  public static String toString(CoordinateSequence cs)
  {
    int size = cs.size();
    if (size == 0) 
      return "()";
    int dim = cs.getDimension();
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (int i = 0; i < size; i++) {
      if (i > 0) builder.append(" ");
      for (int d = 0; d < dim; d++) {
        if (d > 0) builder.append(",");
        builder.append( OrdinateFormat.DEFAULT.format(cs.getOrdinate(i, d)) );
      }
    }
    builder.append(')');
    return builder.toString();
  }

  /**
   *  使用通常的字典比较返回最小坐标。
   *
   *@param  seq  要搜索的坐标序列
   *@return  使用 <code>compareTo</code>找到序列中的最小坐标，
   */
  public static Coordinate minCoordinate(CoordinateSequence seq)
  {
    Coordinate minCoord = null;
    for (int i = 0; i < seq.size(); i++) {
      Coordinate testCoord = seq.getCoordinate(i);
      if (minCoord == null || minCoord.compareTo(testCoord) > 0) {
        minCoord = testCoord;
      }
    }
    return minCoord;
  }
  /**
   *  使用通常的字典比较返回整个坐标序列的最小坐标的索引。
   *
   *@param  seq  要搜索的坐标序列
   *@return  序列中最小坐标的索引，使用<code>compareTo</code>找到
   */
  public static int minCoordinateIndex(CoordinateSequence seq) {
    return minCoordinateIndex(seq, 0, seq.size() - 1);
  }

  /**
   *  使用通常的字典比较，返回坐标序列的一部分（由{@code from}和{@code to}定义）的最小坐标的索引。
   *  
   *@param  seq   要搜索的坐标序列
   *@param  from  开始的搜索索引
   *@param  to    结束的搜索索引
   *@return  the index of the minimum coordinate in the sequence, found using <code>compareTo</code>
   */
  public static int minCoordinateIndex(CoordinateSequence seq, int from, int to)
  {
    int minCoordIndex = -1;
    Coordinate minCoord = null;
    for (int i = from; i <= to; i++) {
      Coordinate testCoord = seq.getCoordinate(i);
      if (minCoord == null || minCoord.compareTo(testCoord) > 0) {
          minCoord = testCoord;
          minCoordIndex = i;
      }
    }
    return minCoordIndex;
  }

  /**
   * 移动坐标的位置，直到<code>firstCoordinate</code>的坐标在第一位置为止。
   *
   *@param  seq      重新排列的坐标序列
   *@param  firstCoordinate  the coordinate to make first
   */
  public static void scroll(CoordinateSequence seq, Coordinate firstCoordinate) {
    int i = indexOf(firstCoordinate, seq);
    if (i <= 0) return;
    scroll(seq, i);
  }

  /**
   *  移动坐标的位置，直到<code>firstCoordinateIndex</code>的坐标在第一位置为止。
   *
   *@param  seq      重新排列的坐标序列
   *@param  indexOfFirstCoordinate  要排列在第一位的坐标索引
   */
  public static void scroll(CoordinateSequence seq, int indexOfFirstCoordinate)
  {
    scroll(seq, indexOfFirstCoordinate, CoordinateSequences.isRing(seq));
  }

  /**
   *  移动坐标的位置，直到<code> firstCoordinateIndex</code>的坐标排列在第一位为止。
   *
   *@param  seq      重新排列的坐标序列
   *@param  indexOfFirstCoordinate
   *                 要排列在第一位的坐标索引
   *@param  ensureRing
   *                 确保{@code}在退出时成为闭环
   */
    public static void scroll(CoordinateSequence seq, int indexOfFirstCoordinate, boolean ensureRing) {
    int i = indexOfFirstCoordinate;
    if (i <= 0) return;

    // make a copy of the sequence
    CoordinateSequence copy = seq.copy();

    // test if ring, determine last index
    int last = ensureRing ? seq.size() - 1: seq.size();

    // fill in values
    for (int j = 0; j < last; j++)
    {
      for (int k = 0; k < seq.getDimension(); k++)
        seq.setOrdinate(j, k, copy.getOrdinate((indexOfFirstCoordinate+j)%last, k));
    }

    // Fix the ring (first == last)
    if (ensureRing) {
      for (int k = 0; k < seq.getDimension(); k++)
        seq.setOrdinate(last, k, seq.getOrdinate(0, k));
    }
  }

  /**
   * 返回{@link CoordinateSequence}中<code>coordinate</code>的索引
   *  排列在第一个位置是 0; 第二个位置是1; 以此类推.
   *
   *@param  coordinate   要搜索的<code>Coordinate</code>
   *@param  seq  要搜索的坐标序列
   *@return              <code>coordinate</code>的位置，如果未找到，则返回-1
   */
  public static int indexOf(Coordinate coordinate, CoordinateSequence seq) {
    for (int i = 0; i < seq.size(); i++) {
      if (coordinate.x == seq.getOrdinate(i, CoordinateSequence.X) &&
          coordinate.y == seq.getOrdinate(i, CoordinateSequence.Y)) {
        return i;
      }
    }
    return -1;
  }}