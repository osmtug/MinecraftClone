package _Minecraft2;

import _Minecraft2.UI.TextureRegistry;
import _Minecraft2.world.block.BlockProperties;
import _Minecraft2.world.block.BlockRegistry;

public class GameInit {
	public static void registerBlocks() {
        BlockProperties grassProps = new BlockProperties(
        	"grass_top.png", "dirt.png", "grass_side.png"
        );
        BlockRegistry.register("grass", 1, grassProps);
        
        BlockProperties dirtProps = new BlockProperties(
            	"dirt.png"
            );
            BlockRegistry.register("dirt", 1, dirtProps);
        
        BlockProperties airProps = new BlockProperties(
            	null, false
            );
            BlockRegistry.register("air", 2, airProps);
	}
	
	public static void registerTexture() {
		TextureRegistry.register("inventoryBackground", "UI/inventory/inventoryBackground.png");
		TextureRegistry.register("slot", "UI/inventory/slot.png");
		TextureRegistry.register("hotbarSlot", "UI/HUD/slot.png");
		TextureRegistry.register("hotbarSelectedSlot", "UI/HUD/sELECTEDslot.png");
	}
}
