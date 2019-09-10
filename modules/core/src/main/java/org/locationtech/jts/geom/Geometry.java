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
   *@return             如果任何<code>Geometry</code>的<code>isEmpty</code>方法返回<code>false</code>，就返回<code>true</code>
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
   * Gets the factory which contains the context in which this geometry was created.
   *
   * @return the factory for this geometry
   */
  public GeometryFactory getFactory() {
         return factory;
  }

  /**
   * Gets the user data object for this geometry, if any.
   *
   * @return the user data object, or <code>null</code> if none set
   */
  public Object getUserData() {
        return userData;
  }

  /**
   * Returns the number of {@link Geometry}s in a {@link GeometryCollection}
   * (or 1, if the geometry is not a collection).
   *
   * @return the number of geometries contained in this geometry
   */
  public int getNumGeometries() {
    return 1;
  }

  /**
   * Returns an element {@link Geometry} from a {@link GeometryCollection}
   * (or <code>this</code>, if the geometry is not a collection).
   *
   * @param n the index of the geometry element
   * @return the n'th geometry contained in this geometry
   */
  public Geometry getGeometryN(int n) {
    return this;
  }


  /**
   * A simple scheme for applications to add their own custom data to a Geometry.
   * An example use might be to add an object representing a Coordinate Reference System.
   * <p>
   * Note that user data objects are not present in geometries created by
   * construction methods.
   *
   * @param userData an object, the semantics for which are defined by the
   * application using this Geometry
   */
  public void setUserData(Object userData) {
        this.userData = userData;
  }


  /**
   *  Returns the <code>PrecisionModel</code> used by the <code>Geometry</code>.
   *
   *@return    the specification of the grid of allowable points, for this
   *      <code>Geometry</code> and all other <code>Geometry</code>s
   */
  public PrecisionModel getPrecisionModel() {
    return factory.getPrecisionModel();
  }

  /**
   *  Returns a vertex of this <code>Geometry</code>
   *  (usually, but not necessarily, the first one).
   *  The returned coordinate should not be assumed
   *  to be an actual Coordinate object used in
   *  the internal representation.
   *
   *@return    a {@link Coordinate} which is a vertex of this <code>Geometry</code>.
   *@return null if this Geometry is empty
   */
  public abstract Coordinate getCoordinate();
  
  /**
   *  Returns an array containing the values of all the vertices for 
   *  this geometry.
   *  If the geometry is a composite, the array will contain all the vertices
   *  for the components, in the order in which the components occur in the geometry.
   *  <p>
   *  In general, the array cannot be assumed to be the actual internal 
   *  storage for the vertices.  Thus modifying the array
   *  may not modify the geometry itself. 
   *  Use the {@link CoordinateSequence#setOrdinate} method
   *  (possibly on the components) to modify the underlying data.
   *  If the coordinates are modified, 
   *  {@link #geometryChanged} must be called afterwards.
   *
   *@return    the vertices of this <code>Geometry</code>
   *@see #geometryChanged
   *@see CoordinateSequence#setOrdinate
   */
  public abstract Coordinate[] getCoordinates();

  /**
   *  Returns the count of this <code>Geometry</code>s vertices. The <code>Geometry</code>
   *  s contained by composite <code>Geometry</code>s must be
   *  Geometry's; that is, they must implement <code>getNumPoints</code>
   *
   *@return    the number of vertices in this <code>Geometry</code>
   */
  public abstract int getNumPoints();

  /**
   * Tests whether this {@link Geometry} is simple.
   * The SFS definition of simplicity
   * follows the general rule that a Geometry is simple if it has no points of
   * self-tangency, self-intersection or other anomalous points.
   * <p>
   * Simplicity is defined for each {@link Geometry} subclass as follows:
   * <ul>
   * <li>Valid polygonal geometries are simple, since their rings
   * must not self-intersect.  <code>isSimple</code>
   * tests for this condition and reports <code>false</code> if it is not met.
   * (This is a looser test than checking for validity).
   * <li>Linear rings have the same semantics.
   * <li>Linear geometries are simple iff they do not self-intersect at points
   * other than boundary points.
   * <li>Zero-dimensional geometries (points) are simple iff they have no
   * repeated points.
   * <li>Empty <code>Geometry</code>s are always simple.
   * </ul>
   *
   * @return <code>true</code> if this <code>Geometry</code> is simple
   * @see #isValid
   */
  public boolean isSimple()
  {
    IsSimpleOp op = new IsSimpleOp(this);
    return op.isSimple();
  }

  /**
   * Tests whether this <code>Geometry</code>
   * is topologically valid, according to the OGC SFS specification.
   * <p>
   * For validity rules see the Javadoc for the specific Geometry subclass.
   *
   *@return <code>true</code> if this <code>Geometry</code> is valid
   *
   * @see IsValidOp
   */
  public boolean isValid()
  {
  	return IsValidOp.isValid(this);
  }

  /**
   * Tests whether the set of points covered by this <code>Geometry</code> is
   * empty.
   *
   *@return <code>true</code> if this <code>Geometry</code> does not cover any points
   */
  public abstract boolean isEmpty();

  /**
   *  Returns the minimum distance between this <code>Geometry</code>
   *  and another <code>Geometry</code>.
   *
   * @param  g the <code>Geometry</code> from which to compute the distance
   * @return the distance between the geometries
   * @return 0 if either input geometry is empty
   * @throws IllegalArgumentException if g is null
   */
  public double distance(Geometry g)
  {
    return DistanceOp.distance(this, g);
  }

  /**
   * Tests whether the distance from this <code>Geometry</code>
   * to another is less than or equal to a specified value.
   *
   * @param geom the Geometry to check the distance to
   * @param distance the distance value to compare
   * @return <code>true</code> if the geometries are less than <code>distance</code> apart.
   */
  public boolean isWithinDistance(Geometry geom, double distance)
  {
    return DistanceOp.isWithinDistance(this, geom, distance);
  }

  /**
   * Tests whether this is a rectangular {@link Polygon}.
   * 
   * @return true if the geometry is a rectangle.
   */
  public boolean isRectangle()
  {
    // Polygon overrides to check for actual rectangle
    return false;
  }

  /**
   *  Returns the area of this <code>Geometry</code>.
   *  Areal Geometries have a non-zero area.
   *  They override this function to compute the area.
   *  Others return 0.0
   *
   *@return the area of the Geometry
   */
  public double getArea()
  {
    return 0.0;
  }

  /**
   *  Returns the length of this <code>Geometry</code>.
   *  Linear geometries return their length.
   *  Areal geometries return their perimeter.
   *  They override this function to compute the area.
   *  Others return 0.0
   *
   *@return the length of the Geometry
   */
  public double getLength()
  {
    return 0.0;
  }

  /**
   * Computes the centroid of this <code>Geometry</code>.
   * The centroid
   * is equal to the centroid of the set of component Geometries of highest
   * dimension (since the lower-dimension geometries contribute zero
   * "weight" to the centroid).
   * <p>
   * The centroid of an empty geometry is <code>POINT EMPTY</code>.
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
   * Computes an interior point of this <code>Geometry</code>.
   * An interior point is guaranteed to lie in the interior of the Geometry,
   * if it possible to calculate such a point exactly. Otherwise,
   * the point may lie on the boundary of the geometry.
   * <p>
   * The interior point of an empty geometry is <code>POINT EMPTY</code>.
   *
   * @return a {@link Point} which is in the interior of this Geometry
   */
  public Point getInteriorPoint()
  {
    if (isEmpty()) return factory.createPoint();
    Coordinate pt = InteriorPoint.getInteriorPoint(this);
    return createPointFromInternalCoord(pt, this);
  }

  /**
   * Returns the dimension of this geometry.
   * The dimension of a geometry is is the topological 
   * dimension of its embedding in the 2-D Euclidean plane.
   * In the JTS spatial model, dimension values are in the set {0,1,2}.
   * <p>
   * Note that this is a different concept to the dimension of 
   * the vertex {@link Coordinate}s.  
   * The geometry dimension can never be greater than the coordinate dimension.
   * For example, a 0-dimensional geometry (e.g. a Point) 
   * may have a coordinate dimension of 3 (X,Y,Z). 
   *
   *@return the topological dimension of this geometry.
   */
  public abstract int getDimension();

  /**
   * Returns the boundary, or an empty geometry of appropriate dimension
   * if this <code>Geometry</code>  is empty.
   * (In the case of zero-dimensional geometries, '
   * an empty GeometryCollection is returned.)
   * For a discussion of this function, see the OpenGIS Simple
   * Features Specification. As stated in SFS Section 2.1.13.1, "the boundary
   * of a Geometry is a set of Geometries of the next lower dimension."
   *
   *@return    the closure of the combinatorial boundary of this <code>Geometry</code>
   */
  public abstract Geometry getBoundary();

  /**
   *  Returns the dimension of this <code>Geometry</code>s inherent boundary.
   *
   *@return    the dimension of the boundary of the class implementing this
   *      interface, whether or not this object is the empty geometry. Returns
   *      <code>Dimension.FALSE</code> if the boundary is the empty geometry.
   */
  public abstract int getBoundaryDimension();

  /**
   *  Gets a Geometry representing the envelope (bounding box) of 
   *  this <code>Geometry</code>. 
   *  <p>
   *  If this <code>Geometry</code> is:
   *  <ul>
   *  <li>empty, returns an empty <code>Point</code>. 
   *  <li>a point, returns a <code>Point</code>.
   *  <li>a line parallel to an axis, a two-vertex <code>LineString</code> 
   *  <li>otherwise, returns a
   *  <code>Polygon</code> whose vertices are (minx miny, maxx miny, 
   *  maxx maxy, minx maxy, minx miny).
   *  </ul>
   *
   *@return a Geometry representing the envelope of this Geometry
   *      
   * @see GeometryFactory#toGeometry(Envelope) 
   */
  public Geometry getEnvelope() {
    return getFactory().toGeometry(getEnvelopeInternal());
  }

  /**
   * Gets an {@link Envelope} containing 
   * the minimum and maximum x and y values in this <code>Geometry</code>.
   * If the geometry is empty, an empty <code>Envelope</code> 
   * is returned.
   * <p>
   * The returned object is a copy of the one maintained internally,
   * to avoid aliasing issues.  
   * For best performance, clients which access this
   * envelope frequently should cache the return value.
   *
   *@return the envelope of this <code>Geometry</code>.
   *@return an empty Envelope if this Geometry is empty
   */
  public Envelope getEnvelopeInternal() {
    if (envelope == null) {
      envelope = computeEnvelopeInternal();
    }
    return new Envelope(envelope);
  }

  /**
   * Notifies this geometry that its coordinates have been changed by an external
   * party (for example, via a {@link CoordinateFilter}). 
   * When this method is called the geometry will flush
   * and/or update any derived information it has cached (such as its {@link Envelope} ).
   * The operation is applied to all component Geometries.
   */
  public void geometryChanged() {
    apply(geometryChangedFilter);
  }

  /**
   * Notifies this Geometry that its Coordinates have been changed by an external
   * party. When #geometryChanged is called, this method will be called for
   * this Geometry and its component Geometries.
   * 
   * @see #apply(GeometryComponentFilter)
   */
  protected void geometryChangedAction() {
    envelope = null;
  }

  /**
   * Tests whether this geometry is disjoint from the argument geometry.
   * <p>
   * The <code>disjoint</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have no point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches 
   * <code>[FF*FF****]</code>
   * <li><code>! g.intersects(this) = true</code>
   * <br>(<code>disjoint</code> is the inverse of <code>intersects</code>)
   * </ul>
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s are
   *      disjoint
   *
   * @see Geometry#intersects
   */
  public boolean disjoint(Geometry g) {
    return ! intersects(g);
  }

  /**
   * Tests whether this geometry touches the
   * argument geometry.
   * <p>
   * The <code>touches</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point in common, 
   * but their interiors do not intersect.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns
   *  <ul>
   *   <li><code>[FT*******]</code>
   *   <li><code>[F**T*****]</code>
   *   <li><code>[F***T****]</code>
   *  </ul>
   * </ul>
   * If both geometries have dimension 0, the predicate returns <code>false</code>,
   * since points have only interiors.
   * This predicate is symmetric.
   * 
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s touch;
   *      Returns <code>false</code> if both <code>Geometry</code>s are points
   */
  public boolean touches(Geometry g) {
    // short-circuit test
    if (! getEnvelopeInternal().intersects(g.getEnvelopeInternal()))
      return false;
    return relate(g).isTouches(getDimension(), g.getDimension());
  }

  /**
   * Tests whether this geometry intersects the argument geometry.
   * <p>
   * The <code>intersects</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the patterns
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
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s intersect
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
   * Tests whether this geometry crosses the
   * argument geometry.
   * <p>
   * The <code>crosses</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have some but not all interior points in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * one of the following patterns:
   *   <ul>
   *    <li><code>[T*T******]</code> (for P/L, P/A, and L/A situations)
   *    <li><code>[T*****T**]</code> (for L/P, A/P, and A/L situations)
   *    <li><code>[0********]</code> (for L/L situations)
   *   </ul>
   * </ul>
   * For any other combination of dimensions this predicate returns <code>false</code>.
   * <p>
   * The SFS defined this predicate only for P/L, P/A, L/L, and L/A situations.
   * In order to make the relation symmetric,
   * JTS extends the definition to apply to L/P, A/P and A/L situations as well.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s cross.
   */
  public boolean crosses(Geometry g) {
    // short-circuit test
    if (! getEnvelopeInternal().intersects(g.getEnvelopeInternal()))
      return false;
    return relate(g).isCrosses(getDimension(), g.getDimension());
  }

  /**
   * Tests whether this geometry is within the
   * specified geometry.
   * <p>
   * The <code>within</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of this geometry is a point of the other geometry,
   * and the interiors of the two geometries have at least one point in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches 
   * <code>[T*F**F***]</code>
   * <li><code>g.contains(this) = true</code>
   * <br>(<code>within</code> is the converse of {@link #contains})
   * </ul>
   * An implication of the definition is that
   * "The boundary of a Geometry is not within the Geometry".
   * In other words, if a geometry A is a subset of
   * the points in the boundary of a geometry B, <code>A.within(B) = false</code>
   * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
   * For a predicate with similar behaviour but avoiding 
   * this subtle limitation, see {@link #coveredBy}.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> is within
   *      <code>g</code>
   *
   * @see Geometry#contains
   * @see Geometry#coveredBy
   */
  public boolean within(Geometry g) {
    return g.contains(this);
  }

  /**
   * Tests whether this geometry contains the
   * argument geometry.
   * <p>
   * The <code>contains</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry,
   * and the interiors of the two geometries have at least one point in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches 
   * the pattern
   * <code>[T*****FF*]</code>
   * <li><code>g.within(this) = true</code>
   * <br>(<code>contains</code> is the converse of {@link #within} )
   * </ul>
   * An implication of the definition is that "Geometries do not
   * contain their boundary".  In other words, if a geometry A is a subset of
   * the points in the boundary of a geometry B, <code>B.contains(A) = false</code>.
   * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
   * For a predicate with similar behaviour but avoiding 
   * this subtle limitation, see {@link #covers}.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> contains <code>g</code>
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
   * Tests whether this geometry overlaps the
   * specified geometry.
   * <p>
   * The <code>overlaps</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point each not shared by the other
   * (or equivalently neither covers the other),
   * they have the same dimension,
   * and the intersection of the interiors of the two geometries has
   * the same dimension as the geometries themselves.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   *   <code>[T*T***T**]</code> (for two points or two surfaces)
   *   or <code>[1*T***T**]</code> (for two curves)
   * </ul>
   * If the geometries are of different dimension this predicate returns <code>false</code>.
   * This predicate is symmetric.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s overlap.
   */
  public boolean overlaps(Geometry g) {
    // short-circuit test
    if (! getEnvelopeInternal().intersects(g.getEnvelopeInternal()))
      return false;
    return relate(g).isOverlaps(getDimension(), g.getDimension());
  }

  /**
   * Tests whether this geometry covers the
   * argument geometry.
   * <p>
   * The <code>covers</code> predicate has the following equivalent definitions:
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
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
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
   * The <code>coveredBy</code> predicate has the following equivalent definitions:
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
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
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
   *@param  g                the <code>Geometry</code> with which to compare
   *      this <code>Geometry</code>
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
   *  Returns the Well-known Text representation of this <code>Geometry</code>.
   *  For a definition of the Well-known Text format, see the OpenGIS Simple
   *  Features Specification.
   *
   *@return    the Well-known Text representation of this <code>Geometry</code>
   */
  public String toText() {
    WKTWriter writer = new WKTWriter();
    return writer.write(this);
  }

  /**
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

