package tileworld.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.text.html.HTMLDocument;
import sim.engine.Schedule;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.IntBag;
import tileworld.environment.NeighbourSpiral;
import tileworld.Parameters;
import tileworld.environment.TWEntity;

import tileworld.environment.TWHole;
import tileworld.environment.TWObject;
import tileworld.environment.TWObstacle;
import tileworld.environment.TWTile;

/**
 * TWAgentMemory
 *
 * @author michaellees
 *
 * Created: Apr 15, 2010
 *
 * Copyright michaellees 2010
 *
 * Description:
 *
 * This class represents the memory of the TileWorld agents. It stores all
 * objects which is has observed for a given period of time. You may want to
 * develop an entirely new memory system or extend/modify this one.
 *
 * The memory is supposed to have a probabilistic decay, whereby an element is
 * removed from memory with a probability proportional to the length of time the
 * element has been in memory. The maximum length of time which the agent can
 * remember is specified as MAX_TIME. Any memories beyond this are automatically
 * removed.
 */
public class TWAgentBoundingMemory extends TWAgentWorkingMemory {
    /*
     * Bounds of lifetime
     * Note that boundMin can be higher than actual lifetime!
     * It is however guaranteed that boundMin <= boundMax
     * and boundMax is never lower than actual lifetime.
     */

    protected long boundMin = 1;
    protected long boundMax = tileworld.Parameters.endTime;

    // Stores turn when a square was last observed. 0 means not observed.
    protected long[][] observed;

    public TWAgentBoundingMemory(TWAgent moi, Schedule schedule, int x, int y) {
        super(moi, schedule, x, y);
        observed = new long[Parameters.xDimension][Parameters.yDimension];
        System.out.println("Bounding memory initialized");
    }

    /**
     * Called at each time step, updates the memory map of the agent. Note that
     * some objects may disappear or be moved, in which case part of sensed may
     * contain null objects.
     *
     * Implements memory that detects differences between sensed and memory.
     *
     * Also note that currently the agent has no sense of moving objects, so an
     * agent may remember the same object at two locations simultaneously.
     *
     * Other agents in the grid are sensed and passed to this function. But it
     * is currently not used for anything. Do remember that an agent sense
     * itself too.
     *
     * @param sensedObjects bag containing the sensed objects
     * @param objectXCoords bag containing x coordinates of objects
     * @param objectYCoords bag containing y coordinates of object
     * @param sensedAgents bag containing the sensed agents
     * @param agentXCoords bag containing x coordinates of agents
     * @param agentYCoords bag containing y coordinates of agents
     */
    public void updateMemory(Bag sensedObjects, IntBag objectXCoords, IntBag objectYCoords, Bag sensedAgents, IntBag agentXCoords, IntBag agentYCoords) {
        // Reset the closest objects for new iteration of the loop (this is short
        // term observation memory if you like) It only lasts one timestep
        closestInSensorRange = new HashMap<Class<?>, TWEntity>(4);

        // Must all be same size.
        assert (sensedObjects.size() == objectXCoords.size() && sensedObjects.size() == objectYCoords.size());

        // Helper grid, could just copy from environment but let's not do
        // anything suspicious
        int sensorGridSize = 2 * Parameters.defaultSensorRange + 1;
        TWEntity[][] grid = new TWEntity[sensorGridSize][sensorGridSize];

        // Loop all sensed entities to the grid
        for (int i = 0; i < sensedObjects.size(); i++) {
            TWEntity o = (TWEntity) sensedObjects.get(i);
            if (!(o instanceof TWObject)) {
                continue;
            }
            // Calculate relative coordinates.
            // First agent position as origin then shift by sensor range.
            // Note that agent is not in origin of this grid, but in
            // [Parameters.defaultSensorRange, Parameters.defaultSensorRange]
            int gridX = objectXCoords.get(i) - me.getX() + Parameters.defaultSensorRange;
            int gridY = objectYCoords.get(i) - me.getY() + Parameters.defaultSensorRange;

            grid[gridX][gridY] = o;
        }

        System.out.println("updating memory, size " + memorySize + " low " + boundMin + " hi " + boundMax + " approx " + (long) Math.round((boundMax + 9 * boundMin) / 10.0));

        // Loop over grids to notice differences
        for (int i = 0; i < sensorGridSize; i++) {
            for (int j = 0; j < sensorGridSize; j++) {
                int envX = me.getX() - Parameters.defaultSensorRange + i;
                int envY = me.getY() - Parameters.defaultSensorRange + j;

                // If not in bounds, skip
                if (!me.getEnvironment().isInBounds(envX, envY)) {
                    continue;
                }

                TWEntity objSensed = grid[i][j];
                TWAgentPercept inMem = objects[envX][envY];

                //System.out.println("updating memory " + i + " " + j);
                //System.out.println("x: " + envX + " y: " + envY);
                // Handle different cases
                if (objSensed == null && inMem == null) {
                    // No need to change.
                    //System.out.println("no change");
                } else if (objSensed != null) {
                    //System.out.println("obj sensed not null");
                    if (inMem == null) {
                        //System.out.println("new entity found");
                        // New entity found
                        this.memorySize++;
                        // Set obj
                        objects[envX][envY] = new TWAgentPercept(objSensed, this.getSimulationTime(), observed[envX][envY]);
                        memoryGrid.set(envX, envY, objSensed);
                        updateClosest(objSensed);
                    } else if (objSensed.getClass() != inMem.getO().getClass()) {
                        //System.out.println("entity changed");
                        // The entity has changed!
                        // Update bounds
                        checkMaxBound(envX, envY, inMem);
                        // Set obj
                        objects[envX][envY] = new TWAgentPercept(objSensed, this.getSimulationTime(), observed[envX][envY]);
                        memoryGrid.set(envX, envY, objSensed);
                        updateClosest(objSensed);
                    } else {
                        //System.out.println("same class found");
                        // Otherwise the objects are of same class, thus
                        // it is impossible to know if they are same or
                        // different...
                        // Because of the low probability of another instance
                        // of the same class being instantiated at the same
                        // place, this would be weak evidence of long lifetime.
                        // Hence update lower bound
                        checkMinBound(envX, envY, inMem);
                    }
                } else if (inMem != null) {
                    //System.out.println("inmem not null, sensed obj null, something disappeared");
                    // Something has disappeared!

                    // Update maximum bound
                    checkMaxBound(envX, envY, inMem);

                    // Remove from memory
                    objects[envX][envY] = null;
                    memoryGrid.set(envX, envY, null);

                    this.memorySize--;
                }

                //System.out.println("observed is " + observed[envX][envY]);
                // Update observed turn
                //System.out.println("Simtime: " + this.getSimulationTime());
                //System.out.println("Simtime long: " + (long) this.getSimulationTime());
                observed[envX][envY] = (long) this.getSimulationTime();
            }
        }

        this.decayMemoryByBound();

//        me.getEnvironment().getMemoryGrid().clear();  // THis is equivalent to only having sensed area in memory
//       this.decayMemory();       // You might want to think about when to call the decay function as well.
//       Agents are currently not added to working memory. Depending on how 
//       communication is modelled you might want to do this.
//        neighbouringAgents.clear();
//        for (int i = 0; i < sensedAgents.size(); i++) {
//            
//            
//            if (!(sensedAgents.get(i) instanceof TWAgent)) {
//                assert false;
//            }
//            TWAgent a = (TWAgent) sensedAgents.get(i);
//            if(a.equals(me)){
//                continue;
//            }
//            neighbouringAgents.add(a);
//        }
    }

//    public TWAgent getNeighbour(){
//        if(neighbouringAgents.isEmpty()){
//            return null;
//        }else{
//            return neighbouringAgents.get(0);
//        }
//    }
    public long getLifetimeMinBound() {
        return boundMin;
    }

