/** TIME SPENT: 13 hours and 0 minutes. */ 

package student;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import danaus.*;

public class Butterfly extends AbstractButterfly {
	TileState[][] ts= null; //array of TileStates found in learn()
	Set<Long> flowers = new HashSet<Long>(); //HashSet of flowers foudn in learn()
	double intensity1= 0; //intensity value of 0
	
	
	/**Butterfly flies over every tile in the map
	 * and creates an array ts with the TileStates of 
	 * every tile stored into its corresponding location
	 * in the array. 
	 * Returns ts.*/
	public @Override TileState[][] learn() {
		ts= new TileState[getMapHeight()][getMapWidth()];
		dfs();
		return ts;
	}


	/**Butterfly is on an unvisited, un-refreshed tile.
	 * Each visited tile's state is updated in ts.
	 * Unreachable tiles are caught in exceptions, set to nil.
	 * Flowers found are added to Set flowers; not collected.
	 * Butterfly should return to its starting tile */
	public void dfs(){
		refreshState();
		ts[state.location.row][state.location.col]= state; 
		for(Flower f: state.getFlowers()){ 
			flowers.add(f.getFlowerId()); //add "old" flowers to flowers HashSet
		}
		for(Direction d:Direction.values()){
			int nextRow= Common.mod(location.row+d.dRow, this.getMapHeight());
			int nextCol= Common.mod(location.col+d.dCol, this.getMapWidth());
			if(ts[nextRow][nextCol]==null){ //check if next tile is obstacle
				try{
					fly(d, Speed.FAST);
					dfs(); //recurse			
					fly(Direction.opposite(d), Speed.FAST); //fly back to beginning
				}
				catch(ObstacleCollisionException o){
					ts[nextRow][nextCol]=TileState.nil; //set tile to nil
				}
			}
		}
		
	}

	/**Butterfly collects all flowers in the List flowerIds*/
	public @Override void run(List<Long> flowerIds) {
		while(flowerIds.size()>0){
			refreshState();
			//if targeted flower is in old list, use shortest path to find and collect
			if(flowers.contains(target(flowerIds, maxAroma(flowerIds)))){
				oldFlowers(target(flowerIds, maxAroma(flowerIds)), flowerIds);
			}
			//else use bfs to find new flowers
			else{
				findFlower(target(flowerIds, maxAroma(flowerIds)), flowerIds);
			}
			//remove collected flower from flowerIds, eventually terminate loop
			flowerIds.remove(target(flowerIds, maxAroma(flowerIds)));
		}
	}

	/**Butterfly finds the shortest path to already learned
	 * flowers found during learn()*/
	public void oldFlowers(long fl, List<Long> flowerIds){
		Direction dir= null;
		for(Direction d: Direction.values()){
			int nextRow= Common.mod(location.row+d.dRow, this.getMapHeight());
			int nextCol= Common.mod(location.col+d.dCol, this.getMapWidth());
			//if next tile is reachable and intensity is greater, reassign intensity,
			//save the direction, and fly in that direction, otherwise loop again
			if(ts[nextRow][nextCol]!=TileState.nil && !state.equals(ts[nextRow][nextCol])){
				double intensity2= getIntensity(ts[nextRow][nextCol].getAromas(), fl);
				if(intensity2>intensity1){
					intensity1= intensity2;
					dir= d;		
				}
			}
		}
		//fly towards the biggest intensity and recurse
		if(dir!=null){
			fly(dir, Speed.FAST);
			refreshState();
			oldFlowers(fl, flowerIds);
		}
		else{
			collectFlower(fl);
		}
	}

	
	/**The butterfly flies to an unrefreshed tile,
	 * in the direction of the flower with the current 
	 * strongest aroma*/
	public void findFlower(long fl, List<Long> flowerIds){
		boolean found= false;
		for(Direction d: Direction.values()){
			int nextRow= Common.mod(location.row+d.dRow, this.getMapHeight());
			int nextCol= Common.mod(location.col+d.dCol, this.getMapWidth());
			//check each tile for most intense aroma, fly to the largest intensity, recurse
			if(ts[nextRow][nextCol]!=TileState.nil && !state.equals(ts[nextRow][nextCol])){
				fly(d, Speed.FAST);
				refreshState();
				double intensity2= getIntensity(state.getAromas(), fl);
				if(intensity2>intensity1){
					intensity1= intensity2;
					findFlower(fl, flowerIds);
					found= true;
					break;
				}
				else{
					fly(Direction.opposite(d), Speed.FAST);
					refreshState();
				}
			}
		}
		if(found==false){
			collectFlower(fl);			
		}
	}
	
	/**Returns the flowerId of the flower that the Butterfly
	 * is looking for based on the aroma.*/
	public long target(List<Long> flowerIds, Aroma max){
		for(Long f: flowerIds){
			if(f==max.getFlowerId()){
				return f;
			}
		}
		return 0;
	}
	
	/**Returns the intensity of the targeted flower*/
	public double getIntensity(List<Aroma> ar, long fl){
		for(Aroma a: ar){
			if(a.getFlowerId()==fl){
				return a.intensity;
			}
		}
		return 0;
	}
	
     /**Collects  the flower of the aroma that the 
	 * butterfly is currently searching for, and 
	 * the corresponding flowerId is removed from the
	 * list flowerIds*/
	public void collectFlower(long fl){
		for(Flower f: state.getFlowers()){
			if(fl==f.getFlowerId()){
				collect(f);
				refreshState();
				intensity1=0;
			}
		}
		return;
	}

	/**Returns the maximum aroma associated with the flower
	 * in the list flowerIds, for the tile that the butterfly
	 * is currently on.*/
	public Aroma maxAroma(List<Long> flowerIds){
		Aroma ar= null;
		for(Aroma aroma: state.getAromas()){
			if(flowerIds.contains(aroma.getFlowerId())){
				if(ar==null){
					ar=aroma;
				}
				if(aroma.intensity>ar.intensity){
					ar=aroma; 
				}
			}
		}
		return ar;
	}
}