package _Minecraft2.UI;

import java.util.HashMap;
import java.util.Map;

import _Minecraft2.GameSettings;
import _Minecraft2.render.TextureLoader;

public class TextureRegistry {
	private static final Map<String, Integer> byName = new HashMap<>();

    public static Integer register(String name, String texturePath) {
    	Integer type = TextureLoader.loadTexture(GameSettings.texturePath + texturePath);
        byName.put(name, type);
        return type;
    }

    public static Integer getByName(String name) {
        return byName.get(name);
    }
}
