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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.algorithm.InteriorPoint;
import org.locationtech.jts.geom.util.GeometryCollectionMapper;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.operation.IsSimpleOp;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlay.snap.SnapIfNeededOverlayOp;
import org.locationtech.jts.operation.predicate.RectangleContains;
import org.locationtech.jts.operation.predicate.RectangleIntersects;
import org.locationtech.jts.operation.relate.RelateOp;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.util.Assert;


/**
 * 平面线性矢量几何的表示。
 * <P>
 *
 *  <H3>二进制谓词</H3>
 *  因为目前还不清楚
 *  *涉及<code>GeometryCollection </code>的空间分析方法的语义是否有用，
 *  * <code>GeometryCollection</code>s不支持作为二进制的参数谓词或<code>relate</code>方法。
 * <H3>叠加方法</H3>
 *
 * overlay方法返回可能代表结果的最具体的类。
 * 如果结果是同类的，如果结果包含单个元素，则将返回<code>Point</code>，<code>LineString</code>或<code>Polygon</code>;
 * 否则，将返回<code>MultiPoint</code>，<code>MultiLineString</code>，或<code>MultiPolygon</code>。
 * 如果结果是异构的，则返回<code>GeometryCollection</code>。
 * <P>
 *
 * 因为目前尚不清楚涉及<code>GeometryCollection</code>的集合理论方法的语义是否有用，
 * <code>GeometryCollections</code>不支持作为集合论方法的参数。
 *
 *  <H4>计算几何的表示 </H4>
 *
 *  SFS指出，集合论方法的结果是通常的集合理论定义（SFS 3.2.21.1）的“点集”结果。
 *  但是，有时会有很多方法将点集表示为<code>Geometry</code>。
 *  <P>
 *
 *  SFS没有指定从空间分析方法返回的给定点集的明确表示。
 *  JTS的一个目标是使该规范精确且明确。
 *  JTS使用从覆盖方法返回的<code>Geometry</code>的规范形式。规范表单是<code>Geometry</code>，它简单且节点化:
 *  <UL>
 *    <LI> 简单意味着根据<code>isSimple</code>的JTS定义，返回的Geometry将很简单.
 *    <LI>Noded仅适用于涉及<code>LineString</code>的叠加层。
 *    它表示<code>LineString</code>s上的所有交叉点将作为结果中<code>LineString</code>的端点出现。
 *  </UL>
 *  这个定义意味着作为空间分析方法的参数的非简单几何必须经过线溶解过程以确保结果简单。
 *
 *  <H4>构造点与精度模型 </H4>
 *
 * 由集合论方法计算的结果可能包含输入<code>Geometry</code>s中不存在的构造点。
 * 这些新点来自输入<code>Geometry</code>的边缘中的线段之间的交叉点。
 * 在一般情况下，不可能准确地表示构造点。这是因为交点的坐标可能包含两倍于输入线段坐标的精度。
 * 为了显式地表示这些构造的点，JTS必须截断它们以适合<code>PrecisionModel</code>。 <P>
 *
 *  不幸的是，截断坐标会略微移动它们。在精确结果中不重合的线段可能在截断的表示中变得一致。
 *  这又导致“拓扑崩溃” ---计算元素的维度低于精确结果的情况。 <P>
 *
 *  当JTS在计算空间分析方法期间检测到拓扑崩溃时，它将引发异常。如果可能，例外将报告崩溃的位置。<P>
 *
 * <h3>几何相等</h3>
 * 
 * 有两种比较几何的方法：
 * <b>结构相等</b> 和 <b>拓扑相等</b>.
 * 
 * <h4>结构相等</h4>
 *
 * 结构相等由{@link #equalsExact(Geometry) }方法提供。这实现了基于精确的结构逐点相等的比较。
 * {@link #equals(Object)}是此方法的同义词，用于提供在Java集合中使用的结构相等语义。
 * 值得注意的是，结构逐点相等很容易受到环序和组件顺序等因素的影响。
 * 在许多情况下，最好在比较它们之前规范化几何（使用{@link #norm()}或{@link #normalize()}方法）。
 * 提供{@link #equalsNorm(Geometry)}作为计算规范化几何上的相等性的便捷方法，但使用起来很昂贵。
 * 最后，{@link #equalsExact(Geometry，double)}允许使用公差值进行点比较。
 * 
 * <h4>拓扑相等</h4>
 * 
 * 拓扑平等由{@link #equalsTopo(Geometry)}方法提供。
 * 它实现了根据DE-9IM矩阵定义的点集相等的SFS定义。
 * 为了支持SFS命名约定，方法{@link #equals(Geometry)}也作为同义词提供。
 * 但是，由于可能与{@link #equals(Object)}混淆，因此不建议使用它。
 * <p>
 * 由于重写了{@link #equals(Object)}和{@link #hashCode()}，因此可以在Java集合中有效地使用几何。
 *
 *@version 1.7
 */
