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
    }

    public int getPosRow() { return posRow; }
    public int getPosCol() { return posCol; }
    public void incPosRow() { posRow++; }
    public void decPosRow() { posRow--; }
    public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }

    // BEGIN

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

    public ArrayList<Node> getNeighbors(Node node) {
        int baseRow = node.x;
        int baseCol = node.y;

        final var neighbors = new ArrayList<Node>();

        for (int i = 0; i < 4; i++) {
            final var row = baseRow + directions[0][i];
            final var col = baseCol + directions[1][i];

            if (this.env.validPos(row, col) && !this.visited[row][col]) {
                final var neighbor = new Node(row, col);
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    public boolean finished(Node node) {
        return this.env.getTileStatus(node.x, node.y) == TileStatus.TARGET;
    }

    public void createPath() {
        var node = this.targetNode;
        this.pathStack.add(this.targetNode.action);

        while (node != null && !node.equals(this.startNode)) {
            node = this.nodeTree.get(node);
            this.pathStack.add(node.action);
        }

        Collections.reverse(this.pathStack);
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
                this.nodeTree.put(n, node.copy());
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
            this.RBFS();
            break;
        case "HillClimbing":
            this.HillClimbing();
        default:
            break;
        }

        this.createPath();
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