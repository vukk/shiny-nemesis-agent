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
public class TWAgentOrangeTwo extends ExpectedValueAgent {
    
    protected AstarPathGenerator astar;
    //protected TWAgentBoundingMemoryComm bmemory;
    protected TWPath curPlan;
    protected int curPlanLvl;
    protected TWAgent simulatedAgent;
    protected TWEntity target;
    protected String agentCommTag;

    //protected TWAgentBoundingMemory memory;
    public TWAgentOrangeTwo(int xpos, int ypos, TWEnvironment env, double fuelLevel, String agentCommTag) {
        super(xpos, ypos, env, fuelLevel, agentCommTag);
        System.out.println("Initializing memory");
        //this.memory = new TWAgentWorkingMemory(this, env.schedule, env.getxDimension(), env.getyDimension());
        //this.memory = new TWAgentBoundingMemory(this, env.schedule, env.getxDimension(), env.getyDimension());
        //this.bmem = (TWAgentBoundingMemory) this.memory;
        //System.out.println("Memory initialized");
        // limit is corner to corner plus around 20% extra
        int astarLimit = Parameters.xDimension + Parameters.yDimension;
        astarLimit = (int) Math.ceil(astarLimit*1.2);
        this.astar = new AstarPathGenerator(env, this, astarLimit);
        this.curPlanLvl = Integer.MAX_VALUE;
        this.agentCommTag = agentCommTag;
    }

    @Override
    protected TWThought think() {
        // prepare
        // reset plan when it has ended
        if (curPlan == null || (curPlan != null && !curPlan.hasNext())) {
            curPlan = null;
            curPlanLvl = Integer.MAX_VALUE;
        }
        
        // lvl-1 replace old plan if blocks appear
        
        // lvl0, refuel, putdown, and pickup
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
        
        /*// lvl1, fuel
        if (curPlanLvl == 1)
            return new TWThought(TWAction.MOVE, curPlan.popNext().getDirection());
        if (this.fuelLevel <= (this.x + this.y) * 1.5 + 10) {
            curPlan = astar.findPath(x, y, 0, 0);
            curPlanLvl = 1;
            return new TWThought(TWAction.MOVE, curPlan.popNext().getDirection());
        }*/
        
        // lvl2, greedy
        if (this.bmemory.approxLifetime() <= 35) {
            return this.greedyThink();
        }
        
        // lvl3, complicated
        return super.think();
        
        // lvlX, just for development
        //return new TWThought(TWAction.MOVE, getRandomDirection());
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

                    // cell is blocked, replan?
                }
                break;

            case PICKUP:
                //pickUpTile((TWHole) this.bmem.getCurrentLocPercept().getO());
                pickUpTile((TWTile) this.target);
                memory.removeAgentPercept(this.target.getX(), this.target.getY());
                //bmem.removeCurrentLocPercept();
                break;

            case PUTDOWN:
                //putTileInHole((TWHole) this.bmem.getCurrentLocPercept().getO());
                putTileInHole((TWHole) this.target);
                memory.removeAgentPercept(this.target.getX(), this.target.getY());
                //bmem.removeCurrentLocPercept();
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
        return agentCommTag+" Agent Orange Two"; // so politically incorrect?
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
                    path = astar.findPath(x, y, tile.getX(), tile.getY());
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
                    System.out.println("more than one tile 2");
                    path = astar.findPath(x, y, hole.getX(), hole.getY());
                }

            }
            System.out.println(path.getStep(0).getDirection());
            return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
        }
    }
}
