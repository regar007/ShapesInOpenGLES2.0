package com.regar007.shapesinopengles20.Shapes;

import android.content.Context;
import android.opengl.GLES20;

import com.regar007.shapesinopengles20.R;
import com.regar007.shapesinopengles20.Utils.RawResourceReader;
import com.regar007.shapesinopengles20.Utils.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by regar007.
 * This implementation uses VBOs(vertex buffer objects) to draw triangles.
 * i.e., Instantiate once and draw always using just render() function.
 *
 * This class takes "Activity", " a Combination of three Points in {x1, y1, z1, x2, y2, z2, x3, y3, z3} order" and "Colors in {r, g, b, a, r, g, b, a, r, g, b, a} order".
 * Use(Once): aTriangles = new Triangles(activity, new float{-1, -1, -1, 1, -1, -1, 1, 1, 1}, new float{1, 0, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0 };
 * Note: Use(OnDrawFrame) call createBuffer() function with changed values.
 * render function takes "MVP Matrix to draw triangle/triangles".
 * Use(OnDrawFrame): aTriangles.render(mvpMatrix);
 */
public class Triangles {
    private final String Tag = "Triangles";
    static final int[] glTriangleBuffer = new int[2];

    private final int aTriangleProgramHandle;

    private int BYTES_PER_FLOAT = 4;
    private int POSITION_DATA_SIZE = 3;
    private int COLOR_POSITION_DATA = 4;

    private int aTrianglePositionsBufferIdx;
    private int aTriangleColorsBufferIdx;
    private int vertexCount;
    private int aMVPMatrixHandle;
    private int aPositionHandle;
    private int aColorHandle;

    /**
     * instantiate the Triangle shape object
     * @param activity
     * @param positions
     * @param colors
     */
    public Triangles(Context activity, float[] positions, float[] colors) {

        /** initialize the line program */
        final String lineVS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.triangle_vertex_shader);
        final String lineFS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.triangle_fragment_shader);

        final int lineVSHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, lineVS);
        final int lineFSHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, lineFS);

        aTriangleProgramHandle = ShaderHelper.createAndLinkProgram(lineVSHandle, lineFSHandle,
                new String[]{"a_Position", "a_Color"});

        // Second, copy these buffers into OpenGL's memory. After, we don't need to keep the client-side buffers around.
        GLES20.glGenBuffers(glTriangleBuffer.length, glTriangleBuffer, 0);

        createBuffers(positions, colors);

    }

    /**
     * creates buffers for Triangle shape object
     * @param positions
     * @param colors
     */
    public void createBuffers(float[] positions, float[] colors) {
        FloatBuffer aTriangleVerticesBuffer;
        FloatBuffer aTriangleColorBuffer;

        vertexCount = positions.length/POSITION_DATA_SIZE;

        // Initialize the buffers.
        aTriangleVerticesBuffer = ByteBuffer.allocateDirect(positions.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        aTriangleColorBuffer = ByteBuffer.allocateDirect(colors.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        aTriangleVerticesBuffer.put(positions).position(0);
        aTriangleColorBuffer.put(colors).position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glTriangleBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, aTriangleVerticesBuffer.capacity() * BYTES_PER_FLOAT, aTriangleVerticesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glTriangleBuffer[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, aTriangleColorBuffer.capacity() * BYTES_PER_FLOAT, aTriangleColorBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        aTrianglePositionsBufferIdx = glTriangleBuffer[0];
        aTriangleColorsBufferIdx = glTriangleBuffer[1];

        aTriangleVerticesBuffer.limit(0);
        aTriangleVerticesBuffer = null;
        aTriangleColorBuffer.limit(0);
        aTriangleColorBuffer = null;
    }

    /**
     * draws the Triangles shape object
     * @param aMVPMatrix
     */
    public void render(float[] aMVPMatrix){
        // disable culling to enable back faces.
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(aTriangleProgramHandle);

        // Set program handles. These will later be used to pass in values to the program.
        aMVPMatrixHandle = GLES20.glGetUniformLocation(aTriangleProgramHandle, "u_MVPMatrix");
        aPositionHandle = GLES20.glGetAttribLocation(aTriangleProgramHandle, "a_Position");
        aColorHandle = GLES20.glGetAttribLocation(aTriangleProgramHandle, "a_Color");

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(aMVPMatrixHandle, 1, false, aMVPMatrix, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aTrianglePositionsBufferIdx);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aTriangleColorsBufferIdx);
        GLES20.glEnableVertexAttribArray(aColorHandle);
        GLES20.glVertexAttribPointer(aColorHandle, COLOR_POSITION_DATA, GLES20.GL_FLOAT, false, 0, 0);

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw the triangle.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

    }

    /**
     * Delete buffers from OpenGL's memory
     */
    public void release() {
        // Delete buffers from OpenGL's memory
        final int[] buffersToDelete = new int[] { aTrianglePositionsBufferIdx, aTriangleColorsBufferIdx};
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }

}
