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

    public double manhattanDistance(Node n1, Node n2) {
        return Math.abs(n1.x - n2.x) + Math.abs(n1.y - n2.y);
    }

    public double heuristic(Node node) {
        if (this.endNode != null) {
            return manhattanDistance(node, this.endNode);
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
            } else {
                final var children = this.getChildren(currentNode);

                for (final var child : children) {
                    nodes.add(child);
                }
            }
        }
    }

    public void AStar() {
        final var fMap = new HashMap<Node, Double>();
        final var gMap = new HashMap<Node, Double>();

        final var openSet = new PriorityQueue<Node>(Comparator.comparing(fMap::get));
        final var closedSet = new HashSet<Node>();

        fMap.put(this.startNode, heuristic(this.startNode));
        gMap.put(this.startNode, 0.0);

        openSet.add(this.startNode);

        while (!openSet.isEmpty()) {
            final var current = openSet.remove();
            closedSet.add(current);

            if (this.completed(current)) {
                break;
            }

            final var neighbors = this.getChildren(current);
            final var currentG = gMap.getOrDefault(current, Double.MAX_VALUE);

            neighbors.stream().filter((node) -> !closedSet.contains(node)).forEach((node) -> {
                final var cost = this.env.getTileCost(node.x, node.y);
                final var nodeG = gMap.getOrDefault(node, Double.MAX_VALUE);

                // TODO: euclid
                final var tmpG = currentG + cost + manhattanDistance(current, node);

                if (tmpG < nodeG) {
                    final var f = tmpG + heuristic(node);

                    gMap.put(node, tmpG);
                    fMap.put(node, f);

                    if (!openSet.contains(node)) {
                        openSet.add(node);
                    }
                }
            });
        }
    }

    public void RBFS() {
        final var fMap = new HashMap<Node, Double>();

        final var openSet = new PriorityQueue<Node>(Comparator.comparing(fMap::get));
        fMap.put(this.startNode, heuristic(this.startNode));

        openSet.add(this.startNode);

        while (!openSet.isEmpty()) {
            final var current = openSet.remove();

            if (this.completed(current)) {
                break;
            }
            final var neighbors = this.getChildren(current);

            neighbors.stream().forEach((node) -> {
                final var cost = this.env.getTileCost(node.x, node.y);
                final var f = heuristic(node) + cost;
                fMap.put(node, f);

                if (!openSet.contains(node)) {
                    openSet.add(node);
                }
            });
        }
    }

    public void HillClimbing() {
        final var fMap = new HashMap<Node, Double>();

        final var openSet = new PriorityQueue<Node>(Comparator.comparing(fMap::get));
        fMap.put(this.startNode, 0.0);

        openSet.add(this.startNode);

        while (!openSet.isEmpty()) {
            final var current = openSet.remove();

            if (this.completed(current)) {
                break;
            }
            final var neighbors = this.getChildren(current);

            neighbors.stream().forEach((node) -> {
                final var cost = this.env.getTileCost(node.x, node.y);
                fMap.put(node, 1.0 * cost);

                if (!openSet.contains(node)) {
                    openSet.add(node);
                }
            });
        }
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
                this.HillClimbing();
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
