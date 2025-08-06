package _Minecraft2.Item;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
	private static final Map<String, Item> itemsByName = new HashMap<>();

    public static void register(Item item) {
        itemsByName.put(item.getName().toLowerCase(), item);
    }

    public static Item getByName(String name) {
        if (name == null) return null;
        return itemsByName.get(name.toLowerCase());
    }
}
