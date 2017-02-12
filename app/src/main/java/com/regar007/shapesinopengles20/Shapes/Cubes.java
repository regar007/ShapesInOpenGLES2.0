package com.regar007.shapesinopengles20.Shapes;

import android.content.Context;
import android.opengl.GLES20;

import com.regar007.shapesinopengles20.R;
import com.regar007.shapesinopengles20.Utils.RawResourceReader;
import com.regar007.shapesinopengles20.Utils.ShaderHelper;
import com.regar007.shapesinopengles20.Utils.ShapeBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by regar007.
 * This implementation make use of VBO's(vertex buffer objects) to draw cubes.
 * i.e., Instantiate once and draw always using just render() function.
 *
 * This class takes "Activity", "Points in {x1, x2 y1, y2, z1, z2} order" and "Colors in {r, g, b, a} order".
 * Use(Once): aCubes = new Cubes(activity, new float{0, 0, 0, 1, 1, 1}, new float{1, 0, 0, 1, 0, 1, 0, 1});
 * Note: Use(OnDrawFrame) call createBuffer() function with changed values.
 * render function takes "MVP Matrix to draw point/points".
 * Use(OnDrawFrame): aCubes.render(mvpMatrix);
 */
public class Cubes {

    private final String Tag = "Cubes";

    private int POSITION_DATA_SIZE_PER_CUBE = 6;
    private int VERTEX_DATA_SIZE_PER_CUBE = 36;

    /** Size of the position data in elements. */
    static final int POSITION_DATA_SIZE = 3;

    /** Size of the normal data in elements. */
    static final int NORMAL_DATA_SIZE = 3;

    /** Size of the texture coordinate data in elements. */
    static final int TEXTURE_COORDINATE_DATA_SIZE = 2;

    /** How many bytes per float. */
    static final int BYTES_PER_FLOAT = 4;

    private int COLOR_DATA_SIZE = 4;

    // X, Y, Z
    // The normal is used in light calculations and is a vector which points
    // orthogonal to the plane of the surface. For a cube model, the normals
    // should be orthogonal to the points of each face.
    final float[] cubeNormalData =
            {
                    // Front face
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,

                    // Right face
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    // Back face
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    // Left face
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    // Top face
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    // Bottom face
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f
            };

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    final float[] cubeTextureCoordinateData =
            {
                    // Front face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Right face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Back face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Left face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Top face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Bottom face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f
            };
    private final int aCubesProgramHandle;
    private int aCubePositionsBufferIdx;
    private int aCubeNormalsBufferIdx;
    private int aCubeTexCoordsBufferIdx;
    private int aCubeColorBufferIdx;

    private int aPositionHandle;
    private int aNormalHandle;
    private int aColorHandle;
    private int aLightPosUniform;
    private int aMVPMatrixHandle;
    private int aMVMatrixHandle;
    private int vertexCount;
    private int aLightPosHandle;
    private int aTextureUniformHandle;
    private int aTextureCoordinateHandle;


