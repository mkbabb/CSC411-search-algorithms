import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

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
    private Stack<Action> actionStack;

    private int[][] directions = {{0, 0, -1, 1}, {1, -1, 0, 0}};

    public class Node implements Comparable<Node> {
        private int x, y;
        private Action action = Action.DO_NOTHING;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }

        public int getY() { return y; }

        public int compareTo(Node other) {
            return Comparator.comparing(Node::getX)
                .thenComparing(Node::getY)
                .compare(this, other);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            Node node = (Node)o;
            return x == node.x && y == node.y;
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

        this.nodeTree = new TreeMap<Node, Node>();
        this.actionStack = new Stack<Action>();

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

    public double h(Node node) {
        return Math.abs(node.x - targetNode.x) +
            Math.abs(node.y - targetNode.y);
    }

    public Node findTargetNode() {
        for (int i = 0; i < env.getRows(); i++) {
            for (int j = 0; j < env.getCols(); j++) {
                final TileStatus status = env.getTileStatus(i, j);
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

        final ArrayList<Node> adjNodes = new ArrayList<Node>();

        for (int i = 0; i < 4; i++) {
            final int x = row + directions[0][i];
            final int y = col + directions[1][i];

            if (env.validPos(x, y) && !visited[x][y]) {
                final Node child = new Node(x, y);
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
        Node node = targetNode;
        actionStack.add(targetNode.action);

        while (true) {
            if (node == null || node.equals(startNode)) {
                break;
            } else {
                node = nodeTree.get(node);
                actionStack.add(node.action);
            }
        }
    }

    public void DFS() {
        final Stack<Node> S = new Stack<Node>();
        S.add(startNode);

        while (!S.isEmpty()) {
            final Node node = S.pop();

            if (finished(node)) {
                break;
            }

            final ArrayList<Node> adjNodes = getAdjNodes(node);

            for (final Node child : adjNodes) {
                S.push(child);
                final Node parent = new Node(node.x, node.y);
                parent.action = child.action;
                nodeTree.put(child, parent);
            }
        }
    }

    public void AStar() {
        final TreeMap<Node, Double> fCost = new TreeMap<Node, Double>();
        final TreeMap<Node, Double> gCost = new TreeMap<Node, Double>();

        final PriorityQueue<Node> open =
            new PriorityQueue<Node>(Comparator.comparing((x) -> fCost.get(x)));
        final TreeSet<Node> closed = new TreeSet<Node>();

        fCost.put(startNode, h(targetNode));
        gCost.put(startNode, 0.0);

        open.add(startNode);

        while (!open.isEmpty()) {
            final Node node = open.remove();

            if (finished(node)) {
                break;
            }

            closed.add(node);

            final ArrayList<Node> adjNodes = getAdjNodes(node);
            Double currentG = gCost.get(node);
            currentG = currentG == null ? Double.MAX_VALUE : currentG;

            for (final Node child : adjNodes) {
                if (closed.contains(child)) {
                    continue;
                }
                Double g = gCost.get(child);
                g = g == null ? Double.MAX_VALUE : g;
                final int d =
                    Math.abs(node.x - child.x) + Math.abs(node.y - child.y);

                final Double tentativeG = currentG + getCost(node) + d;

                if (tentativeG < g) {
                    final Double f = tentativeG + h(child);

                    gCost.put(child, tentativeG);
                    fCost.put(child, f);

                    final Node parent = new Node(node.x, node.y);
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
        final TreeMap<Node, Double> cost = new TreeMap<Node, Double>();
        final PriorityQueue<Node> PQ =
            new PriorityQueue<Node>(Comparator.comparing((x) -> cost.get(x)));
        cost.put(startNode, h(startNode));

        PQ.add(startNode);

        RBFSImpl(PQ, cost);
    }

    public void RBFSImpl(PriorityQueue<Node> PQ, TreeMap<Node, Double> cost) {
        final Node node = PQ.remove();

        if (finished(node)) {
            return;
        }
        final ArrayList<Node> adjNodes = getAdjNodes(node);

        for (final Node child : adjNodes) {
            cost.put(child, h(child) + getCost(child));

            final Node parent = new Node(node.x, node.y);
            parent.action = child.action;
            nodeTree.put(child, parent);

            if (!PQ.contains(child)) {
                PQ.add(child);
            }
        };
        RBFSImpl(PQ, cost);
    }

    public void HillClimbing() {
        final ArrayDeque<Node> Q = new ArrayDeque<Node>();
        Node node = startNode;
        Q.add(node);

        final Random random = new Random();

        while (!finished(node)) {
            node = Q.remove();
            final ArrayList<Node> adjNodes = getAdjNodes(node);

            if (adjNodes.isEmpty()) {
                node = startNode;
                reset();
                Q.clear();
                Q.add(node);
            } else {
                final Node child =
                    adjNodes.get(random.nextInt(adjNodes.size()));
                final Node parent = new Node(node.x, node.y);
                parent.action = child.action;
                nodeTree.put(child, parent);

                Q.add(child);
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

        if (actionStack.isEmpty()) {
            return action;
        }

        switch (searchAlgorithm) {
        case "DFS":
            action = actionStack.pop();
            break;
        case "AStar":
            action = actionStack.pop();
            break;
        case "RBFS":
            action = actionStack.pop();
            break;
        case "HillClimbing":
            action = actionStack.pop();
            break;
        default:
            break;
        }
        return action;
    }
}