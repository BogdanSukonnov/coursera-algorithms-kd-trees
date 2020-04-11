/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdDraw;

public class KdTree {

    private Node root;
    private int size = 0;

    // is the set empty?
    public boolean isEmpty() {
        return root == null;
    }

    // number of points in the set
    public int size() {
        return size;
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D point) {
        if (point == null) throw new IllegalArgumentException();
        double minX = 0.0;
        double maxX = 1.0;
        double minY = 0.0;
        double maxY = 1.0;
        if (root == null) {
            root = new Node(point, new RectHV(point.x(), 0, point.x(), 1));
            ++size;
            return;
        }
        boolean isVertical = false;
        Node parent = root;
        while (true) {
            Node nextParent;
            boolean goLeft = true;
            if (isVertical) {
                if (point.y() < parent.getPoint().y()) {
                    nextParent = parent.getLeftBottom();
                    maxY = parent.getPoint().y();
                }
                else {
                    if (point.equals(parent.getPoint())) return;
                    nextParent = parent.getRightTop();
                    goLeft = false;
                    minY = parent.getPoint().y();
                }
            }
            else {
                if (point.x() < parent.getPoint().x()) {
                    nextParent = parent.getLeftBottom();
                    maxX = parent.getPoint().x();
                }
                else {
                    if (point.equals(parent.getPoint())) return;
                    nextParent = parent.getRightTop();
                    goLeft = false;
                    minX = parent.getPoint().x();
                }
            }
            // insert node
            if (nextParent == null) {
                RectHV rect;
                if (isVertical) {
                    rect = new RectHV(point.x(), minY, point.x(), maxY);
                }
                else {
                    rect = new RectHV(minX, point.y(), maxX, point.y());
                }
                Node newNode = new Node(point, rect);
                if (goLeft) {
                    parent.setLeftBottom(newNode);
                }
                else {
                    parent.setRightTop(newNode);
                }
                ++size;
                return;
            }
            // recursion
            parent = nextParent;
            isVertical = !isVertical;
        }
    }

    // does the set contain point p?
    public boolean contains(Point2D point) {
        if (point == null) throw new IllegalArgumentException();
        Node node = root;
        boolean vertical = true;
        while (true) {
            if (node == null) return false;
            if (point.equals(node.getPoint())) {
                return true;
            }
            if (vertical) {
                if (point.x() < node.getPoint().x()) {
                    node = node.leftBottom;
                }
                else {
                    node = node.rightTop;
                }
            }
            else {
                if (point.y() < node.getPoint().y()) {
                    node = node.leftBottom;
                }
                else {
                    node = node.rightTop;
                }
            }
            vertical = !vertical;
        }
    }

    // draw all points to standard draw
    public void draw() {
        drawNode(root, true);
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new IllegalArgumentException();
        Stack<Point2D> inStack = new Stack<>();
        if (size() == 0) return inStack;
        findInRect(root, true, inStack, rect);
        return inStack;
    }

