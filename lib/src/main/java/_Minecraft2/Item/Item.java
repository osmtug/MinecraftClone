package _Minecraft2.Item;

public class Item {
    private final String name;
    private final boolean stackable;
    private final int maxStackSize;

    public Item(String name, boolean stackable, int maxStackSize) {
        this.name = name;
        this.stackable = stackable;
        this.maxStackSize = maxStackSize;
    }

    public String getName() { return name; }
    public boolean isStackable() { return stackable; }
    public int getMaxStackSize() { return maxStackSize; }
}
