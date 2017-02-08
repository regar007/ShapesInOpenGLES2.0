package com.regar007.shapesinopengles20.Shapes;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.regar007.shapesinopengles20.R;
import com.regar007.shapesinopengles20.Utils.GlUtil;
import com.regar007.shapesinopengles20.Utils.RawResourceReader;
import com.regar007.shapesinopengles20.Utils.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by regar007.
 * This implementation make use of VBO's(vertex buffer objects) to draw points.
 * i.e., Instantiate once and draw always using just render() function.
 *
 * This class takes "Activity", "Point in {x, y, z} order", "quad width", "quad height", and "quad depth".
 * Use(Once): aQuad = new Quad(activity, new float{-1, -1, -1}, 2, 2, 0); // depth is 0 to draw Qaud in XY Plane
 * Note: Use(OnDrawFrame) call createBuffer() function with changed values.
 * render function takes "MVP Matrix" and "Texture" to draw quad.
 * Use(OnDrawFrame): aQuad.render(mvpMatrix, texture);
 */
public class Quad {
    private final static String TAG = "Quad";

    private static final int width = 2;
    private static final int height = 2;

    private int[] qvbo = new int[1];
    private int[] qibo = new int[1];
    private final static int COLOR_DATA_SIZE = 4;
    private final static int BYTES_PER_FLOAT = 4;
    private final static int BYTES_PER_SHORT = 2;
    private final static int POSITION_DATA_SIZE = 3;
    private final static int UV_DATA_SIZE = 2;
    private final int STRIDE = (POSITION_DATA_SIZE + COLOR_DATA_SIZE + UV_DATA_SIZE)
            * BYTES_PER_FLOAT;
    private static float[] aQuadVertexData;
    private static short[] aQuadIndexData;
    private float[] pos;
    private float QUAD_WIDTH;
    private float QUAD_HEIGHT;
    private float QUAD_DEPTH;
    private QUAD_TYPE aQuadType;

    int indexCount;

    private int aPositionHandle;
    private int aColorHandle;
    private int aQuadProgramHandle;
    private int aMVPMatrixHandle;
    private int aTextureCoordinateHandle;
    private int aTextureUniformHandle;

    /**
     * instantiate the Quad shape object
     * @param activity
     * @param positions
     * @param widths
     */
    public Quad(Context activity, float[] positions, float[] widths) {

        final String vertexShader = RawResourceReader.readTextFileFromRawResource(activity,
                R.raw.quad_vertex_shader);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(activity,
                R.raw.quad_fragment_shader);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        aQuadProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, new String[] {
                "a_Position", "a_Color", "a_TexCoordinate" });

        GLES20.glGenBuffers(1, qvbo, 0);
        GLES20.glGenBuffers(1, qibo, 0);

