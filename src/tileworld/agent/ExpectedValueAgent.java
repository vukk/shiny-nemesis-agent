/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent;

import java.util.LinkedList;
import java.util.Queue;
import tileworld.Parameters;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWHole;
import tileworld.environment.TWObject;
import tileworld.environment.TWTile;
import tileworld.agent.EVMessage;
import tileworld.exceptions.CellBlockedException;

/**
 * TWContextBuilder
 *
 * @author Diego Yepiz
 * Created: March 25, 2015
 *
 *
 *
 * Description: Instead of searching in order to pick up the tiles the agent has in memory optimally, 
 * the agent uses an expected value algorithm and it detects via a Naive Bayes the move that will maximice its expected value.
 * This search won't be optimal, but given the fact that obtaining information has a cost (the number of steps to gain information)
 * this algorithm is (seems) ideal because it doesn't really has a cost of exploring, it just moves to where it will gain the most value.
 *
 */
public class ExpectedValueAgent extends TWAgent{
    
    private String ID;
    private EVMessage message;
    private long lifeTime;
    private int moves[][]= {{1,0},{0,1},{-1,0},{0,-1}};
    private int distances[][][]=new int[moves.length][Parameters.yDimension][Parameters.xDimension]; //move, row, column
    private double expectedT[] = new double[(int)Parameters.endTime+1]; //The expected values of finding a tile
    private double expectedH[] = new double[(int)Parameters.endTime+1]; //The expected values of finding a hole
    private final int INF=1000000000; // 10^9 a number big enough so that it will never be reached, yet it fits in a 32 bit int.
    private final double fuelSafety=50; //The level of safety the agent has to determine where to go.
    private boolean didSomething;
    private TWAgentBoundingMemoryComm bmemory;
    
