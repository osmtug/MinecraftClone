package _Minecraft2.render;

import static org.lwjgl.opengl.GL11.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private float x, y, z;        // Position
    private float pitch, yaw;     // Orientation (haut/bas, gauche/droite)
    public float renderDistance = 10;
    public float aspect;
    public float fov;
    public float near;
    public float top;
    public float right;
    public float bottom;
    public float left;
    

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = 0f;
        this.yaw = 0f;
        
        aspect = 800f / 600f;
        fov = 70.0f;
        near = 0.1f;
        renderDistance = 100f;

        top = (float) Math.tan(Math.toRadians(fov / 2)) * near;
        bottom = -top;
        right = top * aspect;
        left = -right;

        glFrustum(left, right, bottom, top, near, renderDistance);
    }

    public void applyView() {
        // Appliquer la rotation (haut/bas = pitch, gauche/droite = yaw)
        glRotatef(pitch, 1, 0, 0);
        glRotatef(yaw, 0, 1, 0);
        // Appliquer la translation (monde à l'envers de la caméra)
        glTranslatef(-x, -y, -z);
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setRotation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public void move(float dx, float dy, float dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public void rotate(float dpitch, float dyaw) {
        this.pitch += dpitch;
        this.yaw += dyaw;
    }
    
    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f();

        // Yaw = gauche/droite (rotation autour de l’axe Y)
        // Pitch = haut/bas (rotation autour de l’axe X)
        view.rotate((float)Math.toRadians(pitch), new Vector3f(1, 0, 0));
        view.rotate((float)Math.toRadians(yaw), new Vector3f(0, 1, 0));

        // Translate dans la direction opposée de la position
        view.translate(-x + 0, -y, -z);

        return view;
    }
    
    public Matrix4f getProjectionMatrix(float aspectRatio) {
        return new Matrix4f().perspective(
            (float) Math.toRadians(fov),
            aspectRatio,
            near,                         
            renderDistance
        );
    }
    
    public Vector3f getViewDirection() {
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        float xDir = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        float yDir = (float) -Math.sin(pitchRad);
        float zDir = (float) (-Math.cos(yawRad) * Math.cos(pitchRad));

        return new Vector3f(xDir, yDir, zDir).normalize();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
}
