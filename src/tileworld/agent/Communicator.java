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
public class Communicator {
    
    private static Message MessageFromFirst;
    private static Message MessageFromSecond;
    
    public static void put(String fromAgent, Message m){
		char c = fromAgent.charAt(0);
		switch(c){
		case 'F': MessageFromFirst = m;
			break;
		case 'S': MessageFromSecond = m;
			break;
		}
	}
	public static Message get(String toAgent){
		char c = toAgent.charAt(0);
		switch(c){
		case 'F': return MessageFromSecond;
		case 'S': return MessageFromFirst;
		}
		return new Message();
	} 
}
