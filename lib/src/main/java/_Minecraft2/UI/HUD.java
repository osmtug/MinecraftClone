package _Minecraft2.UI;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import _Minecraft2.Item.ItemStack;
import _Minecraft2.entity.Player;
import _Minecraft2.util.FontRenderer;

public class HUD {
	
	private long window;
	
	public HUD(long window) {
		this.window = window;
	}

    public void render(Player player) {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        
        IntBuffer widthBuf = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuf = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, widthBuf, heightBuf);
        int winWidth = widthBuf.get(0);
        int winHeight = heightBuf.get(0);
        
        
        glOrtho(0, winWidth, winHeight, 0, -1, 1); 
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        
        
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        drawHearts(0);

        drawHotbar(player, winWidth, winHeight);
        
        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private void drawHearts(int health) {
    	// TODO
    }

    private void drawHotbar(Player player, int winWidth, int winHeight) {
        final float SLOT_MARGIN_RATIO = 0.005f; 
        final int NUM_SLOTS = 9;
        
        float rectWidth, rectHeight;
        
        float winRatio = (float) winWidth / winHeight;
        float rectRatio = 11.0f / 10.0f;
        
		if (winRatio > rectRatio) {
            rectHeight = winHeight * 0.8f;
            rectWidth = rectHeight * rectRatio;
        } else {
            rectWidth = winWidth * 0.8f;
            rectHeight = rectWidth / rectRatio;
        }
		
		int cols = 9;

        float marginX = rectWidth * 0.05f;           
        float availableWidth = rectWidth - 2 * marginX;

        float marginBetweenSlots = rectWidth * 0.02f;              
        float totalMarginX = marginBetweenSlots * (cols - 1);

		float slotSize = (availableWidth - totalMarginX) / cols;
        int slotMargin = (int) (winHeight * SLOT_MARGIN_RATIO);

        float totalWidth = (NUM_SLOTS * slotSize) + ((NUM_SLOTS - 1) * slotMargin);
        float startX = (winWidth - totalWidth) / 2;
        float startY = winHeight - slotSize - (int)(winHeight * 0.015f); 

        int slotTexId = TextureRegistry.getByName("hotbarSlot");
        int selectedSlotTexId = TextureRegistry.getByName("hotbarSelectedSlot");

        int selectedSlot = player.getSelectedHotbarSlot();
        
        List<ItemStack> hotbarItemStack = player.getInventory().getHotbar();
        
        float scale = 0.6f; 
    	float itemSize = slotSize * scale;

        for (int i = 0; i < NUM_SLOTS; i++) {
        	
        	glDisable(GL_DEPTH_TEST);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            
        	float x = startX + i * (slotSize + slotMargin);
        	float y = startY;

        	drawTexture(slotTexId, x, y, slotSize, slotSize);
            if (i == selectedSlot) {
                drawTexture(selectedSlotTexId, x, y, slotSize, slotSize);
            }

            ItemStack stack = hotbarItemStack.get(i);
            if (stack != null && stack.getItem() != null) {

            	float itemX = x + (slotSize - itemSize) / 2f;
            	float itemY = y + (slotSize - itemSize) / 2f;
            	
            	stack.getItem().render(itemX, itemY, itemSize, winWidth, winHeight);
            	
            	if (stack.getQuantity() > 1) {
                    String qtyText = Integer.toString(stack.getQuantity());
                    FontRenderer.drawTextInPixelsRightAligned(qtyText, itemX + itemSize *1.2f , itemY + itemSize*1.2f, new Vector3f(1f, 1f, 1f), winWidth, winHeight, itemSize / 45);
                }
            }
        }
    }
    
    private void drawTexture(int textureId, float x, float y, float width, float height) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glBegin(GL_QUADS);
            glTexCoord2f(0, 0); glVertex2f(x, y);
            glTexCoord2f(1, 0); glVertex2f(x + width, y);
            glTexCoord2f(1, 1); glVertex2f(x + width, y + height);
            glTexCoord2f(0, 1); glVertex2f(x, y + height);
        glEnd();

        glDisable(GL_TEXTURE_2D);
    }
}