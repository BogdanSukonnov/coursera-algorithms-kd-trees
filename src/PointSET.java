/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.TreeSet;

public class PointSET {

    private TreeSet<Point2D> pointSet;

    // construct an empty set of points
    public PointSET() {
        pointSet = new TreeSet<>();
    }

    // is the set empty?
    public boolean isEmpty() {
        return pointSet.isEmpty();
    }

    // number of points in the set
    public int size() {
        return pointSet.size();
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D point) {
        if (point == null) throw new IllegalArgumentException();
        pointSet.add(point);
    }

    // does the set contain point p?
    public boolean contains(Point2D point) {
        if (point == null) throw new IllegalArgumentException();
        return pointSet.contains(point);
    }

    // draw all points to standard draw
    public void draw() {
        for (Point2D point : pointSet) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.01);
            point.draw();
        }
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new IllegalArgumentException();
        TreeSet<Point2D> insidePointSet = new TreeSet<>();
        for (Point2D candidatePoint : pointSet) {
            if (rect.contains(candidatePoint)) {
                insidePointSet.add(candidatePoint);
            }
        }
        return insidePointSet;
    }

    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D point) {
        if (point == null) throw new IllegalArgumentException();
        Point2D nearestPoint = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (Point2D candidatePoint : pointSet) {
            double candidateDistance = point.distanceSquaredTo(candidatePoint);
            if (candidateDistance < shortestDistance) {
                nearestPoint = candidatePoint;
                shortestDistance = candidateDistance;
            }
        }
        return nearestPoint;
    }

    // unit testing of the methods (optional)
    public static void main(String[] args) {
    }
}
