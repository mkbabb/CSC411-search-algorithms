

import java.util.ArrayList;

public class RunSimulation {
	private Environment env;
	private ArrayList<Robot> robots;
	private int numRobots;
	private int timesteps, timestepsStop;
	private boolean goalMet;
	
	public RunSimulation(String searchAlgorithm, int start_row, int start_col, int target_row, int target_col, String env_id) {
		this.env = new Environment(env_id);
		this.env.setTarget(target_row, target_col);
		numRobots = 1;
		robots = new ArrayList<Robot>(); 
		for(int i = 0; i < numRobots; i++) {
			Robot robot = new Robot(env, start_row, start_col, searchAlgorithm);
			robot.plan();
			robots.add(robot);
		}
		// number of time steps since the beginning
		this.timesteps = 0;
		// number of time steps before stopping simulation
		this.timestepsStop = 200;
		this.goalMet = false;
	}
	
	public void run() {
		while (timesteps < timestepsStop) {
			updateEnvironment();
			if (timesteps == timestepsStop) {
				break;
			}
			for(Robot robot : robots) {
				if (env.goalConditionMet(robot)) {
					goalMet = true;
					break;
				}
			}
			if(goalMet) break;
		}
		printPerformanceMeasure();
	}
	
	public double getTimesteps() {
        return timesteps;
	}
	
	public void printPerformanceMeasure() {
		System.out.printf("Simulation Completed in %d timesteps\n", timesteps);
		System.out.println("Goal Condition Met: " + goalMet); 
	}

	// Gets the new state of the world after robot actions
	public void updateEnvironment() {
		timesteps++;
		for(Robot robot : robots) {
			Action action = robot.getAction();
			int row = robot.getPosRow();
			int col = robot.getPosCol();
			switch(action) {
            case MOVE_DOWN:
                if (env.validPos(row+1, col))
                    robot.incPosRow();
                break;
            case MOVE_LEFT:
                if (env.validPos(row, col-1))
                    robot.decPosCol();
                break;
            case MOVE_RIGHT:
                if (env.validPos(row, col+1))
                    robot.incPosCol();
                break;
            case MOVE_UP:
                if (env.validPos(row-1, col))
                    robot.decPosRow();
                break;
            case DO_NOTHING: // pass to default
            default:
                break;
			}
		}
	}
	
	public static void main(String[] args) {
		// String searchAlgorithm = args[0];
		// int start_row = Integer.parseInt(args[1]);
		// int start_col = Integer.parseInt(args[2]);
		
		// int target_row = Integer.parseInt(args[3]);
		// int target_col = Integer.parseInt(args[4]);

        String searchAlgorithm = "RBFS";
        
        int start_row = 0;
        int start_col = 0;

        int target_row = 9;
        int target_col = 9;

        String env_id = "1";
		
		// String env_id = args[5];
		
		RunSimulation sim = new RunSimulation(searchAlgorithm, start_row, start_col, target_row, target_col, env_id);
		sim.run();
    }
}