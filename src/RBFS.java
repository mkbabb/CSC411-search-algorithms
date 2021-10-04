
import java.util.Comparator;
import java.util.PriorityQueue;

public class RBFS extends Solver {
    public RBFS(Environment env, int posRow, int posCol) {
        super(env, posRow, posCol);
    }

    public void solutionDriver() {
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
}