    public long getLifetimeMaxBound() {
        return boundMax;
    }

    public long[][] getObservedTimeGrid() {
        return observed;
    }
    
    @Override
    public void removeAgentPercept(int x, int y) {
        // Remove from memory
                    objects[x][y] = null;
                    memoryGrid.set(x, y, null);

                    this.memorySize--;
    }

    private void checkMinBound(int envX, int envY, TWAgentPercept inMem) {
        double difference = this.getSimulationTime() - inMem.getT();
        // boundMin is an approximation, never move boundMin over boundMax
        if (difference > boundMin && difference <= boundMax) {
            boundMin = (long) difference;
        }
    }

    private void checkMaxBound(int envX, int envY, TWAgentPercept inMem) {
        // this.getSimulationTime() - inMem.getT()
        double difference = this.getSimulationTime() - inMem.getObsDiff();
        if (difference < boundMax) {
            boundMax = (long) difference;
        }
        // boundMin is an approximation and could be wrong, so fix it if needed
        if (boundMin > boundMax) {
            boundMin = boundMax;
            System.out.println("pushing minbound down, simtime " + this.getSimulationTime() + " obsdiff " + inMem.getObsDiff() + " difference " + difference);
            System.out.println("Reason is " + inMem.getO().getClass() + " in " + envX + "," + envY + " observed " + observed[envX][envY]);
        }
    }

    private void decayMemoryByBound() {
        for (int i = 0; i < Parameters.xDimension; i++) {
            for (int j = 0; j < Parameters.yDimension; j++) {
                TWAgentPercept inMem = objects[i][j];
                if (inMem == null) {
                    continue;
                }
                double difference = this.getSimulationTime() - inMem.getT();
                if (difference > boundMax) {
                    // we must set observed to current - boundMax when removing,
                    // otherwise the is a bug when we see entity of same class
                    // but actually different object when the original has been
                    // removed by environment and new one of same class has been
                    // created by the environment to the exactly same spot.
                    observed[i][j] = (long) this.getSimulationTime() - boundMax;

                    // remove from memory
                    memoryGrid.set(i, j, null);
                    objects[i][j] = null;
                    memorySize--;
                }
            }
        }
    }

    @Override
    public ObjectGrid2D getMemoryGrid() {
        System.out.println("Somebody is fetching memoryGrid, class " + this.memoryGrid.getClass());
        return this.memoryGrid;
    }
}
