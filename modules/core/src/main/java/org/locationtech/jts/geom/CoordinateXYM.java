/*
 * Copyright (c) 2018 Vivid Solutions
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
 *
 * 坐标对象Coordinate的子类，支持XYM坐标串
 * <p>
 * 该数据对象适用于<tt>dimension</tt> = 3且 <tt>measure</tt> = 1的坐标序列。
 * <p>
 * {@link Coordinate#z} 字段可见, 但意图被忽略。
 * 
 * @since 1.16
 */
public class CoordinateXYM extends Coordinate {
  private static final long serialVersionUID = 2842127537691165613L;

  /** X的标准坐标索引 */
  public static final int X = 0;

  /** Y的标准坐标索引*/
  public static final int Y = 1;

  /** CoordinateXYM不支持Z值。 */
  public static final int Z = -1;

  /**
   * XYM序列中M的标准纵坐标索引值。
   *
   */
  public static final int M = 2;

  /** 默认构造函数 */
  public CoordinateXYM() {
    super();
    this.m = 0.0;
  }

  /**
   * 使用给定的纵坐标和度量构造一个CoordinateXYM实例。
   * 
   * @param x X纵坐标
   * @param y Y纵坐标
   * @param m M值
   */
  public CoordinateXYM(double x, double y, double m) {
    super(x, y, Coordinate.NULL_ORDINATE);
    this.m = m;
  }

  /**
   * 使用给定Coordinate的x和y坐标构造一个CoordinateXYM实例。
   * 
   * @param coord the coordinate providing the ordinates
   */
  public CoordinateXYM(Coordinate coord) {
    super(coord.x,coord.y);
    m = getM();
  }

  /**
   * 使用给定CoordinateXYM的x和y坐标构造一个CoordinateXY实例。
   * 
   * @param coord the coordinate providing the ordinates
   */
  public CoordinateXYM(CoordinateXYM coord) {
    super(coord.x,coord.y);
    m = coord.m;
  }
  
  /**
   * 创建此CoordinateXYM的副本。
   * 
   * @return a copy of this CoordinateXYM
   */
  public CoordinateXYM copy() {
    return new CoordinateXYM(this);
  }
    
  /** The m-measure. */
  protected double m;

  /** The m-measure, if available. */
  public double getM() {
    return m;
  }

  public void setM(double m) {
    this.m = m;
  }
  
  /** The z-ordinate is not supported */
  @Override
  public double getZ() {
      return NULL_ORDINATE;
  }

  /** The z-ordinate is not supported */
  @Override
  public void setZ(double z) {
      throw new IllegalArgumentException("CoordinateXY dimension 2 does not support z-ordinate");
  }
  
  @Override
  public void setCoordinate(Coordinate other)
  {
    x = other.x;
    y = other.y;
    z = other.getZ();
    m = other.getM();
  }
  
  @Override
  public double getOrdinate(int ordinateIndex) {
      switch (ordinateIndex) {
      case X: return x;
      case Y: return y;
      case M: return m;
      }
      throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
  }
  
  @Override
  public void setOrdinate(int ordinateIndex, double value) {
      switch (ordinateIndex) {
      case X:
        x = value;
        break;
      case Y:
        y = value;
        break;
      case M:
        m = value;
        break;
      default:
        throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
    }
  }
  
  public String toString() {
    return "(" + x + ", " + y + " m=" + getM() + ")";
  }
}
