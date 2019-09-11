

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
 * 提供点、曲线和曲面的维度的常数类
 * 还提供表示空几何和非空几何的维度的常量，以及表示“任何维度”的通配符常量{@link #DONTCARE}。
 * 这些常量用作{@link IntersectionMatrix}中的条目。
 * 
 * @version 1.7
 */
public class Dimension {

  /**
   * point 的维度 (0)
   */
  public final static int P = 0;

  /**
   * curve (1) 的维度.
   */
  public final static int L = 1;

  /**
   *  surface (2) 的维度.
   */
  public final static int A = 2;

  /**
   *  空几何的维度（-1）.
   */
  public final static int FALSE = -1;

  /**
   *  非空几何的维度值(= {P，L，A}).
   */
  public final static int TRUE = -2;

  /**
   *  Dimension value for any dimension (= {FALSE, TRUE}).
   */
  public final static int DONTCARE = -3;

  /**
   * Symbol for the FALSE pattern matrix entry
   */
  public final static char SYM_FALSE = 'F';
  
  /**
   * Symbol for the TRUE pattern matrix entry
   */
  public final static char SYM_TRUE = 'T';
  
  /**
   * Symbol for the DONTCARE pattern matrix entry
   */
  public final static char SYM_DONTCARE = '*';
  
  /**
   * Symbol for the P (dimension 0) pattern matrix entry
   */
  public final static char SYM_P = '0';
  
  /**
   * Symbol for the L (dimension 1) pattern matrix entry
   */
  public final static char SYM_L = '1';
  
  /**
   * Symbol for the A (dimension 2) pattern matrix entry
   */
  public final static char SYM_A = '2';
  
  /**
   * 将维值转换为维度符号， for example, <code>TRUE =&gt; 'T'</code>
   *  .
   *
   *@param  dimensionValue  一个可以存储在<code>IntersectionMatrix</code>中的数字
   *      . 可能的值为 <code> {TRUE，FALSE，DONTCARE，0,1,2} </code>.
   *@return                 用于 <code>IntersectionMatrix</code>的字符串表示形式的字符。可能的值为<code>{T，F，*，0,1,2}</code>
   *      .
   */
  public static char toDimensionSymbol(int dimensionValue) {
    switch (dimensionValue) {
      case FALSE:
        return SYM_FALSE;
      case TRUE:
        return SYM_TRUE;
      case DONTCARE:
        return SYM_DONTCARE;
      case P:
        return SYM_P;
      case L:
        return SYM_L;
      case A:
        return SYM_A;
    }
    throw new IllegalArgumentException("Unknown dimension value: " + dimensionValue);
  }

  /**
   *  例如，将维度符号转换为维度值, <code>'*' =&gt; DONTCARE</code>
   *  .
   *
   *@param  dimensionSymbol  用于<code>IntersectionMatrix</code>的字符串表示形式的字符。可能的值为<code> {T，F，*，0,1,2} </code>
   *      .
   *@return 一个可以存储在 <code>IntersectionMatrix</code>中的数字
   *      . 可能的值为 <code> {TRUE，FALSE，DONTCARE，0,1,2} </code>。
   */
  public static int toDimensionValue(char dimensionSymbol) {
    switch (Character.toUpperCase(dimensionSymbol)) {
      case SYM_FALSE:
        return FALSE;
      case SYM_TRUE:
        return TRUE;
      case SYM_DONTCARE:
        return DONTCARE;
      case SYM_P:
        return P;
      case SYM_L:
        return L;
      case SYM_A:
        return A;
    }
    throw new IllegalArgumentException("Unknown dimension symbol: " + dimensionSymbol);
  }
}


