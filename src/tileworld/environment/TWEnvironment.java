/**
 *
 */
package tileworld.environment;

import java.util.ArrayList;
import java.util.List;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import tileworld.Parameters;
import tileworld.TWGUI;
import tileworld.agent.*;
import tileworld.agent.TWAgent;

/**
 * TWEnvironment
 *
 * @author michaellees
 *
 * Created: Apr 16, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description: Contains the context of the environment and also creates and
 * removes objects at each time step. You don't need to modify this but should
 * look at the methods which may be helpful to you.
 *
 */
public class TWEnvironment extends SimState implements Steppable {

    //Parameters to configure the environment - read from main parameter file
    private final int xDimension = Parameters.xDimension; //size in cells
    private final int yDimension = Parameters.yDimension;

    /**
     * grid environment which stores all TWEntities, ObjectGrd is preferred over
     * SparseGrid. The environment is typically not too sparse and we do not
     * have multiple objects on the same cell.
     */
    private ObjectGrid2D objectGrid;
    private ObjectGrid2D agentGrid;

    private TWObjectCreator<TWTile> tileCreator;
    private TWObjectCreator<TWHole> holeCreator;
    private TWObjectCreator<TWObstacle> obstacleCreator;
    /**
     * Assumed all objects have same lifeTime now.
     */
    //containers for all objects in the environment
    private Bag tiles;
    private Bag holes;
    private Bag obstacles;
    private TWFuelStation fuelingStation;
    
    
    public Bag agents = new Bag();
    public int countCreatedTiles = 0;
    public int countCreatedHoles = 0;
            

    public TWFuelStation getFuelingStation() {
        return fuelingStation;
    }

    public TWEnvironment() {
        this(Parameters.seed);
    }

    public TWEnvironment(long seed) {
        super(seed);

        // create object creation distributions (assumed normal for now)
        this.tileCreator = new TWObjectCreator<TWTile>(Parameters.tileMean, Parameters.tileDev,
                tiles, this.random, new TWTile(), this);
        this.holeCreator = new TWObjectCreator<TWHole>(Parameters.holeMean, Parameters.holeDev,
                holes, this.random, new TWHole(), this);
        this.obstacleCreator = new TWObjectCreator<TWObstacle>(Parameters.obstacleMean,
                Parameters.obstacleDev, obstacles, this.random, new TWObstacle(), this);

        tiles = new Bag();
        holes = new Bag();
        obstacles = new Bag();
    }

    @Override
    public void start() {
        super.start();
        //create my grid
        this.objectGrid = new ObjectGrid2D(getxDimension(), getyDimension());
        this.agentGrid = new ObjectGrid2D(getxDimension(), getyDimension());
        if (TWGUI.instance != null) {
            TWGUI.instance.resetDisplay();
        }

        //The environment is also stepped each step
        schedule.scheduleRepeating(this, 1, 1.0);

        //Now we create some agents
        //createAgent(new SimpleTWAgent(0, 0, this, Parameters.defaultFuelLevel),2);
        
        //createAgent(new TWGreedySimpleAgent(0, 0, this, Parameters.defaultFuelLevel),2);
        //createAgent(new TWGreedySimpleAgent(1, 1, this, Parameters.defaultFuelLevel),3);
        
        //createAgent(new FirstTWAgent(0, 0, this, Parameters.defaultFuelLevel),2);
        //createAgent(new TestTWAgent(0, 0, this, Parameters.defaultFuelLevel), 2);
        //createAgent(new TWAgentOrange(0, 0, this, Parameters.defaultFuelLevel), 2);
        
        //createAgent(new ExpectedValueAgent(0, 0, this, Parameters.defaultFuelLevel, "First"), 2);
        //createAgent(new ExpectedValueAgent(1, 1, this, Parameters.defaultFuelLevel, "Second"), 3);
        
        //createAgent(new TWAgentOrange(0, 0, this, Parameters.defaultFuelLevel), 2);
        //createAgent(new TWAgentOrange(1, 1, this, Parameters.defaultFuelLevel), 3);
        
        //createAgent(new TWAgentOrangeTwo(0, 0, this, Parameters.defaultFuelLevel, "First"), 2);
        //createAgent(new TWAgentOrangeTwo(1, 1, this, Parameters.defaultFuelLevel, "Second"), 3);
        
        createAgent(new TWAgentOrangeThree(0, 0, this, Parameters.defaultFuelLevel, "First"), 2);
        createAgent(new TWAgentOrangeThree(1, 1, this, Parameters.defaultFuelLevel, "Second"), 3);

//      createAgent(new VaisaghAgent(1, 1, this, Parameters.defaultFuelLevel),3);
//        
        //create the fueling station
        fuelingStation = new TWFuelStation(0, 0, this);

    }

