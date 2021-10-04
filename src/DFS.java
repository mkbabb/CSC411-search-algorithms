import java.util.Stack;

public class DFS extends Solver {
    public DFS(Environment env, int posRow, int posCol) {
        super(env, posRow, posCol);
    }

    public void solutionDriver() {
        final Stack<Node> nodes = new Stack<Node>();
        nodes.add(this.startNode);

        while (!nodes.isEmpty()) {
            final Node currentNode = nodes.pop();
            if (this.completed(currentNode)) {
                break;
            }

            final var children = this.getChildren(currentNode);
            for (final var child : children) {
                nodes.add(child);
            }
        }
    }
}
