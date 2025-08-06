package _Minecraft2.world.block;

import static org.lwjgl.opengl.GL11.*;

import _Minecraft2.collision.AABB;
import _Minecraft2.Item.tool.ToolType;

public abstract class Block {
	
	private int posX;
	private int posY;
	private int posZ;
	
	public abstract BlockProperties getProperties();
	
	public Block(int posX, int posY, int posZ) {
		this.posX = posX ;
		this.posY = posY ;
		this.posZ = posZ ;
	}
	
	public int getTextureID(Face face) {
        BlockProperties props = getProperties();
        return switch (face) {
            case UP -> props.upTextureID;
            case DOWN -> props.downTextureID;
            case LEFT -> props.leftTextureID;
            case RIGHT -> props.rightTextureID;
            case FRONT -> props.frontTextureID;
            case BACK -> props.backTextureID;
        };
    }
	
	
	public boolean isNeededTool(String tool) {
		BlockProperties props = getProperties();
		return props.neededTool.equals(tool);
	}

	public void render() {
	    if (!isSolid()) return;

	    glEnable(GL_TEXTURE_2D);
	    glPushMatrix();
	    glTranslatef(posX * 2f, posY * 2f, posZ * 2f);
	    glColor3f(1f, 1f, 1f); // Reset couleur

	    // Face avant
	    glBindTexture(GL_TEXTURE_2D, getTextureID(Face.FRONT));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, -1, 1);
	    glTexCoord2f(1, 0); glVertex3f( 1, -1, 1);
	    glTexCoord2f(1, 1); glVertex3f( 1,  1, 1);
	    glTexCoord2f(0, 1); glVertex3f(-1,  1, 1);
	    glEnd();

	    // Face arrière
	    glBindTexture(GL_TEXTURE_2D, getTextureID(Face.BACK));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f( 1, -1, -1);
	    glTexCoord2f(1, 0); glVertex3f(-1, -1, -1);
	    glTexCoord2f(1, 1); glVertex3f(-1,  1, -1);
	    glTexCoord2f(0, 1); glVertex3f( 1,  1, -1);
	    glEnd();

	    // Face gauche
	    glBindTexture(GL_TEXTURE_2D, getTextureID(Face.LEFT));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, -1, -1);
	    glTexCoord2f(1, 0); glVertex3f(-1, -1,  1);
	    glTexCoord2f(1, 1); glVertex3f(-1,  1,  1);
	    glTexCoord2f(0, 1); glVertex3f(-1,  1, -1);
	    glEnd();

	    // Face droite
	    glBindTexture(GL_TEXTURE_2D, getTextureID(Face.RIGHT));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f( 1, -1,  1);
	    glTexCoord2f(1, 0); glVertex3f( 1, -1, -1);
	    glTexCoord2f(1, 1); glVertex3f( 1,  1, -1);
	    glTexCoord2f(0, 1); glVertex3f( 1,  1,  1);
	    glEnd();

	    // Face du haut
	    glBindTexture(GL_TEXTURE_2D, getTextureID(Face.UP));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, 1, -1);
	    glTexCoord2f(1, 0); glVertex3f( 1, 1, -1);
	    glTexCoord2f(1, 1); glVertex3f( 1, 1,  1);
	    glTexCoord2f(0, 1); glVertex3f(-1, 1,  1);
	    glEnd();

	    // Face du bas
	    glBindTexture(GL_TEXTURE_2D, getTextureID(Face.DOWN));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, -1,  1);
	    glTexCoord2f(1, 0); glVertex3f( 1, -1,  1);
	    glTexCoord2f(1, 1); glVertex3f( 1, -1, -1);
	    glTexCoord2f(0, 1); glVertex3f(-1, -1, -1);
	    glEnd();

	    glPopMatrix();
	    glDisable(GL_TEXTURE_2D);
	    
	    //getAABB().draw();
	}
	
	public void startRender() {
		glPushMatrix();
	    glTranslatef(posX * 2f, posY * 2f, posZ * 2f);
	    glColor3f(1f, 1f, 1f); // Reset couleur
	}
	
	public void renderFront() {
		glBindTexture(GL_TEXTURE_2D, getTextureID(Face.FRONT));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, -1, 1);
	    glTexCoord2f(1, 0); glVertex3f( 1, -1, 1);
	    glTexCoord2f(1, 1); glVertex3f( 1,  1, 1);
	    glTexCoord2f(0, 1); glVertex3f(-1,  1, 1);
	    glEnd();
	}
	
	public void renderBack() {
		glBindTexture(GL_TEXTURE_2D, getTextureID(Face.BACK));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f( 1, -1, -1);
	    glTexCoord2f(1, 0); glVertex3f(-1, -1, -1);
	    glTexCoord2f(1, 1); glVertex3f(-1,  1, -1);
	    glTexCoord2f(0, 1); glVertex3f( 1,  1, -1);
	    glEnd();
	}
	
	public void renderLeft() {
		glBindTexture(GL_TEXTURE_2D, getTextureID(Face.LEFT));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, -1, -1);
	    glTexCoord2f(1, 0); glVertex3f(-1, -1,  1);
	    glTexCoord2f(1, 1); glVertex3f(-1,  1,  1);
	    glTexCoord2f(0, 1); glVertex3f(-1,  1, -1);
	    glEnd();
	}
	
	public void renderRight() {
		glBindTexture(GL_TEXTURE_2D, getTextureID(Face.RIGHT));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f( 1, -1,  1);
	    glTexCoord2f(1, 0); glVertex3f( 1, -1, -1);
	    glTexCoord2f(1, 1); glVertex3f( 1,  1, -1);
	    glTexCoord2f(0, 1); glVertex3f( 1,  1,  1);
	    glEnd();
	}
	
	public void renderUp() {
		glBindTexture(GL_TEXTURE_2D, getTextureID(Face.UP));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, 1, -1);
	    glTexCoord2f(1, 0); glVertex3f( 1, 1, -1);
	    glTexCoord2f(1, 1); glVertex3f( 1, 1,  1);
	    glTexCoord2f(0, 1); glVertex3f(-1, 1,  1);
	    glEnd();
	}
	
	public void renderDown() {
		glBindTexture(GL_TEXTURE_2D, getTextureID(Face.DOWN));
	    glBegin(GL_QUADS);
	    glTexCoord2f(0, 0); glVertex3f(-1, -1,  1);
	    glTexCoord2f(1, 0); glVertex3f( 1, -1,  1);
	    glTexCoord2f(1, 1); glVertex3f( 1, -1, -1);
	    glTexCoord2f(0, 1); glVertex3f(-1, -1, -1);
	    glEnd();
	}
	
	public void stopRender() {
		glPopMatrix();
	}

	public int getPosX() {
		return posX;
	}


	public int getPosY() {
		return posY;
	}


	public int getPosZ() {
		return posZ;
	}

	public float getWidth() {
		BlockProperties props = getProperties();
		return props.width;
	}

	public float getHeight() {
		BlockProperties props = getProperties();
		return props.height;
	}

	public float getDepth() {
		BlockProperties props = getProperties();
		return props.depth;
	}

	public AABB getAABB() {
		BlockProperties props = getProperties();
	    return new AABB(posX*2 - 1f, posY*2 - 1f, posZ*2 - 1f, props.width, props.height, props.depth);
	}

	public boolean isSolid() {
		BlockProperties props = getProperties();
		return props.isSolid;
	}
	
	public BlockType getType() {
		BlockProperties props = getProperties();
		return props.type;
	}

	public abstract Block copy(int x, int y, int z);
	
	
	
}
