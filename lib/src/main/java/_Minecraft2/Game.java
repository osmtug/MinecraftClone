package _Minecraft2;

import org.lwjgl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;

import _Minecraft2.entity.Player;
import _Minecraft2.render.Camera;
import _Minecraft2.world.World;

public class Game {
	private final long window;

    private Player player;
    private World world;
    
    private double lastMouseX = 400, lastMouseY = 300;

    private boolean isInventoryOpen = false;
    private boolean wasEPressedLastFrame = false;
	
	public Game(long window) {
        this.window = window;
    }

    public void init() {
        GameInit.registerBlocks();
        player = new Player(window, 0f, 8f, 5f);
        world = new World(player);
        player.updateChunk(world, 0, 0, 0);
        world.generateFlatWorld();
        
        glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            glViewport(0, 0, width, height);
        });
    }

    public void update(float dt) {
        handleInput();

        if (!isInventoryOpen) {
            double[] xpos = new double[1], ypos = new double[1];
            glfwGetCursorPos(window, xpos, ypos);
            
            double mouseX = xpos[0];
            double mouseY = ypos[0];

            double dx = mouseX - lastMouseX;
            double dy = mouseY - lastMouseY;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            
            player.updateMouse(dx, dy, GameSettings.mouseSensitivity );
            world.update(dt);
        }
    }

    private void handleInput() {
        boolean ePressed = glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS;
        if (ePressed && !wasEPressedLastFrame) {
            isInventoryOpen = !isInventoryOpen;
            if (isInventoryOpen) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                
                int[] width = new int[1];
                int[] height = new int[1];
                glfwGetWindowSize(window, width, height);

                // Place le curseur au centre de la fenêtre
                glfwSetCursorPos(window, width[0] / 2.0, height[0] / 2.0);
            } else {
            	double[] xpos = new double[1], ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);
            	lastMouseX = xpos[0];
                lastMouseY = ypos[0];
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            }
        }
        wasEPressedLastFrame = ePressed;
    }

    public void render() {
    	setup3DProjection();
        Camera cam = player.getCamera();
        cam.applyView();
        world.render(cam, window);
        player.drawoutline();

        if (isInventoryOpen) {
            player.getInventory().render(window);
        } else {
            Main.drawCrosshair(window);
        }
    }
    
    private void setup3DProjection() {
        IntBuffer widthBuf = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuf = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, widthBuf, heightBuf);
        int width = widthBuf.get(0);
        int height = heightBuf.get(0);
        float aspect = (float) width / (float) height;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        float fov = 70f;
        float near = 0.1f;
        float far = 1000f;

        float yScale = (float) (1f / Math.tan(Math.toRadians(fov / 2f)));
        float xScale = yScale / aspect;
        float frustumLength = far - near;

        float[] perspectiveMatrix = {
            xScale, 0,      0,                               0,
            0,      yScale, 0,                               0,
            0,      0,     -((far + near) / frustumLength), -1,
            0,      0,     -((2 * near * far) / frustumLength), 0
        };

        glLoadMatrixf(perspectiveMatrix);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    

}
