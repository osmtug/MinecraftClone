package _Minecraft2.world;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;


import static org.lwjgl.glfw.GLFW.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import _Minecraft2.Item.Item;
import _Minecraft2.collision.AABB;
import _Minecraft2.entity.Entity;
import _Minecraft2.entity.ItemEntity;
import _Minecraft2.entity.Player;
import _Minecraft2.render.Camera;
import _Minecraft2.render.Frustum;
import _Minecraft2.world.block.Block;
import _Minecraft2.world.block.BlockRegistry;
import _Minecraft2.world.block.BlockType;




public class World {
	
	public World(Player player){
		this.player = player;
	}

    private final Map<String, Chunk> chunks = new HashMap<>();
    
    public Player player;

    private String getChunkKey(int chunkX, int chunkY, int chunkZ) {
        return chunkX + "," + chunkY + "," + chunkZ;
    }

    public Chunk getChunk(int x, int y, int z) {
        int chunkX = (int) Math.floor((float) x / Chunk.SIZE);
        int chunkY = (int) Math.floor((float) y / Chunk.SIZE);
        int chunkZ = (int) Math.floor((float) z / Chunk.SIZE);
        
        String key = getChunkKey(chunkX, chunkY, chunkZ);
        Chunk chunk = chunks.get(key);
        return chunks.get(getChunkKey(chunkX, chunkY, chunkZ));
    }

    public Block getBlockAt(int x, int y, int z) {
        Chunk chunk = getChunk(x, y, z);
        
        if (chunk == null) {
        	BlockType type = BlockRegistry.getByName("air");
            Block air = type.createBlock(x, y, z);
        	return air;
        }

        return chunk.getBlock(
            Math.floorMod(x, Chunk.SIZE),
            Math.floorMod(y, Chunk.SIZE),
            Math.floorMod(z, Chunk.SIZE)
        );
    }

    public void setBlock(int x, int y, int z, String name) {
        int chunkX = (int) Math.floor((float) x / Chunk.SIZE);
        int chunkY = (int) Math.floor((float) y / Chunk.SIZE);
        int chunkZ = (int) Math.floor((float) z / Chunk.SIZE);

        String key = getChunkKey(chunkX, chunkY, chunkZ);
        Chunk chunk = chunks.get(key);

        if (chunk == null) {
            chunk = new Chunk(chunkX, chunkY, chunkZ);
            chunks.put(key, chunk);
        }
        BlockType type = BlockRegistry.getByName(name);
        Block block = type.createBlock(x, y, z);

        chunk.setBlock(
            Math.floorMod(x, Chunk.SIZE),
            Math.floorMod(y, Chunk.SIZE),
            Math.floorMod(z, Chunk.SIZE),
            block
        );
    }
    
    public Chunk addChunck(int x, int y, int z) {
    	int chunkX = (int) Math.floor((float) x / Chunk.SIZE);
        int chunkY = (int) Math.floor((float) y / Chunk.SIZE);
        int chunkZ = (int) Math.floor((float) z / Chunk.SIZE);
        
    	String key = getChunkKey(chunkX, chunkY, chunkZ);
        Chunk chunk = chunks.get(key);

        if (chunk == null) {
            chunk = new Chunk(chunkX, chunkY, chunkZ);
            chunks.put(key, chunk);
        }
        return chunk;
    }

