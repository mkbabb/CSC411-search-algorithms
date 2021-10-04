import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStar extends Solver {
    public AStar(Environment env, int posRow, int posCol) {
        super(env, posRow, posCol);
    }

    public void solutionDriver() {
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
}
