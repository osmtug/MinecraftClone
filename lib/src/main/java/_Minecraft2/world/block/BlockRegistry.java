package _Minecraft2.world.block;

import java.util.HashMap;
import java.util.Map;

import _Minecraft2.Item.Item;

public class BlockRegistry {
	private static final Map<String, BlockType> byName = new HashMap<>();
    private static final Map<Integer, BlockType> byId = new HashMap<>();

    public static BlockType register(String name, int id, BlockProperties properties) {
        BlockType type = new BlockType(name, properties);
        byName.put(name, type);
        byId.put(id, type);
        properties.type = type;
        return type;
    }

    public static BlockType getByName(String name) {
        return byName.get(name);
    }

    public static BlockType getById(int id) {
        return byId.get(id);
    }

    public static Item getItemForBlock(Block block) {
        for (BlockType type : byName.values()) {
            if (type.properties == block.getProperties()) return type.item;
        }
        return null;
    }
}
