/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;
import java.util.*;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;

/**
 *
 * @author Vidur
 */
public class EVCommunicator {
    
    private static EVMessage MessageFromFirst;
    private static EVMessage MessageFromSecond;
    
    public static void put(String fromAgent, EVMessage m) {
        //System.out.println(fromAgent+" SendMsg loc: " + m.x + ", " + m.y + ", destroy: " + m.destroy);
        switch(fromAgent.charAt(0)) {
            case 'F': MessageFromFirst = m;
                break;
            case 'S': MessageFromSecond = m;
                break;
        }
    }
    
    public static EVMessage get(String toAgent) {
        EVMessage ret = null;
        switch(toAgent.charAt(0)) {
            case 'F':
                ret = MessageFromSecond;
                MessageFromSecond = null;
                break;
            case 'S':
                ret = MessageFromFirst;
                MessageFromFirst = null;
                break;
        }
        return ret;
    } 
}
