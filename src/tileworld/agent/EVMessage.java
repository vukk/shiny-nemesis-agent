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
    public int[] location;
    public int[] cleared;
    
    public EVMessage(int[] location, int[] cleared) { // location, request, response
        this.location = location;
        this.cleared = cleared;
    }
}
