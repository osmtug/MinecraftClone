package _Minecraft2.entity;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joml.Vector2f;

import _Minecraft2.collision.AABB;
import _Minecraft2.world.Chunk;
import _Minecraft2.world.World;
import _Minecraft2.world.block.Block;

public class Entity {
	protected float x, y, z; 
    public float velY = 0f, velX = 0f, velZ = 0f;
    
    protected float speed = 10.0f;
    protected float acceleration = 500.0f;
    protected float jumpForce =15f;
    protected float gravity = 50.0f;
    
    protected float width = 1;
    protected float height = 3.5f;
    protected float depth = 1;
    
    protected boolean onGround = false;
    
    public AABB getAABB() {
    	return new AABB(x , y , z , width, height, depth);
    }
    
    public Entity(float x, float y, float z){
    	this.x = x;
    	this.y = y;
    	this.z = z;
    }
    
    private float clamp(float val, float min, float max) {
		if(val < min) return min;
		if(val > max) return max;
		return val;
	}
    
    public void update(float deltaTime, World world) {
    	if (!onGround) {
    	    applyGravity(deltaTime, gravity, 4000.0f);
    	} else {
    	    velY = 0; // reset vitesse Y quand sur le sol
    	}
    	
    	move(velX * deltaTime, velY* deltaTime, velZ* deltaTime, world);
    }

	public void move(float dx, float dy, float dz, World world) {
		moveY(dy, world);
		moveX(dx, world);
        moveZ(dz, world);
        updateChunk(world, -dx, -dy, -dz);
    }
	
	public void moveX(float dx, World world) {
		Block collision;
		collision = getCollidingBlock(getAABB().offset(dx, 0, 0), world);
        if (collision == null) {
            x += dx;
        }else {
        	AABB blockAABB = collision.getAABB();
            AABB playerAABB = getAABB();

            if (dx > 0) { 
                teleport(world, blockAABB.minX - playerAABB.getWidth(), y, z);
            } else if (dx < 0) { 
                teleport(world, blockAABB.maxX, y, z);
            }
            
        	velX = 0;
        }
	}
	
	public void moveY(float dy, World world) {
		if (!(this instanceof Player)) {
		}
		Block collision = getCollidingBlock(getAABB().offset(0, dy, 0), world);

		if (collision == null && dy != 0) {
		    y += dy;
		} else {
		    if (dy < 0) {
		    	teleport(world, x, collision.getAABB().maxY, z);
		    } else if (dy > 0) {
		    	teleport(world, x, collision.getAABB().minY - height, z);
		    }
		    if(velY > 0)
		    	velY = 0;
		}

		if (dy <= 0) {
			float epsilon = 0.05f;
			AABB below = getAABB().offset(0, -epsilon, 0);
			onGround = (getCollidingBlock(below, world) != null);
		}else {
			onGround = false;
		}
		
	}

	public void moveZ(float dz, World world) {
		Block collision;
		collision = getCollidingBlock(getAABB().offset(0, 0, dz), world);
        if (collision == null) {
            z += dz;
        }else {
        	AABB blockAABB = collision.getAABB();
            AABB playerAABB = getAABB();

            if (dz > 0) { 
            	teleport(world, x, y, blockAABB.minZ - playerAABB.getDepth());
            } else if (dz < 0) { 
            	teleport(world, x, y, blockAABB.maxZ);
            }
            
        	velZ = 0;
        }
	}
    
