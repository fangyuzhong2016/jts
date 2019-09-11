/*
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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
 * 用于处理Coordinate对象的有用实用程序函数。
 */
public class Coordinates {
  /**
   * 提供对公共Coordinate实现的访问的工厂方法。
   * 
   * @param dimension
   * @return 返回创建的坐标对象
   */
  public static Coordinate create(int dimension)
  {
    return create(dimension, 0);
  }

  /**
   * 提供对公共Coordinate实现的访问的工厂方法。
   * 
   * @param dimension 坐标纬度
   * @param measures M值
   * @return 返回创建的坐标对象
   */
  public static Coordinate create(int dimension, int measures)
  {
    if (dimension == 2) {
      return new CoordinateXY();
    } else if (dimension == 3 && measures == 0) {
      return new Coordinate();
    } else if (dimension == 3 && measures == 1) {
      return new CoordinateXYM();
    } else if (dimension == 4 && measures == 1) {
      return new CoordinateXYZM();
    }
    return new Coordinate();
  }
    
  /**
   * 根据{@link Coordinate}的子类确定维度。
   * 
   * @param coordinate 提供的坐标
   * @return 记录的纵坐标数
   */
  public static int dimension(Coordinate coordinate)
  {
    if (coordinate instanceof CoordinateXY) {
      return 2;
    } else if (coordinate instanceof CoordinateXYM) {
      return 3;
    } else if (coordinate instanceof CoordinateXYZM) {
      return 4;      
    } else if (coordinate instanceof Coordinate) {
      return 3;
    } 
    return 3;
  }

  /**
   * 根据{@link Coordinate}的子类确定度量数。
   * 
   * @param coordinate 提的供坐标
   * @return number of measures recorded
   */
  public static int measures(Coordinate coordinate)
  {
    if (coordinate instanceof CoordinateXY) {
      return 0;
    } else if (coordinate instanceof CoordinateXYM) {
      return 1;
    } else if (coordinate instanceof CoordinateXYZM) {
      return 1;
    } else if (coordinate instanceof Coordinate) {
      return 0;
    } 
    return 0;
  }
    
}
