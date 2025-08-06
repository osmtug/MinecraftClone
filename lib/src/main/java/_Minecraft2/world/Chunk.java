package _Minecraft2.world;


import static org.lwjgl.glfw.GLFW.*;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import _Minecraft2.entity.Entity;
import _Minecraft2.render.Camera;
import _Minecraft2.render.Frustum;
import _Minecraft2.world.block.Block;
import _Minecraft2.world.block.BlockRegistry;
import _Minecraft2.world.block.BlockType;

public class Chunk {

    public static final int SIZE = 16;

    private final Block[][][] blocks = new Block[SIZE][SIZE][SIZE];
    public final int chunkX, chunkY, chunkZ;
    
    private final Set<Entity> entities = new HashSet<>();

    public void addEntity(Entity e) { entities.add(e); }
    public void removeEntity(Entity e) { entities.remove(e); }
    public Set<Entity> getEntities() { return entities; }

    public Chunk(int chunkX, int chunkY, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;

        // Initialisation à de l'air
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                	BlockType type = BlockRegistry.getByName("air");
                    Block air = type.createBlock(getWorldX(x), getWorldY(y), getWorldZ(z));
                    blocks[x][y][z] = air;
                }
            }
        }
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (inBounds(x, y, z)) {
            blocks[x][y][z] = block;
        }
    }

    public Block getBlock(int x, int y, int z) {
        if (inBounds(x, y, z)) {
            return blocks[x][y][z];
        }
        BlockType type = BlockRegistry.getByName("air");
        Block air = type.createBlock(getWorldX(x), getWorldY(y), getWorldZ(z));
        return air;
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE;
    }

    private int getWorldX(int x) {
        return chunkX * SIZE + x;
    }

    private int getWorldY(int y) {
        return chunkY * SIZE + y;
    }

    private int getWorldZ(int z) {
        return chunkZ * SIZE + z;
    }

    public void render(World world, Frustum frustum, Set<Entity> renderedEntities) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    Block block = blocks[x][y][z];
                    if (block == null || !block.isSolid()) continue;

                    int wx = getWorldX(x);
                    int wy = getWorldY(y);
                    int wz = getWorldZ(z);

                    if (!frustum.isBoxInFrustum(wx * 2f, wy * 2f, wz * 2f, 1.0f)) continue;

                    block.startRender();

                    if (world.isAirOrTransparent(wx + 1, wy, wz)) block.renderRight();
                    if (world.isAirOrTransparent(wx - 1, wy, wz)) block.renderLeft();
                    if (world.isAirOrTransparent(wx, wy + 1, wz)) block.renderUp();
                    if (world.isAirOrTransparent(wx, wy - 1, wz)) block.renderDown();
                    if (world.isAirOrTransparent(wx, wy, wz + 1)) block.renderFront();
                    if (world.isAirOrTransparent(wx, wy, wz - 1)) block.renderBack();

                    block.stopRender();
                }
            }
        }

        for (Entity entity : getEntities()) {
            if (!renderedEntities.contains(entity)) {
                entity.render();
                renderedEntities.add(entity); 
            }
        }
    }

    public float[] getBoundingBox() {
        float minX = chunkX * SIZE * 2f;
        float minY = chunkY * SIZE * 2f;
        float minZ = chunkZ * SIZE * 2f;
        float maxX = minX + SIZE * 2f;
        float maxY = minY + SIZE * 2f;
        float maxZ = minZ + SIZE * 2f;
        return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
    }
}