    private void findInRect(Node candidate, boolean isVertical, Stack<Point2D> inStack,
                            RectHV rect) {
        if (candidate == null) return;
        if (rect.contains(candidate.getPoint())) {
            inStack.push(candidate.getPoint());
        }
        boolean needLeftSearch = false;
        boolean needRightSearch = false;
        if (rect.intersects(candidate.getRect())) {
            needLeftSearch = true;
            needRightSearch = true;
        }
        else if (isVertical) {
            if (rect.xmax() < candidate.getPoint().x()) {
                needLeftSearch = true;
            }
            else {
                needRightSearch = true;
            }
        }
        else {
            if (rect.ymax() < candidate.getPoint().y()) {
                needLeftSearch = true;
            }
            else {
                needRightSearch = true;
            }
        }
        if (needLeftSearch) {
            findInRect(candidate.getLeftBottom(), !isVertical, inStack, rect);
        }
        if (needRightSearch) {
            findInRect(candidate.getRightTop(), !isVertical, inStack, rect);
        }
    }

    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D point) {
        if (point == null) throw new IllegalArgumentException();
        if (size() == 0) return null;
        Point2D[] nearest = new Point2D[1];
        nearest[0] = root.getPoint();
        findNearest(root, true, new RectHV(0.0, 0.0, 1.0, 1.0), nearest, point);
        return nearest[0];
    }

    private void findNearest(Node candidate, boolean isVertical, RectHV outRect, Point2D[] nearest,
                             Point2D point) {
        if (candidate == null) return;
        if (candidate.getPoint().distanceSquaredTo(point) <
                nearest[0].distanceSquaredTo(point)) {
            nearest[0] = candidate.getPoint();
        }
        RectHV leftBottomRect = null;
        RectHV rightTopRect = null;
        if (isVertical) {
            leftBottomRect = new RectHV(outRect.xmin(), outRect.ymin(), point.x(), outRect.ymax());
            rightTopRect = new RectHV(point.x(), outRect.ymin(), outRect.xmax(), outRect.ymax());
        }
        else {
            leftBottomRect = new RectHV(outRect.xmin(), outRect.ymin(), outRect.xmax(), point.y());
            rightTopRect = new RectHV(outRect.xmin(), point.y(), outRect.xmax(), outRect.ymax());
        }
        if (point.distanceSquaredTo(closestPoint(leftBottomRect, point)) <
                point.distanceSquaredTo(nearest[0])) {
            findNearest(candidate.getLeftBottom(), !isVertical, leftBottomRect, nearest, point);
        }
        if (point.distanceSquaredTo(closestPoint(rightTopRect, point)) <
                point.distanceSquaredTo(nearest[0])) {
            findNearest(candidate.getRightTop(), !isVertical, rightTopRect, nearest, point);
        }
    }

    private Point2D closestPoint(RectHV rect, Point2D point) {
        if (rect == null || point == null || rect.contains(point)) return point;
        return new Point2D(closestCoordinate(rect.xmin(), rect.xmax(), point.x()),
                           closestCoordinate(rect.ymin(), rect.ymax(), point.y()));
    }

    private double closestCoordinate(double rectMin, double rectMax, double pointCoordinate) {
        // point between min and max - closest is point
        if (rectMin <= pointCoordinate && rectMax >= pointCoordinate) return pointCoordinate;
        // point is more that rect, return max of rect
        if (rectMin <= pointCoordinate) return Math.max(rectMin, rectMax);
        // point is less than rect, return min of rect
        return Math.min(rectMin, rectMax);
    }

    // unit testing of the methods (optional)
    public static void main(String[] args) {
        // no tests
    }

    private static class Node {
        // the point
        private final Point2D point;
        // the axis-aligned rectangle corresponding to this node
        private final RectHV rect;
        // the left/bottom subtree
        private Node leftBottom;
        // the right/top subtree
        private Node rightTop;

        public Node(Point2D point, RectHV rect) {
            this.point = point;
            this.rect = rect;
        }

        public void setLeftBottom(Node leftBottom) {
            this.leftBottom = leftBottom;
        }

        public void setRightTop(Node rightTop) {
            this.rightTop = rightTop;
        }

        public Point2D getPoint() {
            return point;
        }

        public RectHV getRect() {
            return rect;
        }

        public Node getLeftBottom() {
            return leftBottom;
        }

        public Node getRightTop() {
            return rightTop;
        }
    }

    private void drawNode(Node node, boolean isVertical) {
        if (node == null) return;

        drawNode(node.leftBottom, !isVertical);

        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.01);
        node.getPoint().draw();

        StdDraw.setPenColor(isVertical ? StdDraw.RED : StdDraw.BLUE);
        // StdDraw.setPenRadius(0.002);
        node.getRect().draw();

        drawNode(node.getRightTop(), !isVertical);
    }
}