public abstract class Geometry
    implements Cloneable, Comparable, Serializable
{
  private static final long serialVersionUID = 8763622679187376702L;
    
  static final int SORTINDEX_POINT = 0;
  static final int SORTINDEX_MULTIPOINT = 1;
  static final int SORTINDEX_LINESTRING = 2;
  static final int SORTINDEX_LINEARRING = 3;
  static final int SORTINDEX_MULTILINESTRING = 4;
  static final int SORTINDEX_POLYGON = 5;
  static final int SORTINDEX_MULTIPOLYGON = 6;
  static final int SORTINDEX_GEOMETRYCOLLECTION = 7;
  
  private final static GeometryComponentFilter geometryChangedFilter = new GeometryComponentFilter() {
    public void filter(Geometry geom) {
      geom.geometryChangedAction();
    }
  };

  /**
   *  此<code>Geometry</code>的边界框。
   */
  protected Envelope envelope;

  /**
   * {@link GeometryFactory}用于创建此Geometry
   */
  protected final GeometryFactory factory;

  /**
   *  此<code>Geometry</code>使用的空间参照系的ID
   */
  protected int SRID;

  /**
   * 一个对象引用，可用于携带客户端定义的辅助数据。
   */
  private Object userData = null;

  /**
   * 通过指定的GeometryFactory创建一个新的<code>Geometry</code>。
   *
   * @param factory
   */
  public Geometry(GeometryFactory factory) {
    this.factory = factory;
    this.SRID = factory.getSRID();
  }

  /**
   * 返回此Geometry的实际类的名称。
   *
   *@return 这个 <code>Geometry</code>的实际类的名称
   */
  public abstract String getGeometryType();

  /**
   * 如果数组包含任何非空<code>Geometry</code>，则返回true。
   *
   *@param  geometries  一个 <code>Geometry</code>的数组;没有元素可能是<code>null</code>
   *@return             如果任何<code>Geometry</code>的<code>isEmpty</code>方法返回<code>false</code>，
   *                    就返回<code>true</code>
   */
  protected static boolean hasNonEmptyElements(Geometry[] geometries) {
    for (int i = 0; i < geometries.length; i++) {
      if (!geometries[i].isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   * 如果数组包含任何<code>null</code>元素，则返回true。
   *
   *@param  array  要验证的数组
   *@return       如果 <code>array</code>的任何元素是<code>null</code>，则返回<code>true</code>
   */
  protected static boolean hasNullElements(Object[] array) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == null) {
        return true;
      }
    }
    return false;
  }

  /**
   *  返回<code>Geometry</code>使用的空间参考系统(Spatial Reference System)的ID。
   *  <P>
   *
   *  JTS以SFS中定义的简单方式支持空间参考系统信息。
   *  每个<code>Geometry</code>对象中都存在空间参考系统ID（SRID）。
   * <code>Geometry</code>为此字段提供基本访问者操作，但不提供其他操作。 SRID表示为整数。
   *
   *@return    定义 <code>Geometry</code>的坐标空间的ID。
   *
   */
  public int getSRID() {
    return SRID;
  }
    /**
   *  设置<code>Geometry</code>使用的空间参照系的ID。
   *  <p>
   *  <b>注意:</b> 此方法仅应用于特殊情况或向后兼容性。
     *  通常，应在用于创建几何的{@link GeometryFactory}上设置SRID。
   *  使用此方法设置的SRID将<i>not</i>传播到由构造方法返回的几何。
   *  
   *  @see GeometryFactory
   */
  public void setSRID(int SRID) {
    this.SRID = SRID;
  }

  /**
   * 获取创建Geometry的对象的工厂
   *
   * @return the factory for this geometry
   */
  public GeometryFactory getFactory() {
         return factory;
  }

  /**
   * 获取此几何体的用户数据对象（如果有）。
   *
   * @return 用户数据对象，或<code>null</code>如果没有设置
   */
  public Object getUserData() {
        return userData;
  }

  /**
   * 返回 {@link Geometry}中的 {@link GeometryCollection}的数量（如果几何不是集合，则为 1）。
   *
   * @return 几何体geometry中包含的geometry的数量
   */
  public int getNumGeometries() {
    return 1;
  }

  /**
   * 获取几何集合 {@link GeometryCollection} 中指定位置n的几何对象 {@link Geometry}
   * (如果几何不是集合，返回 <code>this</code>).
   *
   * @param n 几何元素的索引
   * @return 此几何体中包含的几何体
   */
  public Geometry getGeometryN(int n) {
    return this;
  }


  /**
   * 应用程序向几何体添加其自己的自定义数据的简单方案。
   * 例如，添加表示坐标参考系统的对象。
   * <p>
   * 请注意，在由构造方法创建的几何图形中不存在用户数据对象。
   *
   * @param userData 对象，其语义由应用程序使用此几何体定义
   */
  public void setUserData(Object userData) {
        this.userData = userData;
  }


  /**
   * 返回<code>Geometry</code>使用的<code>PrecisionModel</code>。
   *@return    the specification of the grid of allowable points, for this <code>Geometry</code> and all other <code>Geometry</code>s
   */
  public PrecisionModel getPrecisionModel() {
    return factory.getPrecisionModel();
  }

  /**
   *  返回该 <code>Geometry</code>的一个顶点坐标串对象
   *  (通常，但不一定，第一个).
   *  不应假定返回的坐标是内部表示中使用的实际坐标对象。
   *
   *@return    一个坐标串 {@link Coordinate} ，表示该 <code>Geometry</code> 的顶点
   *@return 如果此几何体为空，则为 null
   */
  public abstract Coordinate getCoordinate();
  
  /**
   *  返回包含此几何体的所有顶点值的数组。
   *  如果几何体是复合体，则数组将包含零部件的所有顶点，其顺序是零部件在几何体中的出现顺序。
   *  <p>
   *  通常，不能假定数组是顶点的实际内部存储。
   *  因此，修改数组可能不会修改几何本身。
   *  使用 {@link CoordinateSequence#setOrdinate}方法（可能在组件上）修改基础数据。
   *  如果坐标被修改，则随后必须调用{@link #geometryChanged}
   *@return    此几何的顶点坐标数组
   *@see #geometryChanged
   *@see CoordinateSequence#setOrdinate
   */
  public abstract Coordinate[] getCoordinates();

  /**
   * 返回此几何体 <code>Geometry</code>的顶点个数
   *  The <code>Geometry</code>s contained by composite <code>Geometry</code>s must be Geometry's; that is, they must implement <code>getNumPoints</code>
   *
   *@return    the number of vertices in this <code>Geometry</code>
   */
  public abstract int getNumPoints();

  /**
   * 测试该几何体 {@link Geometry}是否是简单的
   * SFS 简单性定义为 – 遵循一般规则，即如果几何体没有 自切点、自交点或其他异常点点，则其是简单的。
   * <p>
   * 为每个 {@link Geometry} 子类定义简单性，如下所示：
   * <ul>
   * <li>有效的多边形几何体很简单，因为它们的环不能自相交。使用<code>isSimple</code>方法测试，如果未能满足，返回<code>false</code>
   * (这是一个比检查有效性更宽松的测试).
   * <li>线性环具有相同的语义。
   * <li>线性几何体是简单的，如果它们不在边界点以外的点上自相交。
   * <li>零维几何（点）是简单的，如果它们没有重复点。
   * <li>空的<code>Geometry</code>总是简单的
   * </ul>
   *
   * @return  如果<code>Geometry</code>是简单的，返回<code>true</code>
   * @see #isValid
   */
  public boolean isSimple()
  {
    IsSimpleOp op = new IsSimpleOp(this);
    return op.isSimple();
  }

  /**
   * 根据 OGC SFS 规范，测试此<code>Geometry</code>在拓扑上是否有效。
   * <p>
   * 有关有效性规则，请参阅特定几何子类的 Javadoc。
   *
   *@return 如果<code>Geometry</code>在拓扑上是有效的，返回<code>true</code>
   *
   * @see IsValidOp
   */
  public boolean isValid()
  {
  	return IsValidOp.isValid(this);
  }

  /**
   * 测试此<code>Geometry</code>涵盖的点集是否为空。
   *
   *@return  如果此<code>Geometry</code>不包括任何点，返回<code>true</code>
   */
  public abstract boolean isEmpty();

  /**
   * 返回此<code>Geometry</code>与另一个<code>Geometry</code>之间的最小距离。
   *
   * @param  g 计算与该几何的最小距离的另一个<code>Geometry</code>
   * @return 几何体之间的距离
   * @return 如果任一输入几何体为空，则为 0
   * @throws IllegalArgumentException 如果 g 为空
   */
  public double distance(Geometry g)
  {
    return DistanceOp.distance(this, g);
  }

  /**
   * 测试此<code>Geometry</code>到另一个几何体的距离值是否小于或等于指定值。
   *
   * @param geom 待检查距离的Geometry
   * @param distance 要比较的距离值
   * @return 此<code>Geometry</code>到另一个几何体的距离值小于或等于指定值<code>distance</code>,返回 <code>true</code>
   */
  public boolean isWithinDistance(Geometry geom, double distance)
  {
    return DistanceOp.isWithinDistance(this, geom, distance);
  }

  /**
   * 测试此Geometry是否是矩形( {@link Polygon})
   * 
   * @return 如果几何体是矩形，则为 true。
   */
  public boolean isRectangle()
  {
    // Polygon overrides to check for actual rectangle
    return false;
  }

  /**
   * 返回此<code>Geometry</code>的面积。
   *  Areal 几何体具有非零面积。
   *  它们重写此函数以计算面积。其他返回 0.0
   *
   *@return 几何体的面积
   */
  public double getArea()
  {
    return 0.0;
  }

  /**
   *  返回此<code>Geometry</code>的长度。
   *  线性几何体返回其长度。
   *  Areal几何体返回其周长。
   *  它们重写此函数以计算长度。
   *  其他返回 0.0
   *
   *@return 几何的长度
   */
  public double getLength()
  {
    return 0.0;
  }

  /**
   * 计算此<code>Geometry</code>的质心。
   * 质心是等于设定最高维度的部件的几何形状的重心
   * (since the lower-dimension geometries contribute zero "weight" to the centroid).
   * <p>
   * 空几何的重心是<code>POINT EMPTY</code>。
   *
   * @return a {@link Point} which is the centroid of this Geometry
   */
  public Point getCentroid()
  {
    if (isEmpty()) 
      return factory.createPoint();
    Coordinate centPt = Centroid.getCentroid(this);
    return createPointFromInternalCoord(centPt, this);
  }

  /**
   * 计算此<code>Geometry</code>的内部点。
   * 如果可以精确计算此点，则保证内部点位于几何体的内部。 否则，点可能位于几何体的边界上。
   * <p>
   * 空几何体的内部点是<code>POINT EMPTY</code>。
   *
   * @return 返回位于此几何体的内部的点{@link Point}
   */
  public Point getInteriorPoint()
  {
    if (isEmpty()) return factory.createPoint();
    Coordinate pt = InteriorPoint.getInteriorPoint(this);
    return createPointFromInternalCoord(pt, this);
  }

  /**
   * 返回此几何体的维度。
   *几何体的维度是其嵌入在二维欧几里得平面中的拓扑维度。
   * 在 JTS 空间模型中，维度值位于集 [0，1，2]中。
   * <p>
   *     请注意，这与顶点坐标{@link Coordinate}的维度的概念不同。
   * Note that this is a different concept to the dimension of the vertex {@link Coordinate}s.
   * 几何维度不能大于坐标维度。
   * 例如，0 维几何体（例如点）的坐标维度可能为 3 （X，Y，Z）。
   *
   *@return 此几何体的拓扑维度。
   */
  public abstract int getDimension();

  /**
   * 返回该几何的边界几何体，如果该几何体<code>Geometry</code>是空的，则返回空几何体
   * (在零维几何体的情况下，将返回一个空几何集合。)
   * 有关此函数的讨论，请参阅 OpenGIS 简单功能规范。
   * 如 SFS 第 2.1.13.1 节所述，"几何的边界是下一个下维的一组几何。
   *
   *@return    返回该几何的边界几何体，如果该几何体<code>Geometry</code>是空的，则返回空几何体
   */
  public abstract Geometry getBoundary();

  /**
   * 返回该几何体的边界几何的维度
   *  Returns the dimension of this <code>Geometry</code>s inherent boundary.
   *
   *@return    实现此接口的类的边界的维度，无论此对象是否为空几何体. 如果边界为空几何体，返回<code>Dimension.FALSE</code> .
   */
  public abstract int getBoundaryDimension();

  /**
   * 获取表示此<code>Geometry</code>的包络（边框）几何 .
   *  <p>
   *  如果该 <code>Geometry</code> 是:
   *  <ul>
   *  <li>空几何, 返回空的 <code>Point</code>.
   *  <li>一个点, 返回该点 <code>Point</code>.
   *  <li>平行于轴线的一条线, 该线段的<code>LineString</code>顶点
   *  <li>否则, 返回<code>Polygon</code>的顶点，(minx miny, maxx miny, maxx maxy, minx maxy, minx miny).
   *  </ul>
   *
   *@return 表示此几何的包络线的几何
   *      
   * @see GeometryFactory#toGeometry(Envelope) 
   */
  public Geometry getEnvelope() {
    return getFactory().toGeometry(getEnvelopeInternal());
  }

  /**
   * 获取<code>Geometry</code>的内部包络矩形 {@link Envelope}(包含了最大最小XY值)
   * 如果该几何体是空的，则返回空的<code>Envelope</code>
   * <p>
   * 返回的对象是一个副本内部进行维护，以避免混淆的问题。
   * 为了获得最佳性能，这经常访问这个包络矩形的对象应该缓存的返回值。
   *
   *@return the envelope of this <code>Geometry</code>.
   *@return 一个空的包络矩形，如果这个几何对象是空
   */
  public Envelope getEnvelopeInternal() {
    if (envelope == null) {
      envelope = computeEnvelopeInternal();
    }
    return new Envelope(envelope);
  }

  /**
   * 通知该几何形状使得其坐标已通过外部方更改（例如，通过{@link CoordinateFilter}）。
   *当这种方法被称为几何将刷新和/或更新的任何导出的信息它已经高速缓存 (such as its {@link Envelope} ).
   * 该操作被施加到所有的部件的几何形状。
   */
  public void geometryChanged() {
    apply(geometryChangedFilter);
  }

  /**
   * 通知此几何，其坐标已经由外方改变。
   * 当#geometryChanged被调用时，该方法将被调用此几何及其部件的几何形状。
   * 
   * @see #apply(GeometryComponentFilter)
   */
  protected void geometryChangedAction() {
    envelope = null;
  }

  /**
   * 测试此几何是否是与给定参数的几何脱节(不相交)。
   * <p>
   *     <code>disjoint</code>(不相交)的谓词有如下的等价定义：
   * <ul>
   * <li>两个几何没有共同的点
   * <li>在DE-9IM交集矩阵的两个几何匹配<code>[FF*FF****]</code>
   * <li><code>! g.intersects(this) = true</code>
   * <br>(<code>disjoint</code> is the inverse of <code>intersects</code>)
   * </ul>
   *
   *@param  g  待比较的 <code>Geometry</code>
   *@return       如果两个几何不相交，返回 <code>true</code>
   *
   * @see Geometry#intersects
   */
  public boolean disjoint(Geometry g) {
    return ! intersects(g);
  }

  /**
   * 测试此几何是否与给定的几何体相邻(touches)。
   * <p>
   * 该<code>touches</code>谓词有如下的等价定义：
   * <ul>
   * <li> 几何形状有至少一个公共点，但他们的内部不相交。
   * <li>在DE-9IM交集矩阵的两个几何匹配下面模式中的至少一个
   *  <ul>
   *   <li><code>[FT*******]</code>
   *   <li><code>[F**T*****]</code>
   *   <li><code>[F***T****]</code>
   *  </ul>
   * </ul>
   * 如果两个几何的维度0，谓词返回false，因为点只有内部。该谓词是对称的。
   * 
   *
   *@param  g  待比较的<code>Geometry</code>
   *@return     如果两个几何体是相邻的返回<code>true</code>
   *      如果两个几何体是点，返回 <code>false</code>
   */
  public boolean touches(Geometry g) {
    // short-circuit test
    if (! getEnvelopeInternal().intersects(g.getEnvelopeInternal()))
      return false;
    return relate(g).isTouches(getDimension(), g.getDimension());
  }

  /**
   * 测试此几何是否与给定的参数几何体相交。
   * <p>
   *  <code>intersects</code> 谓词有如下的等价定义：
   * <ul>
   * <li>两个几何体至少有一个公共点
   * <li>在DE-9IM交集矩阵的两个几何相匹配下面的模式中的至少一个
   *  <ul>
   *   <li><code>[T********]</code>
   *   <li><code>[*T*******]</code>
   *   <li><code>[***T*****]</code>
   *   <li><code>[****T****]</code>
   *  </ul>
   * <li><code>! g.disjoint(this) = true</code>
   * <br>(<code>intersects</code> is the inverse of <code>disjoint</code>)
   * </ul>
   *
   *@param  g  待测试几何体 <code>Geometry</code>
   *@return      如果两个几何体相交，则返回 <code>true</code>
   *
   * @see Geometry#disjoint
   */
  public boolean intersects(Geometry g) {

    // short-circuit envelope test
    if (! getEnvelopeInternal().intersects(g.getEnvelopeInternal()))
      return false;

    /**
     * TODO: (MD) Add optimizations:
     *
     * - for P-A case:
     * If P is in env(A), test for point-in-poly
     *
     * - for A-A case:
     * If env(A1).overlaps(env(A2))
     * test for overlaps via point-in-poly first (both ways)
     * Possibly optimize selection of point to test by finding point of A1
     * closest to centre of env(A2).
     * (Is there a test where we shouldn't bother - e.g. if env A
     * is much smaller than env B, maybe there's no point in testing
     * pt(B) in env(A)?
     */

    // optimization for rectangle arguments
    if (isRectangle()) {
      return RectangleIntersects.intersects((Polygon) this, g);
    }
    if (g.isRectangle()) {
      return RectangleIntersects.intersects((Polygon) g, this);
    }
    if (isGeometryCollection() || g.isGeometryCollection()) {
      for (int i = 0 ; i < getNumGeometries() ; i++) {
        for (int j = 0 ; j < g.getNumGeometries() ; j++) {
          if (getGeometryN(i).intersects(g.getGeometryN(j))) {
            return true;
          }
        }
      }
      return false;
    }
    // general case
    return relate(g).isIntersects();
  }

  /**
   * 测试此几何是否与给定的参数几何体交叉(crosses)
   * <p>
   * The <code>crosses</code> 谓词有如下的等价定义:
   * <ul>
   * <li>几何形状有一些但不是全部共用内部点。
   * <li>在DE-9IM交集矩阵的两个几何匹配下面模式中的一个：
   *   <ul>
   *    <li><code>[T*T******]</code> (for P/L, P/A, and L/A situations)
   *    <li><code>[T*****T**]</code> (for L/P, A/P, and A/L situations)
   *    <li><code>[0********]</code> (for L/L situations)
   *   </ul>
   * </ul>
   * 对于这个谓词的其他的维度组合，返回 <code>false</code>
   * <p>
   * 该SFS只为P/L，P/A，L/L，和L/A的情况下所定义的本谓词。
   * 为了使关系是对称的，JTS延伸的定义适用于L/P，A/P和A/L的情况下也是如此。
   *
   *@param  g  待测试几何体 <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s cross.
   */
  public boolean crosses(Geometry g) {
    // short-circuit test
    if (! getEnvelopeInternal().intersects(g.getEnvelopeInternal()))
      return false;
    return relate(g).isCrosses(getDimension(), g.getDimension());
  }

  /**
   * 测试该几何体是否内含(within)在给定的几何体中
   * <p>
   * <code>within</code> 谓词有如下的等价定义:
   * <ul>
   * <li>几何形状A的线都在几何形状B内部。
   * <li>在DE-9IM交集矩阵的两个几何匹配 <code>[T*F**F***]</code>
   * <li><code>g.contains(this) = true</code>
   * <br>(<code>within</code> is the converse of {@link #contains})
   * </ul>
   * 这个谓词的定义意味着“指定的几何体的边界不在该几何体内部”。
   * 换句话说，如果几何体A是几何体B的边界点集的子集，那么<code>A.within(B) = false</code>
   * 对于类似的行为谓词，但避免这种微妙的限制，请参见{@link #coveredBy}。
   *
   *@param  g  待测试几何体 <code>Geometry</code>
   *@return     如果该几何体在指定的几何体内部，则返回<code>true</code>
   * @see Geometry#contains
   * @see Geometry#coveredBy
   */
  public boolean within(Geometry g) {
    return g.contains(this);
  }

  /**
   * 测试该几何体是否包含指定的几何体(指定的几何体在该几何体内部)
   * <p>
   * The <code>contains</code>谓词有如下的等价定义:
   * <ul>
   * <li>几何形状B的线都在几何形状A内部（区别于内含）
   * <li>在DE-9IM交集矩阵的两个几何匹配
   * <code>[T*****FF*]</code>
   * <li><code>g.within(this) = true</code>
   * <br>(<code>contains</code> is the converse of {@link #within} )
   * </ul>
   *
   *@param  g  待测试几何体 <code>Geometry</code>
   *@return    如果该几何体包含指定的几何体，则返回<code>true</code>
   *
   * @see Geometry#within
   * @see Geometry#covers
   */
  public boolean contains(Geometry g) {
    // optimization - lower dimension cannot contain areas
    if (g.getDimension() == 2 && getDimension() < 2) {
      return false;
    }
    // optimization - P cannot contain a non-zero-length L
    // Note that a point can contain a zero-length lineal geometry, 
    // since the line has no boundary due to Mod-2 Boundary Rule
    if (g.getDimension() == 1 && getDimension() < 1 && g.getLength() > 0.0) {
      return false;
    }
    // optimization - envelope test
    if (! getEnvelopeInternal().contains(g.getEnvelopeInternal()))
      return false;
    // optimization for rectangle arguments
    if (isRectangle()) {
      return RectangleContains.contains((Polygon) this, g);
    }
    // general case
    return relate(g).isContains();
  }

  /**
   * 测试给定的几何体是否与该几何体重叠
   * <p>
   * The <code>overlaps</code> 谓词有如下的等价定义:
   * <ul>
   * <li>几何形状共享一部分但不是所有的公共点，而且相交处有他们自己相同的区域
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   *   <code>[T*T***T**]</code> (for two points or two surfaces)
   *   or <code>[1*T***T**]</code> (for two curves)
   * </ul>
   * If the geometries are of different dimension this predicate returns <code>false</code>.
   * This predicate is symmetric.
   *
   *@param  g  待测试几何体 <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s overlap.
   */
  public boolean overlaps(Geometry g) {
    // short-circuit test
    if (! getEnvelopeInternal().intersects(g.getEnvelopeInternal()))
      return false;
    return relate(g).isOverlaps(getDimension(), g.getDimension());
  }

  /**
   * Tests whether this geometry covers the argument geometry.
   * <p>
   * The <code>covers</code> 谓词有如下的等价定义:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul> 
   *   <li><code>[T*****FF*]</code>
   *   <li><code>[*T****FF*]</code>
   *   <li><code>[***T**FF*]</code>
   *   <li><code>[****T*FF*]</code>
   *  </ul>
   * <li><code>g.coveredBy(this) = true</code>
   * <br>(<code>covers</code> is the converse of {@link #coveredBy})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #contains},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   * In particular, unlike <code>contains</code> it does not distinguish between
   * points in the boundary and in the interior of geometries.
   * For most situations, <code>covers</code> should be used in preference to <code>contains</code>.
   * As an added benefit, <code>covers</code> is more amenable to optimization,
   * and hence should be more performant.
   *
   *@param  g  待测试几何体 <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> covers <code>g</code>
   *
   * @see Geometry#contains
   * @see Geometry#coveredBy
   */
  public boolean covers(Geometry g) {
    // optimization - lower dimension cannot cover areas
    if (g.getDimension() == 2 && getDimension() < 2) {
      return false;
    }
    // optimization - P cannot cover a non-zero-length L
    // Note that a point can cover a zero-length lineal geometry
    if (g.getDimension() == 1 && getDimension() < 1 && g.getLength() > 0.0) {
      return false;
    }
    // optimization - envelope test
    if (! getEnvelopeInternal().covers(g.getEnvelopeInternal()))
      return false;
    // optimization for rectangle arguments
    if (isRectangle()) {
    	// since we have already tested that the test envelope is covered
      return true;
    }
    return relate(g).isCovers();
  }

  /**
   * Tests whether this geometry is covered by the
   * argument geometry.
   * <p>
   * The <code>coveredBy</code> 谓词有如下的等价定义:
   * <ul>
   * <li>Every point of this geometry is a point of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul>
   *   <li><code>[T*F**F***]</code>
   *   <li><code>[*TF**F***]</code>
   *   <li><code>[**FT*F***]</code>
   *   <li><code>[**F*TF***]</code>
   *  </ul>
   * <li><code>g.covers(this) = true</code>
   * <br>(<code>coveredBy</code> is the converse of {@link #covers})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #within},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   *
   *@param  g  待测试几何体 <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> is covered by <code>g</code>
   *
   * @see Geometry#within
   * @see Geometry#covers
   */
  public boolean coveredBy(Geometry g) {
    return g.covers(this);
  }

  /**
   * Tests whether the elements in the DE-9IM
   * {@link IntersectionMatrix} for the two <code>Geometry</code>s match the elements in <code>intersectionPattern</code>.
   * The pattern is a 9-character string, with symbols drawn from the following set:
   *  <UL>
   *    <LI> 0 (dimension 0)
   *    <LI> 1 (dimension 1)
   *    <LI> 2 (dimension 2)
   *    <LI> T ( matches 0, 1 or 2)
   *    <LI> F ( matches FALSE)
   *    <LI> * ( matches any value)
   *  </UL>
   *  For more information on the DE-9IM, see the <i>OpenGIS Simple Features
   *  Specification</i>.
   *
   *@param  g                the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@param  intersectionPattern  the pattern against which to check the
   *      intersection matrix for the two <code>Geometry</code>s
   *@return                      <code>true</code> if the DE-9IM intersection
   *      matrix for the two <code>Geometry</code>s match <code>intersectionPattern</code>
   * @see IntersectionMatrix
   */
  public boolean relate(Geometry g, String intersectionPattern) {
    return relate(g).matches(intersectionPattern);
  }

  /**
   *  Returns the DE-9IM {@link IntersectionMatrix} for the two <code>Geometry</code>s.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        an {@link IntersectionMatrix} describing the intersections of the interiors,
   *      boundaries and exteriors of the two <code>Geometry</code>s
   */
  public IntersectionMatrix relate(Geometry g) {
    checkNotGeometryCollection(this);
    checkNotGeometryCollection(g);
    return RelateOp.relate(this, g);
  }

  /**
  * Tests whether this geometry is 
  * topologically equal to the argument geometry.
   * <p>
   * This method is included for backward compatibility reasons.
   * It has been superseded by the {@link #equalsTopo(Geometry)} method,
   * which has been named to clearly denote its functionality.
   * <p>
   * This method should NOT be confused with the method 
   * {@link #equals(Object)}, which implements 
   * an exact equality comparison.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return true if the two <code>Geometry</code>s are topologically equal
   *
   *@see #equalsTopo(Geometry)
   */
  public boolean equals(Geometry g) {
    if (g == null) return false;
    return equalsTopo(g);
  }

  /**
   * Tests whether this geometry is topologically equal to the argument geometry
   * as defined by the SFS <code>equals</code> predicate.
   * <p>
   * The SFS <code>equals</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common,
   * and no point of either geometry lies in the exterior of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * the pattern <code>T*F**FFF*</code> 
   * <pre>
   * T*F
   * **F
   * FF*
   * </pre>
   * </ul>
   * <b>Note</b> that this method computes <b>topologically equality</b>. 
   * For structural equality, see {@link #equalsExact(Geometry)}.
   *
   *@param g the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return <code>true</code> if the two <code>Geometry</code>s are topologically equal
   *
   *@see #equalsExact(Geometry) 
   */
  public boolean equalsTopo(Geometry g)
  {
    // short-circuit test
    if (! getEnvelopeInternal().equals(g.getEnvelopeInternal()))
      return false;
    return relate(g).isEquals(getDimension(), g.getDimension());
  }
  
  /**
   * Tests whether this geometry is structurally and numerically equal
   * to a given <code>Object</code>.
   * If the argument <code>Object</code> is not a <code>Geometry</code>, 
   * the result is <code>false</code>.
   * Otherwise, the result is computed using
   * {@link #equalsExact(Geometry)}.
   * <p>
   * This method is provided to fulfill the Java contract
   * for value-based object equality. 
   * In conjunction with {@link #hashCode()} 
   * it provides semantics which are most useful 
   * for using
   * <code>Geometry</code>s as keys and values in Java collections.
   * <p>
   * Note that to produce the expected result the input geometries
   * should be in normal form.  It is the caller's 
   * responsibility to perform this where required
   * (using {@link Geometry#norm()}
   * or {@link #normalize()} as appropriate).
   * 
   * @param o the Object to compare
   * @return true if this geometry is exactly equal to the argument 
   * 
   * @see #equalsExact(Geometry)
   * @see #hashCode()
   * @see #norm()
   * @see #normalize()
   */
  public boolean equals(Object o)
  {
    if (! (o instanceof Geometry)) return false;
    Geometry g = (Geometry) o;
    return equalsExact(g);
  }
  
  /**
   * Gets a hash code for the Geometry.
   * 
   * @return an integer value suitable for use as a hashcode
   */
  public int hashCode()
  {
    return getEnvelopeInternal().hashCode();
  }
  
  public String toString() {
    return toText();
  }

  /**
   * 返回该几何体 <code>Geometry</code>的WKT文本
   *@return    几何体<code>Geometry</code>的WKT文本
   */
  public String toText() {
    WKTWriter writer = new WKTWriter();
    return writer.write(this);
  }

  /**
   * 计算该几何体给定宽度值值的缓冲区区域
	 * Computes a buffer area around this geometry having the given width. The
	 * buffer of a Geometry is the Minkowski sum or difference of the geometry
	 * with a disc of radius <code>abs(distance)</code>.
	 * <p> 
	 * Mathematically-exact buffer area boundaries can contain circular arcs. 
	 * To represent these arcs using linear geometry they must be approximated with line segments.
	 * The buffer geometry is constructed using 8 segments per quadrant to approximate 
	 * the circular arcs.
	 * The end cap style is <code>CAP_ROUND</code>.
	 * <p>
	 * The buffer operation always returns a polygonal result. The negative or
	 * zero-distance buffer of lines and points is always an empty {@link Polygon}.
	 * This is also the result for the buffers of degenerate (zero-area) polygons.
	 * 
	 * @param distance
	 *          the width of the buffer (may be positive, negative or 0)
	 * @return a polygonal geometry representing the buffer region (which may be
	 *         empty)
	 * 
	 * @throws TopologyException
	 *           if a robustness error occurs
	 * 
	 * @see #buffer(double, int)
	 * @see #buffer(double, int, int)
	 */
	public Geometry buffer(double distance) {
		return BufferOp.bufferOp(this, distance);
	}

  /**
	 * Computes a buffer area around this geometry having the given width and with
	 * a specified accuracy of approximation for circular arcs.
	 * <p>
	 * Mathematically-exact buffer area boundaries can contain circular arcs. 
	 * To represent these arcs
	 * using linear geometry they must be approximated with line segments. The
	 * <code>quadrantSegments</code> argument allows controlling the accuracy of
	 * the approximation by specifying the number of line segments used to
	 * represent a quadrant of a circle
	 * <p>
	 * The buffer operation always returns a polygonal result. The negative or
	 * zero-distance buffer of lines and points is always an empty {@link Polygon}.
	 * This is also the result for the buffers of degenerate (zero-area) polygons.
	 * 
	 * @param distance
	 *          the width of the buffer (may be positive, negative or 0)
	 * @param quadrantSegments
	 *          the number of line segments used to represent a quadrant of a
	 *          circle
	 * @return a polygonal geometry representing the buffer region (which may be
	 *         empty)
	 * 
	 * @throws TopologyException
	 *           if a robustness error occurs
	 * 
	 * @see #buffer(double)
	 * @see #buffer(double, int, int)
	 */
  public Geometry buffer(double distance, int quadrantSegments) {
    return BufferOp.bufferOp(this, distance, quadrantSegments);
  }

  /**
   * Computes a buffer area around this geometry having the given
   * width and with a specified accuracy of approximation for circular arcs,
   * and using a specified end cap style.
   * <p>
   * Mathematically-exact buffer area boundaries can contain circular arcs.
   * To represent these arcs using linear geometry they must be approximated with line segments.
   * The <code>quadrantSegments</code> argument allows controlling the
   * accuracy of the approximation
   * by specifying the number of line segments used to represent a quadrant of a circle
   * <p>
   * The end cap style specifies the buffer geometry that will be
   * created at the ends of linestrings.  The styles provided are:
   * <ul>
   * <li><code>BufferOp.CAP_ROUND</code> - (default) a semi-circle
   * <li><code>BufferOp.CAP_BUTT</code> - a straight line perpendicular to the end segment
   * <li><code>BufferOp.CAP_SQUARE</code> - a half-square
   * </ul>
	 * <p>
	 * The buffer operation always returns a polygonal result. The negative or
	 * zero-distance buffer of lines and points is always an empty {@link Polygon}.
	 * This is also the result for the buffers of degenerate (zero-area) polygons.
   *
   *@param  distance  the width of the buffer (may be positive, negative or 0)
   *@param quadrantSegments the number of line segments used to represent a quadrant of a circle
   *@param endCapStyle the end cap style to use
   *@return a polygonal geometry representing the buffer region (which may be empty)
   *
   * @throws TopologyException if a robustness error occurs
   *
   * @see #buffer(double)
   * @see #buffer(double, int)
   * @see BufferOp
   */
  public Geometry buffer(double distance, int quadrantSegments, int endCapStyle) {
    return BufferOp.bufferOp(this, distance, quadrantSegments, endCapStyle);
  }

  /**
   *  Computes the smallest convex <code>Polygon</code> that contains all the
   *  points in the <code>Geometry</code>. This obviously applies only to <code>Geometry</code>
   *  s which contain 3 or more points; the results for degenerate cases are
   *  specified as follows:
   *  <TABLE>
   *    <TR>
   *      <TH>    Number of <code>Point</code>s in argument <code>Geometry</code>   </TH>
   *      <TH>    <code>Geometry</code> class of result     </TH>
   *    </TR>
   *    <TR>
   *      <TD>        0      </TD>
   *      <TD>        empty <code>GeometryCollection</code>      </TD>
   *    </TR>
   *    <TR>  <TD>      1     </TD>
   *      <TD>     <code>Point</code>     </TD>
   *    </TR>
   *    <TR>
   *      <TD>      2     </TD>
   *      <TD>     <code>LineString</code>     </TD>
   *    </TR>
   *    <TR>
   *      <TD>       3 or more     </TD>
   *      <TD>      <code>Polygon</code>     </TD>
   *    </TR>
   *  </TABLE>
   *
   *@return    the minimum-area convex polygon containing this <code>Geometry</code>'
   *      s points
   */
  public Geometry convexHull() {
    return (new ConvexHull(this)).getConvexHull();
  }

  /**
   * Computes a new geometry which has all component coordinate sequences
   * in reverse order (opposite orientation) to this one.
   * 
   * @return a reversed geometry
   */
  public abstract Geometry reverse();
  
  /**
   * Computes a <code>Geometry</code> representing the point-set which is
   * common to both this <code>Geometry</code> and the <code>other</code> Geometry.
   * <p>
   * The intersection of two geometries of different dimension produces a result
   * geometry of dimension less than or equal to the minimum dimension of the input
   * geometries. 
   * The result geometry may be a heterogeneous {@link GeometryCollection}.
   * If the result is empty, it is an atomic geometry
   * with the dimension of the lowest input dimension.
   * <p>
   * Intersection of {@link GeometryCollection}s is supported
   * only for homogeneous collection types. 
   * <p>
   * Non-empty heterogeneous {@link GeometryCollection} arguments are not supported.
   *
   * @param  other the <code>Geometry</code> with which to compute the intersection
   * @return a Geometry representing the point-set common to the two <code>Geometry</code>s
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if the argument is a non-empty heterogeneous <code>GeometryCollection</code>
   */
  public Geometry intersection(Geometry other)
  {
  	/**
  	 * TODO: MD - add optimization for P-A case using Point-In-Polygon
  	 */
    // special case: if one input is empty ==> empty
    if (this.isEmpty() || other.isEmpty()) 
      return OverlayOp.createEmptyResult(OverlayOp.INTERSECTION, this, other, factory);

    // compute for GCs
    // (An inefficient algorithm, but will work)
    // TODO: improve efficiency of computation for GCs
    if (this.isGeometryCollection()) {
      final Geometry g2 = other;
      return GeometryCollectionMapper.map(
          (GeometryCollection) this,
          new GeometryMapper.MapOp() {
            public Geometry map(Geometry g) {
              return g.intersection(g2);
            }
      });
    }

    // No longer needed since GCs are handled by previous code
    //checkNotGeometryCollection(this);
    //checkNotGeometryCollection(other);
    return SnapIfNeededOverlayOp.overlayOp(this, other, OverlayOp.INTERSECTION);
  }

  /**
   * Computes a <code>Geometry</code> representing the point-set 
   * which is contained in both this
   * <code>Geometry</code> and the <code>other</code> Geometry.
   * <p>
   * The union of two geometries of different dimension produces a result
   * geometry of dimension equal to the maximum dimension of the input
   * geometries. 
   * The result geometry may be a heterogeneous
   * {@link GeometryCollection}.
   * If the result is empty, it is an atomic geometry
   * with the dimension of the highest input dimension.
   * <p>
   * Unioning {@link LineString}s has the effect of
   * <b>noding</b> and <b>dissolving</b> the input linework. In this context
   * "noding" means that there will be a node or endpoint in the result for
   * every endpoint or line segment crossing in the input. "Dissolving" means
   * that any duplicate (i.e. coincident) line segments or portions of line
   * segments will be reduced to a single line segment in the result. 
   * If <b>merged</b> linework is required, the {@link LineMerger}
   * class can be used.
   * <p>
   * Non-empty {@link GeometryCollection} arguments are not supported.
   * 
   * @param other
   *          the <code>Geometry</code> with which to compute the union
   * @return a point-set combining the points of this <code>Geometry</code> and the
   *         points of <code>other</code>
   * @throws TopologyException
   *           if a robustness error occurs
   * @throws IllegalArgumentException
   *           if either input is a non-empty GeometryCollection
   * @see LineMerger
   */
  public Geometry union(Geometry other)
  {
    // handle empty geometry cases
    if (this.isEmpty() || other.isEmpty()) {
      if (this.isEmpty() && other.isEmpty())
        return OverlayOp.createEmptyResult(OverlayOp.UNION, this, other, factory);
        
    // special case: if either input is empty ==> other input
      if (this.isEmpty()) return other.copy();
      if (other.isEmpty()) return copy();
    }
    
    // TODO: optimize if envelopes of geometries do not intersect
    
    checkNotGeometryCollection(this);
    checkNotGeometryCollection(other);
    return SnapIfNeededOverlayOp.overlayOp(this, other, OverlayOp.UNION);
  }

  /**
   * Computes a <code>Geometry</code> representing the closure of the point-set
   * of the points contained in this <code>Geometry</code> that are not contained in 
   * the <code>other</code> Geometry. 
   * <p>
   * If the result is empty, it is an atomic geometry
   * with the dimension of the left-hand input.
   * <p>
   * Non-empty {@link GeometryCollection} arguments are not supported.
   *
   *@param  other  the <code>Geometry</code> with which to compute the
   *      difference
   *@return a Geometry representing the point-set difference of this <code>Geometry</code> with
   *      <code>other</code>
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if either input is a non-empty GeometryCollection
   */
  public Geometry difference(Geometry other)
  {
    // special case: if A.isEmpty ==> empty; if B.isEmpty ==> A
    if (this.isEmpty()) return OverlayOp.createEmptyResult(OverlayOp.DIFFERENCE, this, other, factory);
    if (other.isEmpty()) return copy();

    checkNotGeometryCollection(this);
    checkNotGeometryCollection(other);
    return SnapIfNeededOverlayOp.overlayOp(this, other, OverlayOp.DIFFERENCE);
  }

  /**
   * Computes a <code>Geometry </code> representing the closure of the point-set
   * which is the union of the points in this <code>Geometry</code> which are not 
   * contained in the <code>other</code> Geometry,
   * with the points in the <code>other</code> Geometry not contained in this
   * <code>Geometry</code>. 
   * If the result is empty, it is an atomic geometry
   * with the dimension of the highest input dimension.
   * <p>
   * Non-empty {@link GeometryCollection} arguments are not supported.
   *
   *@param  other the <code>Geometry</code> with which to compute the symmetric
   *      difference
   *@return a Geometry representing the point-set symmetric difference of this <code>Geometry</code>
   *      with <code>other</code>
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if either input is a non-empty GeometryCollection
   */
  public Geometry symDifference(Geometry other)
  {
    // handle empty geometry cases
    if (this.isEmpty() || other.isEmpty()) {
      // both empty - check dimensions
      if (this.isEmpty() && other.isEmpty())
        return OverlayOp.createEmptyResult(OverlayOp.SYMDIFFERENCE, this, other, factory);
        
    // special case: if either input is empty ==> result = other arg
      if (this.isEmpty()) return other.copy();
      if (other.isEmpty()) return copy();
    }

    checkNotGeometryCollection(this);
    checkNotGeometryCollection(other);
    return SnapIfNeededOverlayOp.overlayOp(this, other, OverlayOp.SYMDIFFERENCE);
  }

	/**
	 * Computes the union of all the elements of this geometry. 
	 * <p>
	 * This method supports
	 * {@link GeometryCollection}s 
	 * (which the other overlay operations currently do not).
	 * <p>
	 * The result obeys the following contract:
	 * <ul>
	 * <li>Unioning a set of {@link LineString}s has the effect of fully noding
	 * and dissolving the linework.
	 * <li>Unioning a set of {@link Polygon}s always 
	 * returns a {@link Polygonal} geometry (unlike {@link #union(Geometry)},
	 * which may return geometries of lower dimension if a topology collapse occurred).
	 * </ul>
	 * 
	 * @return the union geometry
     * @throws TopologyException if a robustness error occurs
	 * 
	 * @see UnaryUnionOp
	 */
	public Geometry union() {
		return UnaryUnionOp.union(this);
	}
  
  /**
   * Returns true if the two <code>Geometry</code>s are exactly equal,
   * up to a specified distance tolerance.
   * Two Geometries are exactly equal within a distance tolerance
   * if and only if:
   * <ul>
   * <li>they have the same structure
   * <li>they have the same values for their vertices,
   * within the given tolerance distance, in exactly the same order.
   * </ul>
   * This method does <i>not</i>
   * test the values of the <code>GeometryFactory</code>, the <code>SRID</code>, 
   * or the <code>userData</code> fields.
   * <p>
   * To properly test equality between different geometries,
   * it is usually necessary to {@link #normalize()} them first.
   *
   * @param other the <code>Geometry</code> with which to compare this <code>Geometry</code>
   * @param tolerance distance at or below which two <code>Coordinate</code>s
   *   are considered equal
   * @return <code>true</code> if this and the other <code>Geometry</code>
   *   have identical structure and point values, up to the distance tolerance.
   *   
   * @see #equalsExact(Geometry)
   * @see #normalize()
   * @see #norm()
   */
  public abstract boolean equalsExact(Geometry other, double tolerance);

  /**
   * Returns true if the two <code>Geometry</code>s are exactly equal.
   * Two Geometries are exactly equal iff:
   * <ul>
   * <li>they have the same structure
   * <li>they have the same values for their vertices,
   * in exactly the same order.
   * </ul>
   * This provides a stricter test of equality than
   * {@link #equalsTopo(Geometry)}, which is more useful
   * in certain situations
   * (such as using geometries as keys in collections).
   * <p>
   * This method does <i>not</i>
   * test the values of the <code>GeometryFactory</code>, the <code>SRID</code>, 
   * or the <code>userData</code> fields.
   * <p>
   * To properly test equality between different geometries,
   * it is usually necessary to {@link #normalize()} them first.
   *
   *@param  other  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return <code>true</code> if this and the other <code>Geometry</code>
   *      have identical structure and point values.
   *      
   * @see #equalsExact(Geometry, double)
   * @see #normalize()
   * @see #norm()
   */
  public boolean equalsExact(Geometry other) 
  { 
    return this == other || equalsExact(other, 0);
  }

  /**
   * Tests whether two geometries are exactly equal
   * in their normalized forms.
   * This is a convenience method which creates normalized
   * versions of both geometries before computing
   * {@link #equalsExact(Geometry)}.
   * <p>
   * This method is relatively expensive to compute.  
   * For maximum performance, the client 
   * should instead perform normalization on the individual geometries
   * at an appropriate point during processing.
   * 
   * @param g a Geometry
   * @return true if the input geometries are exactly equal in their normalized form
   */
  public boolean equalsNorm(Geometry g)
  {
    if (g == null) return false;
    return norm().equalsExact(g.norm());
  }
  

  /**
   *  Performs an operation with or on this <code>Geometry</code>'s
   *  coordinates. 
   *  If this method modifies any coordinate values,
   *  {@link #geometryChanged} must be called to update the geometry state. 
   *  Note that you cannot use this method to
   *  modify this Geometry if its underlying CoordinateSequence's #get method
   *  returns a copy of the Coordinate, rather than the actual Coordinate stored
   *  (if it even stores Coordinate objects at all).
   *
   *@param  filter  the filter to apply to this <code>Geometry</code>'s
   *      coordinates
   */
  public abstract void apply(CoordinateFilter filter);

  /**
   *  Performs an operation on the coordinates in this <code>Geometry</code>'s
   *  {@link CoordinateSequence}s. 
   *  If the filter reports that a coordinate value has been changed, 
   *  {@link #geometryChanged} will be called automatically.
   *
   *@param  filter  the filter to apply
   */
  public abstract void apply(CoordinateSequenceFilter filter);

  /**
   *  Performs an operation with or on this <code>Geometry</code> and its
   *  subelement <code>Geometry</code>s (if any).
   *  Only GeometryCollections and subclasses
   *  have subelement Geometry's.
   *
   *@param  filter  the filter to apply to this <code>Geometry</code> (and
   *      its children, if it is a <code>GeometryCollection</code>).
   */
  public abstract void apply(GeometryFilter filter);

  /**
   *  Performs an operation with or on this Geometry and its
   *  component Geometry's.  Only GeometryCollections and
   *  Polygons have component Geometry's; for Polygons they are the LinearRings
   *  of the shell and holes.
   *
   *@param  filter  the filter to apply to this <code>Geometry</code>.
   */
  public abstract void apply(GeometryComponentFilter filter);

  /**
   * Creates and returns a full copy of this {@link Geometry} object
   * (including all coordinates contained by it).
   * Subclasses are responsible for overriding this method and copying
   * their internal data.  Overrides should call this method first.
   *
   * @return a clone of this instance
   * @deprecated
   */
  public Object clone() {
    try {
      Geometry clone = (Geometry) super.clone();
      if (clone.envelope != null) { clone.envelope = new Envelope(clone.envelope); }
      return clone;
    }
    catch (CloneNotSupportedException e) {
      Assert.shouldNeverReachHere();
      return null;
    }
  }
  
  /**
   * Creates a deep copy of this {@link Geometry} object.
   * Coordinate sequences contained in it are copied.
   * All instance fields are copied (i.e. the <tt>SRID</tt> and <tt>userData</tt>).
   * <p>
   * <b>NOTE:</b> the userData object reference (if present) is copied,
   * but the value itself is not copied.
   * If a deep copy is required this must be performed by the caller. 
   *
   * @return a deep copy of this geometry
   */
  public Geometry copy() {
    Geometry copy = copyInternal();
    copy.SRID = this.SRID;
    copy.userData = this.userData; 
    return copy;
  }
  
  /**
   * An internal method to copy subclass-specific geometry data.
   * 
   * @return a copy of the target geometry object.
   */
  protected abstract Geometry copyInternal();
  
  /**
   *  Converts this <code>Geometry</code> to <b>normal form</b> (or <b>
   *  canonical form</b> ). Normal form is a unique representation for <code>Geometry</code>
   *  s. It can be used to test whether two <code>Geometry</code>s are equal
   *  in a way that is independent of the ordering of the coordinates within
   *  them. Normal form equality is a stronger condition than topological
   *  equality, but weaker than pointwise equality. The definitions for normal
   *  form use the standard lexicographical ordering for coordinates. "Sorted in
   *  order of coordinates" means the obvious extension of this ordering to
   *  sequences of coordinates.
   *  <p>
   *  NOTE that this method mutates the value of this geometry in-place.
   *  If this is not safe and/or wanted, the geometry should be
   *  cloned prior to normalization.
   */
  public abstract void normalize();

  /**
   * Creates a new Geometry which is a normalized
   * copy of this Geometry. 
   * 
   * @return a normalized copy of this geometry.
   * @see #normalize()
   */
  public Geometry norm()
  {
    Geometry copy = copy();
    copy.normalize();
    return copy;
  }
  
  /**
   *  Returns whether this <code>Geometry</code> is greater than, equal to,
   *  or less than another <code>Geometry</code>. <P>
   *
   *  If their classes are different, they are compared using the following
   *  ordering:
   *  <UL>
   *    <LI> Point (lowest)
   *    <LI> MultiPoint
   *    <LI> LineString
   *    <LI> LinearRing
   *    <LI> MultiLineString
   *    <LI> Polygon
   *    <LI> MultiPolygon
   *    <LI> GeometryCollection (highest)
   *  </UL>
   *  If the two <code>Geometry</code>s have the same class, their first
   *  elements are compared. If those are the same, the second elements are
   *  compared, etc.
   *
   *@param  o  a <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return    a positive number, 0, or a negative number, depending on whether
   *      this object is greater than, equal to, or less than <code>o</code>, as
   *      defined in "Normal Form For Geometry" in the JTS Technical
   *      Specifications
   */
  public int compareTo(Object o) {
    Geometry other = (Geometry) o;
    if (getSortIndex() != other.getSortIndex()) {
      return getSortIndex() - other.getSortIndex();
    }
    if (isEmpty() && other.isEmpty()) {
      return 0;
    }
    if (isEmpty()) {
      return -1;
    }
    if (other.isEmpty()) {
      return 1;
    }
    return compareToSameClass(o);
  }

  /**
   *  Returns whether this <code>Geometry</code> is greater than, equal to,
   *  or less than another <code>Geometry</code>,
   * using the given {@link CoordinateSequenceComparator}.
   * <P>
   *
   *  If their classes are different, they are compared using the following
   *  ordering:
   *  <UL>
   *    <LI> Point (lowest)
   *    <LI> MultiPoint
   *    <LI> LineString
   *    <LI> LinearRing
   *    <LI> MultiLineString
   *    <LI> Polygon
   *    <LI> MultiPolygon
   *    <LI> GeometryCollection (highest)
   *  </UL>
   *  If the two <code>Geometry</code>s have the same class, their first
   *  elements are compared. If those are the same, the second elements are
   *  compared, etc.
   *
   *@param  o  a <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@param comp a <code>CoordinateSequenceComparator</code>
   *
   *@return    a positive number, 0, or a negative number, depending on whether
   *      this object is greater than, equal to, or less than <code>o</code>, as
   *      defined in "Normal Form For Geometry" in the JTS Technical
   *      Specifications
   */
  public int compareTo(Object o, CoordinateSequenceComparator comp) {
    Geometry other = (Geometry) o;
    if (getSortIndex() != other.getSortIndex()) {
      return getSortIndex() - other.getSortIndex();
    }
    if (isEmpty() && other.isEmpty()) {
      return 0;
    }
    if (isEmpty()) {
      return -1;
    }
    if (other.isEmpty()) {
      return 1;
    }
    return compareToSameClass(o, comp);
  }

  /**
   *  Returns whether the two <code>Geometry</code>s are equal, from the point
   *  of view of the <code>equalsExact</code> method. Called by <code>equalsExact</code>
   *  . In general, two <code>Geometry</code> classes are considered to be
   *  "equivalent" only if they are the same class. An exception is <code>LineString</code>
   *  , which is considered to be equivalent to its subclasses.
   *
   *@param  other  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *      for equality
   *@return        <code>true</code> if the classes of the two <code>Geometry</code>
   *      s are considered to be equal by the <code>equalsExact</code> method.
   */
  protected boolean isEquivalentClass(Geometry other) {
    return this.getClass().getName().equals(other.getClass().getName());
  }

  /**
   *  Throws an exception if <code>g</code>'s type is a <code>GeometryCollection</code>.
   *  (Its subclasses do not trigger an exception).
   *
   *@param  g the <code>Geometry</code> to check
   *@throws  IllegalArgumentException  if <code>g</code> is a <code>GeometryCollection</code>
   *      but not one of its subclasses
   */
  protected static void checkNotGeometryCollection(Geometry g) {
    if (g.isGeometryCollection()) {
      throw new IllegalArgumentException("Operation does not support GeometryCollection arguments");
    }
  }

  /**
   * Tests whether this is an instance of a general {@link GeometryCollection},
   * rather than a homogeneous subclass.
   * 
   * @return true if this is a heterogeneous GeometryCollection
   */
  protected boolean isGeometryCollection()
  {
    return getSortIndex() == SORTINDEX_GEOMETRYCOLLECTION;
  }

  /**
   *  Returns the minimum and maximum x and y values in this <code>Geometry</code>
   *  , or a null <code>Envelope</code> if this <code>Geometry</code> is empty.
   *  Unlike <code>getEnvelopeInternal</code>, this method calculates the <code>Envelope</code>
   *  each time it is called; <code>getEnvelopeInternal</code> caches the result
   *  of this method.
   *
   *@return    this <code>Geometry</code>s bounding box; if the <code>Geometry</code>
   *      is empty, <code>Envelope#isNull</code> will return <code>true</code>
   */
  protected abstract Envelope computeEnvelopeInternal();

  /**
   *  Returns whether this <code>Geometry</code> is greater than, equal to,
   *  or less than another <code>Geometry</code> having the same class.
   *
   *@param  o  a <code>Geometry</code> having the same class as this <code>Geometry</code>
   *@return    a positive number, 0, or a negative number, depending on whether
   *      this object is greater than, equal to, or less than <code>o</code>, as
   *      defined in "Normal Form For Geometry" in the JTS Technical
   *      Specifications
   */
  protected abstract int compareToSameClass(Object o);

  /**
   *  Returns whether this <code>Geometry</code> is greater than, equal to,
   *  or less than another <code>Geometry</code> of the same class.
   * using the given {@link CoordinateSequenceComparator}.
   *
   *@param  o  a <code>Geometry</code> having the same class as this <code>Geometry</code>
   *@param comp a <code>CoordinateSequenceComparator</code>
   *@return    a positive number, 0, or a negative number, depending on whether
   *      this object is greater than, equal to, or less than <code>o</code>, as
   *      defined in "Normal Form For Geometry" in the JTS Technical
   *      Specifications
   */
  protected abstract int compareToSameClass(Object o, CoordinateSequenceComparator comp);

  /**
   *  Returns the first non-zero result of <code>compareTo</code> encountered as
   *  the two <code>Collection</code>s are iterated over. If, by the time one of
   *  the iterations is complete, no non-zero result has been encountered,
   *  returns 0 if the other iteration is also complete. If <code>b</code>
   *  completes before <code>a</code>, a positive number is returned; if a
   *  before b, a negative number.
   *
   *@param  a  a <code>Collection</code> of <code>Comparable</code>s
   *@param  b  a <code>Collection</code> of <code>Comparable</code>s
   *@return    the first non-zero <code>compareTo</code> result, if any;
   *      otherwise, zero
   */
  protected int compare(Collection a, Collection b) {
    Iterator i = a.iterator();
    Iterator j = b.iterator();
    while (i.hasNext() && j.hasNext()) {
      Comparable aElement = (Comparable) i.next();
      Comparable bElement = (Comparable) j.next();
      int comparison = aElement.compareTo(bElement);
      if (comparison != 0) {
        return comparison;
      }
    }
    if (i.hasNext()) {
      return 1;
    }
    if (j.hasNext()) {
      return -1;
    }
    return 0;
  }

  protected boolean equal(Coordinate a, Coordinate b, double tolerance) {
    if (tolerance == 0) { return a.equals(b); }
    return a.distance(b) <= tolerance;
  }
  
  abstract protected int getSortIndex();

  private Point createPointFromInternalCoord(Coordinate coord, Geometry exemplar)
  {
    exemplar.getPrecisionModel().makePrecise(coord);
    return exemplar.getFactory().createPoint(coord);
  }


}

