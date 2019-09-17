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

import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * JTS的坐标序列接口，几何内部坐标列表的内部表示。
 * <p>
 * 这允许Geometries使用JTS {@link Coordinate}类之外的其他东西来存储它们的点。
 * 例如，存储高效的实现可能将坐标序列存储为x的数组和y的数组。或者自定义坐标类可能支持额外的属性，如M值。
 * <p>
 * 实现自定义坐标存储结构需要实现{@link CoordinateSequence}和{@link CoordinateSequenceFactory}接口。
 * 要使用自定义CoordinateSequence，请创建一个由CoordinateSequenceFactory参数化的新{@link GeometryFactory}
 * 然后可以使用{@link GeometryFactory}创建新的{@link Geometry}。
 * 新Geometries将使用自定义CoordinateSequence实现。
 * <p>
 *
 * @see CoordinateArraySequenceFactory
 * @see PackedCoordinateSequenceFactory
 *
 * @version 1.7
 */
public interface CoordinateSequence
    extends Cloneable
{
  /** 标准坐标索引，其中X为0 */
  int X = 0;

  /** 标准坐标索引，其中Y为1 */
  int Y = 1;
  
  /**
   * 标准坐标索引，其中Z为2。
   *
   * <p>
   *     此常量假定XYZM坐标序列定义，请在使用前使用{@link #getDimension()}和{@link #getMeasures()}检查此假设。
   */
  /**标准z坐标索引 */
  int Z = 2;

  /**
   * 标准坐标索引，其中M为3。
   *
   * <p>
   *     此常量假定XYZM坐标序列定义，请在使用前使用{@link #getDimension()}和{@link #getMeasures()}检查此假设*。
   */
  int M = 3;

  /**
   * 返回此序列的维度（每个坐标中的纵坐标数）。
   *
   * <p>
   *     此总数包括由非零{@link #getMeasures()}表示的任何度量。
   *
   * @return 序列的维度。
   */
  int getDimension();

  /**
   * 返回此序列的每个坐标在{@link #getDimension()}中包含的度量数。
   * 
   * 对于M的坐标序列，返回非零值。
   * <ul>
   * <li>对于XY序列，M是0</li>
   * <li>对于XYM序列,M是1<li>
   * <li>对于XYZ序列，M是0</li>
   * <li>对于XYZM，M是1</li>
   * <li>支持大于1的值</li>
   * </ul>
   *
   * @return 维度中包含的M的值
   */
  default int getMeasures() {
    return 0;
  }
  
  /**
   * 检查{@link #getDimension()}和{@link #getMeasures()}以确定是否支持{@link #getZ(int)}。
   * 
   * @return 如果支持{@link #getZ(int)}，则为true。
   */
  default boolean hasZ() {
      return (getDimension()-getMeasures()) > 2;
  }

  /**
   * 测试序列中的坐标是否具有与之相关的M。
   * 如果{@link #getMeasures()}> 0，则返回true。请参阅{@link #getMeasures()}以确定存在的M。
   *
   * @return 如果支持{@link #getM(int)}，则为true。
   *
   * @see #getMeasures()
   * @see #getM(int)
   */
  default boolean hasM() {
      return getMeasures() > 0;
  }

  /**
   * 创建一个用于此序列的坐标。
   * <p>
   * 创建的坐标支持相同数量的{@link #getDimension()}和{@link #getMeasures()}作为此序列，适用于{@link #getCoordinate(int, Coordinate )}。
   * </p>
   * @return 用于此序列的坐标
   */
  default Coordinate createCoordinate() {
    return Coordinates.create(getDimension(), getMeasures());
  }
  
  /**
   * 返回此序列中第i个坐标的（可能是副本）。
   * 返回的坐标是实际的基础坐标还是仅仅是副本取决于实施。
   * <p>
   * 请注意，将来此方法的语义可能会更改，以保证返回的Coordinate始终是副本。
   * 调用者不应该假设他们可以通过修改此方法返回的对象来修改CoordinateSequence。
   *
   * @param i 要检索的坐标的索引
   * @return 序列中的第i个坐标
   */
  Coordinate getCoordinate(int i);

  /**
   * 返回此序列中第i个坐标的副本。
   * 此方法优化了调用者无论如何要复制的情况 - 如果实现已经创建了新的Coordinate对象，则不需要进一步复制。
   *
   * @param i 要检索的坐标的索引
   * @return 序列中第i个坐标的副本
   */
  Coordinate getCoordinateCopy(int i);

  /**
   * 将序列中的第i个坐标复制到提供的坐标
   * {@link Coordinate}.  仅复制前两个维度。
   *
   * @param index 要复制的坐标的索引
   * @param coord 一个{@link Coordinate}来接收这个值
   */
  void getCoordinate(int index, Coordinate coord);

  /**
   * 返回指定坐标的纵坐标X（0）。
   *
   * @param index
   * @return 索引坐标系中X坐标的值
   */
  double getX(int index);

  /**
   * 返回指定坐标的纵坐标Y（1）。
   *
   * @param index
   * @return 索引坐标中Y坐标的值
   */
  double getY(int index);

  /**
   * 如果可用，返回指定坐标的纵坐标Z.
   * 
   * @param index
   * @return 索引坐标中Z坐标的值，如果未定义，则为Double.NaN。
   */
  default double getZ(int index)
  {
    if (hasZ()) {
        return getOrdinate(index, 2);
    } else {
        return Double.NaN;
    }
  }

  /**
   * 如果可用，返回指定坐标的纵坐标M.
   * 
   * @param index
   * @return 索引坐标中M坐标的值，如果未定义，则为Double.NaN。
   */
  default double getM(int index)
  {
    if (hasM()) {
      final int mIndex = getDimension()-getMeasures();
      return getOrdinate( index, mIndex );
    }
    else {
        return Double.NaN;
    }
  }
  
  /**
   * 返回此序列中坐标的纵坐标。
   * 纵坐标指数0和1假定为X和Y.
   * <p>
   * 坐标索引大于1具有用户定义的语义（例如，它们可能包含{@link #getDimension()}和{@link #getMeasures()}所描述的其他维度或M值。
   *
   * @param index  序列中的坐标索引
   * @param ordinateIndex the ordinate index in the coordinate (in range [0, dimension-1])
   */
  double getOrdinate(int index, int ordinateIndex);

  /**
   * 返回此序列中的坐标数。(坐标序列的长度)
   * @return 序列的大小
   */
  int size();

  /**
   * 以给定坐标的值设置此序列中坐标。
   *
   * @param index  序列中的坐标索引
   * @param ordinateIndex 坐标中的坐标索引(在 [0, dimension-1]范围内)
   * @param value  新的坐标值
   */
  void setOrdinate(int index, int ordinateIndex, double value);

  /**
   * 返回此坐标的集合（可能是副本）。
   * 返回的坐标是否是实际的基础坐标或仅仅是副本取决于实现。
   * 请注意，如果此实现不将其数据存储为Coordinates数组，则此方法将导致性能下降，因为需要从头开始构建数组。
   *
   * @return 包含此序列中的点值的坐标数组
   */
  Coordinate[] toCoordinateArray();

  /**
   * 扩展给定的{@link Envelope}以包含序列中的坐标。
   * 允许实现类以优化对坐标值的访问。
   *
   * @param env the envelope to expand
   * @return a ref to the expanded envelope
   */
  Envelope expandEnvelope(Envelope env);

  /**
   * 返回此集合的深层副本。
   * 由Geometry#clone调用。
   *
   * @return 包含所有点副本的坐标序列的副本
   * @deprecated 推荐{@link #copy()}
   */
  Object clone();
  
  /**
   * 返回此集合的深层副本。
   *
   * @return 包含所有点副本的坐标序列的副本
   */
  CoordinateSequence copy();
}
