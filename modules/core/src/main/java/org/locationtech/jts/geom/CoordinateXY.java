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
 * 坐标对象Coordinate的子类，用于创建二维的XY坐标
 * <p>
 * 该数据对象适用于<tt>dimension</tt> = 2的坐标序列。
 * <p>
 * {@link Coordinate#z} 字段可见，但被忽略
 *
 * @since 1.16
 */
public class CoordinateXY extends Coordinate {
  private static final long serialVersionUID = 3532307803472313082L;

  /** X的标准坐标索引 */
  public static final int X = 0;

  /** Y的标准坐标指数值 */
  public static final int Y = 1;

  /**CoordinateXY不支持Z值. */
  public static final int Z = -1;

  /** CoordinateXY不支持M度量. */
  public static final int M = -1;

  /**默认构造函数 */
  public CoordinateXY() {
    super();
  }

  /**
   * 使用给定的坐标构造一个CoordinateXY实例。
   * 
   * @param x X坐标
   * @param y Y坐标
   */
  public CoordinateXY(double x, double y) {
    super(x, y, Coordinate.NULL_ORDINATE);
  }

  /**
   * 使用给定Coordinate的x和y坐标构造一个CoordinateXY实例。
   * 
   * @param coord the Coordinate providing the ordinates
   */
  public CoordinateXY(Coordinate coord) {
    super(coord.x,coord.y);
  }

  /**
   * 使用给定CoordinateXY的x和y坐标构造一个CoordinateXY实例。
   * 
   * @param coord the CoordinateXY providing the ordinates
   */
  public CoordinateXY(CoordinateXY coord) {
    super(coord.x,coord.y);  
  }

  /**
   *创建此CoordinateXY的副本。
   * 
   * @return 这个CoordinateXY的副本
   */
  public CoordinateXY copy() {
    return new CoordinateXY(this);
  }
    
  /** 不支持z坐标 */
  @Override
  public double getZ() {
      return NULL_ORDINATE;
  }

  /** 不支持z坐标 */
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
  }
  
  @Override
  public double getOrdinate(int ordinateIndex) {
      switch (ordinateIndex) {
      case X: return x;
      case Y: return y;
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
      default:
        throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
    }
  }
  
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}