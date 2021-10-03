import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
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
    public Stack<Node> pathStack;

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
            if (o == null) {
                return false;
            }
            
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
        this.pathStack = new Stack<Node>();

        this.visited = new boolean[this.env.getRows()][this.env.getCols()];
    }

    public int getPosRow() { return posRow; }
    public int getPosCol() { return posCol; }
    public void incPosRow() { posRow++; }
    public void decPosRow() { posRow--; }
    public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }

    // BEGIN
    public void reset() {
        for (int i = 0; i < this.env.getRows(); i++) {
            for (int j = 0; j < this.env.getCols(); j++) {
                this.visited[i][j] = false;
            }
        }
        this.nodeTree.clear();
    }

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
                this.visited[x][y] = true;
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
        this.pathStack.add(this.targetNode);

        while (node != null && !node.equals(this.startNode)) {
            node = this.nodeTree.get(node);
            this.pathStack.add(node);
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

            for (final var child : neighbors) {
                S.push(child);

                final var parent = new Node(node.x, node.y, child.action);
                this.nodeTree.put(child, parent);
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

            for (final var child : neighbors) {
                if (closed.contains(child)) {
                    continue;
                }

                final var g = gCost.getOrDefault(child, Double.MAX_VALUE);
                final var tentativeG = currentG + this.getCost(node) +
                                       manhattanDistance(node, child);

                if (tentativeG < g) {
                    final var f = tentativeG + h(child);

                    gCost.put(child, tentativeG);
                    fCost.put(child, f);

                    final var parent = new Node(node.x, node.y, child.action);
                    this.nodeTree.put(child, parent);

                    if (!open.contains(child)) {
                        open.add(child);
                    }
                }
            }
        }
    }

    public void RBFS() {
        final var fCost = new HashMap<Node, Double>();
        final var PQ =
            new PriorityQueue<Node>(Comparator.comparing(fCost::get));
        fCost.put(this.startNode, h(this.startNode));

        PQ.add(this.startNode);

        RBFSImpl(PQ, fCost);
    }

    public void RBFSImpl(PriorityQueue<Node> PQ, HashMap<Node, Double> fCost) {
        final var node = PQ.remove();

        if (this.finished(node)) {
            return;
        }
        final var neighbors = this.getNeighbors(node);

        for (final var child : neighbors) {
            final var f = h(child) + this.getCost(child);
            fCost.put(child, f);

            final var parent = new Node(node.x, node.y, child.action);
            this.nodeTree.put(child, parent);

            if (!PQ.contains(child)) {
                PQ.add(child);
            }
        };

        RBFSImpl(PQ, fCost);
    }

    public void HillClimbing() {
        final var Q = new ArrayDeque<Node>();
        var node = this.startNode;
        Q.add(node);

        final var random = new Random();

        while (!this.finished(node)) {
            node = Q.remove();
            final var neighbors = this.getNeighbors(node);

            if (neighbors.isEmpty()) {
                node = this.startNode;
                this.reset();
                Q.clear();
                Q.add(node);
            } else {
                final var child =
                    neighbors.get(random.nextInt(neighbors.size()));
                final var parent = new Node(node.x, node.y, child.action);
                this.nodeTree.put(child, parent);

                Q.add(child);
            }
        }
    }
    // END

    public void printPath() {
        for (int i = 0; i < this.env.getRows(); i++) {
            final var row = new StringBuilder();

            for (int j = 0; j < this.env.getCols(); j++) {
                if (j > 0) {
                    row.append(" ");
                }

                final var node = new Node(i, j);

                if (this.pathStack.contains(node)) {
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

        if (this.done) {
            this.createPath();
            this.printPath();
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
            action = this.pathStack.pop().action;
            break;
        case "AStar":
            action = this.pathStack.pop().action;
            break;
        case "RBFS":
            action = this.pathStack.pop().action;
            break;
        case "HillClimbing":
            action = this.pathStack.pop().action;
            break;
        default:
            break;
        }
        return action;
    }
}