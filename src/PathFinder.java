import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class PathFinder {
    public Environment env;
    public int energyCost = 0;
    public int expanded = 0;

    public int rowPos, colPos;

    public boolean complete;

    public Map<Node, Action> actionMap;
    public Map<Node, Node> allNodes;

    public Stack<Node> nodePath;

    public Node startNode, endNode;

    public static int[] rowVector = {0, 0, -1, 1};
    public static int[] colVector = {1, -1, 0, 0};

    public class Node {
        private int x, y;
        private boolean visited = false;
        private Node parent = null;

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
            if (o == this)
                return true;
            if (!(o instanceof Node)) {
                return false;
            }
            Node node = (Node) o;
            return Objects.equals(this.x, node.x) && Objects.equals(this.y, node.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y);
        }
    }

    public PathFinder(Environment env, int rowPos, int colPos) {
        this.complete = false;

        this.env = env;
        this.rowPos = rowPos;
        this.colPos = colPos;

        this.actionMap = new HashMap<Node, Action>();
        this.nodePath = new Stack<Node>();
        this.allNodes = new HashMap<Node, Node>();

        this.startNode = new Node(rowPos, colPos);

        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                if (this.env.getTileStatus(i, j) == TileStatus.TARGET) {
                    this.endNode = new Node(i, j);
                }
            }
        }

        this.allNodes.put(startNode, startNode);
        this.allNodes.put(endNode, endNode);
    }

    public double euclideanDistance(Node n1, Node n2) {
        return Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2));
    }

    public double heuristic(Node node) {
        if (this.endNode != null) {
            return euclideanDistance(node, this.endNode);
        } else {
            return 1000;
        }
    }

    public int getRows() {
        return this.env.getRows();
    }

    public int getCols() {
        return this.env.getCols();
    }

    public boolean isValid(Node node) {
        return this.env.validPos(node.x, node.y);
    }

    public Node getOrPutNode(Node node) {
        if (this.allNodes.containsKey(node)) {
            return this.allNodes.get(node);
        } else {
            this.allNodes.put(node, node);
            return node;
        }
    }

    public boolean completed(Node node) {
        return this.complete = this.env.getTileStatus(node.x, node.y) == TileStatus.TARGET;
    }

    public int getCost(Node node) {
        return this.env.getTileCost(node.x, node.y);
    }

    public TileStatus getStatus(Node node) {
        return this.env.getTileStatus(node.x, node.y);
    }

    public void getPath() {
        var node = this.endNode;
        this.nodePath.add(node);

        while (node != null && !node.equals(this.startNode)) {
            this.energyCost += this.getCost(node);
            node = node.parent;
            this.nodePath.add(node);
        }
    }

    public ArrayList<Node> getChildren(Node node) {
        final var neighbors = new ArrayList<Node>();

        for (int i = 0; i < rowVector.length; i++) {
            final var row = node.x + rowVector[i];
            final var col = node.y + colVector[i];

            if (this.env.validPos(row, col)) {
                var child = new Node(row, col);
                child = this.getOrPutNode(child);

                if (!child.visited) {
                    child.parent = node;
                    child.visited = true;
                    this.actionMap.put(child, Action.values()[i]);

                    neighbors.add(child);
                }
            }
        }

        this.expanded += neighbors.size();
        return neighbors;
    }

    public void DFS() {
        final var nodes = new Stack<Node>();
        nodes.add(this.startNode);

        while (!nodes.isEmpty()) {
            final var currentNode = nodes.pop();

            if (this.completed(currentNode)) {
                break;
            }

            final var children = this.getChildren(currentNode);
            for (final var child : children) {
                nodes.add(child);
            }
        }
    }

    public void AStar() {
        final var openSet = new PriorityQueue<Node>(Comparator.comparing(Node::getF));
        final var closedSet = new HashSet<Node>();

        this.startNode.f = heuristic(this.startNode);
        this.startNode.g = 0.0;

        openSet.add(this.startNode);

        while (!openSet.isEmpty()) {
            final var currentNode = openSet.remove();
            closedSet.add(currentNode);

            if (this.completed(currentNode)) {
                break;
            }

            final var children = this.getChildren(currentNode);
            for (final var child : children) {
                if (!closedSet.contains(child)) {
                    final var testingGValue =
                        currentNode.g + this.getCost(child) + euclideanDistance(currentNode, child);

                    if (testingGValue < child.g) {
                        child.g = testingGValue;
                        child.f = testingGValue + heuristic(child);

                        if (!openSet.contains(child)) {
                            openSet.add(child);
                        }
                    }
                }
            }
        }
    }

    public void RBFS() {
        final var nodes = new PriorityQueue<Node>(Comparator.comparing(Node::getF));
        this.startNode.f = heuristic(startNode);
        nodes.add(startNode);

        RBFSDriver(nodes);
    }

    public void RBFSDriver(PriorityQueue<Node> nodes) {
        final var node = nodes.remove();

        if (this.completed(node)) {
            return;
        }
        final var children = this.getChildren(node);

        for (final Node child : children) {
            child.f = heuristic(child) + getCost(child);

            if (!nodes.contains(child)) {
                nodes.add(child);
            }
        };

        RBFSDriver(nodes);
    }

    public void search(String searchAlgorithm) {
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
                break;
            default:
                break;
        }

        if (this.complete) {
            this.getPath();
            this.printPath();
        }
    }

    public void printPath() {
        for (int i = 0; i < this.getRows(); i++) {
            final var row = new StringBuilder();

            for (int j = 0; j < this.getCols(); j++) {
                if (j > 0) {
                    row.append(" ");
                }
                final var node = new Node(i, j);
                final var status = this.getStatus(node);

                if (this.nodePath.contains(node)) {
                    if (status == TileStatus.TARGET) {
                        row.append("G");
                    } else if (this.nodePath.peek().equals(node)) {
                        row.append("S");
                    } else {
                        row.append("*");
                    }
                } else {
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
}
