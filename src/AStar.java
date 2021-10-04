import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStar extends Solver {
    public AStar(Environment env, int posRow, int posCol) {
        super(env, posRow, posCol);
    }

    public void solutionDriver() {
        final PriorityQueue<Node> opened = new PriorityQueue<Node>(Comparator.comparing(Node::getF));
        final HashSet<Node> closed = new HashSet<Node>();

        startNode.f = heuristicFunction(startNode);
        startNode.g = 0.0;

        opened.add(startNode);

        while (!opened.isEmpty()) {
            final var currentNode = opened.remove();
            closed.add(currentNode);

            if (completed(currentNode)) {
                break;
            }

            final var children = getChildren(currentNode);
            for (final var child : children) {
                if (!closed.contains(child)) {
                    final double testingGValue =
                        currentNode.g + getCost(child) + euclideanDistance(currentNode, child);

                    if (testingGValue < child.g) {
                        child.g = testingGValue;
                        child.f = testingGValue + heuristicFunction(child);

                        if (!opened.contains(child)) {
                            opened.add(child);
                        }
                    }
                }
            }
        }
    }
}
