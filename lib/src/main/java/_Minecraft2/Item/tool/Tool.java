package _Minecraft2.Item.tool;

import _Minecraft2.Item.Item;

public abstract class Tool extends Item {
	public Tool(int id, String name, int damage, ToolType type, String texturePath) {
		super(name, false, 1, texturePath);
		this.damage = damage;
		this.type = type;
	}

	int damage;
	ToolType type;
	
}