    public void generateFlatWorld() {
    	int xMin = -10;
    	int xMax = 100;
    	int yMin = -30;
    	int yMax = -4;
    	int zMin = -10;
    	int Zmax = 100;
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= Zmax; z++) {
                setBlock(x, yMax, z, "grass");
            }
        }
        
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= Zmax; z++) {
            	for (int y = yMin; y < yMax; y++) {
                    setBlock(x, y, z, "dirt");
                }
            }
        }
        
        setBlock(0, -3, 0, "dirt");
        setBlock(1, -3, 0, "dirt");
        setBlock(2, -3, 0, "dirt");
        setBlock(3, -3, 0, "dirt");
        setBlock(4, -3, 0, "dirt");
        setBlock(5, -3, 0, "dirt");
    }

    public void render(Camera camera, long window) {
        // Calcul du frustum pour le culling
        IntBuffer widthBuf = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuf = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, widthBuf, heightBuf);
        int windowWidth = widthBuf.get(0);
        int windowHeight = heightBuf.get(0);
        float aspectRatio = (float) windowWidth / windowHeight;

        Matrix4f projection = camera.getProjectionMatrix(aspectRatio);
        Matrix4f view = camera.getViewMatrix();
        Matrix4f combined = new Matrix4f().set(projection).mul(view);

        float[] matrixArray = new float[16];
        combined.get(matrixArray);

        Frustum frustum = new Frustum();
        frustum.extractFrustum(matrixArray);

        Set<Entity> renderedEntities = new HashSet<>();

        glEnable(GL_TEXTURE_2D);
        for (Chunk chunk : chunks.values()) {
            float chunkWorldX = chunk.chunkX * Chunk.SIZE * 2; 
            float chunkWorldY = chunk.chunkY * Chunk.SIZE * 2;
            float chunkWorldZ = chunk.chunkZ * Chunk.SIZE * 2;
            float size = Chunk.SIZE * 2;

            if (!frustum.isBoxInFrustum(chunkWorldX, chunkWorldY, chunkWorldZ, size)) {
                continue; 
            }

            chunk.render(this, frustum, renderedEntities);
        }
        glDisable(GL_TEXTURE_2D);
    }

    public Iterable<Chunk> getAllChunks() {
        return chunks.values();
    }

    public boolean isAirOrTransparent(int x, int y, int z) {
        Block neighbor = getBlockAt(x, y, z);
        return neighbor == null || !neighbor.isSolid();
    }
    
    public boolean hasEntityInBlock(int blockX, int blockY, int blockZ) {
    	Block b = getBlockAt(blockX, blockY, blockZ);
    	AABB blockBox;
    	if (b != null) {
    		blockBox = b.getAABB();
    	}else {
	        blockBox = new AABB(blockX * 2f, blockY * 2f, blockZ * 2f,
	                                 2f, 2f, 2f);
    	}
        
        for (int cx = -1; cx <= 1; cx++) {
            for (int cy = -1; cy <= 1; cy++) {
                for (int cz = -1; cz <= 1; cz++) {
                    Chunk chunk = getChunk(blockX  ,
                                            blockY  ,
                                            blockZ );
                    if (chunk == null) continue;

                    for (Entity e : chunk.getEntities()) {
                        if (blockBox.intersects(e.getAABB()) && !(e instanceof ItemEntity)) {
                            return true;  
                        }
                    }
                }
            }
        }
        return false; 
    }
    
    public void update(float dt) {
    	Set<Entity> uniqueEntities = new HashSet<>();
    	for (Chunk chunk : getChunksAroundPlayer(10)) {
    		uniqueEntities.addAll(chunk.getEntities());
        }
    	
    	for (Entity entity : uniqueEntities) {
    			entity.update(dt, this);
        }
    	
    }
    
    public Iterable<Chunk> getChunksAroundPlayer(int range) {
        int playerChunkX = (int) Math.floor(player.getX() / 2 / Chunk.SIZE);
        int playerChunkY = (int) Math.floor(player.getY() / 2 / Chunk.SIZE);
        int playerChunkZ = (int) Math.floor(player.getZ() / 2 / Chunk.SIZE);

        List<Chunk> result = new ArrayList<>();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    int chunkX = playerChunkX + dx;
                    int chunkY = playerChunkY + dy;
                    int chunkZ = playerChunkZ + dz;

                    Chunk chunk = getChunk(chunkX * Chunk.SIZE, chunkY * Chunk.SIZE, chunkZ * Chunk.SIZE);
                    if (chunk != null) {
                        result.add(chunk);
                    }else {
                    	this.addChunck(chunkX, chunkY, chunkZ);
                    }
                }
            }
        }
        
        return result;
    }

	public void removeEntity(ItemEntity itemEntity) {
		for(Chunk chunk : itemEntity.getChunks(this)) {
			chunk.removeEntity(itemEntity);
		}
		
	}
	
	public void destroyBlock(int x, int y, int z) {
	    Block block = getBlockAt(x, y, z);
	    if (block == null || block.getType().name.equals("air")) return;

	    setBlock(x, y, z, "air"); 

	    Item dropped = block.getType().item;
	    if (dropped != null) {
	        ItemEntity itemEntity = new ItemEntity(dropped, 1, x * 2f - 0.5f , y * 2f - 0.5f , z * 2f - 0.5f );
	        Random random = new Random();
	        int randomNumber = random.nextInt(6) + 5;
	        itemEntity.velY = randomNumber;
	        randomNumber = random.nextInt(9) - 4;
	        itemEntity.velZ = randomNumber;
	        randomNumber = random.nextInt(9) - 4;
	        itemEntity.velX = randomNumber;
	        itemEntity.updateChunk(this, 0, 0, 0);
	    }
	}
}