package _Minecraft2;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;


import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;


public class Main {
    private long window;
    private Game game;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(800, 600, "Minecraft 2", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Window creation failed");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(win, true);
            }
        });

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.2f, 0.3f, 0.4f, 1.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);

        game = new Game(window);
        game.init();
    }

    private void loop() {
        double lastTime = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glLoadIdentity();

            game.update(deltaTime);
            game.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public static void drawCrosshair(long window) {
    	IntBuffer width  = BufferUtils.createIntBuffer(1);
    	IntBuffer height = BufferUtils.createIntBuffer(1);

    	glfwGetWindowSize(window, width, height);
    	

    	int windowWidth  = width.get(0);
    	int windowHeight = height.get(0);
    	
    	
        // Passer en mode orthographique (2D)
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, windowWidth, windowHeight, 0, -1, 1); // haut-gauche = (0,0)
        
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // Désactiver textures et profondeur
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        // Couleur du crosshair (blanc)
        glColor3f(1f, 1f, 1f);

        // Coordonnées du centre de l'écran
        int centerX = windowWidth / 2;
        int centerY = windowHeight / 2;
        int size = 5; // taille du demi crosshair

        // Dessiner les 2 lignes
        glBegin(GL_LINES);
            // Ligne horizontale
            glVertex2i(centerX - size, centerY);
            glVertex2i(centerX + size, centerY);
            // Ligne verticale
            glVertex2i(centerX, centerY - size);
            glVertex2i(centerX, centerY + size);
        glEnd();

        // Restaurer les matrices
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix(); // MODELVIEW
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }
}
