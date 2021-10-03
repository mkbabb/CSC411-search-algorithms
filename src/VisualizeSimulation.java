

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A Visual Guide toward testing whether your robot
 * agent is operating correctly. This visualization
 * will run for 200 time steps. If the agent reaches
 * the target location before the 200th time step, the
 * simulation will end automatically.
 * You are free to modify the environment for test cases.
 * @author Adam Gaweda
 */
public class VisualizeSimulation extends JFrame {
	private static final long serialVersionUID = 1L;
	private EnvironmentPanel envPanel;
	private Environment env;
	private ArrayList<Robot> robots;
	private int numRobots;
	
	/* Builds the environment; while not necessary for this problem set,
	 * this could be modified to allow for different types of environments,
	 * for example loading from a file, or creating multiple agents that
	 * can communicate/interact with each other.
	 */
	public VisualizeSimulation(String searchAlgorithm, int start_row, int start_col, int target_row, int target_col, String env_id) {
		Environment env = new Environment(env_id);
		// This will not always be at the bottom right, be sure to design
		// your algorithm for that
		env.setTarget(target_row, target_col);
		numRobots = 1;
		robots = new ArrayList<Robot>(); 
		for(int i = 0; i < numRobots; i++) {
			Robot robot = new Robot(env, start_row, start_col, searchAlgorithm);
			robot.plan();
			robots.add(robot);
		}
    	envPanel = new EnvironmentPanel(env, robots);
    	add(envPanel);
	}
	
	public static void main(String[] args) {
		String searchAlgorithm = args[0];
		int start_row = Integer.parseInt(args[1]);
		int start_col = Integer.parseInt(args[2]);
		
		int target_row = Integer.parseInt(args[3]);
		int target_col = Integer.parseInt(args[4]);
		
		String env_id = "1";
		
	    JFrame frame = new VisualizeSimulation(searchAlgorithm, start_row, start_col, target_row, target_col, env_id);

	    frame.setTitle("CSC 520 - HW01");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
    }
}

@SuppressWarnings("serial")
class EnvironmentPanel extends JPanel{
	private Timer timer;
	private Environment env;
	private ArrayList<Robot> robots;
	private int timesteps, timestepsStop;
	public static final int TILESIZE = 100;
	// 250 millisecond time steps
	private int timeSteps = 250;
	private int path_cost = 0;
	
	public EnvironmentPanel(Environment env, ArrayList<Robot> robots) {
	    setPreferredSize(new Dimension(env.getCols()*TILESIZE, env.getRows()*TILESIZE));
		this.env = env;
		this.robots = robots;
		// number of time steps since the beginning
		this.timesteps = 0;
		// number of time steps before stopping simulation
		this.timestepsStop = 200;
		this.path_cost = 0;
		
		this.timer = new Timer(timeSteps, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnvironment();
				repaint();
				if (timesteps == timestepsStop) {
					timer.stop();
					System.out.println("Maximum time step reached");
					printPerformanceMeasure();
				}
				for(Robot robot : robots) {
					if (env.goalConditionMet(robot)) {
						timer.stop();
						printPerformanceMeasure();
						break;
					}
				}
			}
			
			public double getTimesteps() {
		        return timesteps;
			}
			
			public void printPerformanceMeasure() {
				System.out.printf("Simulation Completed in %d timesteps with %d costs.\n", timesteps, path_cost);
				for (Robot robot: robots){
					System.out.printf("Expanded nodes: %d\n", robot.expanded);
				}
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
		                	path_cost += env.getTileCost(row+1, col);
		                    robot.incPosRow();
		                break;
		            case MOVE_LEFT:
		                if (env.validPos(row, col-1))
		                	path_cost += env.getTileCost(row, col-1);
		                    robot.decPosCol();
		                break;
		            case MOVE_RIGHT:
		                if (env.validPos(row, col+1))
		                	path_cost += env.getTileCost(row, col+1);
		                    robot.incPosCol();
		                break;
		            case MOVE_UP:
		                if (env.validPos(row-1, col))
		                	path_cost += env.getTileCost(row-1, col);
		                    robot.decPosRow();
		                break;
		            case DO_NOTHING: // pass to default
		            default:
		                break;
					}
				}
			}
		});
		this.timer.start();
	}
	
	/*
	 * The paintComponent method draws all of the objects onto the
	 * panel. This is updated at each time step when we call repaint().
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Paint Environment Tiles
		Tile[][] tiles = env.getTiles();
		for(int row = 0; row < env.getRows(); row++)
		    for(int col = 0; col < env.getCols(); col++) {
		    	if(tiles[row][col].getStatus() == TileStatus.PLAIN) {
                    g.setColor(Properties.LIGHTGREEN);
                } else if(tiles[row][col].getStatus() == TileStatus.PUDDLE) {
                    g.setColor(Properties.LIGHTBLUE);
                } else if(tiles[row][col].getStatus() == TileStatus.MOUNTAIN) {
                    g.setColor(Properties.BLACK);
                } else if(tiles[row][col].getStatus() == TileStatus.TARGET) {
                    g.setColor(Properties.RED);
                }
		        // fillRect(int x, int y, int width, int height)
		        g.fillRect( col * TILESIZE, 
                            row * TILESIZE,
                            TILESIZE, TILESIZE);
		        
		        g.setColor(Properties.BLACK);
                g.drawRect( col * TILESIZE, 
                            row * TILESIZE,
                            TILESIZE, TILESIZE);
		    }
		// Paint Robot
		g.setColor(Properties.GREEN);
		for(Robot robot : robots) {
		    g.fillOval(robot.getPosCol() * TILESIZE+TILESIZE/4, 
    		            robot.getPosRow() * TILESIZE+TILESIZE/4,
    		            TILESIZE/2, TILESIZE/2);
		}
	}
}