	protected Block getCollidingBlock(AABB box, World world) {
	    int minX = (int) Math.floor(box.minX);
	    int maxX = (int) Math.floor(box.maxX);
	    int minY = (int) Math.floor(box.minY);
	    int maxY = (int) Math.floor(box.maxY);
	    int minZ = (int) Math.floor(box.minZ);
	    int maxZ = (int) Math.floor(box.maxZ);
	    
	    
	    
	    if (minX > 0) minX++;
	    if (maxX > 0) maxX++;
	    if (minZ > 0) minZ++;
	    if (maxZ > 0) maxZ++;
	    if (minY > 0) minY++;
	    if (maxY > 0) maxY++;
	    

	    for (int x = minX; x <= maxX; x++) {
	        for (int y = minY; y <= maxY; y++) {
	            for (int z = minZ; z <= maxZ; z++) {
	                Block block = world.getBlockAt(x/2, y/2, z/2);
	                if (block.isSolid() && box.intersects(block.getAABB())) {
	                    return block;
	                }else{
	                	
	                }
	            }
	        }
	    }
	    return null;
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public Block drawCollidingBlock(AABB box, World world) {
		int minX = (int) Math.floor(box.minX);
	    int maxX = (int) Math.floor(box.maxX);
	    int minY = (int) Math.floor(box.minY);
	    int maxY = (int) Math.floor(box.maxY);
	    int minZ = (int) Math.floor(box.minZ);
	    int maxZ = (int) Math.floor(box.maxZ);
	    
	    if (minX > 0) minX++;
	    if (maxX > 0) maxX++;
	    if (minZ > 0) minZ++;
	    if (maxZ > 0) maxZ++;
	    if (minY > 0) minY++;
	    if (maxY > 0) maxY++;

	    for (int x = minX; x <= maxX; x++) {
	        for (int y = minY; y <= maxY; y++) {
	            for (int z = minZ; z <= maxZ; z++) {
	                Block block = world.getBlockAt(x/2, y/2, z/2);
	                block.getAABB().draw();
	            }
	        }
	    }
	    return null;
	}
	
	public void applyGravity(float deltaTime, float gravity, float maxFallSpeed) {
        velY -= gravity * deltaTime;

        // Clamp la vitesse de chute à -maxFallSpeed
        if (velY < -maxFallSpeed) {
            velY = -maxFallSpeed;
        }
    }
	
	public void clampVelocityXZ(Vector2f movement) {
    	if (onGround) {
    	    velX = clamp(velX, -speed*Math.abs(movement.x), speed*Math.abs(movement.x));
    	    velZ = clamp(velZ, -speed*Math.abs(movement.y), speed*Math.abs(movement.y));
	    }else {
	    	velX = clamp(velX, -speed, speed);
    	    velZ = clamp(velZ, -speed, speed);
	    }
    }
	
	public void limitSpeed() {
    	float totalSpeed = (float) Math.sqrt(velX * velX + velZ * velZ);
	    if (totalSpeed > speed) {
	        float scale = speed / totalSpeed;
	        velX *= scale;
	        velZ *= scale;
	    }
    }
    
    public void applyFriction(float dt){
    	velX *= 0.8f * dt;
	    velZ *= 0.8f * dt;
    }
    
    public Set<Chunk> getChunks(World world){
    	AABB aabb = getAABB();
    	
    	int minX = (int) Math.floor(aabb.minX);
	    int maxX = (int) Math.floor(aabb.maxX);
	    int minY = (int) Math.floor(aabb.minY);
	    int maxY = (int) Math.floor(aabb.maxY);
	    int minZ = (int) Math.floor(aabb.minZ);
	    int maxZ = (int) Math.floor(aabb.maxZ);
	    
	    
	    
	    if (minX > 0) minX++;
	    if (maxX > 0) maxX++;
	    if (minZ > 0) minZ++;
	    if (maxZ > 0) maxZ++;
	    if (minY > 0) minY++;
	    if (maxY > 0) maxY++;
	    
	    minX /= 2;
	    maxX /= 2;
	    minY /= 2;
	    maxY /= 2;
	    minZ /= 2;
	    maxZ /= 2;
	    
	    Set<Chunk> addedChunk = new HashSet<>();
	    Chunk tmp;

	    tmp = world.getChunk(minX, minY, minZ);
	    if(tmp == null) tmp = world.addChunck(minX, minY, minZ);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(minX, minY, maxZ);
	    if(tmp == null) tmp = world.addChunck(minX, minY, maxZ);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(minX, maxY, minZ);
	    if(tmp == null) tmp = world.addChunck(minX, maxY, minZ);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(minX, maxY, maxZ);
	    if(tmp == null) tmp = world.addChunck(minX, maxY, maxZ);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, minY, minZ);
	    if(tmp == null) tmp = world.addChunck(maxX, minY, minZ);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, minY, maxZ);
	    if(tmp == null) tmp = world.addChunck(maxX, minY, maxZ);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, maxY, minZ);
	    if(tmp == null) tmp = world.addChunck(maxX, maxY, minZ);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, maxY, maxZ);
	    if(tmp == null) tmp = world.addChunck(maxX, maxY, maxZ);
	    addedChunk.add(tmp);
	    
	    return addedChunk;
    }
    
    public Chunk getChunk(World world){
    	AABB aabb = getAABB();
    	
    	int minX = (int) Math.floor(aabb.minX);
	    int minY = (int) Math.floor(aabb.minY);
	    int minZ = (int) Math.floor(aabb.minZ);
	    
	    
	    
	    if (minX > 0) minX++;
	    if (minZ > 0) minZ++;
	    if (minY > 0) minY++;

	    return world.getChunk(minX, minY, minZ);
    }
    
    public void updateChunk(World world, float dx, float dy, float dz) {
    	AABB aabb = getAABB();
    	AABB oldAabb = getAABB().offset(dx, dy, dz);
    	int minX = (int) Math.floor(aabb.minX);
	    int maxX = (int) Math.floor(aabb.maxX);
	    int minY = (int) Math.floor(aabb.minY);
	    int maxY = (int) Math.floor(aabb.maxY);
	    int minZ = (int) Math.floor(aabb.minZ);
	    int maxZ = (int) Math.floor(aabb.maxZ);
	    
	    
	    
	    if (minX > 0) minX++;
	    if (maxX > 0) maxX++;
	    if (minZ > 0) minZ++;
	    if (maxZ > 0) maxZ++;
	    if (minY > 0) minY++;
	    if (maxY > 0) maxY++;
	    
	    int oldminX = (int) Math.floor(oldAabb.minX);
	    int oldmaxX = (int) Math.floor(oldAabb.maxX);
	    int oldminY = (int) Math.floor(oldAabb.minY);
	    int oldmaxY = (int) Math.floor(oldAabb.maxY);
	    int oldminZ = (int) Math.floor(oldAabb.minZ);
	    int oldmaxZ = (int) Math.floor(oldAabb.maxZ);
	    
	    
	    
	    if (oldminX > 0) oldminX++;
	    if (oldmaxX > 0) oldmaxX++;
	    if (oldminZ > 0) oldminZ++;
	    if (oldmaxZ > 0) oldmaxZ++;
	    if (oldminY > 0) oldminY++;
	    if (oldmaxY > 0) oldmaxY++;
	    
	    minX /= 2;
	    maxX /= 2;
	    minY /= 2;
	    maxY /= 2;
	    minZ /= 2;
	    maxZ /= 2;
	    
	    oldminX /= 2;
	    oldmaxX /= 2;
	    oldminZ /= 2;
	    oldmaxZ /= 2;
	    oldminY /= 2;
	    oldmaxY /= 2;
	    
	    
	    Set<Chunk> addedChunk = new HashSet<>();
	    Chunk tmp;
        
	    
	    tmp = world.getChunk(minX, minY, minZ);
	    if(tmp == null) tmp = world.addChunck(minX, minY, minZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(minX, minY, maxZ);
	    if(tmp == null) tmp = world.addChunck(minX, minY, maxZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(minX, maxY, minZ);
	    if(tmp == null) tmp = world.addChunck(minX, maxY, minZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(minX, maxY, maxZ);
	    if(tmp == null) tmp = world.addChunck(minX, maxY, maxZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, minY, minZ);
	    if(tmp == null) tmp = world.addChunck(maxX, minY, minZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, minY, maxZ);
	    if(tmp == null) tmp = world.addChunck(maxX, minY, maxZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, maxY, minZ);
	    if(tmp == null) tmp = world.addChunck(maxX, maxY, minZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    tmp = world.getChunk(maxX, maxY, maxZ);
	    if(tmp == null) tmp = world.addChunck(maxX, maxY, maxZ);
	    tmp.addEntity(this);
	    addedChunk.add(tmp);
	    
	    
	    
	    tmp = world.getChunk(oldminX, oldminY, oldminZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
	    tmp = world.getChunk(oldminX, oldminY, oldminZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
	    tmp = world.getChunk(oldminX, oldminY, oldmaxZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
	    tmp = world.getChunk(oldminX, oldmaxY, oldminZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
	    tmp = world.getChunk(oldminX, oldmaxY, oldmaxZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
	    tmp = world.getChunk(oldmaxX, oldminY, oldmaxZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
	    tmp = world.getChunk(oldmaxX, oldmaxY, oldminZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
	    tmp = world.getChunk(oldmaxX, oldmaxY, oldmaxZ);
	    if(tmp != null && !addedChunk.contains(tmp)) {
		    tmp.removeEntity(this);
		    addedChunk.add(tmp);
	    }
	    
    }
    
    public void teleport(World world, float x, float y, float z) {
    	float dx = x - this.x;
    	float dy = y - this.y;
    	float dz = z - this.z;
    	
	    this.x = x;
	    this.y = y;
	    this.z = z;
	    
	    updateChunk(world, dx, dy, dz);
    }

	public void render() {
	}
}
