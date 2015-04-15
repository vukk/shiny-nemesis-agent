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
    
    public static void put(String fromAgent, EVMessage m){
        char c = fromAgent.charAt(0);
        switch(c){
            case 'F': MessageFromFirst = m;
                break;
            case 'S': MessageFromSecond = m;
                break;
        }
    }
    
    public static Object get(String toAgent){
        char c = toAgent.charAt(0);
        switch(c){
            case 'F': return MessageFromSecond;
            case 'S': return MessageFromFirst;
        }
        return null;
    } 
}
