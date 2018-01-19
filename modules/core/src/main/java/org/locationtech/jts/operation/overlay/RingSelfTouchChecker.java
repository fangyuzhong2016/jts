package org.locationtech.jts.operation.overlay;

import java.util.List;
import java.util.TreeSet;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.LinearRing;

/**
 * Checks whether a list of geometries contain a {@link LinearRing) component which self-touches at a vertex.
 * A LinearRing with a self-touch is invalid.
 * <p>
 * This can be used to check for invalid topology created by an overlay operation.
 * <p>
 * The restriction to check at vertices provides much faster performance than a full edge self-intersection test
 * 
 * @author Martin Davis
 *
 */
public class RingSelfTouchChecker {
  
  private List geomList;
  private Coordinate intPt = null;

  public RingSelfTouchChecker(List geomList) {
    this.geomList = geomList;
    checkTouch();
  }

  void checkTouch() {
    for (Object o : geomList) {
      Geometry geom = (Geometry) o;

      RingSelfTouchFilter filter = new RingSelfTouchFilter();
      geom.apply(filter);
      if (filter.hasIntersection()) {
        intPt = filter.getIntersection();
        return;
      }
    }
  }
  
  /**
   * Report whether some ring has a self-touch at a vertex.
   * @return true if soem ring has a self-touch
   */
  public boolean hasSelfTouch() {
    return intPt != null;
  }
  
  /**
   * Gets the location of the self-touch.
   * 
   * @return a coordinate for the location of the self-touch
   */
  public Coordinate getIntersection() {
    return intPt;
  }
  
  static class RingSelfTouchFilter implements GeometryComponentFilter {
    
    private Coordinate intPt;
    
    public void filter(Geometry g) {
      
      // needed because GeometryComponentFilter doesn't provide short-circuiting
      if (intPt != null) return;
      if (! (g instanceof LinearRing)) return;
      
      LinearRing ring = (LinearRing) g;
      TreeSet<Coordinate> coords = new TreeSet<Coordinate>();
      CoordinateSequence seq = ring.getCoordinateSequence();
      // test all except last (duplicated) endpoint
      for (int i = 0; i < seq.size()-1; i++) {
        Coordinate p = seq.getCoordinate(i);
        if (coords.contains(p)) {
          intPt = new Coordinate(p);
        }
        coords.add(p);
      }
    }
    public boolean hasIntersection() {
      return intPt != null;
    }
    public Coordinate getIntersection() {
      return intPt;
    }
  }
}
