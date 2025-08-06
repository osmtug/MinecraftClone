package _Minecraft2.world.block;

public class GenericBlock extends Block {

	private final BlockProperties properties;

    public GenericBlock(BlockProperties properties, int x, int y, int z) {
        super(x, y, z);
        this.properties = properties;
    }

    @Override
    public BlockProperties getProperties() {
        return properties;
    }

    @Override
    public Block copy(int x, int y, int z) {
        return new GenericBlock(properties, x, y, z);
    }

}
