/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

/**
 *
 * @author Vidur
 */
public class Message {
    
    
    private int sx;
    private int sy;
    
    private int tx;
    private int ty;
    
    private TWAgentPercept obj1;
    private TWAgentPercept obj2;
    private TWAgentPercept obj3;
    
    
    
    public Message(int x, int y, TWAgentPercept percept1, TWAgentPercept percept2){ // location, request, response
		this.sx = x;
		this.sy = y;
                this.obj1 = percept1;
                this.obj2 = percept2;
	}
    /*
    public Message(int x1, int y1, int x2, int y2, TWAgentPercept percept){ // location, request, response
		this.sx = x1;
		this.sy = y1;
                this.tx = x2;
		this.ty = y2;
                this.obj1=percept;
		
	}
    */
    public Message(TWAgentPercept percept1, TWAgentPercept percept2, TWAgentPercept percept3){
        
                this.obj1 = percept1;
                this.obj2 = percept2;
                this.obj3 = percept3;
                
                
    }
    
    public Message(){
        this.obj1=null;
        this.obj2=null;
        this.obj3=null;
        
        this.sx=-1;
        this.sy=-1;
        this.tx=-1;
        this.ty=-1;
        
    }
    
    public Object[] getContents(){
        Object[] contents = new Object[3];
            

        if(obj3!=null){
            contents[0] = obj1;
            contents[1] = obj2;
            contents[2] = obj3;
        }
        
        contents[0] = new int[]{sx, sy};
        contents[1] = obj1;
        contents[2] = obj2;
        
        
        return contents;
    }
            
        
}
