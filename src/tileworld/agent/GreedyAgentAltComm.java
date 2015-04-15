/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import tileworld.Parameters;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEntity;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.AstarPathGenerator;
import tileworld.planners.TWPath;

/**
 *
 * @author SRIPORNA
 */
public class GreedyAgentAltComm extends TWAgent{
    int threshold = 10;
    private TWEntity target;
    private AstarPathGenerator planner;
    private int[] friendPos;
    private Message messageSend;
    private Message messageRec;
    private String agentName;

    public GreedyAgentAltComm(int xpos, int ypos, TWEnvironment env, double fuelLevel, String agentName) {
        super(xpos,ypos,env,fuelLevel);
        this.planner= new AstarPathGenerator(env, this, 100);
        this.target=null;
        this.messageRec = new Message();
        this.messageSend = new Message();
        friendPos = null;
        this.agentName = agentName;
    }
    
    @Override
    protected void sense(){
        super.sense();
        this.receiveMessage();
    }

    protected TWThought think() {
        
        //to refuel the agent
        if(this.x==0 && this.y==0 && this.fuelLevel <= Parameters.defaultFuelLevel -1){
            return new TWThought(TWAction.REFUEL, null);
        }
        TWTile tile = this.getMemory().getNearbyTile(x, y, threshold);
        TWHole hole = this.getMemory().getNearbyHole(x, y, threshold);
        
        //check fuel level
        if(this.fuelLevel <= (this.x +this.y)*1.5 + 10){
            
            if( this.carriedTiles.size()>0 && hole!=null && this.sameLocation(hole)){
                this.target=hole;
                return new TWThought(TWAction.PUTDOWN, null);
            }
            
            TWPath path = planner.findPath(x, y, 0,0);
            System.out.println(path.getStep(0).getX()+ ", " + path.getStep(0).getY() );
            return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
            
        }
        /*If no carried tiles:
            if no tile seen --> move randomly
            else pickup tile
        If one or more carried tiles:
        */
        
        TWPath path=null;
        if(this.carriedTiles.isEmpty()){
            //to check the steps needed to reach a tile
            if(tile==null){
                //no tile around in its memory
                System.out.println("No carried tiles, no tiles in memory");
                return new TWThought(TWAction.MOVE, getStepDirection());                
            }
            //tile seen in memory
            else{
                if(this.sameLocation(tile)){
                    this.target = tile;
                    System.out.println("no carried tiles- tile at same location- pickup");
                    return new TWThought(TWAction.PICKUP, null);
                }
                else{
                 
                 path = planner.findPath(x, y, tile.getX(), tile.getY());
                 System.out.println("no carried tiles- tile seen in memory");
                 return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                }
            }  
        }else if(this.carriedTiles.size() < 3){
            if(tile==null){
                if(hole!=null){
                    if(this.sameLocation(hole)){
                        this.target = hole;
                        System.out.println("carried tiles < 3 - tile=null, hole!=null");
                        return new TWThought(TWAction.PUTDOWN, null);
                    }
                   else{
                        path = planner.findPath(x, y, hole.getX(), hole.getY());
                        System.out.println("carried tiles < 3 - tile=null, hole=null");
                    }   
                }
                else{
                    System.out.println("carried tiles < 3 -- no tile or hole found");
                    return new TWThought(TWAction.MOVE, this.getStepDirection());
                }
            }
            else{
                if(this.sameLocation(tile)){
                    this.target = tile;
                    System.out.println("carried tiles < 3 - tile at same location- pickup");
                    return new TWThought(TWAction.PICKUP, null);
                }
                else{
                    if(hole!=null){
                        if(this.sameLocation(hole)){
                            this.target = hole;
                            System.out.println("carried tiles < 3 - hole at same location- putdown");
                            return new TWThought(TWAction.PUTDOWN, null);
                        }
                        else{
                            TWPath pathToHole = planner.findPath(x, y, hole.getX(), hole.getY());
                            TWPath pathToTile = planner.findPath(x, y, tile.getX(), tile.getY());
                            if(pathToHole.getpath().size() < pathToTile.getpath().size()){
                                System.out.println("carried tiles < 3 - Found tile and hole- going for the hole");
                                this.target = hole;
                                path = pathToHole;
                            }
                            else{
                                System.out.println("carried tiles < 3 - Found tile and hole- going for the tile");
                                this.target = tile;
                                path = pathToTile;
                            } 
                        }
                    }
                    else{
                        
                        path = planner.findPath(x, y, tile.getX(), tile.getY());
                        System.out.println("carried tiles < 3 - tile found, no hole");
                        return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                    } 
                }
            } 
        }
        else{
            if(hole!=null){
                if(this.sameLocation(hole)){
                    System.out.println("carried tiles = 3 --- hole in same location");
                    this.target = hole;
                    return new TWThought(TWAction.PUTDOWN, null);
                }
                else{
                    path = planner.findPath(x, y, hole.getX(), hole.getY());
                    System.out.println("carried tiles = 3 --- hole seen");
                }
            }
            else
                return new TWThought(TWAction.MOVE, getStepDirection());
        }

        return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
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
                memory.getMemoryGrid().set(this.target.getX() , this.target.getY(), null);
                break;
            
            case PUTDOWN:
                putTileInHole((TWHole)this.target);
                memory.removeAgentPercept(this.target.getX(), this.target.getY());
                memory.getMemoryGrid().set(this.target.getX() , this.target.getY(), null);
                break;
                
            case REFUEL:
                refuel();
                break;
                
        }
        
        this.sendMessage();

        
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


    @Override
    public String getName() {
        return this.agentName;
    }
}