        createBuffers(positions, widths);
    }

    /**
     * create single array having vertices, colors and uv coordinates.
     */
    private void createVertexData(float[] positions, float[] widths){
        final int floatsPerVertex = POSITION_DATA_SIZE + COLOR_DATA_SIZE + UV_DATA_SIZE;
        aQuadVertexData = new float[width * height * floatsPerVertex];

        // Now build the index data
        final int numStripsRequired = height - 1;
        final int numDegensRequired = 2 * (numStripsRequired - 1);
        final int verticesPerStrip = 2 * width;
        aQuadIndexData = new short[((verticesPerStrip * numStripsRequired) + numDegensRequired)];

        indexCount = aQuadIndexData.length;

        int offset = 0;
        pos = positions;
        QUAD_WIDTH = widths[0];
        QUAD_HEIGHT = widths[1];
        QUAD_DEPTH = widths[2];

        if(QUAD_WIDTH == 0 && QUAD_HEIGHT != 0 && QUAD_DEPTH != 0){
            aQuadType = QUAD_TYPE.YZ;
        }else if(QUAD_WIDTH != 0 && QUAD_HEIGHT == 0 && QUAD_DEPTH != 0){
            aQuadType = QUAD_TYPE.XZ;
        }else if(QUAD_WIDTH != 0 && QUAD_HEIGHT != 0 && QUAD_DEPTH == 0){
            aQuadType = QUAD_TYPE.XY;
        }else {
            Log.d(TAG, "Quad param are not correct!");
            return;
        }

        try {
            int uvCount = 0;

            // First, build the data for the vertex buffer
            for (int y = 0; y < height; y++){
                for (int x = width; x > 0; x--) {
                    final float xRatio = (x - 1f) / (float) (width - 1);

                    // Build our heightmap from the top down, so that our triangles are counter-clockwise.
                    final float yRatio = (y / (float) (height - 1));

                    // Position
                    if(aQuadType == QUAD_TYPE.XY) {
                        float _xPosition = pos[0] + (xRatio * QUAD_WIDTH);
                        float _yPosition = pos[1] + (yRatio * QUAD_HEIGHT);
                        aQuadVertexData[offset++] = _xPosition;
                        aQuadVertexData[offset++] = _yPosition;//((xPosition * xPosition) + (zPosition * zPosition)) / 10f;
                        aQuadVertexData[offset++] = pos[2];
                    }else if(aQuadType == QUAD_TYPE.YZ){
                        float _yPosition = pos[1] + (xRatio * QUAD_HEIGHT);
                        float _zPosition = pos[2] + (yRatio * QUAD_DEPTH);
                        aQuadVertexData[offset++] = pos[0];
                        aQuadVertexData[offset++] = _yPosition;//((xPosition * xPosition) + (zPosition * zPosition)) / 10f;
                        aQuadVertexData[offset++] = _zPosition;
                    }else {
                        float _xPosition = pos[0] + (xRatio * QUAD_WIDTH);
                        float _zPosition = pos[2] + (yRatio * QUAD_DEPTH);
                        aQuadVertexData[offset++] = _xPosition;
                        aQuadVertexData[offset++] = pos[1];//((xPosition * xPosition) + (zPosition * zPosition)) / 10f;
                        aQuadVertexData[offset++] = _zPosition;
                    }

                    // Add some fancy colors.
                    aQuadVertexData[offset++] = 0.0f;
                    aQuadVertexData[offset++] = 1.0f;
                    aQuadVertexData[offset++] = 0.0f;
                    aQuadVertexData[offset++] = 1f;

                    // Add uv coordinates.
                    if(uvCount == 0) {
                        aQuadVertexData[offset++] = 1.0f;
                        aQuadVertexData[offset++] = 1.0f;
                    }else if(uvCount == 1){
                        aQuadVertexData[offset++] = 0.0f;
                        aQuadVertexData[offset++] = 1.0f;
                    }else if(uvCount == 2){
                        aQuadVertexData[offset++] = 1.0f;
                        aQuadVertexData[offset++] = 0.0f;
                    }else if(uvCount == 3){
                        aQuadVertexData[offset++] = 0.0f;
                        aQuadVertexData[offset++] = 0.0f;
                    }
                    uvCount++;
                }
            }

            offset = 0;
            for (int y = 0; y < height - 1; y++) {
                if (y > 0) {
                    // Degenerate begin: repeat first vertex
                    aQuadIndexData[offset++] = (short) (y * height);
                }

                for (int x = 0; x < width; x++) {
                    // One part of the strip
                    aQuadIndexData[offset++] = (short) ((y * height) + x);
                    aQuadIndexData[offset++] = (short) (((y + 1) * height) + x);
                }

                if (y < height - 2) {
                    // Degenerate end: repeat last vertex
                    aQuadIndexData[offset++] = (short) (((y + 1) * height) + (width - 1));
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, t);
        }

    }

    /**
     * creates buffers for Quad shape object
     * @param pos
     * @param widths
     */
    public void createBuffers( float[] pos, float[] widths) {
        createVertexData(pos, widths);

        final FloatBuffer heightMapVertexDataBuffer = ByteBuffer
                .allocateDirect(aQuadVertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        heightMapVertexDataBuffer.put(aQuadVertexData).position(0);

        final ShortBuffer heightMapIndexDataBuffer = ByteBuffer
                .allocateDirect(aQuadIndexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        heightMapIndexDataBuffer.put(aQuadIndexData).position(0);

        if (qvbo[0] > 0 && qibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, qvbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, qibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity()
                    * BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        } else {
            GlUtil.checkGlError("glGenBuffers");
        }
    }

    /**
     * draws Quad shape object.
     * @param aMVPMatrix
     * @param texture
     */
    public void render(float[] aMVPMatrix, final int texture) {
//        // Use culling to remove back faces.
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(aQuadProgramHandle);

        // Set program handles for cube drawing.
        aMVPMatrixHandle = GLES20.glGetUniformLocation(aQuadProgramHandle, "u_MVPMatrix");
        aPositionHandle = GLES20.glGetAttribLocation(aQuadProgramHandle, "a_Position");
        aColorHandle = GLES20.glGetAttribLocation(aQuadProgramHandle, "a_Color");
        aTextureCoordinateHandle = GLES20.glGetAttribLocation(aQuadProgramHandle, "a_TexCoordinate");
        aTextureUniformHandle = GLES20.glGetUniformLocation(aQuadProgramHandle, "u_Texture");

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(aMVPMatrixHandle, 1, false, aMVPMatrix, 0);

        if (qvbo[0] > 0 && qibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, qvbo[0]);

            // Bind Attributes
            GLES20.glVertexAttribPointer(aPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false,
                    STRIDE, 0);
            GLES20.glEnableVertexAttribArray(aPositionHandle);

            GLES20.glVertexAttribPointer(aColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false,
                    STRIDE, (POSITION_DATA_SIZE) * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(aColorHandle);

            GLES20.glVertexAttribPointer(aTextureCoordinateHandle, UV_DATA_SIZE, GLES20.GL_FLOAT, false,
                    STRIDE, (POSITION_DATA_SIZE + COLOR_DATA_SIZE) * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(aTextureCoordinateHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES20.glUniform1f(aTextureUniformHandle, 0);

            // Draw
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, qibo[0]);
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

//        // Use culling to remove back faces.
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    /**
     * deletes buffers from OpenGL's memory.
     */
    void release() {
        if (qvbo[0] > 0) {
            GLES20.glDeleteBuffers(qvbo.length, qvbo, 0);
            qvbo[0] = 0;
        }

        if (qibo[0] > 0) {
            GLES20.glDeleteBuffers(qibo.length, qibo, 0);
            qibo[0] = 0;
        }
    }
}

enum QUAD_TYPE{
    XY,
    YZ,
    XZ,
}
