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
 * This implementation uses VBOs(vertex buffer objects) to draw lines.
 * i.e., Instantiate once and draw always using just render() function.
 *
 * This class takes "Activity", " a Combination of two Points in {x1, y1, z1, x2, y2, z2} order" and "Colors in {r, g, b, a, r, g, b, a} order".
 * Use(Once): aLines = new Lines(activity, new float{-1, -1, -1, 1, 1, 1}, new float{1, 0, 0, 1, 0, 1, 0, 1});
 * Note: Use(OnDrawFrame) call createBuffer() function with changed values.
 * render function takes "MVP Matrix to draw point/points".
 * Use(OnDrawFrame): aPoint.render(mvpMatrix);
 */
public class Lines {
    private final String Tag = "Lines";
    static final int[] glLineBuffer = new int[2];

    private final int aLineProgramHandle;
    private int BYTES_PER_FLOAT = 4;
    private int POSITION_DATA_SIZE = 3;

    private int aLinePositionsBufferIdx;
    private int aLineColorsBufferIdx;
    private int vertexCount;

    /**
     * instantiate the Lines shape object
     * @param activity
     * @param positions
     * @param colors
     */
    public Lines(Context activity, float[] positions, float[] colors) {

        /** initialize the line program */
        final String lineVS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.line_vertex_shader);
        final String lineFS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.line_fragment_shader);

        final int lineVSHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, lineVS);
        final int lineFSHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, lineFS);

        aLineProgramHandle = ShaderHelper.createAndLinkProgram(lineVSHandle, lineFSHandle,
                new String[]{"a_Position", "a_Color"});

        // Second, copy these buffers into OpenGL's memory. After, we don't need to keep the client-side buffers around.
        GLES20.glGenBuffers(glLineBuffer.length, glLineBuffer, 0);

        createBuffers(positions, colors);
    }

    /**
     * create buffers for the Lines shape object
     * @param linePositions
     * @param lineColors
     */
    public void createBuffers(float[] linePositions, float[] lineColors) {
        final int lineDataLength = linePositions.length;
        vertexCount = linePositions.length / POSITION_DATA_SIZE;

        final FloatBuffer lineBuffer = ByteBuffer.allocateDirect(lineDataLength * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        lineBuffer.put(linePositions);
        final FloatBuffer colorBuffer = ByteBuffer.allocateDirect(lineColors.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(lineColors);

        lineBuffer.position(0);
        colorBuffer.position(0);

        FloatBuffer linePositionsBuffer = lineBuffer;
        FloatBuffer lineColorsBuffers = colorBuffer;

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glLineBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, linePositionsBuffer.capacity() * BYTES_PER_FLOAT, linePositionsBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glLineBuffer[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, lineColorsBuffers.capacity() * BYTES_PER_FLOAT, lineColorsBuffers, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        aLinePositionsBufferIdx = glLineBuffer[0];
        aLineColorsBufferIdx = glLineBuffer[1];

        linePositionsBuffer.limit(0);
        linePositionsBuffer = null;
        lineColorsBuffers.limit(0);
        lineColorsBuffers = null;
    }

    /**
     * draws the Lines shape object
     * @param aMVPMatrix
     */
    public void render(float[] aMVPMatrix) {

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(aLineProgramHandle);

        int aLineMVPMatrixHandle = GLES20.glGetUniformLocation(aLineProgramHandle, "u_MVPMatrix");
        int aLinePositionHandle = GLES20.glGetAttribLocation(aLineProgramHandle, "a_Position");
        int aLineColorHandle = GLES20.glGetAttribLocation(aLineProgramHandle, "a_Color");

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(aLineMVPMatrixHandle, 1, false, aMVPMatrix, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aLinePositionsBufferIdx);
        GLES20.glEnableVertexAttribArray(aLinePositionHandle);
        GLES20.glVertexAttribPointer(aLinePositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aLineColorsBufferIdx);
        GLES20.glEnableVertexAttribArray(aLineColorHandle);
        GLES20.glVertexAttribPointer(aLineColorHandle, 4, GLES20.GL_FLOAT, false, 0, 0);

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw the line.
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

    }

    /**
     * Delete buffers from OpenGL's memory
     */
    public void release() {
        // Delete buffers from OpenGL's memory
        final int[] buffersToDelete = new int[] { aLineColorsBufferIdx, aLinePositionsBufferIdx };
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }

}
