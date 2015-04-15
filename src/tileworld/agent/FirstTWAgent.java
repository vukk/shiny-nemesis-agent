/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import tileworld.environment.TWDirection;
import tileworld.environment.TWEnvironment;
import tileworld.exceptions.CellBlockedException;

/**
 *
 * - Search/COVER - Pick up - Search/COVER
 *
 *
 * Other notes: - could sample environment changing speeds and approximate -
 * then act accordingly and set memory decay - timestamp memories, calculate
 * probability of them still being relevant - share current approximations with
 * the cooperating agent(s) - A* for moving to destination, but covering ground
 * is also important - -
 *
 */
public class FirstTWAgent extends TWAgent {

    public FirstTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos, ypos, env, fuelLevel);
        this.memory = new TWAgentWorkingMemory(this, env.schedule, env.getxDimension(), env.getyDimension());
    }

    protected TWThought think() {
//        getMemory().getClosestObjectInSensorRange(Tile.class);
        System.out.println("Simple Score: " + this.score);
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
        return "First Agent";
    }
}