    private void createTWObjects(double time) {
        try {
            Bag createdHoles = holeCreator.createTWObjects(time);
            Bag createdTiles = tileCreator.createTWObjects(time);
            
            countCreatedHoles += createdHoles.numObjs;
            countCreatedTiles += createdTiles.numObjs;
            
            tiles.addAll(createdTiles);
            holes.addAll(createdHoles);
            obstacles.addAll(obstacleCreator.createTWObjects(time));
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Horribly inefficient, Context is not ordered so need complete iteration
     * of context to remove items. This is done every timestep
     */
    private void removeTWObjects(double timeNow) {

        for (int i = 0; i < tiles.size(); i++) {
            if (((TWObject) tiles.get(i)).getTimeLeft(timeNow) <= 0) {
                this.objectGrid.set(((TWObject) tiles.get(i)).getX(), ((TWObject) tiles.get(i)).getY(), null);
                tiles.remove(i);

            }
        }

        for (int i = 0; i < holes.size(); i++) {
            if (((TWObject) holes.get(i)).getTimeLeft(timeNow) <= 0) {
                this.objectGrid.set(((TWObject) holes.get(i)).getX(), ((TWObject) holes.get(i)).getY(), null);
                holes.remove(i);
            }
        }

        for (int i = 0; i < obstacles.size(); i++) {
            if (((TWObject) obstacles.get(i)).getTimeLeft(timeNow) <= 0) {
                this.objectGrid.set(((TWObject) obstacles.get(i)).getX(), ((TWObject) obstacles.get(i)).getY(), null);
                obstacles.remove(i);
            }
        }
    }

    public void step(SimState state) {
        // output performance
        double agentScores = 0;
        for(Object a : agents) {
            TWAgent ag = (TWAgent) a;
            agentScores += ag.getScore();
        }
        double opportunities = Math.min(countCreatedTiles, countCreatedHoles);
        System.out.println("Performance: " + agentScores/opportunities);
        
        double time = state.schedule.getTime();
        // create new objects
        createTWObjects(time);
        // remove old objects (dead ones)
        removeTWObjects(time);
    }

    /**
     * @return the grid
     */
    public ObjectGrid2D getObjectGrid() {
        return objectGrid;
    }

    public ObjectGrid2D getAgentGrid() {
        return agentGrid;
    }

    /**
     * @return the xDimension
     */
    public int getxDimension() {
        return xDimension;
    }

    /**
     * @return the yDimension
     */
    public int getyDimension() {
        return yDimension;
    }

    public boolean isCellOccupied(int x, int y) {
        return (objectGrid.get(x, y) != null);
    }
    
    /*
     * Called internally when a tile is picked up.
     * DO NOT CALL THIS FUNCTION YOURSELF.
     * Use TWAgent.pickUpTile() instead.
     */
    public void agentPicksUpTile(TWTile tile) {
        objectGrid.set(tile.getX(), tile.getY(), null);
        // OK, since the agent should still hold a reference to the hole object
        tiles.remove(tile);
    }
    
    /*
     * Called internally when a hole is plugged.
     * DO NOT CALL THIS FUNCTION YOURSELF.
     * Use TWAgent.putTileInHole() instead.
     */
    public void agentPutsTileInHole(TWHole hole) {
        objectGrid.set(hole.getX(), hole.getY(), null);
        holes.remove(hole);
    }

    /**
     * returns true if the specified location contains a TWEntity.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isCellBlocked(int x, int y) {
        if (this.isValidLocation(x, y)) {
            TWEntity e = (TWEntity) objectGrid.get(x, y);
            return (e != null && (e instanceof TWObstacle));
        } else {
            return true;
        }

    }

    public boolean doesCellContainObject(int x, int y) {
        return !(objectGrid.get(x, y) == null);
    }

    /**
     * Returns the manhattan distance from this entity to a specified location.
     *
     * @param x1 x coordinate to check to
     * @param y1 y coordinate to check to
     * @return the manhattan distance from this entity to the coordinate x1,y1
     */
    public double getDistance(int x, int y, int x1, int y1) {
        return Math.abs(x1 - x) + Math.abs(y1 - y);
    }

    /**
     * returns true if b is closer to o than c is.
     *
     * @param o
     * @param b
     * @param c
     * @return
     */
    public boolean isCloser(TWEntity o, TWEntity b, TWEntity c) {
        return (o.getDistanceTo(b) < o.getDistanceTo(c));
    }

    /**
     * picks a random location from the environment, used for free walk
     * algorithm. Bit stupid now, will be very slow when the environment fills
     * up.
     *
     * @param gx - this will be resulting x coordinate
     * @param gy
     */
    public Int2D generateRandomLocation() {
        int gx = 1, gy = 1;
        while (!isValidCreationLocation(
                gx = this.random.nextInt(this.xDimension),
                gy = this.random.nextInt(this.yDimension))) {
        }

        return new Int2D(gx, gy);
    }

    /**
     * Generates a random free location at least minDistance away from x, y
     *
     * @param x
     * @param y
     * @param minDistance
     */
    public Int2D generateFarRandomLocation(int x, int y, int minDistance) {
        int gx = 1, gy = 1;
        while (!isValidCreationLocation(gx = this.random.nextInt(this.xDimension),
                gy = this.random.nextInt(this.yDimension)) && this.getDistance(x, y, gx, gy) < minDistance) {
        }

        return new Int2D(gx, gy);
    }

    /**
     * Check if a given location is in the bounds of the environment
     *
     * @param x The x coordinate of the location to check
     * @param y The y coordinate of the location to check
     * @return True if the location is inside environment boundary
     */
    public boolean isInBounds(int x, int y) {
        return !((x < 0) || (y < 0) || (x >= this.xDimension || y >= this.yDimension));

    }

    /**
     * Check if a given location is valid for a move, both in bounds and not
     * blocked This is a cell which is in bounds and doesn't contain an
     * obstacle.
     *
     * @param x The x coordinate of the location to check
     * @param y The y coordinate of the location to check
     * @return True if the location is valid
     */
    public boolean isValidLocation(int x, int y) {

        return (isInBounds(x, y));

    }

    /**
     * Different to is ValidLocation, returns true if the location contains no
     * objects at all
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isValidCreationLocation(int x, int y) {

        return (isInBounds(x, y) && !this.doesCellContainObject(x, y));

    }

    /**
     * Creates and schedues a TWAgent. Also adds the agent to the portrayal if a
     * portrayal exists.
     *
     * Remember lower priority means it is executed earlier.
     *
     *
     * @param a
     * @param ordering
     */
    private void createAgent(TWAgent a, int ordering) {
        schedule.scheduleRepeating(a, ordering, 1.0);
        if (TWGUI.instance != null) {
            TWGUI.instance.addMemoryPortrayal(a);
        }
        agents.add(a);
    }

}
