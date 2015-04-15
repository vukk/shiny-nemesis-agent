package tileworld.environment;

import java.awt.Color;
import sim.portrayal.Portrayal;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Int2D;

/**
 * TWHole
 *
 * @author michaellees
 *
 * Created: Apr 15, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 *
 * Holes in Tileworld
 *
 */
public class TWHole extends TWObject {
    
    protected static Portrayal portrayal;
    protected static Portrayal memoryPortrayal;
    
    /**
     * @param creationTime
     * @param deathTime
     */
    public TWHole(int x, int y, TWEnvironment env, double creationTime, double deathTime) {
        super(x, y, env, creationTime, deathTime);

    }

    public TWHole(Int2D pos, TWEnvironment env, Double creationTime, Double deathTime) {
        super(pos, env, creationTime, deathTime);

    }
    
    public TWHole() {
    }

    public static Portrayal getPortrayal() {
        //brown filled box.
        if (TWHole.portrayal == null)
            TWHole.portrayal = new RectanglePortrayal2D(new Color(188, 143, 143), true);
        return TWHole.portrayal;
    }

    public static Portrayal getMemoryPortrayal() {
        if (TWHole.memoryPortrayal == null)
            TWHole.memoryPortrayal = new RectanglePortrayal2D(new Color(188, 143, 143), false);
        return TWHole.memoryPortrayal;
    }
}
