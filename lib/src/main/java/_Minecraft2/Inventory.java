package _Minecraft2;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import _Minecraft2.Item.Item;
import _Minecraft2.Item.ItemStack;
import _Minecraft2.UI.TextureRegistry;
import _Minecraft2.util.FontRenderer;

public class Inventory {
	private final List<ItemStack> slots;
	private ItemStack takenItemStack;
    private final int size;
	private long window;
	public boolean isOpen = false;
	
	private Set<Integer> visitedSlotIndex = new HashSet<Integer>();
	
	private boolean isLeftCLickPressed = false;
	private boolean isRightCLickPressed = false;
	private int totalItemTaken = 0;
	private Item itemTaken = null;
	
	private long lastClickTime = 0;
	private int lastClickedSlot = -1;
	private static final long DOUBLE_CLICK_DELAY = 300;

    public Inventory(int size, long window) {
        this.size = size;
        this.window = window;
        this.slots = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            slots.add(null);
        }
        
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW_PRESS && isOpen) {
                    DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xpos, ypos);
                    
                    double mouseX = xpos.get(0);
                    double mouseY = ypos.get(0);
                    
                    handleMouseClick(mouseX, mouseY, button);
                }else if(action == GLFW_RELEASE) {
                	handleMouseRelease();
                }
            }
        });
    }
    
    public List<ItemStack> getHotbar(){
    	return slots.subList(Math.max(slots.size() - 9, 0), slots.size());
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

        for (int i = 0; i < size; i++) {
            if (slots.get(i) == null) {
                int toAdd = Math.min(item.getMaxStackSize(), quantity);
                slots.set(i, new ItemStack(item, toAdd));
                quantity -= toAdd;
                if (quantity <= 0) return true;
            }
        }

        return false; 
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
    	this.window = window;
    	updateMouseDrag();
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
        
        float scale = 0.6f; 
    	float itemSize = slotSize * scale;

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

                    	float itemX = slotX + (slotSize - itemSize) / 2f;
                    	float itemY = slotY + (slotSize - itemSize) / 2f;
                    	
                    	stack.getItem().render(itemX, itemY, itemSize, windowWidth, windowHeight);
                    	
                        if (stack.getQuantity() > 1) {
                            String qtyText = Integer.toString(stack.getQuantity());
                            FontRenderer.drawTextInPixelsRightAligned(qtyText, itemX + itemSize *1.2f , itemY + itemSize*1.2f, new Vector3f(1f, 1f, 1f), windowWidth, windowHeight, itemSize / 45);
                        }
                    }
                }

                slotIndex++;
            }
        }
        renderTakenItemStack(itemSize, windowWidth, windowHeight, rectHeight);
    }
    
    public void renderTakenItemStack(float itemSize, float windowWidth, float windowHeight, float rectHeight) {
    	ItemStack stack = takenItemStack;
        if (stack != null && stack.getItem() != null) {

            DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);

            GLFW.glfwGetCursorPos(window, xpos, ypos);
            float itemX = (float) xpos.get(0) - itemSize/2;
            float itemY = (float) ypos.get(0) - itemSize/2;
        	
        	stack.getItem().render(itemX, itemY, itemSize, windowWidth, windowHeight, 50f);
        	
            if (stack.getQuantity() > 1) {
            	int factor = 3;
            	if (stack.getQuantity() >= 10) factor = 2;
                String qtyText = Integer.toString(stack.getQuantity());
                FontRenderer.drawTextInPixels(qtyText, itemX + (itemSize/4)*factor , itemY + itemSize*1.2f, new Vector3f(1f, 1f, 1f), windowWidth, windowHeight, rectHeight / 800);
            }
        }
    }
    
    public void handleMouseClick(double mouseX, double mouseY, int button) {
        int clickedSlot = getSlotAtPosition(mouseX, mouseY);
        
        if (clickedSlot != -1) {
        	if (button == 0) {
        		long currentTime = System.currentTimeMillis();
        		
        		if (clickedSlot == lastClickedSlot && 
        		    (currentTime - lastClickTime) <= DOUBLE_CLICK_DELAY) { 
        			
        			gatherSimilarItems(clickedSlot);
        		} else {
        			handleSlotClick(clickedSlot, true);
        		}
        		
        		lastClickTime = currentTime;
        		lastClickedSlot = clickedSlot;
        	} else if (button == 1) {
        		handleSlotClick(clickedSlot, false);
        	}
        }
    }
    
    public void handleMouseRelease() {
    	if (takenItemStack != null && takenItemStack.isEmpty()) takenItemStack = null;
    	isLeftCLickPressed = false;
    	isRightCLickPressed = false;
    	visitedSlotIndex.clear();
    }

    private int getSlotAtPosition(double mouseX, double mouseY) {
        IntBuffer widthBuf = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuf = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, widthBuf, heightBuf);
        int winWidth = widthBuf.get(0);
        int winHeight = heightBuf.get(0);

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

        // calcul des paramètres des slots (même logique que dans renderSlots)
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

                if (mouseX >= slotX && mouseX <= slotX + slotSize &&
                    mouseY >= slotY && mouseY <= slotY + slotSize) {
                    return slotIndex;
                }

                slotIndex++;
            }
        }

        return -1; 
    }
    
    private void handleSlotClick(int slotIndex, boolean isLeftClick) {
        if (slotIndex < 0 || slotIndex >= size) return;

        ItemStack clickedStack = slots.get(slotIndex);

        if (takenItemStack == null) {
            if (clickedStack != null) {
            	if (isLeftClick) {
            		takenItemStack = clickedStack;
                    slots.set(slotIndex, null);
            	}else {
                    int tmp = clickedStack.quantity / 2;
                    takenItemStack = new ItemStack(clickedStack.getItem(), clickedStack.quantity - tmp);
                    if (tmp == 0) {
                    	slots.set(slotIndex, null);
                    }else {
                    	clickedStack.quantity = tmp;
                    }
                    
            	}
            }
        } else {
            if (clickedStack == null) {
                this.isLeftCLickPressed = isLeftClick;
                this.isRightCLickPressed = !isLeftClick;
                totalItemTaken = takenItemStack.getQuantity();
                itemTaken = takenItemStack.getItem();
            } else {
                if (clickedStack.getItem().getName().equals(takenItemStack.getItem().getName()) && 
                    clickedStack.getItem().isStackable()) {
                    int spaceLeft = clickedStack.getItem().getMaxStackSize() - clickedStack.getQuantity();
                    int toAdd = Math.min(spaceLeft, takenItemStack.getQuantity());
                    
                    if (toAdd > 0) {
                        clickedStack.add(toAdd);
                        takenItemStack.remove(toAdd);
                        
                        if (takenItemStack.isEmpty()) {
                            takenItemStack = null;
                        }
                    } else{
                        ItemStack temp = clickedStack;
                        slots.set(slotIndex, takenItemStack);
                        takenItemStack = temp;
                    }
                } else {
                    ItemStack temp = clickedStack;
                    slots.set(slotIndex, takenItemStack);
                    takenItemStack = temp;
                }
            }
        }
    }
    
    private void updateMouseDrag() {
    	if (isLeftCLickPressed == isRightCLickPressed) return;
    	DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(window, xpos, ypos);
        
        double mouseX = xpos.get(0);
        double mouseY = ypos.get(0);
        
        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex < 0 || slotIndex >= size || visitedSlotIndex.contains(slotIndex)) return;
        
        
        if(this.isLeftCLickPressed) {
        	if (slots.get(slotIndex) != null) {
        		return;
        	}
        	visitedSlotIndex.add(slotIndex);
        	int totalSlots = visitedSlotIndex.size();
        	int value = this.totalItemTaken / totalSlots;
        	if (value == 0) value = 1;
        	int reste = totalItemTaken;
        	for (Integer index : visitedSlotIndex) {
        		if (slots.get(index) == null) {
        			slots.set(index, new ItemStack(itemTaken, value));
        		}else {
        			slots.get(index).quantity = value;        			
        		}
        	    reste -= value;
        	}
        	if (value == 1 && reste == 0) {
        		takenItemStack = null;
        		handleMouseRelease();
        	}else {
        		takenItemStack.quantity = reste;
        	}
        }else {
        	visitedSlotIndex.add(slotIndex);
        	if (slots.get(slotIndex) == null) {
        		slots.set(slotIndex, new ItemStack(itemTaken, 1));        		
        	}else if(itemTaken.getName().equals(slots.get(slotIndex).getItem().getName()) && slots.get(slotIndex).canAdd(1)) {
        		slots.get(slotIndex).add(1);
        	}else {
        		return;
        	}
        	takenItemStack.quantity--;
        	if(takenItemStack.quantity == 0) {
        		takenItemStack = null;
        		handleMouseRelease();
        	}
        }
    }

    private void gatherSimilarItems(int targetSlotIndex) {
        if (targetSlotIndex < 0 || targetSlotIndex >= size) return;
        Item targetItem;
        ItemStack targetStack = slots.get(targetSlotIndex);
        if (targetStack != null) {
        	targetItem = targetStack.getItem();
        }else if (takenItemStack != null) {
        	targetItem = takenItemStack.getItem();
        }else {
        	return;
        }
        
        
        if (!targetItem.isStackable()) {
            handleSlotClick(targetSlotIndex, true);
            return;
        }
        
        int maxStackSize = targetItem.getMaxStackSize();
        int totalGathered = 0;
        
        for (int i = 0; i < size; i++) {
            ItemStack stack = slots.get(i);
            if (stack != null && stack.getItem().getName().equals(targetItem.getName())) {
                totalGathered += stack.getQuantity();
            }
        }
        if (takenItemStack != null && takenItemStack.getItem().getName().equals(targetItem.getName())) {
            totalGathered += takenItemStack.getQuantity();
        }

        int quantityToTake = Math.min(totalGathered, maxStackSize);
        
        int remaining = quantityToTake;
        
        if (takenItemStack != null) {
        	remaining -= takenItemStack.getQuantity();
        }
        
        takenItemStack = new ItemStack(targetItem, quantityToTake);
        
        ItemStack clickedStack = slots.get(targetSlotIndex);
        if (clickedStack != null && clickedStack.getItem().getName().equals(targetItem.getName())) {
            int toRemove = Math.min(remaining, clickedStack.getQuantity());
            clickedStack.remove(toRemove);
            remaining -= toRemove;
            
            if (clickedStack.isEmpty()) {
                slots.set(targetSlotIndex, null);
            }
        }
        
        for (int i = 0; i < size && remaining > 0; i++) {
            if (i == targetSlotIndex) continue; 
            
            ItemStack stack = slots.get(i);
            if (stack != null && stack.getItem().getName().equals(targetItem.getName())) {
                int toRemove = Math.min(remaining, stack.getQuantity());
                stack.remove(toRemove);
                remaining -= toRemove;
                
                if (stack.isEmpty()) {
                    slots.set(i, null);
                }
            }
        }
        
        if (takenItemStack.getQuantity() == 0) {
            takenItemStack = null;
        }
    }

}
