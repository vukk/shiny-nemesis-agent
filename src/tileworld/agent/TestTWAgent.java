/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import tileworld.environment.TWDirection;
import tileworld.environment.TWEnvironment;
import tileworld.exceptions.CellBlockedException;

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
public class TestTWAgent extends TWAgent {

    //protected TWAgentBoundingMemory memory;
    public TestTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos, ypos, env, fuelLevel);
        System.out.println("Initializing memory");
        this.memory = new TWAgentBoundingMemory(this, env.schedule, env.getxDimension(), env.getyDimension());
        System.out.println("Memory initialized");
    }

    protected TWThought think() {
//        getMemory().getClosestObjectInSensorRange(Tile.class);
        //System.out.println("Simple Score: " + this.score);
        return new TWThought(TWAction.MOVE, getRandomDirection());
    }

    @Override
    protected void act(TWThought thought) {

        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()
        try {
            this.move(thought.getDirection());
        } catch (CellBlockedException ex) {

            // Cell is blocked, replan?
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
        return "Test Agent";
    }
}
