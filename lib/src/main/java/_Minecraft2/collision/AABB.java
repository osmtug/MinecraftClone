package _Minecraft2.collision;


import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;


public class AABB {
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public boolean intersectsX(AABB other) {
        return minX < other.maxX && maxX > other.minX;
    }
    public boolean intersectsY(AABB other) {
        return minY < other.maxY && maxY > other.minY;
    }
    public boolean intersectsZ(AABB other) {
        return minZ < other.maxZ && maxZ > other.minZ;
    }

    public boolean intersects(AABB other) {
        return (minX < other.maxX && maxX > other.minX) &&
               (minY < other.maxY && maxY > other.minY) &&
               (minZ < other.maxZ && maxZ > other.minZ);
    }

    public AABB offset(float dx, float dy, float dz) {
        return new AABB(minX + dx, minY + dy, minZ + dz,
                        getWidth(), getHeight(), getDepth());
    }
    
    public AABB(float x, float y, float z, float width, float height, float depth) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x + width;
        this.maxY = y + height;
        this.maxZ = z + depth;
    }
    
    public void draw() {
        draw(1, 0, 0);
    }
    
    public void draw(float red, float green, float blue) {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glColor3f(red, green, blue);

        glBegin(GL_LINES);

        float[][] v = {
            {minX, minY, minZ},
            {maxX, minY, minZ},
            {maxX, maxY, minZ},
            {minX, maxY, minZ},
            {minX, minY, maxZ},
            {maxX, minY, maxZ},
            {maxX, maxY, maxZ},
            {minX, maxY, maxZ}
        };

        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] edge : edges) {
            glVertex3f(v[edge[0]][0], v[edge[0]][1], v[edge[0]][2]);
            glVertex3f(v[edge[1]][0], v[edge[1]][1], v[edge[1]][2]);
        }

        glEnd();
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }
    
    public float getWidth() {
    	return this.maxX - minX;
    }
    
    public float getHeight() {
    	return this.maxY - minY;
    }
    
    public float getDepth() {
    	return this.maxZ - minZ;
    }
 

    public Vector3f getHitFace(float startX, float startY, float startZ, float endX, float endY, float endZ) {
    	Face res = Face.NONE;
    	if ( startX < minX && endX > minX || startX > minX && endX < minX ) res = Face.LEFT; 
    	if ( startX < maxX && endX > maxX || startX > maxX && endX < maxX ) res = Face.RIGHT; 
    	if ( startY < minY && endY > minY || startY > minY && endY < minY ) res = Face.BOTTOM; 
    	if ( startY < maxY && endY > maxY || startY > maxY && endY < maxY ) res = Face.TOP; 
    	if ( startZ < minZ && endZ > minZ || startZ > minZ && endZ < minZ ) res = Face.BACK; 
    	if ( startZ < maxZ && endZ > maxZ || startZ > maxZ && endZ < maxZ ) res = Face.FRONT; 
        return getNormalFromFace(res);
    }
    
    public static Vector3f getNormalFromFace(Face face) {
        switch (face) {
            case LEFT:
                return new Vector3f(-1, 0, 0);
            case RIGHT:
                return new Vector3f(1, 0, 0);
            case BOTTOM:
                return new Vector3f(0, -1, 0);
            case TOP:
                return new Vector3f(0, 1, 0);
            case BACK:
                return new Vector3f(0, 0, -1);
            case FRONT:
                return new Vector3f(0, 0, 1);
            default:
                return new Vector3f(0, 0, 0); // Par défaut (aucune face)
        }
    }
    
    

    enum Face {
        NONE,    // Pas d'intersection
        FRONT,   // Face avant (Z-)
        BACK,    // Face arrière (Z+)
        LEFT,    // Face gauche (X-)
        RIGHT,   // Face droite (X+)
        BOTTOM,  // Face inférieure (Y-)
        TOP      // Face supérieure (Y+)
    }
    
    public float getHalfWidth() {
        return getWidth() / 2f;
    }

    public float getHalfHeight() {
        return getHeight() / 2f;
    }

    public float getHalfDepth() {
        return getDepth() / 2f;
    }
}
