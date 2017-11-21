package com.regar007.shapesinopengles20.Shapes;

/**
 * Created by regar007.
 * This implementation make use of VBO's(vertex buffer objects) to draw cubes.
 * i.e., Instantiate once and draw always using just render() function.
 *
 * This class takes "Activity", "Points in {x1, x2 y1, y2, z1, z2} order" and "Colors in {r, g, b, a} order".
 * Use(Once): aHeightMap = new HeightMap(activity, new float{0, 0, 0, 1, 1, 1}, new float{1, 0, 0, 1, 0, 1, 0, 1});
 * Note: Use(OnDrawFrame) call createBuffer() function with changed values.
 * render function takes "MVP Matrix to draw point/points".
 * Use(OnDrawFrame): aCubes.render(mvpMatrix);
 */

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.regar007.shapesinopengles20.R;
import com.regar007.shapesinopengles20.Utils.GlUtil;
import com.regar007.shapesinopengles20.Utils.MathUtils;
import com.regar007.shapesinopengles20.Utils.RawResourceReader;
import com.regar007.shapesinopengles20.Utils.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class HeightMap {
    private final static String TAG = "HeightMap";
    public static boolean isActive = true;

    static float MIN_POSITION = -1.1f;
    static float POSITION_RANGE = 2.2f;
    static float AMPLITUDE_FACTOR = 5.0f;

    static final int[] vbo = new int[1];
    static final int[] ibo = new int[1];
    private final static int NORMAL_DATA_SIZE = 3;
    private final static int COLOR_DATA_SIZE = 4;
    private final static int BYTES_PER_FLOAT = 4;
    private final static int BYTES_PER_SHORT = 2;
    private final static int POSITION_DATA_SIZE = 3;
    private final int STRIDE = (POSITION_DATA_SIZE + NORMAL_DATA_SIZE + COLOR_DATA_SIZE)
            * BYTES_PER_FLOAT;
    private static float[] xzRangeValues;
    private static float[] aHeightMapVertexData;
    private static short[] aHeightMapIndexData;

    int indexCount;

    private int aPositionHandle;
    private int aNormalHandle;
    private int aColorHandle;
    private int aProgramHandle;
    private int aLightPosUniform;
    private int aMVPMatrixHandle;
    private int aMVMatrixHandle;

    public HeightMap(Context context, int xLen, int zLen, float plotRange, float plotMin) {
        int xLength = xLen;
        int zLength = zLen;
        MIN_POSITION = plotMin;
        POSITION_RANGE = plotRange;

        xzRangeValues = new float[xLength];

        initializeGLProgram(context);

        try {
            final int floatsPerVertex = POSITION_DATA_SIZE + NORMAL_DATA_SIZE
                    + COLOR_DATA_SIZE;

            aHeightMapVertexData = new float[xLength * zLength * floatsPerVertex];

            int offset = 0;
            int currZ = 0;

            // First, build the data for the vertex buffer
            for (int z = 0; z < zLength; z++) {
                for (int x = xLength; x > 0; x--) {
                    final float xRatio = x / (float) (xLength - 1);

                    // Build our heightmap from the top down, so that our triangles are counter-clockwise.
                    final float zRatio = 1f - (z / (float) (zLength - 1));

                    final float xPosition = MIN_POSITION + (xRatio * POSITION_RANGE);
                    final float zPosition = MIN_POSITION + (zRatio * POSITION_RANGE);

                    if(currZ == z) {
                        xzRangeValues[z] = zPosition;
                        currZ++;
                    }
                    // Position
                    aHeightMapVertexData[offset++] = xPosition;
                    aHeightMapVertexData[offset++] = 0.0f;//((xPosition * xPosition) + (zPosition * zPosition)) / 10f;
                    aHeightMapVertexData[offset++] = zPosition;
                    // Cheap normal using a derivative of the function.
                    // The slope for X will be 2X, for Z will be 2Z.
                    // Divide by 10 since the position's Y is also divided by 10.
                    final float xSlope = (2 * xPosition) / 1f;
                    final float zSlope = (2 * zPosition) / 1f;

                    // Calculate the normal using the cross product of the slopes.
                    final float[] planeVectorX = {1f, xSlope, 0f,};
                    final float[] planeVectorZ = {0f, zSlope, 1f};
                    final float[] normalVector = {
                            (planeVectorX[1] * planeVectorZ[2]) - (planeVectorX[2] * planeVectorZ[1]),
                            (planeVectorX[0] * planeVectorZ[1]) - (planeVectorX[1] * planeVectorZ[0]),
                            (planeVectorX[2] * planeVectorZ[0]) - (planeVectorX[0] * planeVectorZ[2]),
                    };

                    // Normalize the normal
                    final float length = Matrix.length(normalVector[0], normalVector[1], normalVector[2]);

                    aHeightMapVertexData[offset++] = normalVector[0] / length;
                    aHeightMapVertexData[offset++] = normalVector[1] / length;
                    aHeightMapVertexData[offset++] = normalVector[2] / length;

                    // Add some fancy colors.
                    float g = (float )(x)/(float)(xLength);
                    float b = (float )(z)/(float)(zLength);
                    aHeightMapVertexData[offset++] = 0.0f;
                    aHeightMapVertexData[offset++] = g;//1.0f;
                    aHeightMapVertexData[offset++] = b;//0.0f;
                    aHeightMapVertexData[offset++] = 1f;
                }
            }

            // Now build the index data
            final int numStripsRequired = zLength - 1;
            final int numDegensRequired = 2 * (numStripsRequired - 1);
            final int verticesPerStrip = 2 * xLength;

            aHeightMapIndexData = new short[(verticesPerStrip * numStripsRequired) + numDegensRequired];

            offset = 0;

            for (int z = 0; z < zLength - 1; z++) {
                if (z > 0) {
                    // Degenerate begin: repeat first vertex
                    aHeightMapIndexData[offset++] = (short) (z * zLength);
                }

                for (int x = 0; x < xLength; x++) {
                    // One part of the strip
                    aHeightMapIndexData[offset++] = (short) ((z * zLength) + x);
                    aHeightMapIndexData[offset++] = (short) (((z + 1) * zLength) + x);
                }

                if (z < zLength - 2) {
                    // Degenerate end: repeat last vertex
                    aHeightMapIndexData[offset++] = (short) (((z + 1) * zLength) + (xLength - 1));
                }
            }

            indexCount = aHeightMapIndexData.length;


        } catch (Throwable t) {
            Log.w(TAG, t);
        }
    }

    public static void pushDataPointsToHeightMap(float[] positions, float[] colors){
        final int floatsPerVertex = POSITION_DATA_SIZE + NORMAL_DATA_SIZE
                + COLOR_DATA_SIZE;
        try {
            for (int i = 0; i < positions.length; i = i + 3) {
                int posIdx = i;
                int xIdx = MathUtils.binarySearchNearest(xzRangeValues, (positions[posIdx]* POSITION_RANGE) + MIN_POSITION );
                int zIdx = MathUtils.binarySearchNearest(xzRangeValues, (positions[posIdx + 2] * POSITION_RANGE) + MIN_POSITION);

                int vertexIdx = floatsPerVertex * ((xIdx) + (zIdx * xzRangeValues.length));
                aHeightMapVertexData[vertexIdx + 1] =
                        (aHeightMapVertexData[vertexIdx + 1] + ((POSITION_RANGE/4) + ((positions[posIdx + 1] * POSITION_RANGE + MIN_POSITION ) / 2.0f))) * AMPLITUDE_FACTOR / 2.0f; // y axis
                aHeightMapVertexData[vertexIdx + POSITION_DATA_SIZE + NORMAL_DATA_SIZE] =
                        aHeightMapVertexData[vertexIdx + POSITION_DATA_SIZE + NORMAL_DATA_SIZE] + aHeightMapVertexData[vertexIdx + 1]; //red
                aHeightMapVertexData[vertexIdx + POSITION_DATA_SIZE + NORMAL_DATA_SIZE + 1] = 0.0f; //green
                //aHeightMapVertexData[vertexIdx + POSITION_DATA_SIZE + NORMAL_DATA_SIZE +2] = 0.0f; //blue
            }
            createBuffers();
        }catch (Exception e){
            Log.d(TAG,"data points integration failed!",e);
        }
    }

    public static void createBuffers() {
        final FloatBuffer heightMapVertexDataBuffer = ByteBuffer
                .allocateDirect(aHeightMapVertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        heightMapVertexDataBuffer.put(aHeightMapVertexData).position(0);

        final ShortBuffer heightMapIndexDataBuffer = ByteBuffer
                .allocateDirect(aHeightMapIndexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        heightMapIndexDataBuffer.put(aHeightMapIndexData).position(0);

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity()
                    * BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        } else {
            GlUtil.checkGlError("glGenBuffers");
        }
    }

    private void initializeGLProgram(Context context) {
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(context,
                R.raw.heightmap_vertex_shader);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(context,
                R.raw.heightmap_fragment_shader);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        aProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, new String[] {
                "a_Position", "a_Normal", "a_Color" });

        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);

        isActive = true;

    }

    public void render(float[] aMVPMatrix) {
        // Use culling to remove back faces.
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(aProgramHandle);

        // Set program handles for cube drawing.
        aMVPMatrixHandle = GLES20.glGetUniformLocation(aProgramHandle, "u_MVPMatrix");
        aMVMatrixHandle = GLES20.glGetUniformLocation(aProgramHandle, "u_MVMatrix");
        aLightPosUniform = GLES20.glGetUniformLocation(aProgramHandle, "u_LightPos");
        aPositionHandle = GLES20.glGetAttribLocation(aProgramHandle, "a_Position");
        aNormalHandle = GLES20.glGetAttribLocation(aProgramHandle, "a_Normal");
        aColorHandle = GLES20.glGetAttribLocation(aProgramHandle, "a_Color");

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(aMVPMatrixHandle, 1, false, aMVPMatrix, 0);

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

            // Bind Attributes
            GLES20.glVertexAttribPointer(aPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false,
                    STRIDE, 0);
            GLES20.glEnableVertexAttribArray(aPositionHandle);

            GLES20.glVertexAttribPointer(aNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false,
                    STRIDE, POSITION_DATA_SIZE * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(aNormalHandle);

            GLES20.glVertexAttribPointer(aColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false,
                    STRIDE, (POSITION_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(aColorHandle);


            // Draw
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    void release() {
        if (vbo[0] > 0) {
            GLES20.glDeleteBuffers(vbo.length, vbo, 0);
            vbo[0] = 0;
        }

        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(ibo.length, ibo, 0);
            ibo[0] = 0;
        }
    }
}
