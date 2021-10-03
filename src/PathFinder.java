import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public class PathFinder
{
    public Environment env;
    public ArrayList<Queue<Integer>> dimQueues;

    public int rowPos, colPos;

    public boolean reachedTarget;

    public boolean[][] visitedTiles;

    public Node startNode, endNode;

    public Map<Node, Node> pathMap;
    public ArrayList<Node> path;

    // TODO: 8 directions?
    public static int[] rowVector = {-1, 1, 0, 0, -1, -1, 1, 1};
    public static int[] colVector = {0, 0, 1, -1, -1, 1, -1, 1};

    // public static int[] rowVector = {-1, 1, 0, 0};
    // public static int[] colVector = {0, 0, 1, -1};

    public class Node implements Comparable<Node>
    {
        public int x, y;

        public Node(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public int compareTo(Node other)
        {
            final var xCompare = Integer.compare(x, other.x);
            final var yCompare = Integer.compare(y, other.y);

            if (xCompare != 0) {
                return xCompare;
            } else if (yCompare != 0) {
                return yCompare;
            }

            return 0;
        }

        @Override public boolean equals(Object o)
        {
            if (o == this)
                return true;
            if (!(o instanceof Node)) {
                return false;
            }
            Node node = (Node) o;
            return Objects.equals(x, node.x) && Objects.equals(y, node.y);
        }

        @Override public int hashCode()
        {
            return Objects.hash(x, y);
        }

        public String toString()
        {
            return String.format("(%s, %s)", this.x, this.y);
        }
    }

    public PathFinder(Environment env, int rowPos, int colPos)
    {
        this.dimQueues = new ArrayList<Queue<Integer>>();

        this.dimQueues.add(new ArrayDeque<Integer>());
        this.dimQueues.add(new ArrayDeque<Integer>());

        this.reachedTarget = false;

        this.env = env;
        this.rowPos = rowPos;
        this.colPos = colPos;

        this.startNode = new Node(this.rowPos, this.colPos);
        this.endNode = null;

        this.visitedTiles = new boolean[this.getRows()][this.getCols()];

        this.pathMap = new HashMap<Node, Node>();
        this.path = new ArrayList<Node>();

        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                this.visitedTiles[i][j] = false;
            }
        }
    }

    public int getRows()
    {
        return this.env.getRows();
    }

    public int getCols()
    {
        return this.env.getCols();
    }

    public Queue<Integer> rowQueue()
    {
        return this.dimQueues.get(0);
    }

    public Queue<Integer> colQueue()
    {
        return this.dimQueues.get(1);
    }

    public boolean isValid(int row, int col)
    {
        return this.env.validPos(row, col) && !this.visitedTiles[row][col];
    }

    public void expandNode(int... positions)
    {
        int baseRow = positions[0];
        int baseCol = positions[1];

        final var parent = new Node(baseRow, baseCol);

        for (int i = 0; i < rowVector.length; i++) {
            final var row = baseRow + rowVector[i];
            final var col = baseCol + colVector[i];

            if (this.isValid(row, col)) {
                final var child = new Node(row, col);

                final var tileCost = this.env.getTileCost(row, col);

                this.rowQueue().add(row);
                this.colQueue().add(col);

                this.visitedTiles[row][col] = true;
                this.pathMap.put(child, parent);
            }
        }
    }

    public void printPath()
    {
        for (int i = 0; i < this.getRows(); i++) {
            final var row = new StringBuilder();

            for (int j = 0; j < this.getCols(); j++) {
                if (j > 0) {
                    row.append(" ");
                }

                final var node = new Node(i, j);

                if (this.path.contains(node)) {
                    row.append("*");
                } else {
                    final var status = this.env.getTileStatus(i, j);

                    switch (status) {
                        case IMPASSABLE:
                            row.append("x");
                            break;
                        case MOUNTAIN:
                            row.append("m");
                            break;
                        case PLAIN:
                            row.append(".");
                            break;
                        case PUDDLE:
                            row.append("w");
                            break;
                        case TARGET:
                            row.append("G");
                            break;
                        default:
                            break;
                    }
                }
            }
            System.out.println(row);
        }
    }

    public void backtrack()
    {
        var node = this.endNode;

        this.path.add(this.endNode);

        while (node != null && !node.equals(this.startNode)) {
            node = this.pathMap.get(node);
            this.path.add(node);
        }

        Collections.reverse(this.path);

        this.printPath();
    }

    public void BFS()
    {
        this.rowQueue().add(this.rowPos);
        this.colQueue().add(this.colPos);

        while (!this.rowQueue().isEmpty()) {
            final var row = this.rowQueue().remove();
            final var col = this.colQueue().remove();

            if (this.env.getTileStatus(row, col) == TileStatus.TARGET) {
                this.endNode = new Node(row, col);
                this.reachedTarget = true;
                break;
            }

            this.expandNode(row, col);
        }

        if (this.reachedTarget) {
            this.backtrack();
        }
    }
}
