package _Minecraft2.Item;

import _Minecraft2.util.GLUtils;
import _Minecraft2.world.block.BlockRegistry;
import static org.lwjgl.opengl.GL11.*;

public class BlockItem extends Item {

	public BlockItem(String name, boolean stackable, int maxStackSize, String itemTexturePath) {
		super(name, stackable, maxStackSize, itemTexturePath);
	}
	
	public int getUpTexture(){
		return BlockRegistry.getByName(getName()).properties.upTextureID;
	}
	
	public int getDownTexture(){
		return BlockRegistry.getByName(getName()).properties.downTextureID;
	}
	
	public int getFrontTexture(){
		return BlockRegistry.getByName(getName()).properties.frontTextureID;
	}
	
	public int getRightTexture(){
		return BlockRegistry.getByName(getName()).properties.rightTextureID;
	}
	
	public int getBackTexture(){
		return BlockRegistry.getByName(getName()).properties.backTextureID;
	}
	
	public int getLeftTexture(){
		return BlockRegistry.getByName(getName()).properties.leftTextureID;
	}
	
	@Override
	public void render(float x, float y, float size, float windowWidth, float windowHeight) {
	    glPushAttrib(GL_ENABLE_BIT | GL_TRANSFORM_BIT);
	    
	    glMatrixMode(GL_PROJECTION);
	    glPushMatrix();
	    glLoadIdentity();
	    glOrtho(0, windowWidth, windowHeight, 0, -100, 100);

	    glMatrixMode(GL_MODELVIEW);
	    glPushMatrix();
	    glLoadIdentity();

	    glEnable(GL_DEPTH_TEST);
	    glDepthFunc(GL_LESS);
	    
	    glDisable(GL_CULL_FACE);

	    glEnable(GL_TEXTURE_2D);

	    glTranslatef(x + size / 2, y + size / 2, 0);
	    
	    float scale = size / 1.4f ;
	    glScalef(scale, scale, scale);

	    glRotatef(150f, 1f, 0f, 0f);
	    glRotatef(135f, 0f, 1f, 0f);

	    drawCube(getFrontTexture(), getUpTexture(), getRightTexture()); 

	    glPopMatrix();
	    glMatrixMode(GL_PROJECTION);
	    glPopMatrix();
	    glMatrixMode(GL_MODELVIEW);
	    
	    glPopAttrib();
	}
	
	private void drawCube(int texFront, int texTop, int texRight) {
	    // Front
	    glBindTexture(GL_TEXTURE_2D, texFront);
	    glBegin(GL_QUADS);
	    glTexCoord2f(0f, 0f); glVertex3f(-0.5f, -0.5f, 0.5f);
	    glTexCoord2f(1f, 0f); glVertex3f( 0.5f, -0.5f, 0.5f);
	    glTexCoord2f(1f, 1f); glVertex3f( 0.5f,  0.5f, 0.5f);
	    glTexCoord2f(0f, 1f); glVertex3f(-0.5f,  0.5f, 0.5f);
	    glEnd();

	    // Top
	    glBindTexture(GL_TEXTURE_2D, texTop);
	    glBegin(GL_QUADS);
	    glTexCoord2f(0f, 0f); glVertex3f(-0.5f, 0.5f,  0.5f);
	    glTexCoord2f(1f, 0f); glVertex3f( 0.5f, 0.5f,  0.5f);
	    glTexCoord2f(1f, 1f); glVertex3f( 0.5f, 0.5f, -0.5f);
	    glTexCoord2f(0f, 1f); glVertex3f(-0.5f, 0.5f, -0.5f);
	    glEnd();

	    // Right
	    glBindTexture(GL_TEXTURE_2D, texRight);
	    glBegin(GL_QUADS);
	    glTexCoord2f(0f, 0f); glVertex3f(0.5f, -0.5f,  0.5f);
	    glTexCoord2f(1f, 0f); glVertex3f(0.5f, -0.5f, -0.5f);
	    glTexCoord2f(1f, 1f); glVertex3f(0.5f,  0.5f, -0.5f);
	    glTexCoord2f(0f, 1f); glVertex3f(0.5f,  0.5f,  0.5f);
	    glEnd();
	}

}
