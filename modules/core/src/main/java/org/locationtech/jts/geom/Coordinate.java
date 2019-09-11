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

import java.io.Serializable;
import java.util.Comparator;

import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.NumberUtil;


/**
 * 用于在二维笛卡尔平面上存储坐标的轻量级类。
 * 它与Point不同，Point是Geometry的子类。
 * 不同于Point类型的对象（包含诸如包络，精度模型和空间参考系统信息等附加信息），
 * Coordinate仅包含纵坐标值和存取方法。
 * 坐标是二维点，附加Z坐标。如果未指定或未定义Z坐标值，
 * 则构造坐标的Z坐标为NaN（也是NULL_ORDINATE的值）。
 * 标准比较函数忽略Z坐标。除了基本访问器功能外，JTS仅支持涉及Z纵坐标的特定操作。
 *
 *
 * 实现可以选择支持Z-ordinate和M-measure值，以适用于{@link CoordinateSequence}.
 * 建议使用{@link #getZ()}和{@link #getM()}访问器，或使用{@link #getOrdinate(int)}方法。</p>
 *
 * @version 1.16
 */
public class Coordinate implements Comparable<Coordinate>, Cloneable, Serializable {
  private static final long serialVersionUID = 6683108902428366910L;
  
  /**
   * 用于指示空值或缺少纵坐标值的值。
   * 特别是，用于尺寸大于坐标的定义尺寸的纵坐标值。
   */
  public static final double NULL_ORDINATE = Double.NaN;
  
  /** 标准纵坐标索引，其中X为0 */
  public static final int X = 0;

  /** 标准纵坐标索引，其中Y为1 */
  public static final int Y = 1;
  
  /**
   * 标准纵坐标索引，其中Z为2。
   *
   */
  public static final int Z = 2;

  /**
   * 标准纵坐标索引，其中M为3。
   *
   */
  public static final int M = 3;
  
  /**
   * x坐标。
   */
  public double x;
  
  /**
   * 纵坐标。
   */
  public double y;
  
  /**
   * z坐标。
   * <p>
   * 不鼓励直接访问该字段;使用{@link #getZ()}。
   */
  public double z;

