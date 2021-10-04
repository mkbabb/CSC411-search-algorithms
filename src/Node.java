import java.util.Comparator;
import java.util.Objects;

public class Node {
    public int x, y;
    public boolean visited = false;
    public Node parent = null;

    public double f = 0;
    public double g = Double.MAX_VALUE;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Node(int x, int y, Node parent) {
        this(x, y);
        this.parent = parent;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public double getF() {
        return this.f;
    }

    public int compareTo(Node node) {
        return Comparator.comparing(Node::getX).thenComparing(Node::getY).compare(this, node);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Node)) {
            return false;
        }
        Node node = (Node) o;
        return this.x == node.x && this.y == node.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}
