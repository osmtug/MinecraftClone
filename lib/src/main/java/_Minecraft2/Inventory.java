package _Minecraft2;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

import _Minecraft2.Item.Item;
import _Minecraft2.Item.ItemStack;

public class Inventory {
	private final List<ItemStack> slots;
    private final int size;

    public Inventory(int size) {
        this.size = size;
        this.slots = new ArrayList<>(size);

        // Initialise les slots vides
        for (int i = 0; i < size; i++) {
            slots.add(null);
        }
    }

    public boolean addItem(Item item, int quantity) {
        if (item.isStackable()) {
            // Essaye d’empiler sur un slot existant
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

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glColor4f(0.1f, 0.1f, 0.1f, 0.8f);

        // Ratio du rectangle (par exemple 3:2)
        float rectRatio = 3.0f / 2.0f;

        float rectWidth;
        float rectHeight;

        float winRatio = (float) winWidth / winHeight;

        if (winRatio > rectRatio) {
            // La fenêtre est plus large que le ratio → limiter par la hauteur
            rectHeight = winHeight * 0.8f;
            rectWidth = rectHeight * rectRatio;
        } else {
            // La fenêtre est plus haute que le ratio → limiter par la largeur
            rectWidth = winWidth * 0.8f;
            rectHeight = rectWidth / rectRatio;
        }

        // Centrage
        float x = (winWidth - rectWidth) / 2.0f;
        float y = (winHeight - rectHeight) / 2.0f;

        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + rectWidth, y);
        glVertex2f(x + rectWidth, y + rectHeight);
        glVertex2f(x, y + rectHeight);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }
}
