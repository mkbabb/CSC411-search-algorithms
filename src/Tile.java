

/**
 * A simple object representing the Tiles in the
 * environment.
 * DO NOT MODIFY.
 * @author Adam Gaweda
 */
public class Tile {
	private TileStatus status;
	private int cost;
	
	public Tile(TileStatus status, int cost) {
		this.status = status;
		this.cost = cost;
	}
	
	public TileStatus getStatus() { return status; }
	public int getCost() {return this.cost; }
	public String toString() { return ""+status.toString().charAt(0); }
}
