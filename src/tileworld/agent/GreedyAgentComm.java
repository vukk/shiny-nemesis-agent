/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent;

import sim.engine.Schedule;
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
 * Created: Feb 6, 2011
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class GreedyAgentComm extends TWAgent{
    
    
    int threshold = 10;
    private TWEntity target;
    private AstarPathGenerator planner;
    private int[] friendPos;
    private Message messageSend;
    private Message messageRec;
    private String agentName;

    public GreedyAgentComm(int xpos, int ypos, TWEnvironment env, double fuelLevel, String agentName) {
        super(xpos,ypos,env,fuelLevel);
        this.planner= new AstarPathGenerator(env, this, 100);
        this.target=null;
        this.messageRec = new Message();
        this.messageSend = new Message();
        friendPos = null;
        this.agentName = agentName;
        this.memory = new TWAgentWorkingMemory(this, env.schedule, env.getxDimension(), env.getyDimension());
    }

    
    
    protected void sense(){
        super.sense();
        this.receiveMessage();
    }

    protected TWThought think() {
//      getMemory().getClosestObjectInSensorRange(Tile.class);
        
        //to refuel the agent
        if(this.x==0 && this.y==0 && this.fuelLevel <= Parameters.defaultFuelLevel -1){
            return new TWThought(TWAction.REFUEL, null);
        }
        
        TWTile tile = this.getMemory().getNearbyTile(x, y, threshold);
        TWHole hole = this.getMemory().getNearbyHole(x, y, threshold);
        
        //check fuel level
        if(this.fuelLevel <= (this.x +this.y)*1.5 +10){
            
            if( this.carriedTiles.size()>0 && hole!=null && this.sameLocation(hole)){
                this.target=hole;
                return new TWThought(TWAction.PUTDOWN, null);
            }
            //this.getMemory().setTarget(0,0);
            TWPath path = planner.findPath(x, y, 0,0);
            System.out.println(path.getStep(0).getX()+ ", " + path.getStep(0).getY() );
            return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
            
        }
        
        
        
        
        
        //Maintain minimum distance
        if (friendPos!= null){
            int dist = Math.abs(friendPos[0] - this.getX()) + Math.abs(friendPos[1] - this.getY());
            if(dist<=30)
                return new TWThought(TWAction.MOVE, this.getAwayDirection());
        }
        
        
        //
        
        if(this.carriedTiles.isEmpty()){
            //to check the steps needed to reach a tile
            if(tile==null){
                //this.getMemory().noTarget();
                return new TWThought(TWAction.MOVE, getStepDirection());                
            }
            
            else{
           
            //no tile around in its memory
                if(this.sameLocation(tile)){
                    this.target = tile;
                    //this.getMemory().noTarget();
                    return new TWThought(TWAction.PICKUP, null);
                }
                else{
                
                 //this.getMemory().setTarget(tile.getX(), tile.getY());
                 TWPath path = planner.findPath(x, y, tile.getX(), tile.getY());
                 return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                
                }//the the agent is not empty handed
            }
        
            
            
        //agent has more than one tile
        }else{
            
            
            TWPath path=null;
            if(hole==null){
                    if(tile!=null && this.carriedTiles.size() < 3){
                        if(this.sameLocation(tile)){
                            this.target=tile;
                            return new TWThought(TWAction.PICKUP, null);
                        }
                        //this.getMemory().setTarget(tile.getX(), tile.getY());
                        System.out.println("more than one tile 1");
                        path = planner.findPath(x, y, tile.getX(), tile.getY());
                    }
                    else{
                        return new TWThought(TWAction.MOVE, getStepDirection());
                    }
                }
            
            else{
                if(this.sameLocation(hole)){
                    this.target = hole;
                    //this.getMemory().noTarget();
                    return new TWThought(TWAction.PUTDOWN, null);
                }
                
                else{
                    //this.getMemory().setTarget(hole.getX(), hole.getY());
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
    private TWDirection getStepDirection(){

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.W;
        }else if(this.getX()<=1 ){
            randomDir = TWDirection.E;
        }else if(this.getY()<=1 ){
            randomDir = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.N;
        }

       return randomDir;

    }
    
    
    private TWDirection getAwayDirection(){

        //TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];
        if (friendPos==null)
            return this.getRandomDirection();
        
        TWDirection[] awayDir = new TWDirection[2];
        
        if (this.getX()> this.friendPos[0])
            awayDir[0] = TWDirection.E;
        else
            awayDir[0] = TWDirection.W;
        
        if(this.getY()> this.friendPos[1])
            awayDir[1] = TWDirection.S;
        else
            awayDir[1] = TWDirection.N;
        
        TWDirection awayDirection = awayDir[(int)(Math.random() * awayDir.length)];
        

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            awayDirection = TWDirection.W;
        }else if(this.getX()<=1 ){
            awayDirection = TWDirection.E;
        }else if(this.getY()<=1 ){
            awayDirection = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            awayDirection = TWDirection.N;
        }
        

       return awayDirection;

    }
  
    @Override
    protected void act(TWThought thought) {

        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()
        
        
        switch(thought.getAction()){
            case MOVE : 
                try {
                this.move(thought.getDirection());
                }  catch (CellBlockedException ex) {

                // ell is blocked, replan?
                }
                break;
            
            case PICKUP:
                pickUpTile((TWTile)this.target);
                memory.removeAgentPercept(this.target.getX(), this.target.getY());
                memory.getMemoryGrid().set(this.target.getX(), this.target.getY(), null);
                break;
                
            case PUTDOWN:
                putTileInHole((TWHole)this.target);
                memory.removeAgentPercept(this.target.getX(), this.target.getY());
                memory.getMemoryGrid().set(this.target.getX(), this.target.getY(), null);
                break;
                
            case REFUEL:
                refuel();
                break;
                
        }
        
        this.sendMessage();

        
    }
    
    /*
    send message
    1. current location
    2. hole closest to the other
    3. tile closest to the other
    */
    private void sendMessage(){
        //Schedule schedule  = new Schedule();
        //private method?
        //get target;
        //TWAgentPercept target = new TWAgentPercept(,memory.getSimulationTime());//private method for time
        //OR in x,y form
        
        
        int[] agentPos = new int[]{this.getX(),this.getY()};//agent's current position
        
        //get tile and hole, nearest to the other agent
        double threshold = 100.0;
        
        TWTile tile = null;
        TWHole hole = null;
        
        if(friendPos != null){
            tile = memory.getNearbyTile(friendPos[0], friendPos[1], threshold);
            hole = memory.getNearbyHole(friendPos[0], friendPos[1], threshold);
        }
        
        TWAgentPercept tilePer = null;
        TWAgentPercept holePer = null;
        
        if (tile!=null)
            tilePer = memory.getPerceptAt(tile.getX(), tile.getY());
        
        if(hole!=null)    
            holePer = memory.getPerceptAt(hole.getX(), hole.getY());
                
                
        //Create and send message
        this.messageSend = new Message(agentPos[0], agentPos[1], tilePer, holePer);
        //OR
        //messageSend = new Message(int[], tilePer, holePer);
        
        Communicator.put(getName(), this.messageSend);
        
    }
    
    
    /*
    receive message
    */
    private boolean receiveMessage(){
        this.messageRec = Communicator.get(getName());
        //update memory with the contents of the message
        
        Object[] contents=null;
        if (this.messageRec!=null)
            contents = messageRec.getContents();
        
        
        //checking if object[0] is of type int[];
        
        if(contents!=null){
        if(contents[0] instanceof int[]){
            this.friendPos = (int[])contents[0];
        }
        else{
            this.friendPos = null;
        }
        
        if (contents[0] instanceof TWAgentPercept){
            memory.updateMemory((TWAgentPercept)contents[0]);
        }
        
        if (contents[1] instanceof TWAgentPercept){
            memory.updateMemory((TWAgentPercept)contents[1]);
        }
        
        if (contents[2] instanceof TWAgentPercept){
            memory.updateMemory((TWAgentPercept)contents[2]);
        }
        }
        
           

        
        return true;
    }


    private TWDirection getRandomDirection(){

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.W;
        }else if(this.getX()<=1 ){
            randomDir = TWDirection.E;
        }else if(this.getY()<=1 ){
            randomDir = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.N;
        }

       return randomDir;

    }

    @Override
    public String getName() {
        return this.agentName;
    }
    
    
}
