package tileworld;

/**
 * Parameters
 *
 * @author michaellees
 *
 * Created: Apr 21, 2010
 *
 * Copyright michaellees
 *
 * Description:
 *
 * Class used to store global simulation parameters. Environment related
 * parameters are still in the TWEnvironment class.
 *
 */
public class Parameters {

    //Simulation Parameters
    public final static int seed = 40462015; //no effect with gui
    public static final long endTime = 5000; //no effect with gui

    //Object Parameters
    // e1
    /*
    //Agent Parameters
    public static final int defaultFuelLevel = 1000;
    public static final int defaultSensorRange = 3;
    
    //Environment Parameters
    public static final int xDimension = 100; //size in cells
    public static final int yDimension = 100;
    
    public static final double tileMean = 0.2;
    public static final double holeMean = 0.2;
    public static final double obstacleMean = 0.2;
    public static final double tileDev = 0.05f;
    public static final double holeDev = 0.05f;
    public static final double obstacleDev = 0.05f;
    public static final int lifeTime = 100;
    */
    // e2
    /*
    //Agent Parameters
    public static final int defaultFuelLevel = 1000;
    public static final int defaultSensorRange = 3;
    
    //Environment Parameters
    public static final int xDimension = 50; //size in cells
    public static final int yDimension = 50;
    
    public static final double tileMean = 2.0;
    public static final double holeMean = 2.0;
    public static final double obstacleMean = 2.0;
    public static final double tileDev = 0.5f;
    public static final double holeDev = 0.5f;
    public static final double obstacleDev = 0.5f;
    public static final int lifeTime = 30;
    */
    // e3
    //Agent Parameters
    public static final int defaultFuelLevel = 1200;
    public static final int defaultSensorRange = 3;
    
    //Environment Parameters
    public static final int xDimension = 300; //size in cells
    public static final int yDimension = 25;
    
    public static final double tileMean = 0.8;
    public static final double holeMean = 0.2;
    public static final double obstacleMean = 4.0;
    public static final double tileDev = 0.01f;
    public static final double holeDev = 0.002f;
    public static final double obstacleDev = 0.2f;
    public static final int lifeTime = 200;
}
