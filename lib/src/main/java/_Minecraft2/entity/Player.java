package _Minecraft2.entity;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

import _Minecraft2.Inventory;
import _Minecraft2.collision.AABB;
import _Minecraft2.render.Camera;
import _Minecraft2.util.Raycast;
import _Minecraft2.world.World;
import _Minecraft2.world.block.Block;

public class Player extends Entity {

	    public Raycast.Result resultRaycast;
	    
	    private float breakCooldown = 0.2f;
	    private float placeCooldown = 0.2f;
	    private float breakDuration = 1;
	    private float placeDuration = 1;

	    private Camera camera;
	    private long window;
	    
	    private Inventory inventory;
	    
	    
	    public Player(long window, float startX, float startY, float startZ) {
	    	super(startX, startY, startZ);
	    	inventory = new Inventory(10);
	        this.window = window;
	        this.camera = new Camera(x, y + 1.5f, z); 
	    }
	    
	    boolean hasSupportUnderAABB(AABB aabb, World world) {
	    	float margin = 0.01f;
		    Vector3f[] corners = new Vector3f[] {
		        new Vector3f(aabb.minX + margin, aabb.minY, aabb.minZ + margin),
		        new Vector3f(aabb.maxX - margin, aabb.minY, aabb.minZ + margin),
		        new Vector3f(aabb.minX + margin, aabb.minY, aabb.maxZ - margin),
		        new Vector3f(aabb.maxX - margin, aabb.minY, aabb.maxZ - margin)
		    };
		    for (Vector3f pos : corners) {
		        if (Raycast.raycastDDABool(world, pos, new Vector3f(0, -1, 0), 1f)) {
		            return true;
		        }
		    }
		    return false;
		}

	    public void update(float deltaTime, World world) {
	    	AABB playerAABB = getAABB();
	    	
	    	handleMovementXZ(deltaTime);
	    	if (handleSneak()) {
	    		
	    		float edgeCheckDistance = 0.1f; // petite distance en avant du mouvement

	    		// Calcule les AABB décalés dans les directions X et Z
	    		AABB offsetAABBX = playerAABB.offset(Math.signum(velX) * edgeCheckDistance, 0, 0);
	    		AABB offsetAABBZ = playerAABB.offset(0, 0, Math.signum(velZ) * edgeCheckDistance);

	    		// Fonction pour tester 4 coins bas d'un AABB
	    		

	    		if (onGround) {
	    		    // Teste si y a du support sous le futur déplacement X
	    		    if (!hasSupportUnderAABB(offsetAABBX, world)) {
	    		        velX = 0;
	    		    }
	    		    // Teste si y a du support sous le futur déplacement Z
	    		    if (!hasSupportUnderAABB(offsetAABBZ, world)) {
	    		        velZ = 0;
	    		    }
	    		}
	    		
	    	}
	    	
	    	aplyRaycast(world, deltaTime);
	    	
	    	if (onGround) {
	    	    if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
		    	    velY = jumpForce;
		    	    onGround = false;
		    	}
	    	}
	    	
	    	super.update(deltaTime, world);

	    	// Update de la caméra
	    	camera.setPosition(x + width/2, y + height - 0.5f, z + depth/2);
	    }
	    
	    public void updateMouse(double dx, double dy, float sensitivity) {
	        camera.rotate((float) dy * sensitivity, (float) dx * sensitivity);
	    }

	    public Camera getCamera() {
	        return camera;
	    }
	    
	    public void handleMovementXZ(float deltaTime){

	    	Vector2f movement = getMovementVectorWithInput();
	    	
	    	if (movement.length() > 0) {
	    	    movement.normalize();
	    	    
	    	    float currentAccel = acceleration;
	    	    if (!onGround) currentAccel = 40f;
	    	    
	    	    velX += movement.x * currentAccel * deltaTime;
	    	    velZ += movement.y * currentAccel * deltaTime;
	    	    
	    	    clampVelocityXZ(movement);
	    	    limitSpeed();
	    	    
	    	} else {
	    	    applyFriction(deltaTime);
	    	}
	    }
	    
	    public Vector2f getMovementVectorWithInput(){
	    	float radians = (float) Math.toRadians(camera.getYaw());
	    	
	    	Vector2f forward = new Vector2f((float)Math.sin(radians), -(float)Math.cos(radians));
	    	Vector2f right   = new Vector2f(forward.y, -forward.x); // perpendiculaire
	    	Vector2f movement = new Vector2f();

	    	if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) movement.add(forward);
	    	if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) movement.sub(forward);
	    	if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) movement.add(right);
	    	if (glfwGetKey(window, GLFW_KEY_D)  == GLFW_PRESS) movement.sub(right);
	    	
	    	return movement;
	    }
	    
	    public void aplyRaycast(World world, float dt) {
	    	resultRaycast = Raycast.raycastDDA(world, 
    		    new Vector3f(camera.getX(), camera.getY(), camera.getZ()),
    		    camera.getViewDirection(), 
    		    8.0f
    		);
	    	if(resultRaycast != null) {
	    	}
	    	if (breakDuration > 0) breakDuration -= dt;
	    	if (placeDuration > 0) placeDuration -= dt;
	    	
	    	if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
	    	    if (resultRaycast != null && breakDuration < 0) {
	    	    	int x = resultRaycast.block.getPosX();
	                int y = resultRaycast.block.getPosY();
	                int z = resultRaycast.block.getPosZ();
	                
	                breakDuration = breakCooldown;
	                
	                world.destroyBlock(x, y, z);
	    	        
	    	    }
	    	}else if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
	    		breakDuration =-1;
	    	}
	    	
	    	if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
	    		if (resultRaycast != null && placeDuration < 0) {
	    			int x = resultRaycast.block.getPosX();
	                int y = resultRaycast.block.getPosY();
	                int z = resultRaycast.block.getPosZ();
	                Vector3f face = resultRaycast.hitFace;
	                
	                placeDuration = placeCooldown;
	                
	                int newX = (int) (x + face.x);
	                int newY = (int) (y + face.y);
	                int newZ = (int) (z + face.z);
	                
	                
	                Block existing = world.getBlockAt(newX, newY, newZ);
	                if ((existing == null || !existing.isSolid()) && !world.hasEntityInBlock(newX, newY, newZ)) {
	                    world.setBlock(newX, newY, newZ, "grass");
	                }
	    	    }
	    	}else if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_RELEASE) {
	    		placeDuration = -1;
	    	}
	    }
	    
	    public void drawoutline(){
	    	if (resultRaycast == null) return;
	    	resultRaycast.block.getAABB().draw(1, 1, 1);
	    	
	    	
	    }
	    
	    public boolean handleSneak() {
	    	if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
	    		height = 3.2f;
	    		speed = 3.5f;
	    		return true;
	    	}else {
	    		height = 3.5f;
	    		speed = 10f;
	    		return false;
	    	}
	    }

		public Inventory getInventory() {
			return inventory;
		}

		public void setInventory(Inventory inventory) {
			this.inventory = inventory;
		}

}
