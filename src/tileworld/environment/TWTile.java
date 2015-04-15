/**
 *
 */
package tileworld.environment;

import java.awt.Color;
import sim.portrayal.Portrayal;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Int2D;

/**
 * TWTile
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
 * Tiles in Tileworld.
 */
public class TWTile extends TWObject {
    
    protected static Portrayal portrayal;
    protected static Portrayal memoryPortrayal;

    /**
     * @param creationTime
     * @param deathTime
     */
    public TWTile(int x, int y, TWEnvironment env, double creationTime, double deathTime) {
        super(x, y, env, creationTime, deathTime);
    }

    public TWTile(Int2D pos, TWEnvironment env, double creationTime, double deathTime) {
        super(pos, env, creationTime, deathTime);

    }

    public TWTile(Int2D pos, TWEnvironment env, Double creationTime, Double deathTime) {
        super(pos, env, creationTime, deathTime);

    }

    public TWTile() {
    }

    public static Portrayal getPortrayal() {
        // green filled box
        if (TWTile.portrayal == null)
            TWTile.portrayal = new RectanglePortrayal2D(new Color(0.0f, 1.0f, 0.0f), true);
        return TWTile.portrayal;
    }

    public static Portrayal getMemoryPortrayal() {
        if (TWTile.memoryPortrayal == null)
            TWTile.memoryPortrayal = new RectanglePortrayal2D(new Color(0.0f, 1.0f, 0.0f), false);
        return TWTile.memoryPortrayal;
    }
}
