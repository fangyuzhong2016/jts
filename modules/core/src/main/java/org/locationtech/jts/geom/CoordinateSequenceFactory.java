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
 * 一个工厂接口，用于创建{@link CoordinateSequence}的具体实例。
 * 用于配置{@link GeometryFactory}以提供特定种类的CoordinateSequences。
 *
 * @version 1.7
 */
public interface CoordinateSequenceFactory
{

  /**
   * 根据给定的数组返回{@link CoordinateSequence}。
   * 数组是复制还是简单引用是依赖于实现的。
   * 此方法必须通过创建空序列来处理空参数。
   *
   * @param coordinates 坐标数组对象 coordinates
   */
  CoordinateSequence create(Coordinate[] coordinates);

  /**
   * 创建{@link CoordinateSequence}，它是给定{@link CoordinateSequence}的副本。
   * 此方法必须通过创建空序列来处理空参数。
   *
   * @param coordSeq 要复制的坐标序列
   */
  CoordinateSequence create(CoordinateSequence coordSeq);

  /**
   * 创建指定大小和维度的{@link CoordinateSequence}。
   * 为了使其有用，{@link CoordinateSequence}实现必须是可变的。
   * <p>
   * 如果请求的维度大于CoordinateSequence实现可以提供的维度，则应创建一系列最大可能维度。
   * 不应该抛出错误。
   *
   * @param size 序列中的坐标数
   * @param dimension 序列中坐标的维度（如果用户可指定，否则忽略）
   */
  CoordinateSequence create(int size, int dimension);

  /**
   * 使用M值支持创建指定大小和维度的{@link CoordinateSequence}。
   * 为了使其有用，{@link CoordinateSequence}实现必须是可变的。
   * <p>
   * 如果请求的维度或M值大于CoordinateSequence实现可以提供的，则应创建最大可能维度的序列。
   * 不应该抛出错误。
   *
   * @param size 序列中的坐标数
   * @param dimension 序列中坐标的维度（如果用户可指定，否则忽略）
   * @param measures 序列中坐标的M值（如果用户可指定，否则忽略）
   */
  default CoordinateSequence create(int size, int dimension, int measures) {
      return create(size, dimension);
  }
}