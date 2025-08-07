package _Minecraft2;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.*;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import _Minecraft2.Item.Item;
import _Minecraft2.Item.ItemStack;
import _Minecraft2.UI.TextureRegistry;
import _Minecraft2.util.FontRenderer;

public class Inventory {
	private final List<ItemStack> slots;
    private final int size;

    public Inventory(int size) {
        this.size = size;
        this.slots = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            slots.add(null);
        }
    }

    public boolean addItem(Item item, int quantity) {
        if (item.isStackable()) {
            for (int i = 0; i < size; i++) {
                ItemStack stack = slots.get(i);
                if (stack != null && stack.getItem().getName().equals(item.getName()) && stack.getQuantity() < item.getMaxStackSize()) {
                    int spaceLeft = item.getMaxStackSize() - stack.getQuantity();
                    int toAdd = Math.min(spaceLeft, quantity);
                    stack.add(toAdd);
                    quantity -= toAdd;
                    if (quantity <= 0) return true;
                }
            }
        }

        // Ajoute dans des slots vides
        for (int i = 0; i < size; i++) {
            if (slots.get(i) == null) {
                int toAdd = Math.min(item.getMaxStackSize(), quantity);
                slots.set(i, new ItemStack(item, toAdd));
                quantity -= toAdd;
                if (quantity <= 0) return true;
            }
        }

        return false; // Pas assez de place
    }

    public ItemStack getSlot(int index) {
        if (index < 0 || index >= size) return null;
        return slots.get(index);
    }

    public void removeItem(int index, int quantity) {
        if (index < 0 || index >= size) return;
        ItemStack stack = slots.get(index);
        if (stack == null) return;

        stack.remove(quantity);
        if (stack.isEmpty()) {
            slots.set(index, null);
        }
    }

    public int getSize() {
        return size;
    }

    public void printInventory() {
        for (int i = 0; i < size; i++) {
            ItemStack stack = slots.get(i);
            if (stack != null) {
                System.out.println("Slot " + i + ": " + stack.getItem().getName() + " x" + stack.getQuantity());
            } else {
                System.out.println("Slot " + i + ": empty");
            }
        }
    }
    
    public void render(long window) {
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

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        float rectRatio = 11.0f / 10.0f;
        float rectWidth, rectHeight;
        float winRatio = (float) winWidth / winHeight;

        if (winRatio > rectRatio) {
            rectHeight = winHeight * 0.8f;
            rectWidth = rectHeight * rectRatio;
        } else {
            rectWidth = winWidth * 0.8f;
            rectHeight = rectWidth / rectRatio;
        }

        float x = (winWidth - rectWidth) / 2.0f;
        float y = (winHeight - rectHeight) / 2.0f;

        renderBackground(x, y, rectWidth, rectHeight);
        
        renderSlots(x, y, rectWidth, rectHeight, winWidth, winHeight);

        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }
    
    public void renderBackground(float x, float y, float rectWidth, float rectHeight) {
    	Integer textureId = TextureRegistry.getByName("inventoryBackground");

        glBindTexture(GL_TEXTURE_2D, textureId);

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f); glVertex2f(x, y);
        glTexCoord2f(1.0f, 0.0f); glVertex2f(x + rectWidth, y);
        glTexCoord2f(1.0f, 1.0f); glVertex2f(x + rectWidth, y + rectHeight);
        glTexCoord2f(0.0f, 1.0f); glVertex2f(x, y + rectHeight);
        glEnd();
    }
    
    public void renderSlots(float x, float y, float rectWidth, float rectHeight, float windowWidth, float windowHeight) {
        Integer slotTextureId = TextureRegistry.getByName("slot");

        if (slotTextureId != null) {
            glBindTexture(GL_TEXTURE_2D, slotTextureId);

            int cols = 9;
            int rows = 4;

            float marginX = rectWidth * 0.05f;           
            float availableWidth = rectWidth - 2 * marginX;

            float marginBetweenSlots = rectWidth * 0.02f;              
            float totalMarginX = marginBetweenSlots * (cols - 1);
            float slotSize = (availableWidth - totalMarginX) / cols;

            float marginBetweenRows = rectWidth * 0.01f;
            float extraMarginBeforeHotbar = rectWidth * 0.07f;  
            float bottomMargin = rectWidth * 0.05f;

            float totalMarginY = marginBetweenRows * (rows - 2) + extraMarginBeforeHotbar;

            float totalSlotsHeight = slotSize * rows + totalMarginY;

            float startX = x + marginX;
            float startY = y + rectHeight - totalSlotsHeight - bottomMargin;

            glColor4f(1f, 1f, 1f, 1f);

            int slotIndex = 0;

            for (int row = 0; row < rows; row++) {
                float yOffset;

                if (row == rows - 1) {
                    yOffset = slotSize * row + marginBetweenRows * (row - 1) + extraMarginBeforeHotbar;
                } else {
                    yOffset = slotSize * row + marginBetweenRows * row;
                }

                for (int col = 0; col < cols; col++) {
                    float slotX = startX + col * (slotSize + marginBetweenSlots);
                    float slotY = startY + yOffset;

                    glBindTexture(GL_TEXTURE_2D, slotTextureId);
                    glBegin(GL_QUADS);
                    glTexCoord2f(0f, 0f); glVertex2f(slotX, slotY);
                    glTexCoord2f(1f, 0f); glVertex2f(slotX + slotSize, slotY);
                    glTexCoord2f(1f, 1f); glVertex2f(slotX + slotSize, slotY + slotSize);
                    glTexCoord2f(0f, 1f); glVertex2f(slotX, slotY + slotSize);
                    glEnd();

                    if (slots != null && slotIndex < slots.size() && slots.get(slotIndex) != null) {
                        ItemStack stack = slots.get(slotIndex);
                        if (stack.getItem() != null) {
                        	float scale = 0.6f; 
                        	float itemSize = slotSize * scale;

                        	float itemX = slotX + (slotSize - itemSize) / 2f;
                        	float itemY = slotY + (slotSize - itemSize) / 2f;
                        	
                        	stack.getItem().render(itemX, itemY, itemSize, windowWidth, windowHeight);
                        	
                            if (stack.getQuantity() > 1) {
                            	int factor = 3;
                            	if (stack.getQuantity() >= 10) factor = 2;
                                String qtyText = Integer.toString(stack.getQuantity());
                                FontRenderer.drawTextInPixels(qtyText, slotX + (slotSize / 4) * factor, slotY + slotSize, new Vector3f(1f, 1f, 1f), windowWidth, windowHeight, rectHeight / 700);
                            }
                        }
                    }

                    slotIndex++;
                }
            }
        }
    }



}
