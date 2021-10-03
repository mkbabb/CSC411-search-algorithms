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

    private boolean done;
    private boolean[][] visited;

    private Node startNode, targetNode;

    private Map<Node, Node> nodeTree;
    private Stack<Node> pathStack;

    private int[][] directions = {{0, 0, -1, 1}, {1, -1, 0, 0}};

    public class Node implements Comparable<Node> {
        private int x, y;
        private Action action = Action.DO_NOTHING;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return this.x; }

        public int getY() { return this.y; }

        public int compareTo(Node other) {
            return Comparator.comparing(Node::getX)
                .thenComparing(Node::getY)
                .compare(this, other);
        }

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
        nodeTree.clear();
        visited = new boolean[env.getRows()][env.getCols()];
    }

    public double manhattanDistance(Node n1, Node n2) {
        return Math.abs(n1.x - n2.x) + Math.abs(n1.y - n2.y);
    }

    public double h(Node node) { return manhattanDistance(node, targetNode); }

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

    public double getCost(Node node) { return env.getTileCost(node.x, node.y); }

    public ArrayList<Node> getAdjNodes(Node node) {
        int row = node.x;
        int col = node.y;

        final var adjNodes = new ArrayList<Node>();

        for (int i = 0; i < 4; i++) {
            final var x = row + directions[0][i];
            final var y = col + directions[1][i];

            if (env.validPos(x, y) && !visited[x][y]) {
                final var child = new Node(x, y);
                child.action = Action.values()[i];
                adjNodes.add(child);
                visited[x][y] = true;
            }
        }
        expanded += 1;
        return adjNodes;
    }

    public boolean finished(Node node) {
        return done = env.getTileStatus(node.x, node.y) == TileStatus.TARGET;
    }

    public void createPath() {
        var node = targetNode;
        pathStack.add(targetNode);

        while (true) {
            if (node == null || node.equals(startNode)) {
                break;
            } else {
                node = nodeTree.get(node);
                pathStack.add(node);
            }
        }
    }

    public void DFS() {
        final Stack<Node> S = new Stack<Node>();
        S.add(startNode);

        while (!S.isEmpty()) {
            final var node = S.pop();

            if (finished(node)) {
                break;
            }

            final var adjNodes = getAdjNodes(node);

            for (final var child : adjNodes) {
                S.push(child);
                final var parent = new Node(node.x, node.y);
                parent.action = child.action;
                nodeTree.put(child, parent);
            }
        }
    }

    public void AStar() {
        final var fCost = new HashMap<Node, Double>();
        final var gCost = new HashMap<Node, Double>();

        final var open =
            new PriorityQueue<Node>(Comparator.comparing(fCost::get));
        final var closed = new HashSet<Node>();

        fCost.put(startNode, h(targetNode));
        gCost.put(startNode, 0.0);

        open.add(startNode);

        while (!open.isEmpty()) {
            final var node = open.remove();

            if (finished(node)) {
                break;
            }

            closed.add(node);

            final var adjNodes = getAdjNodes(node);
            var currentG = gCost.get(node);
            currentG = currentG == null ? Double.MAX_VALUE : currentG;

            for (final var child : adjNodes) {
                if (closed.contains(child)) {
                    continue;
                }
                var g = gCost.get(child);
                g = g == null ? Double.MAX_VALUE : g;
                final var d =
                    Math.abs(node.x - child.x) + Math.abs(node.y - child.y);

                final var tentativeG = currentG + getCost(node) + d;

                if (tentativeG < g) {
                    final var f = tentativeG + h(child);

                    gCost.put(child, tentativeG);
                    fCost.put(child, f);

                    final var parent = new Node(node.x, node.y);
                    parent.action = child.action;
                    nodeTree.put(child, parent);

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
        fCost.put(startNode, h(startNode));

        PQ.add(startNode);

        RBFSImpl(PQ, fCost);
    }

    public void RBFSImpl(PriorityQueue<Node> PQ, HashMap<Node, Double> fCost) {
        final var node = PQ.remove();

        if (finished(node)) {
            return;
        }
        final var adjNodes = getAdjNodes(node);

        for (final var child : adjNodes) {
            final var f = h(child) + getCost(child);
            fCost.put(child, f);

            final var parent = new Node(node.x, node.y);
            parent.action = child.action;
            nodeTree.put(child, parent);

            if (!PQ.contains(child)) {
                PQ.add(child);
            }
        };
        RBFSImpl(PQ, fCost);
    }

    public void HillClimbing() {
        final var Q = new ArrayDeque<Node>();
        var node = startNode;
        Q.add(node);

        final var random = new Random();

        while (!finished(node)) {
            node = Q.remove();
            final var adjNodes = getAdjNodes(node);

            if (adjNodes.isEmpty()) {
                node = startNode;
                reset();
                Q.clear();
                Q.add(node);
            } else {
                final var child = adjNodes.get(random.nextInt(adjNodes.size()));
                final var parent = new Node(node.x, node.y);
                parent.action = child.action;
                nodeTree.put(child, parent);

                Q.add(child);
            }
        }
    }
    // END

    public void printPath() {
        for (int i = 0; i < env.getRows(); i++) {
            final var row = new StringBuilder();

            for (int j = 0; j < env.getCols(); j++) {
                if (j > 0) {
                    row.append(" ");
                }

                final var node = new Node(i, j);

                if (pathStack.contains(node)) {
                    row.append("*");
                } else {
                    final var status = env.getTileStatus(i, j);

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
            DFS();
            break;
        case "AStar":
            AStar();
            break;
        case "RBFS":
            RBFS();
            break;
        case "HillClimbing":
            HillClimbing();
        default:
            break;
        }
        if (done) {
            createPath();
            printPath();
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

        if (pathStack.isEmpty()) {
            return action;
        }

        switch (searchAlgorithm) {
        case "DFS":
            action = pathStack.pop().action;
            break;
        case "AStar":
            action = pathStack.pop().action;
            break;
        case "RBFS":
            action = pathStack.pop().action;
            break;
        case "HillClimbing":
            action = pathStack.pop().action;
            break;
        default:
            break;
        }
        return action;
    }
}