package _Minecraft2.util;

import _Minecraft2.collision.AABB;
import _Minecraft2.world.World;
import _Minecraft2.world.block.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Outil de Raycast générique — utilisable avec n'importe quelle origine et direction.
 */
public class Raycast {

    public static class Result {
    	public final Block block;
        public final Vector3f hitPosition;
        public final Vector3f previousPosition;
        public final Vector3f hitFace;

        public Result(Block block, Vector3f hitPosition, Vector3f hitFace) {
            this.block = block;
            this.hitPosition = hitPosition;
			this.previousPosition = new Vector3f();
            this.hitFace = hitFace;
        }
    }
    
    public static boolean raycastDDABool(World world, Vector3f origin, Vector3f direction, float maxDistance) {
        Vector3f dir = new Vector3f(direction).normalize();

        int mapX = worldToGrid(origin.x);
        int mapY = worldToGrid(origin.y);
        int mapZ = worldToGrid(origin.z);

        float cellSize = 2.0f;

        float deltaDistX = (dir.x != 0) ? Math.abs(cellSize / dir.x) : Float.POSITIVE_INFINITY;
        float deltaDistY = (dir.y != 0) ? Math.abs(cellSize / dir.y) : Float.POSITIVE_INFINITY;
        float deltaDistZ = (dir.z != 0) ? Math.abs(cellSize / dir.z) : Float.POSITIVE_INFINITY;

        int stepX = (dir.x < 0) ? -1 : 1;
        int stepY = (dir.y < 0) ? -1 : 1;
        int stepZ = (dir.z < 0) ? -1 : 1;

        float currentGridWorldX = gridToWorld(mapX);
        float currentGridWorldY = gridToWorld(mapY);
        float currentGridWorldZ = gridToWorld(mapZ);

        float sideDistX = (dir.x < 0) ?
                (origin.x - currentGridWorldX) / -dir.x :
                (currentGridWorldX + cellSize - origin.x) / dir.x;

        float sideDistY = (dir.y < 0) ?
                (origin.y - currentGridWorldY) / -dir.y :
                (currentGridWorldY + cellSize - origin.y) / dir.y;

        float sideDistZ = (dir.z < 0) ?
                (origin.z - currentGridWorldZ) / -dir.z :
                (currentGridWorldZ + cellSize - origin.z) / dir.z;

        float deltaMax = maxDistance;

        while (true) {
            if (sideDistX < sideDistY && sideDistX < sideDistZ) {
                mapX += stepX;
                if (sideDistX > deltaMax) break;
                sideDistX += deltaDistX;
            } else if (sideDistY < sideDistZ) {
                mapY += stepY;
                if (sideDistY > deltaMax) break;
                sideDistY += deltaDistY;
            } else {
                mapZ += stepZ;
                if (sideDistZ > deltaMax) break;
                sideDistZ += deltaDistZ;
            }

            Block block = world.getBlockAt(mapX, mapY, mapZ);
            if (block != null && block.isSolid()) {
                return true; // on a touché un bloc !
            }
        }

        return false; // rien touché dans la distance max
    }
    
    
    public static Result raycastDDA(World world, Vector3f origin, Vector3f direction, float maxDistance) {
        // Normaliser la direction
        Vector3f dir = new Vector3f(direction).normalize();
        
        // Position actuelle dans la grille
        int mapX = worldToGrid(origin.x);
        int mapY = worldToGrid(origin.y);
        int mapZ = worldToGrid(origin.z);
        
        float cellSize = 2.0f;
        
        float deltaDistX = Math.abs(cellSize / dir.x);
        float deltaDistY = Math.abs(cellSize / dir.y);  
        float deltaDistZ = Math.abs(cellSize / dir.z);
        
        int stepX, stepY, stepZ;
        float sideDistX, sideDistY, sideDistZ;
        
        float currentGridWorldX = gridToWorld(mapX);
        if (dir.x < 0) {
            stepX = -1;
            sideDistX = (origin.x - currentGridWorldX) * deltaDistX / cellSize;
        } else {
            stepX = 1;
            sideDistX = (currentGridWorldX + cellSize - origin.x) * deltaDistX / cellSize;
        }
        
        float currentGridWorldY = gridToWorld(mapY);
        if (dir.y < 0) {
            stepY = -1;
            sideDistY = (origin.y - currentGridWorldY) * deltaDistY / cellSize;
        } else {
            stepY = 1;
            sideDistY = (currentGridWorldY + cellSize - origin.y) * deltaDistY / cellSize;
        }
        
        float currentGridWorldZ = gridToWorld(mapZ);
        if (dir.z < 0) {
            stepZ = -1;
            sideDistZ = (origin.z - currentGridWorldZ) * deltaDistZ / cellSize;
        } else {
            stepZ = 1;
            sideDistZ = (currentGridWorldZ + cellSize - origin.z) * deltaDistZ / cellSize;
        }
        
        // Effectuer le DDA
        boolean hit = false;
        int side = 0; // 0=X, 1=Y, 2=Z pour déterminer quelle face a été touchée
        float perpWallDist = 0;
        
        while (!hit) {
            if (sideDistX < sideDistY && sideDistX < sideDistZ) {
                sideDistX += deltaDistX;
                mapX += stepX;
                side = 0;
                perpWallDist = (gridToWorld(mapX) - origin.x + (cellSize * (1 - stepX)) / 2) / dir.x;
            } else if (sideDistY < sideDistZ) {
                sideDistY += deltaDistY;
                mapY += stepY;
                side = 1;
                perpWallDist = (gridToWorld(mapY) - origin.y + (cellSize * (1 - stepY)) / 2) / dir.y;
            } else {
                sideDistZ += deltaDistZ;
                mapZ += stepZ;
                side = 2;
                perpWallDist = (gridToWorld(mapZ) - origin.z + (cellSize * (1 - stepZ)) / 2) / dir.z;
            }
            
            if (perpWallDist > maxDistance) {
                break;
            }
            
            Block block = world.getBlockAt(mapX, mapY, mapZ);
            if (block != null && block.isSolid()) {
                hit = true;
                
                // Calculer le point d'intersection
                Vector3f intersectionPoint = new Vector3f(origin).fma(perpWallDist, dir);
                
                // Calculer la normale de la face touchée
                Vector3f faceNormal = new Vector3f();
                switch (side) {
                    case 0: // Face X
                        faceNormal.set(-stepX, 0, 0);
                        break;
                    case 1: // Face Y
                        faceNormal.set(0, -stepY, 0);
                        break;
                    case 2: // Face Z
                        faceNormal.set(0, 0, -stepZ);
                        break;
                }
                
                return new Result(block, intersectionPoint, faceNormal);
            }
        }
        
        return null;
    }

    // Fonctions utilitaires pour la conversion monde <-> grille
    private static int worldToGrid(float worldCoord) {
    	return (int)Math.floor((worldCoord + 1) / 2.0f);
    }

    private static float gridToWorld(int gridCoord) {
    	return gridCoord * 2.0f - 1;
    }

    
}


