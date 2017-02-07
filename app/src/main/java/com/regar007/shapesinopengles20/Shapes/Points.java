package com.regar007.shapesinopengles20.Shapes;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.regar007.shapesinopengles20.Utils.RawResourceReader;
import com.regar007.shapesinopengles20.Utils.ShaderHelper;
import com.regar007.shapesinopengles20.R;

/**
 * Created by regar007.
 * This implementation make use of VBO's(vertex buffer objects) to draw points. 
 * i.e., Instantiate once and draw always using just render() function.  
 *
 * This class takes "Activity", "Points in {x, y, z} order" and "Colors in {r, g, b, a} order".
 * Use(Once): aPoint = new Points(activity, new float{0, 0, 0, 1, 1, 1}, new float{1, 0, 0, 1, 0, 1, 0, 1});
 * Note: Use(OnDrawFrame) call createBuffer() function with changed values.
 * render function takes "MVP Matrix to draw point/points".
 * Use(OnDrawFrame): aPoint.render(mvpMatrix);
 */
public class Points {
    private final String Tag = "Points";

    /** How many bytes per float. */
    static final int BYTES_PER_FLOAT = 4;

    /** Size of the position data in elements. */
    static final int POSITION_DATA_SIZE = 3;

    static final int[] glPointBuffer = new int[3];

    /** Size of the color data in elements. */
    private int COLOR_DATA_SIZE = 4;
    
    private final int aPointProgramHandle;

    private int aPointPositionsBufferIdx;
    private int aPointColorsBufferIdx;

    private int vertexCount;

    private int aPositionHandle;
    private int aColorHandle;
    private int aMVPMatrixHandle;

    /**
     * instantiate the Points shape object
     * @param aActivity
     * @param positions
     * @param colors
     */
    public Points(Context aActivity, float[] positions, float[] colors){

        /** initialize the point program */
        final String pointVS = RawResourceReader.readTextFileFromRawResource(aActivity, R.raw.point_vertex_shader);
        final String pointFS = RawResourceReader.readTextFileFromRawResource(aActivity, R.raw.point_fragment_shader);

        final int pointVSHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVS);
        final int pointFSHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFS);

        aPointProgramHandle = ShaderHelper.createAndLinkProgram(pointVSHandle, pointFSHandle,
                new String[]{"a_Position", "a_Color"});

        // Second, copy these buffers into OpenGL's memory. After, we don't need to keep the client-side buffers around.
        GLES20.glGenBuffers(glPointBuffer.length, glPointBuffer, 0);

        createBuffers(positions, colors);
    }

    /**
     * create buffers for the Points shape object
     * @param pointPositions
     * @param pointColors
     */
    public void createBuffers(float[] pointPositions, float[] pointColors) {
        // First, copy cube information into client-side floating point buffers.
        FloatBuffer pointPositionsBuffer;
        FloatBuffer pointColorsBuffer;

        try{
            vertexCount = pointPositions.length / POSITION_DATA_SIZE;

            pointPositionsBuffer = ByteBuffer.allocateDirect(pointPositions.length * BYTES_PER_FLOAT)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            pointPositionsBuffer.put(pointPositions).position(0);

            pointColorsBuffer = ByteBuffer.allocateDirect(pointColors.length * BYTES_PER_FLOAT)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            pointColorsBuffer.put(pointColors).position(0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glPointBuffer[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pointPositionsBuffer.capacity() * BYTES_PER_FLOAT, pointPositionsBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glPointBuffer[1]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pointColorsBuffer.capacity() * BYTES_PER_FLOAT, pointColorsBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            aPointPositionsBufferIdx = glPointBuffer[0];
            aPointColorsBufferIdx = glPointBuffer[1];

            pointPositionsBuffer.limit(0);
            pointPositionsBuffer = null;
            pointColorsBuffer.limit(0);
            pointColorsBuffer = null;
        }catch (Exception e){
            Log.d(Tag,"point buffer creation failed:", e);
        }
    }

    /**
     * draws the Points shape object
     * @param aMVPMatrix
     */
    public void render(float[] aMVPMatrix) {

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(aPointProgramHandle);

        aMVPMatrixHandle = GLES20.glGetUniformLocation(aPointProgramHandle, "u_MVPMatrix");
        aPositionHandle = GLES20.glGetAttribLocation(aPointProgramHandle, "a_Position");
        aColorHandle = GLES20.glGetAttribLocation(aPointProgramHandle, "a_Color");

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(aMVPMatrixHandle, 1, false, aMVPMatrix, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aPointPositionsBufferIdx);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aPointColorsBufferIdx);
        GLES20.glEnableVertexAttribArray(aColorHandle);
        GLES20.glVertexAttribPointer(aColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);
    }

    /**
     * Delete buffers from OpenGL's memory
     */
    public void release() {
        // Delete buffers from OpenGL's memory
        final int[] buffersToDelete = new int[] { aPointPositionsBufferIdx, aPointColorsBufferIdx };
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }

}
