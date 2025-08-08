package _Minecraft2.Item;

import static org.lwjgl.opengl.GL11.*;

import _Minecraft2.GameSettings;
import _Minecraft2.render.TextureLoader;  

public class Item {
    private final String name;
    private final boolean stackable;
    private final int maxStackSize;
	private int textureId;

    public Item(String name, boolean stackable, int maxStackSize, String TexturePath) {
        this.name = name;
        this.stackable = stackable;
        this.maxStackSize = maxStackSize;
        this.textureId = 0;
        if(TexturePath != null)
        	this.textureId = TextureLoader.loadTexture(GameSettings.texturePath + TexturePath);
    }

    public String getName() { return name; }
    public boolean isStackable() { return stackable; }
    public int getMaxStackSize() { return maxStackSize; }
    
 // Cette méthode dessine l'item à la position (x, y) avec une taille donnée
    public void render(float x, float y, float size, float windowWidth, float windowHeight) {

        glBindTexture(GL_TEXTURE_2D, textureId);

        glBegin(GL_QUADS);
        glTexCoord2f(0f, 0f); glVertex2f(x, y);
        glTexCoord2f(1f, 0f); glVertex2f(x + size, y);
        glTexCoord2f(1f, 1f); glVertex2f(x + size, y + size);
        glTexCoord2f(0f, 1f); glVertex2f(x, y + size);
        glEnd();
    }
    
    public void render(float x, float y, float size, float windowWidth, float windowHeight, float dz) {
    	render(x, y, size, windowWidth, windowHeight);
    }
}
