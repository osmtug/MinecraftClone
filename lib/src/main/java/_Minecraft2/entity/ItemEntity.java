package _Minecraft2.entity;

import _Minecraft2.Item.BlockItem;
import _Minecraft2.Item.Item;
import _Minecraft2.collision.AABB;
import _Minecraft2.world.World;
import _Minecraft2.world.block.Block;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemEntity extends Entity {
	private final Item item;
    private int quantity;
    private float animationTime = 0f;

    public ItemEntity(Item item, int quantity, float x, float y, float z) {
        super(x, y, z);
        this.item = item;
        this.quantity = quantity;
        this.width = 1;
        this.height = 1;
        this.depth = 1;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }

    @Override
    public void update(float dt, World world) {
    	
    	AABB itemAABB = this.getAABB();
    	Block collision;
		collision = getCollidingBlock(itemAABB, world);
        
        super.update(dt, world); // mouvement + gravité + collisions
        if (collision != null) {
        	pushOutOfBlock(world, itemAABB, collision);
        }
        
    	animationTime += dt;
        
        
        if(onGround) {
        	applyFriction(20, dt);
        }

        // Ramassage automatique si proche d'un joueur
        if (world.player.getAABB().intersects(itemAABB)) {
            if (world.player.getInventory().addItem(item, quantity)) {
                world.removeEntity(this); 
            }
        }
    }
    
    public void applyFriction(float deceleration, float dt) {
	    float amount = deceleration * dt;

	    // velX
	    if (velX > 0) {
	        velX = Math.max(0, velX - amount);
	    } else if (velX < 0) {
	        velX = Math.min(0, velX + amount);
	    }

	    // velZ
	    if (velZ > 0) {
	        velZ = Math.max(0, velZ - amount);
	    } else if (velZ < 0) {
	        velZ = Math.min(0, velZ + amount);
	    }
    }
    
    public void pushOutOfBlock(World world, AABB itemBox, Block block) {
        AABB blockBox = block.getAABB();
        float pushAmount = 0.1f;

        // Distances dans chaque direction
        float dxMin = Math.abs(itemBox.maxX - blockBox.minX);
        float dxMax = Math.abs(blockBox.maxX - itemBox.minX);
        float dyMin = Math.abs(itemBox.maxY - blockBox.minY);
        float dyMax = Math.abs(blockBox.maxY - itemBox.minY);
        float dzMin = Math.abs(itemBox.maxZ - blockBox.minZ);
        float dzMax = Math.abs(blockBox.maxZ - itemBox.minZ);

       
        class Direction {
            float dx, dy, dz, distance;
            Direction(float dx, float dy, float dz, float distance) {
                this.dx = dx;
                this.dy = dy;
                this.dz = dz;
                this.distance = distance;
            }
        }

        List<Direction> directions = new ArrayList<>();
        directions.add(new Direction(-(dxMin + pushAmount), 0, 0, dxMin));  // Gauche
        directions.add(new Direction(+(dxMax + pushAmount), 0, 0, dxMax));  // Droite
        
        directions.add(new Direction(0, -(dyMin + pushAmount), 0, dyMin)); // Bas
        directions.add(new Direction(0, +(dyMax + pushAmount), 0, dyMax)); // Haut
        
        directions.add(new Direction(0, 0, -(dzMin + pushAmount), dzMin));  // Arrière
        directions.add(new Direction(0, 0, +(dzMax + pushAmount), dzMax));  // Avant

       
        directions.sort(Comparator.comparingDouble(d -> d.distance));

        // Tester chaque direction jusqu'à trouver une libre
        for (Direction dir : directions) {
            float newX = x + dir.dx;
            float newY = y + dir.dy;
            float newZ = z + dir.dz;

            AABB newBox = itemBox.offset(dir.dx, dir.dy, dir.dz);
            Block collision = getCollidingBlock(newBox, world);

            if (collision == null || !collision.isSolid()) {
                move(dir.dx, dir.dy, dir.dz, world);
                velX = 0;
                velY = 0;
                velZ = 0;
                onGround = true;
                return; 
            }
        }
    }

    @Override
    public void render() {
    	if (item == null) return;
    	super.render();

        glPushMatrix();

        // Position de base
        glTranslatef(x+0.5f, y+0.5f, z+0.5f);
        
        

        // Flottement sinusoïdal haut/bas
        float time = (System.currentTimeMillis() % 10000L) / 1000f; 
        float bobbing = (float) Math.sin(animationTime * 2f) * 0.1f;
        glTranslatef(0f, 0.25f + bobbing, 0f); 

        // Rotation autour de Y
        float rotation = animationTime * 40f;
        glRotatef(rotation, 0f, 1f, 0f);

        // Mise à l'échelle
        glScalef(0.3f, 0.3f, 0.3f);

        if (item instanceof BlockItem blockItem) {
            renderTexturedCube(blockItem);
        } else {
            renderSimpleItemBox();
        }

        glPopMatrix();
    }
    
    private void renderTexturedCube(BlockItem blockItem) {
        float s = 1f;

        // Front
        glBindTexture(GL_TEXTURE_2D, blockItem.getFrontTexture());
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-s, -s,  s);
        glTexCoord2f(1, 0); glVertex3f( s, -s,  s);
        glTexCoord2f(1, 1); glVertex3f( s,  s,  s);
        glTexCoord2f(0, 1); glVertex3f(-s,  s,  s);
        glEnd();

        // Back
        glBindTexture(GL_TEXTURE_2D, blockItem.getBackTexture());
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( s, -s, -s);
        glTexCoord2f(1, 0); glVertex3f(-s, -s, -s);
        glTexCoord2f(1, 1); glVertex3f(-s,  s, -s);
        glTexCoord2f(0, 1); glVertex3f( s,  s, -s);
        glEnd();

        // Left
        glBindTexture(GL_TEXTURE_2D, blockItem.getLeftTexture());
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-s, -s, -s);
        glTexCoord2f(1, 0); glVertex3f(-s, -s,  s);
        glTexCoord2f(1, 1); glVertex3f(-s,  s,  s);
        glTexCoord2f(0, 1); glVertex3f(-s,  s, -s);
        glEnd();

        // Right
        glBindTexture(GL_TEXTURE_2D, blockItem.getRightTexture());
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f( s, -s,  s);
        glTexCoord2f(1, 0); glVertex3f( s, -s, -s);
        glTexCoord2f(1, 1); glVertex3f( s,  s, -s);
        glTexCoord2f(0, 1); glVertex3f( s,  s,  s);
        glEnd();

        // Top
        glBindTexture(GL_TEXTURE_2D, blockItem.getUpTexture());
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-s,  s, -s);
        glTexCoord2f(1, 0); glVertex3f( s,  s, -s);
        glTexCoord2f(1, 1); glVertex3f( s,  s,  s);
        glTexCoord2f(0, 1); glVertex3f(-s,  s,  s);
        glEnd();

        // Bottom
        glBindTexture(GL_TEXTURE_2D, blockItem.getDownTexture());
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex3f(-s, -s,  s);
        glTexCoord2f(1, 0); glVertex3f( s, -s,  s);
        glTexCoord2f(1, 1); glVertex3f( s, -s, -s);
        glTexCoord2f(0, 1); glVertex3f(-s, -s, -s);
        glEnd();
    }

    private void renderSimpleItemBox() {
        glColor3f(1f, 1f, 0f); // fallback jaune
        glBegin(GL_QUADS);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f( 0.5f, -0.5f, 0.5f);
        glVertex3f( 0.5f,  0.5f, 0.5f);
        glVertex3f(-0.5f,  0.5f, 0.5f);
        glEnd();
    }
}
