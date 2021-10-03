import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class PathFinder
{
    public Environment env;

    public int rowPos, colPos;

    public boolean reachedTarget;

    public boolean[][] visitedTiles;

    public Node startNode, endNode;
    public Map<Node, Node> nodes;

    public Map<Node, Node> pathMap;
    public ArrayList<Node> path;

    public static int[] rowVector = {0, 0, -1, 1};
    public static int[] colVector = {1, -1, 0, 0};

    public static Action mapActionIx(int ix)
    {
        return Action.values()[ix];
    }

    public class Node implements Comparable<Node>
    {
        public int x, y;
        public double f, g, h;

        public Action action;

        public Node(int x, int y)
        {
            this.x = x;
            this.y = y;
            this.action = Action.DO_NOTHING;

            this.f = Double.MAX_VALUE;
            this.g = Double.MAX_VALUE;
            this.h = 0;
        }

        public Node(int x, int y, Action action)
        {
            this(x, y);
            this.action = action;
        }

        @Override public int compareTo(Node n)
        {
            return Double.compare(this.f, n.f);
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
            return String.format("(%s, %s); %s", this.x, this.y, this.action);
        }
    }

    public PathFinder(Environment env, int rowPos, int colPos)
    {
        this.reachedTarget = false;

        this.env = env;
        this.rowPos = rowPos;
        this.colPos = colPos;

        this.startNode = new Node(this.rowPos, this.colPos, null);
        this.startNode.g = 0;
        this.startNode.h = h(this.startNode);

        this.endNode = null;

        this.nodes = new HashMap<Node, Node>();

        this.visitedTiles = new boolean[this.getRows()][this.getCols()];

        this.pathMap = new HashMap<Node, Node>();
        this.path = new ArrayList<Node>();

        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                this.visitedTiles[i][j] = false;
            }
        }
    }

    public double euclideanDistance(Node n1, Node n2)
    {
        return Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2));
    }

    public double manhattanDistance(Node n1, Node n2)
    {
        return Math.abs(n1.x - n2.x) + Math.abs(n1.y - n2.y);
    }

    public double h(Node node)
    {
        return 0;
    }

    public int getRows()
    {
        return this.env.getRows();
    }

    public int getCols()
    {
        return this.env.getCols();
    }

    public boolean isValid(int row, int col)
    {
        return this.env.validPos(row, col) && !this.visitedTiles[row][col];
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
                            row.append(".");
                    }
                }
            }
            System.out.println(row);
        }
    }

    public void getPath()
    {
        var node = this.endNode;

        this.path.add(this.endNode);

        while (node != null && !node.equals(this.startNode)) {
            node = this.pathMap.get(node);
            this.path.add(node);
        }

        Collections.reverse(this.path);
    }

    public ArrayList<Node> getNeighbors(Node node)
    {
        int baseRow = node.x;
        int baseCol = node.y;

        final var neighbors = new ArrayList<Node>();

        for (int i = 0; i < rowVector.length; i++) {
            final var row = baseRow + rowVector[i];
            final var col = baseCol + colVector[i];

            if (this.isValid(row, col)) {
                var child = new Node(row, col);
                child = this.nodes.getOrDefault(child, child);

                final var action = mapActionIx(i);
                child.action = action;

                // this.visitedTiles[row][col] = true;
                neighbors.add(child);

                this.nodes.put(node, node);
            }
        }

        return neighbors;
    }

    public boolean finishSearch(Node node)
    {
        if (this.env.getTileStatus(node.x, node.y) == TileStatus.TARGET) {
            this.endNode = node;
            this.reachedTarget = true;
            return true;
        } else {
            return false;
        }
    }

    public void DFS()
    {
        final var rowStack = new ArrayDeque<Integer>();
        final var colStack = new ArrayDeque<Integer>();

        rowStack.add(this.rowPos);
        colStack.add(this.colPos);

        while (!rowStack.isEmpty()) {
            final var row = rowStack.removeFirst();
            final var col = colStack.removeFirst();
            final var current = new Node(row, col);

            if (this.finishSearch(current)) {
                break;
            }

            final var neighbors = this.getNeighbors(current);

            neighbors.forEach((node) -> {
                rowStack.addFirst(node.x);
                colStack.addFirst(node.y);

                pathMap.put(node, new Node(current.x, current.y, node.action));
            });
        }
    }

    public void BFS()
    {
        final var rowQueue = new ArrayDeque<Integer>();
        final var colQueue = new ArrayDeque<Integer>();

        rowQueue.add(this.rowPos);
        colQueue.add(this.colPos);

        while (!rowQueue.isEmpty()) {
            final var row = rowQueue.remove();
            final var col = colQueue.remove();
            final var current = new Node(row, col);

            if (this.finishSearch(current)) {
                break;
            }

            final var neighbors = this.getNeighbors(current);

            neighbors.forEach((node) -> {
                rowQueue.add(node.x);
                colQueue.add(node.y);

                pathMap.put(node, new Node(current.x, current.y, node.action));
            });
        }
    }

    public void AStar()
    {
        final var openSet = new PriorityQueue<Node>();
        final var closedSet = new HashSet<Node>();

        openSet.add(this.startNode);

        while (!openSet.isEmpty()) {
            final var current = openSet.remove();
            closedSet.add(current);

            if (this.finishSearch(current)) {
                break;
            }

            final var neighbors = this.getNeighbors(current);

            neighbors.stream()
                .filter((node) -> !closedSet.contains(node))
                .forEach((node) -> {
                    final var cost = this.env.getTileCost(node.x, node.y);
                    final var g =
                        current.g + manhattanDistance(current, node) + cost;

                    if (g < node.g) {
                        node.g = g;
                        node.f = g + h(node);

                        final var parent =
                            new Node(current.x, current.y, node.action);
                        pathMap.put(node, parent);

                        if (!openSet.contains(node)) {
                            openSet.add(node);
                        }
                    }
                });
        }
    }

    public ArrayList<Node> search(String searchAlgorithm)
    {
        switch (searchAlgorithm) {
            case "BFS":
                this.BFS();
                break;
            case "DFS":
                this.DFS();
            case "AStar":
                this.AStar();
                break;
            case "RBFS":
                break;
            case "HillClimbing":
                break;
            default:
                break;
        }

        if (this.reachedTarget) {
            this.getPath();
            this.printPath();
            return this.path;
        } else {
            return null;
        }
    }
}
