/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import sim.engine.SimState;
import tileworld.Parameters;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEntity;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;
import tileworld.exceptions.CellBlockedException;

import tileworld.planners.AstarPathGenerator;
import tileworld.planners.TWPath;
import tileworld.planners.TWPathStep;

/**
 * TWContextBuilder
 *
 * @author michaellees
 *
 * Created: Feb 6, 2011
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in
 * Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class TWAgentOrangeThree extends ExpectedValueAgent {
    
    protected AstarPathGenerator astar;
    protected TWEntity target;
    protected String agentCommTag;

    //protected TWAgentBoundingMemory memory;
    public TWAgentOrangeThree(int xpos, int ypos, TWEnvironment env, double fuelLevel, String agentCommTag) {
        super(xpos, ypos, env, fuelLevel, agentCommTag);
        this.agentCommTag = agentCommTag;
        // init astar
        int astarLimit = Parameters.xDimension + Parameters.yDimension;
        astarLimit = (int) Math.ceil(astarLimit*1.2);
        this.astar = new AstarPathGenerator(env, this, astarLimit);
    }

    @Override
    protected TWThought think() { 
        // lvl1, refuel, putdown, and pickup
        if (this.x == 0 && this.y == 0 && this.fuelLevel <= Parameters.defaultFuelLevel - 1) {
            return new TWThought(TWAction.REFUEL, null);
        }
        // plugging a hole uses no fuel, so do that if possible
        TWAgentPercept cur = this.memory.getCurrentLocPercept();
        if (cur != null && cur.getO() instanceof TWHole && this.carriedTiles.size() > 0) {
            this.target = cur.getO();
            return new TWThought(TWAction.PUTDOWN, null);
        }
        // same applies for picking up a tile
        if (cur != null && cur.getO() instanceof TWTile && this.carriedTiles.size() < 3) {
            this.target = cur.getO();
            return new TWThought(TWAction.PICKUP, null);
        }
        
        // lvl2, greedy
        if (this.bmemory.approxLifetime() <= 35) {
            TWThought greedyThought = this.greedyThinkAlt();
            // because greedy is ..
            if(greedyThought != null) return greedyThought;
            // otherwise just default to EVAgent which knows how to handle the situation :)
        }
        
        // lvl3, complicated
        return super.think();
    }

    private TWDirection getRandomDirection() {

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];

        if (this.getX() >= this.getEnvironment().getxDimension()) {
            randomDir = TWDirection.W;
        } else if (this.getX() <= 1) {
            randomDir = TWDirection.E;
        } else if (this.getY() <= 1) {
            randomDir = TWDirection.S;
        } else if (this.getY() >= this.getEnvironment().getxDimension()) {
            randomDir = TWDirection.N;
        }

        return randomDir;

    }

    @Override
    public String getName() {
        return agentCommTag+" Agent Orange Three"; // so politically incorrect?
    }
    
    
    protected TWThought greedyThinkAlt() {
        
        TWTile tile = this.getMemory().getNearbyTile(x, y, 10);
        TWHole hole = this.getMemory().getNearbyHole(x, y, 10);
        
        TWPath path=null;
        if(this.carriedTiles.isEmpty()){
            //to check the steps needed to reach a tile
            if(tile==null){
                //no tile around in its memory
                //System.out.println("No carried tiles, no tiles in memory");
                return new TWThought(TWAction.MOVE, getRandomDirection());                
            }
            //tile seen in memory
            else{
                if(this.sameLocation(tile)){
                    this.target = tile;
                    //System.out.println("no carried tiles- tile at same location- pickup");
                    return new TWThought(TWAction.PICKUP, null);
                }
                else{
                 
                 path = astar.findPath(x, y, tile.getX(), tile.getY());
                 if(path == null) return null;
                 //System.out.println("no carried tiles- tile seen in memory");
                 return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                }
            }  
        }else if(this.carriedTiles.size() < 3){
            if(tile==null){
                if(hole!=null){
                    if(this.sameLocation(hole)){
                        this.target = hole;
                        //System.out.println("carried tiles < 3 - tile=null, hole!=null");
                        return new TWThought(TWAction.PUTDOWN, null);
                    }
                   else{
                        path = astar.findPath(x, y, hole.getX(), hole.getY());
                        if(path == null) return null;
                        //System.out.println("carried tiles < 3 - tile=null, hole=null");
                    }   
                }
                else{
                    //System.out.println("carried tiles < 3 -- no tile or hole found");
                    return new TWThought(TWAction.MOVE, this.getRandomDirection());
                }
            }
            else{
                if(this.sameLocation(tile)){
                    this.target = tile;
                    //System.out.println("carried tiles < 3 - tile at same location- pickup");
                    return new TWThought(TWAction.PICKUP, null);
                }
                else{
                    if(hole!=null){
                        if(this.sameLocation(hole)){
                            this.target = hole;
                            //System.out.println("carried tiles < 3 - hole at same location- putdown");
                            return new TWThought(TWAction.PUTDOWN, null);
                        }
                        else{
                            TWPath pathToHole = astar.findPath(x, y, hole.getX(), hole.getY());
                            if(path == null) return null;
                            TWPath pathToTile = astar.findPath(x, y, tile.getX(), tile.getY());
                            if(path == null) return null;
                            if(pathToHole.getpath().size() < pathToTile.getpath().size()){
                                //System.out.println("carried tiles < 3 - Found tile and hole- going for the hole");
                                this.target = hole;
                                path = pathToHole;
                            }
                            else{
                                //System.out.println("carried tiles < 3 - Found tile and hole- going for the tile");
                                this.target = tile;
                                path = pathToTile;
                            } 
                        }
                    }
                    else{
                        
                        path = astar.findPath(x, y, tile.getX(), tile.getY());
                        if(path == null) return null;
                        //System.out.println("carried tiles < 3 - tile found, no hole");
                        return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                    } 
                }
            } 
        }
        else{
            if(hole!=null){
                if(this.sameLocation(hole)){
                    //System.out.println("carried tiles = 3 --- hole in same location");
                    this.target = hole;
                    return new TWThought(TWAction.PUTDOWN, null);
                }
                else{
                    path = astar.findPath(x, y, hole.getX(), hole.getY());
                    if(path == null) return null;
                    //System.out.println("carried tiles = 3 --- hole seen");
                }
            }
            else
                return new TWThought(TWAction.MOVE, getRandomDirection());
        }

        return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
    }
    
    
    protected TWThought greedyThink() {
        TWTile tile = this.getMemory().getNearbyTile(x, y, 10);
        TWHole hole = this.getMemory().getNearbyHole(x, y, 10);

        //
        if (this.carriedTiles.isEmpty()) {
            //to check the steps needed to reach a tile
            if (tile == null) {
                this.getMemory().noTarget();
                return new TWThought(TWAction.MOVE, getRandomDirection());
            } else {

                //no tile around in its memory
                if (this.sameLocation(tile)) {
                    this.target = tile;
                    this.getMemory().noTarget();
                    return new TWThought(TWAction.PICKUP, null);
                } else {

                    this.getMemory().setTarget(tile.getX(), tile.getY());
                    TWPath path = astar.findPath(x, y, tile.getX(), tile.getY());
                    if(path == null) return null;
                    return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());

                }//the the agent is not empty handed
            }

            //agent has more than one tile
        } else {

            TWPath path = null;
            if (hole == null) {
                if (tile != null && this.carriedTiles.size() < 3) {
                    if (this.sameLocation(tile)) {
                        this.target = tile;
                        return new TWThought(TWAction.PICKUP, null);
                    }
                    this.getMemory().setTarget(tile.getX(), tile.getY());
                    //System.out.println("more than one tile 1");
                    path = astar.findPath(x, y, tile.getX(), tile.getY());
                    if(path == null) return null;
                } else {
                    return new TWThought(TWAction.MOVE, getRandomDirection());
                }
            } else {
                if (this.sameLocation(hole)) {
                    this.target = hole;
                    this.getMemory().noTarget();
                    return new TWThought(TWAction.PUTDOWN, null);
                } else {
                    this.getMemory().setTarget(hole.getX(), hole.getY());
                    //System.out.println("more than one tile 2");
                    path = astar.findPath(x, y, hole.getX(), hole.getY());
                    if(path == null) return null;
                }

            }
            //System.out.println(path.getStep(0).getDirection());
            return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
        }
    }
}
