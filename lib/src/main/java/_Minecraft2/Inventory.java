package _Minecraft2;

import java.util.ArrayList;
import java.util.List;

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
}
