

/**
 * The world in which this simulation exists. As a base
 * world, this produces a 10x10 room of tiles. In addition,
 * 20% of the room is covered with "walls" (tiles marked as IMPASSABLE).
 *
 * This object will allow the agent to explore the world and is how
 * the agent will retrieve information about the environment.
 * DO NOT MODIFY.
 * @author Adam Gaweda
 */
public class Environment {
    private Tile[][] tiles;
    private int rows, cols;
    private int targetRow, targetCol;

    public Environment() { this(10, 10); }
    public Environment(String envID) {
        this();

        if (envID == null) {
            return;
        }

        switch (envID) {
        case "1":
            setObstacles1();
            break;
        case "2":
            setObstacles2();
            break;
        default:
            setObstacles1();
            break;
        }
    }
    public Environment(int width, int height) {
        // Columns refer to the WIDTH of the environment
        // Rows refer to the HEIGHT of the environment
        this.cols = width;
        this.rows = height;
        tiles = new Tile[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                tiles[row][col] = new Tile(TileStatus.PLAIN, 1);
            }
        }
    }

    private void setObstacles1() {
        for (int i = 1; i < cols - 1; i++) {
            tiles[1][i] = new Tile(TileStatus.PUDDLE, 100000000);
            tiles[cols - 2][i] = new Tile(TileStatus.PUDDLE, 100000000);
        }

        tiles[1][0] = new Tile(TileStatus.MOUNTAIN, 5);
        tiles[cols - 2][cols - 1] = new Tile(TileStatus.MOUNTAIN, 5);

        tiles[cols / 2 - 1][1] = new Tile(TileStatus.MOUNTAIN, 5);
        tiles[cols / 2][1] = new Tile(TileStatus.MOUNTAIN, 5);
        tiles[cols / 2 - 1][cols - 2] = new Tile(TileStatus.MOUNTAIN, 5);
        tiles[cols / 2][cols - 2] = new Tile(TileStatus.MOUNTAIN, 5);

        for (int i = 3; i < cols - 2; i++) {
            tiles[3][i] = new Tile(TileStatus.PUDDLE, 100000000);
        }

        for (int i = 2; i < cols - 3; i++) {
            tiles[cols - 4][i] = new Tile(TileStatus.PUDDLE, 100000000);
        }
    }

    private void setObstacles2() {
        for (int i = 1; i < cols - 1; i++) {
            tiles[i][cols / 2 - 1] = new Tile(TileStatus.PUDDLE, 100000000);
        }
    }

    /* Traditional Getters and Setters */
    public Tile[][] getTiles() { return tiles; }
    public int getRows() { return this.rows; }
    public int getCols() { return this.cols; }
    public void setTarget(int row, int col) {
        // Only set if it is a coordinate within the environment
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            tiles[targetRow][targetCol] = new Tile(TileStatus.PLAIN, 1);
            targetRow = row;
            targetCol = col;
            tiles[targetRow][targetCol] = new Tile(TileStatus.TARGET, 1);
        }
    }
    public int getTargetRow() { return this.targetRow; }
    public int getTargetCol() { return this.targetCol; }

    /*
     * Returns a the status of a tile at a given [row][col] coordinate
     */
    public TileStatus getTileStatus(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            return TileStatus.IMPASSABLE;
        else
            return tiles[row][col].getStatus();
    }
    public int getTileCost(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            return 100000000;
        else
            return tiles[row][col].getCost();
    }
    public void setTileStatuse(int row, int col, TileStatus status, int cost) {
        // Only set if it is a coordinate within the environment
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            tiles[row][col] = new Tile(status, cost);
        }
    }

    /* Counts number of tiles that are not walls */
    public int getNumTiles() {
        int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (this.tiles[row][col].getStatus() != TileStatus.IMPASSABLE)
                    count++;
            }
        }
        return count;
    }

    /* Determines if a particular [row][col] coordinate is within
     * the boundaries of the environment. This is a rudimentary
     * "collision detection" to ensure the agent does not walk
     * outside the world (or through walls).
     */
    public boolean validPos(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols &&
            tiles[row][col].getStatus() != TileStatus.IMPASSABLE &&
            tiles[row][col].getStatus() != TileStatus.PUDDLE;
    }
    public boolean goalConditionMet(Robot robot) {
        return robot.getPosRow() == targetRow && robot.getPosCol() == targetCol;
    }
}
