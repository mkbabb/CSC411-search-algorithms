

/**
    Represents an intelligent agent moving through a particular room.
    The robot only has one sensor - the ability to get the status of any
    tile in the environment through the command env.getTileStatus(row, col).

    Your task is to modify the getAction method below so that is reached the
    TARGET POSITION with a minimal number of steps. There is only one (1)
    target position, which you can locate using env.getTargetRow() and
   env.getTargetCol()
*/

public class Robot
{
    private Environment env;
    private int posRow;
    private int posCol;
    private String searchAlgorithm;
    public int expanded = 0;
    private int timeStep = 0;

    private PathFinder pathfinder;
    /**
        Initializes a Robot on a specific tile in the environment.
    */
    public Robot(Environment env)
    {
        this(env, 0, 0, "");
    }
    public Robot(
        Environment env,
        int posRow,
        int posCol,
        String searchAlgorithm)
    {
        this.env = env;
        this.posRow = posRow;
        this.posCol = posCol;
        this.searchAlgorithm = searchAlgorithm;

        this.pathfinder = new PathFinder(this.env, this.posRow, this.posCol);
    }
    public int getPosRow()
    {
        return posRow;
    }
    public int getPosCol()
    {
        return posCol;
    }
    public void incPosRow()
    {
        posRow++;
    }
    public void decPosRow()
    {
        posRow--;
    }
    public void incPosCol()
    {
        posCol++;
    }
    public void decPosCol()
    {
        posCol--;
    }

    /**
     * Construct search tree before Robot start moving.
     */
    public void plan()
    {
        this.pathfinder.search(searchAlgorithm);
    }

    /**
        Simulate the passage of a single time-step.
        At each time-step, the Robot decides which direction
        to move.
    */
    public Action getAction()
    {
        // you can get a tile's status with
        TileStatus status = env.getTileStatus(posRow, posCol);
        Action action = Action.DO_NOTHING;

        action = this.pathfinder.path.get(this.timeStep).action;
        this.timeStep += 1;

        return action;
    }
}