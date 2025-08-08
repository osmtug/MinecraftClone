package _Minecraft2.world.block;

import _Minecraft2.Item.BlockItem;
import _Minecraft2.Item.ItemRegistry;

public class BlockType {
    public final String name;
    public final BlockProperties properties;
    public final BlockItem item;

    public BlockType(String name,BlockProperties properties) {
        this.name = name;
        this.properties = properties;
        this.item = new BlockItem(name, true, 64);
        ItemRegistry.register(this.item);
    }

    public Block createBlock(int x, int y, int z) {
        return new GenericBlock(properties, x, y, z);
    }
}