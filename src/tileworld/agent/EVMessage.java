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
public class EVMessage {
    
    public int x;
    public int y;
    
    public boolean destroy;
    
    public long turn;
    
    public EVMessage(int xx, int yy, boolean dd, long tturn) { // x location, y location, destroyed memory
        x=xx;
        y=yy;
        destroy=dd;
        turn=tturn;
    }   
}
