package _Minecraft2.Item;

public class ItemStack {
	private final Item item;
    private int quantity;

    public ItemStack(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }

    public void add(int amount) {
        this.quantity = Math.min(quantity + amount, item.getMaxStackSize());
    }

    public void remove(int amount) {
        this.quantity = Math.max(quantity - amount, 0);
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    public boolean canStackWith(ItemStack other) {
        return item.getName().equals(other.getItem().getName()) && item.isStackable();
    }
}
