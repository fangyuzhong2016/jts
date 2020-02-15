

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *{@link Coordinate}列表，可以设置为阻止列表中出现重复坐标。
 *
 *
 * @version 1.7
 */
public class CoordinateList
  extends ArrayList<Coordinate>
{
  private static final long serialVersionUID = -1626110935756089896L;
//With contributions from Markus Schaber [schabios@logi-track.com]
  //[Jon Aquino 2004-03-25]
  private final static Coordinate[] coordArrayType = new Coordinate[0];

  /**
   * 构造一个没有任何坐标的新列表
   */
  public CoordinateList()
  {
  }

  /**
   * 从一组坐标构造一个新列表，允许重复点。
   * （即，这个构造函数生成一个{@link CoordinateList}，它与输入数组具有完全相同的点集。）
   * 
   * @param coord 初始坐标
   */
  public CoordinateList(Coordinate[] coord)
  {
  	ensureCapacity(coord.length);
    add(coord, true);
  }

  /**
   * 从Coordinates数组构造一个新列表，允许调用者指定是否要删除重复的点。
   *
   * @param coord 要加载到列表中的坐标数组
   * @param allowRepeated 如果为 <code>false</code>，删除重复的点
   */
  public CoordinateList(Coordinate[] coord, boolean allowRepeated)
  {
  	ensureCapacity(coord.length);
    add(coord, allowRepeated);
  }

  public boolean add(Coordinate coord) {
	return super.add(coord);
  }

  public Coordinate getCoordinate(int i) { return (Coordinate) get(i); }


  /** 
   * 将一组坐标添加到列表中。
   * @param coord  坐标coordinates 对象
   * @param allowRepeated 如果设置为false，则折叠重复的坐标
   * @param start 开始的索引
   * @param end 要加起来但不包括的索引
   * @return true (as by general collection contract)
   */
  public boolean add(Coordinate[] coord, boolean allowRepeated, int start, int end)
  {
    int inc = 1;
    if (start > end) inc = -1;
    
    for (int i = start; i != end; i += inc) {
      add(coord[i], allowRepeated);
    }
    return true;
  }

  /** 
   * 向列表添加坐标数组。
   * @param coord The coordinates
   * @param allowRepeated 如果设置为false，则折叠重复的坐标
   * @param direction 果为false，则以相反的顺序添加数组
   * @return true (as by general collection contract)
   */
  public boolean add(Coordinate[] coord, boolean allowRepeated, boolean direction)
  {
    if (direction) {
      for (int i = 0; i < coord.length; i++) {
        add(coord[i], allowRepeated);
      }
    }
    else {
      for (int i = coord.length - 1; i >= 0; i--) {
        add(coord[i], allowRepeated);
      }
    }
    return true;
  }


  /** 
   * 向列表添加坐标数组。
   * @param coord The coordinates
   * @param allowRepeated 如果设置为false，则折叠重复的坐标
   * @return true (as by general collection contract)
   */
  public boolean add(Coordinate[] coord, boolean allowRepeated)
  {
    add(coord, allowRepeated, true);
    return true;
  }

  /** 
   * 将坐标添加到列表中。
   * @param obj 要添加的坐标
   * @param allowRepeated 如果设置为false，则折叠重复的坐标
   * @return true (as by general collection contract)
   */
  public boolean add(Object obj, boolean allowRepeated)
  {
    add((Coordinate) obj, allowRepeated);
    return true;
  }

  /**
   * 将坐标添加到列表的末尾。
   * 
   * @param coord The coordinates
   * @param allowRepeated 如果设置为false，则折叠重复的坐标
   */
  public void add(Coordinate coord, boolean allowRepeated)
  {
    // don't add duplicate coordinates
    if (! allowRepeated) {
      if (size() >= 1) {
        Coordinate last = (Coordinate) get(size() - 1);
        if (last.equals2D(coord)) return;
      }
    }
    super.add(coord);
  }

  /**
   * 将指定坐标插入此列表中的指定位置。
   * 
   * @param i 插入的位置
   * @param coord 要插入的坐标
   * @param allowRepeated 如果设置为false，则折叠重复的坐标
   */
  public void add(int i, Coordinate coord, boolean allowRepeated)
  {
    // don't add duplicate coordinates
    if (! allowRepeated) {
      int size = size();
      if (size > 0) {
        if (i > 0) {
          Coordinate prev = (Coordinate) get(i - 1);
          if (prev.equals2D(coord)) return;
        }
        if (i < size) {
          Coordinate next = (Coordinate) get(i);
          if (next.equals2D(coord)) return;
        }
      }
    }
    super.add(i, coord);
  }

  /**
   * 添加坐标数组
   * @param coll The coordinates
   * @param allowRepeated 如果设置为false，则折叠重复的坐标
   * @return true (as by general collection contract)
   */
  public boolean addAll(Collection<? extends Coordinate> coll, boolean allowRepeated)
  {
    boolean isChanged = false;
    for (Iterator<? extends Coordinate> i = coll.iterator(); i.hasNext(); ) {
      add(i.next(), allowRepeated);
      isChanged = true;
    }
    return isChanged;
  }

  /**
   * 如果需要，通过添加起点确保此coordList是一个环
   */
  public void closeRing()
  {
    if (size() > 0) {
      Coordinate duplicate = get(0).copy();
      add(duplicate, false);
    }
  }

  /**
   * 返回此集合中的坐标。
   *
   * @return the coordinates
   */
  public Coordinate[] toCoordinateArray()
  {
    return (Coordinate[]) toArray(coordArrayType);
  }

  /**
   * Creates an array containing the coordinates in this list,
   * oriented in the given direction (forward or reverse).
   * 
   * @param direction the direction value: true for forward, false for reverse
   * @return an oriented array of coordinates
   */
  public Coordinate[] toCoordinateArray(boolean isForward)
  {
    if (isForward) {
      return (Coordinate[]) toArray(coordArrayType);
    }
    // construct reversed array
    int size = size();
    Coordinate[] pts = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      pts[i] = get(size - i - 1);
    }
    return pts;
  }

  /**
   * 返回此<tt>CoordinateList</tt>实例的深层副本。
   *
   * @return 此 <tt>CoordinateList</tt>实例的克隆
   */
  public Object clone() {
      CoordinateList clone = (CoordinateList) super.clone();
      for (int i = 0; i < this.size(); i++) {	  
          clone.add(i, (Coordinate) this.get(i).clone());
      }
      return clone;
  }
}
