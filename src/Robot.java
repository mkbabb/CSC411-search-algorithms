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
import java.util.Random;
import java.util.Stack;

/**
    Represents an intelligent agent moving through a particular room.
    The robot only has one sensor - the ability to get the status of any
    tile in the environment through the command env.getTileStatus(row, col).

    Your task is to modify the getAction method below so that is reached the
    TARGET POSITION with a minimal number of steps. There is only one (1)
    target position, which you can locate using env.getTargetRow() and
   env.getTargetCol()
*/

public class Robot {
    private Environment env;
    private int posRow;
    private int posCol;
    private String searchAlgorithm;
    public int expanded = 0;

    public boolean done;
    public boolean[][] visited;

    public Node startNode, targetNode;

    public Map<Node, Node> nodeTree;
    public Stack<Action> pathStack;

    public static int[][] directions = {{0, 0, -1, 1}, {1, -1, 0, 0}};

    public class Node implements Comparable<Node> {
        public int x, y;
        public Action action;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.action = Action.DO_NOTHING;
        }

        public Node(int x, int y, Action action) {
            this(x, y);
            this.action = action;
        }

        public int compareTo(Node other) {
            final var xCompare = Integer.compare(x, other.x);
            final var yCompare = Integer.compare(y, other.y);

            if (xCompare != 0) {
                return xCompare;
            } else if (yCompare != 0) {
                return yCompare;
            }

            return 0;
        }

        public Node copy() { return new Node(this.x, this.y, this.action); }

        @Override
        public boolean equals(Object o) {

            Node node = (Node)o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        public String toString() {
            return String.format("(%s, %s); %s", this.x, this.y, this.action);
        }
    }

    /**
        Initializes a Robot on a specific tile in the environment.
    */
    public Robot(Environment env) { this(env, 0, 0, ""); }
    public Robot(Environment env, int posRow, int posCol,
                 String searchAlgorithm) {
        this.env = env;
        this.posRow = posRow;
        this.posCol = posCol;
        this.searchAlgorithm = searchAlgorithm;

        this.startNode = new Node(posRow, posCol);
        this.targetNode = this.findTargetNode();

        this.nodeTree = new HashMap<Node, Node>();
        this.pathStack = new Stack<Action>();

        this.visited = new boolean[this.env.getRows()][this.env.getCols()];
    }

    public int getPosRow() { return posRow; }
    public int getPosCol() { return posCol; }
    public void incPosRow() { posRow++; }
    public void decPosRow() { posRow--; }
    public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }

    // BEGIN

    public double manhattanDistance(Node n1, Node n2) {
        return Math.abs(n1.x - n2.x) + Math.abs(n1.y - n2.y);
    }

    public double h(Node node) {
        return this.manhattanDistance(node, this.targetNode);
    }

    public Node findTargetNode() {
        for (int i = 0; i < env.getRows(); i++) {
            for (int j = 0; j < env.getCols(); j++) {
                final var status = env.getTileStatus(i, j);
                if (status == TileStatus.TARGET) {
                    return new Node(i, j);
                }
            }
        }
        return null;
    }

    public double getCost(Node node) {
        return this.env.getTileCost(node.x, node.y);
    }

    public ArrayList<Node> getNeighbors(Node node) {
        int row = node.x;
        int col = node.y;

        final var neighbors = new ArrayList<Node>();

        for (int i = 0; i < 4; i++) {
            final var x = row + directions[0][i];
            final var y = col + directions[1][i];

            if (this.env.validPos(x, y) && !this.visited[x][y]) {
                neighbors.add(new Node(x, y, Action.values()[i]));
            }
        }

        return neighbors;
    }

    public boolean finished(Node node) {
        return this.done =
                   this.env.getTileStatus(node.x, node.y) == TileStatus.TARGET;
    }

    public void createPath() {
        var node = this.targetNode;
        this.pathStack.add(this.targetNode.action);

        while (node != null && !node.equals(this.startNode)) {
            node = this.nodeTree.get(node);
            this.pathStack.add(node.action);
        }
    }

    public void DFS() {
        final Stack<Node> S = new Stack<Node>();
        S.add(this.startNode);

        while (!S.isEmpty()) {
            final var node = S.pop();

            if (this.finished(node)) {
                break;
            }

            final var neighbors = this.getNeighbors(node);

            for (final var n : neighbors) {
                S.push(n);

                final var parent = new Node(node.x, node.y, n.action);
                this.nodeTree.put(n, parent);

                this.visited[n.x][n.y] = true;
            }
        }
    }

    public void AStar() {
        final var fCost = new HashMap<Node, Double>();
        final var gCost = new HashMap<Node, Double>();

        final var open =
            new PriorityQueue<Node>(Comparator.comparing(fCost::get));
        final var closed = new HashSet<Node>();

        fCost.put(this.startNode, h(this.targetNode));
        gCost.put(this.startNode, 0.0);

        open.add(this.startNode);

        while (!open.isEmpty()) {
            final var node = open.peek();

            if (this.finished(node)) {
                break;
            }
            open.remove();
            closed.add(node);

            final var neighbors = this.getNeighbors(node);
            final var currentG = gCost.get(node);

            for (final var n : neighbors) {
                if (closed.contains(n)) {
                    continue;
                }

                final var g = gCost.getOrDefault(n, Double.MAX_VALUE);
                final var tentative =
                    currentG + this.getCost(node) + manhattanDistance(node, n);

                if (tentative < g) {
                    gCost.put(n, tentative);
                    fCost.put(n, tentative + h(n));

                    final var parent = new Node(node.x, node.y, n.action);
                    this.nodeTree.put(n, parent);

                    open.add(n);
                }
            }
        }
    }

    // END

    /**
     * Construct search tree before Robot start moving.
     */
    public void plan() {
        switch (searchAlgorithm) {
        case "DFS":
            this.DFS();
            break;
        case "AStar":
            this.AStar();
            break;
        case "RBFS":
            // this.RBFS();
            break;
        case "HillClimbing":
            // this.HillClimbing();
        default:
            break;
        }

        if (this.done) {
            this.createPath();
        }
    }

    /**
        Simulate the passage of a single time-step.
        At each time-step, the Robot decides which direction
        to move.
    */
    public Action getAction() {
        // you can get a tile's status with
        TileStatus status = env.getTileStatus(posRow, posCol);

        Action action = Action.DO_NOTHING;

        if (this.pathStack.isEmpty()) {
            return action;
        }

        switch (searchAlgorithm) {
        case "DFS":
            action = this.pathStack.pop();
            break;
        case "AStar":
            action = this.pathStack.pop();
            break;
        case "RBFS":
            action = this.pathStack.pop();
            break;
        case "HillClimbing":
            action = this.pathStack.pop();
            break;
        default:
            break;
        }
        return action;
    }
}