    public ExpectedValueAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel, String ID) {
        super(xpos,ypos,env,fuelLevel);
        this.memory = new TWAgentBoundingMemoryComm(this, env.schedule, env.getxDimension(), env.getyDimension(), ID);
        this.bmemory = (TWAgentBoundingMemoryComm) this.memory;
        this.ID=ID;
    }

    protected TWThought think() {
        //System.out.println(this.ID+" is thinking.");
        didSomething=false;
        //System.out.println("Simple Score: " + this.score);
        if(this.fuelLevel<Parameters.defaultFuelLevel && x==0 && y==0) { //If its standing in the refuelery and doesnt have full fuel
            return new TWThought(TWAction.REFUEL,TWDirection.Z);
        }
        if(this.fuelLevel==0) {
            return new TWThought(TWAction.MOVE,TWDirection.Z);
        }
        if(this.bmemory.objects[x][y]==null) return new TWThought(TWAction.MOVE,getSmartDirection());
        didSomething=true;
        TWObject o = (TWObject) this.bmemory.objects[x][y].getO();//get mem object
        if(this.carriedTiles.size()<3 && TWTile.class.isInstance(o)) { //If its standing on a tile and can pick it up
            return new TWThought(TWAction.PICKUP,TWDirection.Z);
        }
        if(this.carriedTiles.size()>0 && TWHole.class.isInstance(o)) { //If its standing on a hole and can drop a tile
            return new TWThought(TWAction.PUTDOWN,TWDirection.Z);
        } 
        didSomething=false;
        return new TWThought(TWAction.MOVE,getSmartDirection());
    }

    @Override
    protected void act(TWThought thought) {
        try {
            switch(thought.getAction()) {
            case MOVE : 
                this.move(thought.getDirection());
                break;
            case PICKUP:
                pickUpTile((TWTile) this.bmemory.objects[x][y].getO());
                memory.removeAgentPercept(x,y);
                break;
            case PUTDOWN:
                putTileInHole((TWHole) this.bmemory.objects[x][y].getO());
                memory.removeAgentPercept(x, y);
                break;
            case REFUEL:
                refuel();
                break;   
            }
            this.sendMessage();
        }  catch (CellBlockedException ex) {

           // Cell is blocked, replan?
        }
    }
    
    private class Position { // TODO: Move to its own file, or not, it doesn't really matter
        int row, col;
        Position(int r, int c) {
           row=r;
           col=c;
        }
    }
    
    private boolean validPos(int row, int col) {
        return row>=0&&row<Parameters.yDimension&&col>=0&&col<Parameters.xDimension&&!this.bmemory.isCellBlocked(col, row);
    }
    
    private double getExpected(int row, int col, int dist) {
        double ret=0.0;
        double sumP=this.bmemory.tileMean+this.bmemory.holeMean;
        int currentTime=(int)Math.round(this.bmemory.getSimulationTime());
        ret=0;
        if(this.bmemory.objects[col][row]==null) { //Empty square
            int lastVisited=(int)(this.bmemory).getObservedTimeGrid()[col][row];
            if(this.carriedTiles.size()<3) {
                ret+=(1.0-expectedT[currentTime-lastVisited]); //The inverse since these are the probabilities of it being empty when the agent reaches it.
            }
            if(!this.carriedTiles.isEmpty()) {
                ret+=(1.0-expectedH[currentTime-lastVisited]); //The inverse since these are the probabilities of it being empty when the agent reaches it.
            }
        }
        else {
            TWObject o =(TWObject) this.bmemory.objects[col][row].getO();//get mem object
            if((this.carriedTiles.size()<3 && TWTile.class.isInstance(o))) {
                ret = 1.0;
            }
            if((this.carriedTiles.size()>0 && TWHole.class.isInstance(o))) {
                ret = 1.0;
            }
        }
        return ret/Math.pow(dist,2); //Change from 1 to 2, and let the one that shows better results
    }


    private TWDirection getSmartDirection() {
        
        for(int i=0; i<moves.length; i++) {
            for(int j=0; j<Parameters.yDimension; j++) {
                for(int k=0; k<Parameters.xDimension; k++) {
                    distances[i][j][k]=INF;
                }
             }
        }
        
        lifeTime=(this.bmemory.getLifetimeMaxBound()+this.bmemory.getLifetimeMinBound())/2;
        
        
        /*
        //This is actually just an approximation, another formula will give better results.
        double expVal=0.02;
        expected[0]=expVal;
        for(int i=1; i<=lifeTime; i++) {
            expected[i]=expected[i-1]*(1.0-expVal);
        }
        
        //Sum the probabilities to get the expected values
        for(int i=1; i<=lifeTime; i++) {
            expected[i]+=expected[i-1];
        }
         */
        
        //This formula should give better results
        //double probAppear=0.0000266666;
        double probAppearT=bmemory.tileMean;
        double probAppearH=bmemory.holeMean;
        expectedH[0]=1.0;
        expectedT[0]=1.0;
        for(int i=1; i<=Parameters.endTime; i++) {
            expectedT[i]=expectedT[i-1]*(1.0-probAppearT);
            if(i-lifeTime-1>=0) expectedT[i]+=expectedT[i-(int)lifeTime-1]*probAppearT;
            expectedH[i]=expectedH[i-1]*(1.0-probAppearH);
            if(i-lifeTime-1>=0) expectedH[i]+=expectedH[i-(int)lifeTime-1]*probAppearH;
        }
        
        Queue<Position> q=new LinkedList<Position>();
        
        double maxExpected=-1.0;
        int dir=-1, bestFuel=INF, bestFuelId=-1, movesAvailable=0;
        double average=0.0;
        
        //Iterate over all 4 directions and get the one that offers the best expected value.
        for(int i=0; i<moves.length; i++) {
            int newRow=y+moves[i][1];
            int newCol=x+moves[i][0];
            double curExpected=0.0;
            if(validPos(newRow,newCol)) {
                movesAvailable++;
                Position pos= new Position(newRow,newCol);
                q.add(pos); //Insert in the front of the queue
                distances[i][newRow][newCol]=1;
                while(q.peek()!=null) { 
                    Position front=q.peek(); //Get the front of the queue
                    q.remove(); //Pop front of the queue
                    curExpected+=getExpected(front.row, front.col, distances[i][front.row][front.col]); //Test with 1 and 2, in theory 1 should be better.
                    for(int j=0; j<moves.length; j++) {
                        newRow=front.row+moves[j][1];
                        newCol=front.col+moves[j][0];
                        if(validPos(newRow, newCol) && distances[i][newRow][newCol] > distances[i][front.row][front.col]+1) {
                            distances[i][newRow][newCol]=distances[i][front.row][front.col]+1;
                            Position newPos= new Position(newRow,newCol);
                            q.add(newPos);
                        }
                    }
                }
            }
            //System.out.println("Dir: "+ i+" with value: "+curExpected + ", Visited: "+visitCount);
            average+=curExpected;
            if(bestFuel>distances[i][0][0]) {
                bestFuel=distances[i][0][0];
                bestFuelId=i;
            }
            if(maxExpected<curExpected) {
                maxExpected=curExpected;
                dir=i;
            }
        }
        average/=movesAvailable;
        
        //If the Agent can't reach the refueling (it knows the path is blocked), then make sure there is enough fuel, otherwise dont move.
        if(bestFuel==INF && this.x+this.y+fuelSafety <fuelLevel) {
            bestFuel=0;
        }
        
        // Check if the agent should go refuel
        if(Parameters.endTime-this.bmemory.getSimulationTime()<=fuelLevel);
        else if(fuelLevel<=fuelSafety||maxExpected<average*(bestFuel+fuelSafety)/(fuelLevel-fuelSafety)) { 
            dir=bestFuelId;
        }
        
        TWDirection direction;
        
        switch(dir) { //Set direction
            case 0:
                direction=TWDirection.E;
                break;
            case 1:
                direction=TWDirection.S;
                break;
            case 2:
                direction=TWDirection.W;
                break;
            case 3:
                direction=TWDirection.N;
                break;
            default:
                direction=TWDirection.Z;
                break;
        }
        
        return direction;
    }

    private void sendMessage() {
        this.message = new EVMessage(x, y, didSomething);
        EVCommunicator.put(getName(), this.message);
    }
    
    @Override
    public String getName() {
        return ID+" Expected Value Agent";
    }
}
