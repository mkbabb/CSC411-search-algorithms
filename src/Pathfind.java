import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Pathfind
{
    public Environment env;
    public ArrayList<Queue<Integer>> dimQueues;

    public int rowPos;
    public int colPos;

    public boolean reachedTarget;

    public boolean[][] visitedTiles;
    public int[][] prevTiles;

    public static int[] rowVector = {-1, 1, 0, 0};
    public static int[] colVector = {0, 0, 1, -1};

    public Pathfind(Environment env, int rowPos, int colPos)
    {
        this.dimQueues = new ArrayList<Queue<Integer>>();

        this.dimQueues.add(new ArrayDeque<Integer>());
        this.dimQueues.add(new ArrayDeque<Integer>());

        this.reachedTarget = false;

        this.env = env;
        this.rowPos = rowPos;
        this.colPos = colPos;

        this.visitedTiles = new boolean[this.env.getRows()][this.env.getCols()];
        this.prevTiles = new int[this.env.getRows()][this.env.getCols()];
    }

    public Queue<Integer> rowQueue()
    {
        return this.dimQueues.get(0);
    }

    public Queue<Integer> colQueue()
    {
        return this.dimQueues.get(1);
    }

    public void expandNode(int... positions)
    {
        int baseRow = positions[0];
        int baseCol = positions[1];

        for (int i = 0; i < rowVector.length; i++) {
            final var row = baseRow + rowVector[i];
            final var col = baseCol + colVector[i];

            final var tileCost = this.env.getTileCost(row, col);

            final var visited = this.visitedTiles[row][col];

            if (this.env.validPos(row, col) && !visited) {
                this.rowQueue().add(row);
                this.colQueue().add(col);

                this.visitedTiles[row][col] = true;
                this.prevTiles[row][col] = this.prevTiles[baseRow][baseCol] + 1;
            }
        }
    }

    public void BFS()
    {
        this.rowQueue().add(this.rowPos);
        this.colQueue().add(this.colPos);

        while (!this.rowQueue().isEmpty()) {
            final var row = this.rowQueue().remove();
            final var col = this.colQueue().remove();

            if (this.env.getTileStatus(row, col) == TileStatus.TARGET) {
                this.reachedTarget = true;
                break;
            }

            this.expandNode(row, col);
        }
    }
}
