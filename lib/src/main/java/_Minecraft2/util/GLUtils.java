package _Minecraft2.util;

import static org.lwjgl.opengl.GL11.*;

public class GLUtils {
	public static void perspective(float fovY, float aspect, float zNear, float zFar) {
	    float fH = (float) Math.tan(Math.toRadians(fovY / 2)) * zNear;
	    float fW = fH * aspect;
	    glFrustum(-fW, fW, -fH, fH, zNear, zFar);
	}

}

