package _Minecraft2.Item;

import _Minecraft2.world.block.BlockRegistry;

public class BlockItem extends Item {

	public BlockItem(String name, boolean stackable, int maxStackSize) {
		super(name, stackable, maxStackSize);
	}
	
	public int getUpTexture(){
		return BlockRegistry.getByName(getName()).properties.upTextureID;
	}
	
	public int getDownTexture(){
		return BlockRegistry.getByName(getName()).properties.downTextureID;
	}
	
	public int getFrontTexture(){
		return BlockRegistry.getByName(getName()).properties.frontTextureID;
	}
	
	public int getRightTexture(){
		return BlockRegistry.getByName(getName()).properties.rightTextureID;
	}
	
	public int getBackTexture(){
		return BlockRegistry.getByName(getName()).properties.backTextureID;
	}
	
	public int getLeftTexture(){
		return BlockRegistry.getByName(getName()).properties.leftTextureID;
	}

}