    /**
     * Instantiate cube shape objects
     * @param activity
     * @param cubePositions
     * @param cubeColors
     */
    public Cubes(Context activity, float[] cubePositions, float[] cubeColors){
        /** initialize the cube program */
        final String cubeVS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.cube_vertex_shader);
        final String cubeFS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.cube_fragment_shader);

        final int cubeVSHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, cubeVS);
        final int cubeFSHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, cubeFS);

        aCubesProgramHandle = ShaderHelper.createAndLinkProgram(cubeVSHandle, cubeFSHandle,
                new String[] {"a_Position", "a_Color",  "a_Normal", "a_TexCoordinate"});

        if(cubePositions != null) {
            createBuffers(cubePositions, cubeColors);
        }
    }

    /**
     * create cube vertices from given boundary points
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param z1
     * @param z2
     * @return
     */
    protected float[] buildCube(float x1, float x2, float y1, float y2, float z1, float z2){
        final float[] p1p = { x1, y2, z2 };
        final float[] p2p = { x2, y2, z2 };
        final float[] p3p = { x1, y1, z2 };
        final float[] p4p = { x2, y1, z2 };
        final float[] p5p = { x1, y2, z1 };
        final float[] p6p = { x2, y2, z1 };
        final float[] p7p = { x1, y1, z1 };
        final float[] p8p = { x2, y1, z1 };

        final float[] thisCubePositionData = ShapeBuilder.generateCubeData(p1p, p2p, p3p, p4p, p5p, p6p, p7p, p8p,
                p1p.length);

        return thisCubePositionData;
    }

    /**
     * create buffers for cube shape objects
     * @param cubePositions
     * @param cubeColors
     */
    public void createBuffers(float[] cubePositions, float[] cubeColors) {

        vertexCount = cubePositions.length * VERTEX_DATA_SIZE_PER_CUBE;
        int noOfCubes = cubePositions.length/POSITION_DATA_SIZE_PER_CUBE;

        float[] cubePositionsData = new float[VERTEX_DATA_SIZE_PER_CUBE * POSITION_DATA_SIZE * noOfCubes];
        for(int k = 0; k < noOfCubes; k++){
            int idx = k * POSITION_DATA_SIZE_PER_CUBE;
            float[] cube = buildCube(cubePositions[idx],cubePositions[idx+1],cubePositions[idx+2],cubePositions[idx+3],cubePositions[idx+4],cubePositions[idx+5]);
            System.arraycopy(cube, 0, cubePositionsData, k * cube.length, cube.length);
        }

        final float[] colors = new float[COLOR_DATA_SIZE * noOfCubes * VERTEX_DATA_SIZE_PER_CUBE];
        for (int k = 0; k < noOfCubes; k++){
            int idx = k * COLOR_DATA_SIZE;
            float[] currColor = {cubeColors[idx], cubeColors[idx+1], cubeColors[idx+2], cubeColors[idx+3]};
            for(int j = 0; j < VERTEX_DATA_SIZE_PER_CUBE; j++){
                System.arraycopy(currColor, 0, colors, (VERTEX_DATA_SIZE_PER_CUBE * COLOR_DATA_SIZE * k) +(j * COLOR_DATA_SIZE), COLOR_DATA_SIZE);
            }
        }

        // First, copy cube information into client-side floating point buffers.
        FloatBuffer cubePositionsBuffer;
        FloatBuffer cubeNormalsBuffer;
        FloatBuffer cubeTextureCoordinatesBuffer;
        FloatBuffer cubeColorBuffer;

        cubePositionsBuffer = ByteBuffer.allocateDirect(cubePositionsData.length * BYTES_PER_FLOAT )
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubePositionsBuffer.put(cubePositionsData).position(0);

        cubeColorBuffer = ByteBuffer.allocateDirect(colors.length * BYTES_PER_FLOAT )
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubeColorBuffer.put(colors).position(0);

        cubeNormalsBuffer = ByteBuffer.allocateDirect(cubeNormalData.length * BYTES_PER_FLOAT * noOfCubes)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        cubeTextureCoordinatesBuffer = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * BYTES_PER_FLOAT * noOfCubes)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < (noOfCubes); i++) {
            cubeNormalsBuffer.put(cubeNormalData);
            cubeTextureCoordinatesBuffer.put(cubeTextureCoordinateData);
        }
        cubeNormalsBuffer.position(0);
        cubeTextureCoordinatesBuffer.position(0);

        // Second, copy these buffers into OpenGL's memory. After, we don't need to keep the client-side buffers around.
        final int buffers[] = new int[4];
        GLES20.glGenBuffers(buffers.length, buffers, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubePositionsBuffer.capacity() * BYTES_PER_FLOAT, cubePositionsBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeNormalsBuffer.capacity() * BYTES_PER_FLOAT, cubeNormalsBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[2]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeTextureCoordinatesBuffer.capacity() * BYTES_PER_FLOAT, cubeTextureCoordinatesBuffer,
                GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[3]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeColorBuffer.capacity() * BYTES_PER_FLOAT, cubeColorBuffer,
                GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        aCubePositionsBufferIdx = buffers[0];
        aCubeNormalsBufferIdx = buffers[1];
        aCubeTexCoordsBufferIdx = buffers[2];
        aCubeColorBufferIdx = buffers[3];

        cubePositionsBuffer.limit(0);
        cubePositionsBuffer = null;
        cubeNormalsBuffer.limit(0);
        cubeNormalsBuffer = null;
        cubeTextureCoordinatesBuffer.limit(0);
        cubeTextureCoordinatesBuffer = null;
        cubeColorBuffer.limit(0);
        cubeColorBuffer = null;

    }

    /**
     * draws cube shape objects
     * @param aMVPMatrix
     * @param texture
     */
    public void render(float[] aMVPMatrix, int texture) {
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(aCubesProgramHandle);

        // Set program handles for cube drawing.
        aMVPMatrixHandle = GLES20.glGetUniformLocation(aCubesProgramHandle, "u_MVPMatrix");
        aMVMatrixHandle = GLES20.glGetUniformLocation(aCubesProgramHandle, "u_MVMatrix");
        aLightPosHandle = GLES20.glGetUniformLocation(aCubesProgramHandle, "u_LightPos");
        int aColorHandle = GLES20.glGetAttribLocation(aCubesProgramHandle, "a_Color");
        int aUseColorHandle = GLES20.glGetUniformLocation(aCubesProgramHandle, "u_UseColor");
        aTextureUniformHandle = GLES20.glGetUniformLocation(aCubesProgramHandle, "u_Texture");
        aPositionHandle = GLES20.glGetAttribLocation(aCubesProgramHandle, "a_Position");
        aNormalHandle = GLES20.glGetAttribLocation(aCubesProgramHandle, "a_Normal");
        aTextureCoordinateHandle = GLES20.glGetAttribLocation(aCubesProgramHandle, "a_TexCoordinate");

        // Pass in the position information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aCubePositionsBufferIdx);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        // Pass in the normal information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aCubeColorBufferIdx);
        GLES20.glEnableVertexAttribArray(aColorHandle);
        GLES20.glVertexAttribPointer(aColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        // Pass in the normal information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aCubeNormalsBufferIdx);
        GLES20.glEnableVertexAttribArray(aNormalHandle);
        GLES20.glVertexAttribPointer(aNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        // Pass in the texture information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aCubeTexCoordsBufferIdx);
        GLES20.glEnableVertexAttribArray(aTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(aTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false,
                0, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1f(aTextureUniformHandle, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(aMVMatrixHandle, 1, false, aMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(aMVPMatrixHandle, 1, false, aMVPMatrix, 0);

        GLES20.glUniform1f(aUseColorHandle, 0.0f);
        // Pass in the color .
//            GLES20.glUniform4f(aColorHandle, Color.red(color), Color.green(color), Color.blue(color), 1);

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw the cubes.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
    }

    /**
     * deletes buffers from opneGL's memory
     */
    public void release() {
        // Delete buffers from OpenGL's memory
        final int[] buffersToDelete = new int[] { aCubePositionsBufferIdx, aCubeNormalsBufferIdx,
                aCubeTexCoordsBufferIdx, aCubeColorBufferIdx };
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }

}