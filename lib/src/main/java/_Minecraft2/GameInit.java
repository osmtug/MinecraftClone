package _Minecraft2;

import _Minecraft2.world.block.BlockProperties;
import _Minecraft2.world.block.BlockRegistry;

public class GameInit {
	public static void registerBlocks() {
        BlockProperties grassProps = new BlockProperties(
        	"grass_top.png", "dirt.png", "grass_side.png"
        );
        BlockRegistry.register("grass", 1, grassProps);
        
        BlockProperties airProps = new BlockProperties(
            	null, false
            );
            BlockRegistry.register("air", 2, airProps);
        
        
    }
}