  /**
   *  使用 (x,y,z)初始化构造<code>Coordinate</code>.
   *
   *@param  x x坐标
   *@param  y  y坐标
   *@param  z  z坐标
   */
  public Coordinate(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * 使用 (0,0,NaN)初始化构造<code>Coordinate</code>.
   */
  public Coordinate() {
    this(0.0, 0.0);
  }

  /**
   *  构造一个<code>Coordinate</code>，其值与<code>c</code>相同的（x，y，z）。
   *
   *@param  c  要复制的<code>Coordinate</code>。
   */
  public Coordinate(Coordinate c) {
    this(c.x, c.y, c.getZ());
  }

  /**
   * 使用 (x,y,NaN)初始化构造<code>Coordinate</code>.
   *
   *@param  x  x值
   *@param  y  y值
   */
  public Coordinate(double x, double y) {
    this(x, y, NULL_ORDINATE);
  }

  /**
   *  通过<code>other</code>的(x,y,z) 构造  <code>Coordinate</code>
   *@param  other  the <code>Coordinate</code> to copy
   */
  public void setCoordinate(Coordinate other) {
    x = other.x;
    y = other.y;
    z = other.getZ();
  }

  /**
   * 检索X坐标的值。
   *  
   *  @return X坐标的值
   */  
  public double getX() {
    return x;
  }

  /**
   * 设置X坐标值。
   * 
   * @param x X坐标值
   */
  public void setX(double x) {
    this.x = x;
  }
  
  /**
   *  检索Y坐标的值。
   *  
   *  @return Y坐标的值
   */  
  public double getY() {
      return y;      
  }

  /**
   * 设置Y坐标值。
   * 
   * @param y 要设置为Y的值
   */
  public void setY(double y) {
    this.y = y;
  }
  
  /**
   *  检索Z坐标的值（如果存在）。
   *  如果不存在Z值，则返回<tt>NaN</tt>。
   *  
   *  @return Z纵坐标的值，或<tt>NaN</tt>
   */   
  public double getZ() {
      return z;      
  }
  
  /**
   * 设置Z坐标值。
   * 
   * @param z 要设置为Z的值
   */
  public void setZ(double z) {
    this.z = z;
  }
  
  /**
   *  检索M的值（如果存在）。
   *  如果没有M值，则返回<tt>NaN</tt>。
   *  
   *  @return M的值，或<tt>NaN</tt>
   */    
  public double getM() {
    return Double.NaN;     
  }
  
  /**
   * 如果支持，设置M值。
   * 
   * @param m 要设置为M的值
   */
  public void setM(double m) {
    throw new IllegalArgumentException("Invalid ordinate index: " + M);
  }
  
  /**
   * 获取给定索引的坐标值。
   * 
   * @param ordinateIndex 坐标项索引
   * @return 获取的坐标项的值
   * @throws IllegalArgumentException 如果索引无效，抛出该异常
   */
  public double getOrdinate(int ordinateIndex)
  {
    switch (ordinateIndex) {
    case X: return x;
    case Y: return y;
    case Z: return getZ(); // sure to delegate to subclass rather than offer direct field access
    }
    throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
  }
  
  /**
   * 将给定索引的坐标设置为给定值。
   *
   * @param ordinateIndex 坐标项索引
   * @param value 待设置的值
   * @throws IllegalArgumentException 如果索引无效，抛出异常
   */
  public void setOrdinate(int ordinateIndex, double value)
  {
    switch (ordinateIndex) {
      case X:
        x = value;
        break;
      case Y:
        y = value;
        break;
      case Z:
        setZ(value); // delegate to subclass rather than offer direct field access
        break;
      default:
        throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
    }
  }

  /**
   *  返回两个<code>Coordinate</code>的平面投影坐标是否相等。
   *
   *@param  other 用于进行2D比较的<code>Coordinate</code>。
   *@return       如果x坐标和y坐标相等，则为<code>true</code>; z坐标不必相等。
   */
  public boolean equals2D(Coordinate other) {
    if (x != other.x) {
      return false;
    }
    if (y != other.y) {
      return false;
    }
    return true;
  }

  /**
   * 测试另一个坐标在指定的容差内是否具有相同的X和Y坐标值。
   * Z坐标被忽略。
   *
   *@param c 用于进行2D比较的<code>Coordinate</code>。
   *@param tolerance 要使用的容差值
   *@return 如果<code>other</code>和<code>Coordinate</code>是具有相同X和Y值，则为true。
   */
  public boolean equals2D(Coordinate c, double tolerance){
    if (! NumberUtil.equalsWithTolerance(this.x, c.x, tolerance)) {
      return false;
    }
    if (! NumberUtil.equalsWithTolerance(this.y, c.y, tolerance)) {
      return false;
    }
    return true;
  }
  
  /**
   * 测试另一个坐标是否具有X，Y和Z坐标的相同值。
   *
   *@param other 用于进行3D比较的<code> Coordinate </ code>。
   *@return 如果 <code>other</code>和<code>Coordinate</code> 的X，Y和Z的值相同，则为true。
   */
  public boolean equals3D(Coordinate other) {
    return (x == other.x) && (y == other.y) &&
               ((getZ() == other.getZ()) ||
               (Double.isNaN(getZ()) && Double.isNaN(other.getZ())));
  }
  
  /**
   * 在容差范围内测试另一个坐标是否具有相同的Z值。
   * 
   * @param c 待比较的坐标串coordinate
   * @param tolerance 容差值
   * @return 如果Z坐标在给定容差范围内，则为true
   */
  public boolean equalInZ(Coordinate c, double tolerance){
    return NumberUtil.equalsWithTolerance(this.getZ(), c.getZ(), tolerance);
  }
  
  /**
   * 如果<code>other</code>具有相同的x和y坐标值，则返回<code>true</code>。
   *  由于坐标为2.5D，因此该例程在进行比较时会忽略z值。
   *
   *@param  other  用于进行比较的<code>Coordinate</code>。
   *@return        如果<code>other</code>具有相同的x和y坐标值，则返回<code>true</code>。
   */
  public boolean equals(Object other) {
    if (!(other instanceof Coordinate)) {
      return false;
    }
    return equals2D((Coordinate) other);
  }

  /**
   *  将此{@link Coordinate}与指定的{@link Coordinate}进行比较以进行排序
   *  此方法在进行比较时忽略z值。
   *  返回:
   *  <UL>
   *    <LI> -1 : this.x &lt; other.x || ((this.x == other.x) &amp;&amp; (this.y &lt; other.y))
   *    <LI> 0 : this.x == other.x &amp;&amp; this.y = other.y
   *    <LI> 1 : this.x &gt; other.x || ((this.x == other.x) &amp;&amp; (this.y &gt; other.y))
   *
   *  </UL>
   *  注意: 此方法假定坐标值是有效数字。 NaN值未正确处理。
   *
   *@param  o  用于比较此<code>Coordinate</code>的另一个<code>Coordinate</code>
   *@return    -1, 0, 和 1 分别表示该坐标 <code>Coordinate</code> 小于、等于和大于指定的<code>Coordinate</code>
   */
  public int compareTo(Coordinate o) {
    Coordinate other = (Coordinate) o;

    if (x < other.x) return -1;
    if (x > other.x) return 1;
    if (y < other.y) return -1;
    if (y > other.y) return 1;
    return 0;
  }

  /**
   * 返回<I>(x,y,z)</I>  形式的坐标字符串
   *
   *@return   <I>(x,y,z)</I> 形式的坐标字符串
   */
  public String toString() {
    return "(" + x + ", " + y + ", " + getZ() + ")";
  }

  /**
   * 复制坐标对象 {@link Coordinate}
   * @return
   */
  public Object clone() {
    try {
      Coordinate coord = (Coordinate) super.clone();

      return coord; // return the clone
    } catch (CloneNotSupportedException e) {
      Assert.shouldNeverReachHere(
          "this shouldn't happen because this class is Cloneable");

      return null;
    }
  }
  
  /**
   * 创建此坐标{@link Coordinate}的副本。
   * 
   * @return 这个坐标的副本。
   */
  public Coordinate copy() {
    return new Coordinate(this);
  }

  /**
   * 计算两点的二维欧几里德距离。
   * Z纵坐标被忽略。
   * 
   * @param c 一个点
   * @return 两点之间的二维欧几里德距离
   */
  public double distance(Coordinate c) {
    double dx = x - c.x;
    double dy = y - c.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * 计算两点位置的三维欧几里德距离。
   * 
   * @param c 一个坐标
   * @return 两点之间的三维欧几里德距离
   */
  public double distance3D(Coordinate c) {
    double dx = x - c.x;
    double dy = y - c.y;
    double dz = getZ() - c.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  /**
   * 获取此坐标的哈希码。
   * 
   * @return 此坐标的哈希码
   */
  public int hashCode() {
    //Algorithm from Effective Java by Joshua Bloch [Jon Aquino]
    int result = 17;
    result = 37 * result + hashCode(x);
    result = 37 * result + hashCode(y);
    return result;
  }

  /**
   *
   * 使用Joshua Bloch的书<i>Effective Java</i>中的算法计算double值的哈希码
   * 
   * @param x 要计算的值
   * @return x的哈希码
   */
  public static int hashCode(double x) {
    long f = Double.doubleToLongBits(x);
    return (int)(f^(f>>>32));
  }


  /**
   * 比较两个坐标，允许进行二维或三维比较，并正确处理NaN值。
   */
  public static class DimensionalComparator
      implements Comparator<Coordinate>
  {
    /**
     * 比较两个<code>double</code>，允许NaN值。
     * NaN被视为小于任何有效数字。
     *
     * @param a  待比较的<code>double</code>
     * @param b  待比较的<code>double</code>
     * @return -1,0或1取决于a是否小于，等于或大于b
     */
    public static int compare(double a, double b)
    {
      if (a < b) return -1;
      if (a > b) return 1;

      if (Double.isNaN(a)) {
        if (Double.isNaN(b)) return 0;
        return -1;
      }

      if (Double.isNaN(b)) return 1;
      return 0;
    }

    private int dimensionsToTest = 2;

    /**
     * 为2维坐标创建比较器。
     */
    public DimensionalComparator()
    {
      this(2);
    }

    /**
     * 根据提供的值创建2或3维坐标的比较器。
     *
     * @param dimensionsToTest 要测试的维数
     */
    public DimensionalComparator(int dimensionsToTest)
    {
      if (dimensionsToTest != 2 && dimensionsToTest != 3)
        throw new IllegalArgumentException("only 2 or 3 dimensions may be specified");
      this.dimensionsToTest = dimensionsToTest;
    }

    /**
     *比较两个 {@link Coordinate}
     *
     * @param c1 待比较的{@link Coordinate}
     * @param c2 待比较的 {@link Coordinate}
     * @return -1,0或1取决于c1是否小于，等于或大于c2
     *
     */
    public int compare(Coordinate c1, Coordinate c2)
    {
      int compX = compare(c1.x, c2.x);
      if (compX != 0) return compX;

      int compY = compare(c1.y, c2.y);
      if (compY != 0) return compY;

      if (dimensionsToTest <= 2) return 0;

      int compZ = compare(c1.getZ(), c2.getZ());
      return compZ;
    }
  }

}