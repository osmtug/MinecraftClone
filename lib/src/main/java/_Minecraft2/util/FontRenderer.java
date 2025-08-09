package _Minecraft2.util;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class FontRenderer {

    public final static int BITMAP_W = 512;
    public final static int BITMAP_H = 512;

    private static STBTTBakedChar.Buffer charData;
    private static int fontTex;
    private static int shaderProgram;
    private static int uniColor;
    private static int uniTex;
    private static int uniProjection;
    private static boolean isInit = false;
    
    // VAO/VBO pour le rendu moderne
    private static int VAO;
    private static int VBO;

    public static void init() {
        if (isInit) return;
        isInit = true;

        // Charger la police en mémoire
        ByteBuffer ttfBuffer;
        try {
            ttfBuffer = loadFileToByteBuffer("assets/textures/font/Minecraft.ttf");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font file", e);
        }

        // Créer le bitmap des glyphes
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        charData = STBTTBakedChar.malloc(96); // 96 glyphes (ASCII 32-127)
        int result = STBTruetype.stbtt_BakeFontBitmap(ttfBuffer, 32, bitmap, BITMAP_W, BITMAP_H, 32, charData);
        if (result <= 0) {
            throw new RuntimeException("Failed to bake font bitmap");
        }

        // Créer la texture OpenGL
        fontTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_W, BITMAP_H, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Compiler le shader
        shaderProgram = createShaderProgram();
        glUseProgram(shaderProgram);
        uniColor = glGetUniformLocation(shaderProgram, "textColor");
        uniTex = glGetUniformLocation(shaderProgram, "fontTex");
        uniProjection = glGetUniformLocation(shaderProgram, "projection");
        glUniform1i(uniTex, 0);
        glUseProgram(0);

        // Créer VAO/VBO
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        
        // Position (location 0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // Texture coords (location 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        glBindVertexArray(0);
    }

    private static int createShaderProgram() {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, """
            #version 330 core
            layout(location = 0) in vec2 position;
            layout(location = 1) in vec2 texCoord;
            out vec2 fragUV;
            uniform mat4 projection;
            void main() {
                gl_Position = projection * vec4(position, 0.0, 1.0);
                fragUV = texCoord;
            }
            """);
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, """
            #version 330 core
            in vec2 fragUV;
            out vec4 fragColor;
            uniform sampler2D fontTex;
            uniform vec3 textColor;
            void main() {
                float alpha = texture(fontTex, fragUV).r;
                fragColor = vec4(textColor, alpha);
            }
            """);
        glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        checkCompileErrors(program, "PROGRAM");

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }

    private static void checkCompileErrors(int shader, String type) {
        int success;
        if (type.equals("PROGRAM")) {
            success = glGetProgrami(shader, GL_LINK_STATUS);
            if (success == GL_FALSE) {
                String infoLog = glGetProgramInfoLog(shader);
                throw new RuntimeException("Shader program linking error:\n" + infoLog);
            }
        } else {
            success = glGetShaderi(shader, GL_COMPILE_STATUS);
            if (success == GL_FALSE) {
                String infoLog = glGetShaderInfoLog(shader);
                throw new RuntimeException(type + " shader compilation error:\n" + infoLog);
            }
        }
    }

    // Version avec coordonnées en pixels
    public static void drawTextInPixels(String text, float pixelX, float pixelY, Vector3f color, float windowWidth, float windowHeight) {
        if (!isInit) init();

        // Matrice de projection orthographique pour les pixels
        float[] projMatrix = new float[16];
        // Orthographic: left, right, bottom, top, near, far
        createOrthoMatrix(projMatrix, 0, windowWidth, windowHeight, 0, -1, 1);

        drawTextWithProjection(text, pixelX, pixelY, color, projMatrix);
    }

    // Version avec coordonnées normalisées (-1 à 1)
    public static void drawText(String text, float x, float y, Vector3f color) {
        if (!isInit) init();

        // Matrice identité pour les coordonnées normalisées
        float[] identityMatrix = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };

        drawTextWithProjection(text, x, y, color, identityMatrix);
    }

    private static void drawTextWithProjection(String text, float startX, float startY, Vector3f color, float[] projMatrix) {
        // Active blending alpha
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(shaderProgram);
        glUniform3f(uniColor, color.x, color.y, color.z);
        glUniformMatrix4fv(uniProjection, false, projMatrix);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontTex);

        // Construire les vertices
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuf = stack.floats(startX);
            FloatBuffer yBuf = stack.floats(startY);

            // Calculer le nombre de caractères valides
            int validChars = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c >= 32 && c < 128) validChars++;
            }

            if (validChars == 0) return;

            // Chaque quad = 6 vertices (2 triangles), chaque vertex = 4 floats (x,y,u,v)
            FloatBuffer vertices = BufferUtils.createFloatBuffer(validChars * 6 * 4);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
                STBTruetype.stbtt_GetBakedQuad(charData, BITMAP_W, BITMAP_H, c - 32, xBuf, yBuf, q, true);

                // Premier triangle du quad
                vertices.put(q.x0()).put(q.y0()).put(q.s0()).put(q.t0());
                vertices.put(q.x1()).put(q.y0()).put(q.s1()).put(q.t0());
                vertices.put(q.x1()).put(q.y1()).put(q.s1()).put(q.t1());

                // Deuxième triangle du quad
                vertices.put(q.x0()).put(q.y0()).put(q.s0()).put(q.t0());
                vertices.put(q.x1()).put(q.y1()).put(q.s1()).put(q.t1());
                vertices.put(q.x0()).put(q.y1()).put(q.s0()).put(q.t1());
            }

            vertices.flip();

            // Upload vertices to GPU
            glBindVertexArray(VAO);
            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

            // Dessiner
            glDrawArrays(GL_TRIANGLES, 0, vertices.limit() / 4);

            glBindVertexArray(0);
        }

        glUseProgram(0);
        glDisable(GL_BLEND);
    }

    // Créer une matrice orthographique
    private static void createOrthoMatrix(float[] matrix, float left, float right, float bottom, float top, float near, float far) {
        // Initialiser à zéro
        for (int i = 0; i < 16; i++) matrix[i] = 0;

        matrix[0] = 2.0f / (right - left);
        matrix[5] = 2.0f / (top - bottom);
        matrix[10] = -2.0f / (far - near);
        matrix[12] = -(right + left) / (right - left);
        matrix[13] = -(top + bottom) / (top - bottom);
        matrix[14] = -(far + near) / (far - near);
        matrix[15] = 1.0f;
    }

    // Charge un fichier en ByteBuffer
    public static ByteBuffer loadFileToByteBuffer(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }

    // Nettoyage des ressources
    public static void cleanup() {
        if (charData != null) {
            charData.free();
        }
        if (fontTex != 0) {
            glDeleteTextures(fontTex);
        }
        if (shaderProgram != 0) {
            glDeleteProgram(shaderProgram);
        }
        if (VAO != 0) {
            glDeleteVertexArrays(VAO);
        }
        if (VBO != 0) {
            glDeleteBuffers(VBO);
        }
        isInit = false;
    }
    
    private static void drawTextWithProjectionAndScale(String text, float startX, float startY, Vector3f color, float[] projMatrix, float scale) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(shaderProgram);
        glUniform3f(uniColor, color.x, color.y, color.z);
        glUniformMatrix4fv(uniProjection, false, projMatrix);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontTex);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuf = stack.floats(startX);
            FloatBuffer yBuf = stack.floats(startY);

            int validChars = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c >= 32 && c < 128) validChars++;
            }

            if (validChars == 0) return;

            FloatBuffer vertices = BufferUtils.createFloatBuffer(validChars * 6 * 4);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
                STBTruetype.stbtt_GetBakedQuad(charData, BITMAP_W, BITMAP_H, c - 32, xBuf, yBuf, q, true);

                // Appliquer le scale
                float x0 = startX + (q.x0() - startX) * scale;
                float y0 = startY + (q.y0() - startY) * scale;
                float x1 = startX + (q.x1() - startX) * scale;
                float y1 = startY + (q.y1() - startY) * scale;

                // Premier triangle
                vertices.put(x0).put(y0).put(q.s0()).put(q.t0());
                vertices.put(x1).put(y0).put(q.s1()).put(q.t0());
                vertices.put(x1).put(y1).put(q.s1()).put(q.t1());

                // Deuxième triangle
                vertices.put(x0).put(y0).put(q.s0()).put(q.t0());
                vertices.put(x1).put(y1).put(q.s1()).put(q.t1());
                vertices.put(x0).put(y1).put(q.s0()).put(q.t1());
            }

            vertices.flip();

            glBindVertexArray(VAO);
            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

            glDrawArrays(GL_TRIANGLES, 0, vertices.limit() / 4);

            glBindVertexArray(0);
        }

        glUseProgram(0);
        glDisable(GL_BLEND);
    }
    
    public static void drawTextInPixels(String text, float pixelX, float pixelY, Vector3f color, float windowWidth, float windowHeight, float scale) {
        if (!isInit) init();

        // Matrice de projection orthographique pour les pixels
        float[] projMatrix = new float[16];
        createOrthoMatrix(projMatrix, 0, windowWidth, windowHeight, 0, -1, 1);

        drawTextWithProjectionAndScale(text, pixelX, pixelY, color, projMatrix, scale);
    }
    
    public static void drawTextInPixelsRightAligned(String text, float pixelX, float pixelY, Vector3f color, float windowWidth, float windowHeight, float scale) {
        if (!isInit) init();

        // Calcul de la largeur totale du texte en pixels (avec le scale)
        float totalWidth = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuf = stack.floats(0f);
            FloatBuffer yBuf = stack.floats(0f);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
                STBTruetype.stbtt_GetBakedQuad(charData, BITMAP_W, BITMAP_H, c - 32, xBuf, yBuf, q, true);
            }
            totalWidth = xBuf.get(0) * scale; // xBuf contient la position X après le dernier char
        }

        // Ajuster pixelX pour qu'il soit le bord gauche après décalage
        float startX = pixelX - totalWidth;

        // Matrice de projection orthographique
        float[] projMatrix = new float[16];
        createOrthoMatrix(projMatrix, 0, windowWidth, windowHeight, 0, -1, 1);

        // Dessiner
        drawTextWithProjectionAndScale(text, startX, pixelY, color, projMatrix, scale);
    }

}
