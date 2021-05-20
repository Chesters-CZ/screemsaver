package educanet;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Game {

    private static final float[] vertices = {
            0.6f, -0.4f, 0.0f, // 0 -> Top right
            0.6f, -0.6f, 0.0f, // 1 -> Bottom right
            0.4f, -0.6f, 0.0f, // 2 -> Bottom left
            0.4f, -0.4f, 0.0f, // 3 -> Top left
    };

    private static final float[] colors = {
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f,
    };

    private static final float[] textures = {
            1f, 0f,
            1f, 1f,
            0f, 1f,
            0f, 0f,
    };

    private static final int[] indices = {
            0, 1, 3, // First triangle
            1, 2, 3 // Second triangle
    };

    private static int way = 0; // 0↗ pak doprava */
    private static float squareX =  0.75f;
    private static float squareY = -0.25f;

    private static int squareVaoId;
    private static int squareVboId;
    private static int squareEboId;
    private static int colorsId;
    private static int uniformMatrixLocation;

    private static int textureId;
    private static int textureIndicesId;


    private static Matrix4f matrix = new Matrix4f()
            .identity()
            .translate(0.25f, 0.25f, 0.25f);
    // 4x4 -> FloatBuffer of size 16
    private static FloatBuffer matrixFloatBuffer = BufferUtils.createFloatBuffer(16);

    public static void init(long window) {
        // Setup shaders
        Shaders.initShaders();

        // Generate all the ids
        squareVaoId = GL33.glGenVertexArrays();
        squareVboId = GL33.glGenBuffers();
        squareEboId = GL33.glGenBuffers();
        colorsId = GL33.glGenBuffers();
        textureId = GL33.glGenBuffers();
        textureIndicesId = GL33.glGenBuffers();

        loadTexture();

        // Get uniform location
        uniformMatrixLocation = GL33.glGetUniformLocation(Shaders.shaderProgramId, "matrix");

        // Tell OpenGL we are currently using this object (vaoId)
        GL33.glBindVertexArray(squareVaoId);

        // Tell OpenGL we are currently writing to this buffer (eboId)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, squareEboId);
        IntBuffer ib = BufferUtils.createIntBuffer(indices.length)
                .put(indices)
                .flip();
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, ib, GL33.GL_STATIC_DRAW);

        // Change to VBOs...
        // Tell OpenGL we are currently writing to this buffer (vboId)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, squareVboId);

        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length)
                .put(vertices)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(0);

        // Clear the buffer from the memory (it's saved now on the GPU, no need for it here)
        MemoryUtil.memFree(fb);

        // Change to Color...
        // Tell OpenGL we are currently writing to this buffer (colorsId)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, colorsId);

        FloatBuffer cb = BufferUtils.createFloatBuffer(colors.length)
                .put(colors)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, textureIndicesId);

        FloatBuffer fit = BufferUtils.createFloatBuffer(textures.length)
                .put(textures)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fit, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(2);

        GL33.glUseProgram(Shaders.shaderProgramId);

        // Sending Mat4 to GPU
        matrix.get(matrixFloatBuffer);
        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);

        // Clear the buffer from the memory (it's saved now on the GPU, no need for it here)
        MemoryUtil.memFree(cb);
        MemoryUtil.memFree(fb);
    }

    public static void render(long window) {

        // Draw using the glDrawElements function
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
        GL33.glBindVertexArray(squareVaoId);
        GL33.glDrawElements(GL33.GL_TRIANGLES, indices.length, GL33.GL_UNSIGNED_INT, 0);
    }

    public static void update(long window) {
        //System.out.println("x = " + squareX + ", y = " + squareY);
        float offset = 0.1f;
        if (squareY >= 1 - offset || squareY <= -1 + offset || squareX >= 1 - offset || squareX <= -1 + offset) {
            way++;
            System.out.println("hit!");
        }
        switch (way % 4) {
            case 0 -> {
                matrix = matrix.translate(offset / Main.width, offset / Main.height, 0f);
                squareX += offset / Main.width;
                squareY += offset / Main.height;
            }
            case 1 -> {
                matrix = matrix.translate(offset / Main.width, -offset / Main.height, 0f);
                squareX += offset / Main.width;
                squareY += -offset / Main.height;
            }
            case 2 -> {
                matrix = matrix.translate(-offset / Main.width, -offset / Main.height, 0f);
                squareX += -offset / Main.width;
                squareY += -offset / Main.height;
            }
            case 3 -> {
                matrix = matrix.translate(-offset / Main.width, offset / Main.height, 0f);
                squareX += -offset / Main.width;
                squareY += offset / Main.height;
            }
            default -> System.out.println("o shit o fuck " + way % 4);
        }

        // TODO: Send to GPU only if position updated
        matrix.get(matrixFloatBuffer);
        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);


    }

    private static void loadTexture() {
        textureId = GL33.glGenTextures();

        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer width = stack.mallocInt(1);
        IntBuffer height = stack.mallocInt(1);
        IntBuffer comp = stack.mallocInt(1);    //neřeš


        ByteBuffer img = STBImage.stbi_load("resources/matrix/Untitled.png", width, height, comp, 4); //edit if alpha
        if (img != null) {
            img.flip();
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, width.get(), height.get(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, img); //edit if alpha
            GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);

            STBImage.stbi_image_free(img);
        } else System.out.println("fuck.");
        stack.close();
    }

}
