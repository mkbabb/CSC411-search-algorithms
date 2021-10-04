
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

public class PathFinder {
    public Environment env;

    public int posRow, posCol;

    public boolean complete;

    public Map<Node, Action> actionMap;
    public Map<Node, Node> allNodes;

    public Stack<Node> nodePath;

    public Node startNode, endNode;

    public int energyCost = 0;
    public int expanded = 0;

    public static int[] rowVector = {0, 0, -1, 1};
    public static int[] colVector = {1, -1, 0, 0};

    public PathFinder(Environment env, int posRow, int posCol) {
        this.complete = false;

        this.env = env;
        this.posRow = posRow;
        this.posCol = posCol;

        this.actionMap = new HashMap<Node, Action>();
        this.nodePath = new Stack<Node>();
        this.allNodes = new HashMap<Node, Node>();

        this.startNode = new Node(posRow, posCol);

        for (int i = 0; i < this.env.getRows(); i++) {
            for (int j = 0; j < this.env.getCols(); j++) {
                if (this.env.getTileStatus(i, j) == TileStatus.TARGET) {
                    this.endNode = new Node(i, j);
                }
            }
        }

        this.allNodes.put(startNode, startNode);
        this.allNodes.put(endNode, endNode);
    }

    public double euclideanDistance(Node node1, Node node2) {
        return Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2));
    }

    public double heuristicFunction(Node node) {
        return euclideanDistance(node, this.endNode);
    }

    public boolean validPos(Node node) {
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

        this.startNode.f = heuristicFunction(this.startNode);
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
                        child.f = testingGValue + heuristicFunction(child);

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
        this.startNode.f = heuristicFunction(startNode);
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
            child.f = heuristicFunction(child) + getCost(child);

            if (!nodes.contains(child)) {
                nodes.add(child);
            }
        };

        RBFSDriver(nodes);
    }

    public void printPath() {
        for (int i = 0; i < this.env.getRows(); i++) {
            final var row = new StringBuilder();

            for (int j = 0; j < this.env.getCols(); j++) {
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
