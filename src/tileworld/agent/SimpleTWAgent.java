/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import tileworld.Parameters;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;
import tileworld.environment.TWEntity;
import tileworld.exceptions.CellBlockedException;
import tileworld.agent.TWAgentWorkingMemory;
import tileworld.agent.TWAgent;
import tileworld.agent.TWThought;
import tileworld.planners.AstarPathGenerator;
import tileworld.planners.TWPath;

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
public class SimpleTWAgent extends TWAgent {

    int threshold = 10;
    private TWEntity target;
    private AstarPathGenerator planner;

    public SimpleTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos, ypos, env, fuelLevel);
        this.planner = new AstarPathGenerator(env, this, 100);
        this.target = null;
        this.memory = new TWAgentWorkingMemory(this, env.schedule, env.getxDimension(), env.getyDimension());
    }

    protected TWThought think() {
//      getMemory().getClosestObjectInSensorRange(Tile.class);

        //to refuel the agent
        if (this.x == 0 && this.y == 0 && this.fuelLevel <= Parameters.defaultFuelLevel - 1) {
            return new TWThought(TWAction.REFUEL, null);
        }
        TWTile tile = this.getMemory().getNearbyTile(x, y, threshold);
        TWHole hole = this.getMemory().getNearbyHole(x, y, threshold);

        //check fuel level
        if (this.fuelLevel <= (this.x + this.y) * 1.5 + 10) {

            if (this.carriedTiles.size() > 0 && hole != null && this.sameLocation(hole)) {
                this.target = hole;
                return new TWThought(TWAction.PUTDOWN, null);
            }
            this.getMemory().setTarget(0, 0);
            TWPath path = planner.findPath(x, y, 0, 0);
            System.out.println(path.getStep(0).getX() + ", " + path.getStep(0).getY());
            return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());

        }

        //
        if (this.carriedTiles.isEmpty()) {
            //to check the steps needed to reach a tile
            if (tile == null) {
                this.getMemory().noTarget();
                return new TWThought(TWAction.MOVE, getStepDirection());
            } else {

                //no tile around in its memory
                if (this.sameLocation(tile)) {
                    this.target = tile;
                    this.getMemory().noTarget();
                    return new TWThought(TWAction.PICKUP, null);
                } else {

                    this.getMemory().setTarget(tile.getX(), tile.getY());
                    TWPath path = planner.findPath(x, y, tile.getX(), tile.getY());
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
                    System.out.println("more than one tile 1");
                    path = planner.findPath(x, y, tile.getX(), tile.getY());
                } else {
                    return new TWThought(TWAction.MOVE, getStepDirection());
                }
            } else {
                if (this.sameLocation(hole)) {
                    this.target = hole;
                    this.getMemory().noTarget();
                    return new TWThought(TWAction.PUTDOWN, null);
                } else {
                    this.getMemory().setTarget(hole.getX(), hole.getY());
                    System.out.println("more than one tile 2");
                    path = planner.findPath(x, y, hole.getX(), hole.getY());
                }

            }
            System.out.println(path.getStep(0).getDirection());
            return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
            /* if(tile == null && hole == null){
             this.getMemory().noTarget();
             return new TWThought(TWAction.MOVE, getStepDirection());
             } 
             if(tile != null && hole == null){
             if(this.sameLocation(tile)){
             this.target = tile;
             this.getMemory().noTarget();
             return new TWThought(TWAction.PICKUP, null);
             }else{
             this.getMemory().setTarget(tile.getX(), tile.getY());
             path = planner.findPath(x, y, tile.getX(), tile.getY());
             }
             }
             if(tile == null && hole != null){
             if(this.sameLocation(hole)){
             this.target = hole;
             this.getMemory().noTarget();
             return new TWThought(TWAction.PUTDOWN, null);
             }else{
             this.getMemory().setTarget(hole.getX(), hole.getY());
             path = planner.findPath(x, y, hole.getX(), hole.getY());
             }
             }
             if(tile != null && hole != null){
             if(this.sameLocation(tile)){
             this.target = tile;
             this.getMemory().noTarget();
             return new TWThought(TWAction.PICKUP,null);
             }
                
             if(this.sameLocation(hole)){
             this.target = hole;
             this.getMemory().noTarget();
             return new TWThought(TWAction.PUTDOWN,null);
            
             }
                
                
             }*/

        }
        /*else{ //the agent carries 3 tiles
         TWHole hole = this.getMemory().getNearbyHole(x, y, Step_to_Hole);
         if(hole==null){
         this.getMemory().noTarget();
         return new TWThought(TWAction.MOVE, getStepDirection());
         }else{
         if(this.sameLocation(hole)){
         this.target=hole;
         this.getMemory().noTarget();
         return new TWThought(TWAction.PUTDOWN, null);
         }else{
         this.getMemory().setTarget(hole.getX(), hole.getY());
         TWPath path = planner.findPath(x, y, hole.getX(), hole.getY());
         return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
         }
         }
         } */
    }

    //decide the the step direction
    private TWDirection getStepDirection() {

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
    protected void act(TWThought thought) {

        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()
        switch (thought.getAction()) {
            case MOVE:
                try {
                    this.move(thought.getDirection());
                } catch (CellBlockedException ex) {

                    // ell is blocked, replan?
                }
                break;

            case PICKUP:

                pickUpTile((TWTile) this.target);
                memory.removeAgentPercept(this.target.getX(), this.target.getY());
                break;

            case PUTDOWN:
                putTileInHole((TWHole) this.target);
                memory.removeAgentPercept(this.target.getX(), this.target.getY());
                break;

            case REFUEL:
                refuel();
                break;

        }

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
        return "Dumb Agent";
    }
}
