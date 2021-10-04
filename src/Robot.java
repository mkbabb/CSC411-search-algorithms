

/**
    Represents an intelligent agent moving through a particular room.
    The robot only has one sensor - the ability to get the status of any
    tile in the environment through the command env.getTileStatus(row, col).

    Your task is to modify the getAction method below so that is reached the
    TARGET POSITION with a minimal number of steps. There is only one (1)
    target position, which you can locate using env.getTargetRow() and
   env.getTargetCol()
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Robot {
    private Environment env;
    private int posRow;
    private int posCol;
    private String searchAlgorithm;

    public int expanded = 0;
    public int energyCost = 0;
    public boolean completed = false;

    public DFS dfs;
    public AStar aStar;
    public RBFS rbfs;

    public Map<String, Solver> solvers;

    /**
        Initializes a Robot on a specific tile in the environment.
    */
    public Robot(Environment env) {
        this(env, 0, 0, "");
    }
    public Robot(Environment env, int posRow, int posCol, String searchAlgorithm) {
        this.env = env;
        this.posRow = posRow;
        this.posCol = posCol;
        this.searchAlgorithm = searchAlgorithm;

        this.dfs = new DFS(env, posRow, posCol);
        this.aStar = new AStar(env, posRow, posCol);
        this.rbfs = new RBFS(env, posRow, posCol);

        this.solvers = new HashMap<String, Solver>() {
            {
                put("DFS", dfs);
                put("AStar", aStar);
                put("RBFS", rbfs);
            }
        };
    }

    public int getPosRow() {
        return posRow;
    }
    public int getPosCol() {
        return posCol;
    }
    public void incPosRow() {
        posRow++;
    }
    public void decPosRow() {
        posRow--;
    }
    public void incPosCol() {
        posCol++;
    }
    public void decPosCol() {
        posCol--;
    }

    /**
     * Construct search tree before Robot start moving.
     */
    public void plan() {
        final var solver = this.solvers.get(searchAlgorithm);

        if (solver != null) {
            solver.solve();

            this.expanded = solver.expanded;
            this.energyCost = solver.energyCost;
            this.completed = solver.complete;
        }
    }

    /**
        Simulate the passage of a single time-step.
        At each time-step, the Robot decides which direction
        to move.
    */
    public Action getAction() {
        var action = Action.DO_NOTHING;
        final var solver = this.solvers.get(searchAlgorithm);

        if (solver != null) {
            action = solver.getAction();
        }

        return action;
    }
}