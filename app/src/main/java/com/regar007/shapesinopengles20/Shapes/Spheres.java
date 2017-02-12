package com.regar007.shapesinopengles20.Shapes;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.regar007.shapesinopengles20.R;
import com.regar007.shapesinopengles20.Utils.RawResourceReader;
import com.regar007.shapesinopengles20.Utils.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by regar007.
 * This implementation make use of VBO's(vertex buffer objects) to draw cubes.
 * i.e., Instantiate once and draw always using just render() function.
 *
 * This class takes "Activity", "Blending(0 or 1)", steps is smoothness", Points in {x, y, z} order", "Colors in {r, g, b, a} order" and Radii in {r}.
 * Use(Once): aSpheres = new spheres(activity, 0, 50, new float{0, 0, 0}, new float{1, 0, 0, 1}, new float[]{.5f});
 * Note: Use(OnDrawFrame) call createBuffer() function with changed values.
 * render function takes "MVP Matrix to draw sphere/spheres".
 * Use(OnDrawFrame): aSpheres.render(mvpMatrix);
 */
public class Spheres {
    private static final int COORDS_PER_VERTEX = 3;
    private final int[] glSphereBuffer;
    private int BYTES_PER_FLOAT = 4;
    private int POSITION_DATA_SIZE = 3;
    private int COLOR_DATA_SIZE = 4;

    private final int aSphereProgramHandle;
    private boolean BLENDING;

    // Set color with red, green, blue and alpha (opacity) values
    private float[] _colors;
    private float[] _normals;
    private float[] _vertices;
    private int vertexCount = 0;
    private int indexCount = 0;
    private ShortBuffer indicesBuffer;
    private FloatBuffer _vertexBuffer;
    private FloatBuffer _colorBuffer;
    private int spheresPositionsLength;
    private int aSpheresVerticesBufferIdx;
    private int aSpheresColorsBufferIdx;
    private int aSphereIndicesBufferIdx;

    /**
     * Instantiate Sphere objects
     * @param activity
     * @param glTrue
     * @param steps
     * @param positions
     * @param colors
     * @param radii
     */
    public Spheres(Context activity, int glTrue, int steps, float[] positions, float[] colors, float[] radii){

        /** initialize the sphere program */
        final String sphereVS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.sphere_vertex_shader);
        final String sphereFS = RawResourceReader.readTextFileFromRawResource(activity, R.raw.sphere_fragment_shader);

        final int sphereVSHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, sphereVS);
        final int sphereFSHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, sphereFS);

        aSphereProgramHandle = ShaderHelper.createAndLinkProgram(sphereVSHandle, sphereFSHandle,
                new String[]{"a_Position", "a_Color"});

        // Second, copy these buffers into OpenGL's memory. After, we don't need to keep the client-side buffers around.
        glSphereBuffer = new int[4];
        GLES20.glGenBuffers(glSphereBuffer.length, glSphereBuffer, 0);

        BLENDING = (glTrue == GLES20.GL_TRUE) ? true : false;

        createBuffers(positions, colors, radii, steps);
    }

    /**
     * creates Sphere objects from given radius, smoothness in lat and long, and position
     * @param radius
     * @param _lats
     * @param _longs
     * @param position
     * @return
     */
    private float[] createSphere(double radius, int _lats, int _longs, float[] position){
        int i, j;
        int lats = _lats;
        int longs =_longs;
        float[] vertices = new float[lats*longs*6*3];

        int triIndex = 0;
        double sphereSize = -0.5; // -1 for half sphere
        for(i = 0; i < lats; i++) {
            double lat0 = Math.PI * (sphereSize + (double) (i) / lats);
            double z0  =  radius * Math.sin(lat0) + position[2];
            double zr0 =  Math.cos(lat0);

            double lat1 = Math.PI * (sphereSize + (double) (i+1) / lats);
            double z1 =  radius * Math.sin(lat1) + position[2];
            double zr1 = Math.cos(lat1);

            //glBegin(GL_QUAD_STRIP);
            for(j = 0; j < longs; j++) {
                double lng = 2 * Math.PI * (double) (j - 1) / longs;
                double x = radius * Math.cos(lng);
                double y = radius * Math.sin(lng);

                lng = 2 * Math.PI * (double) (j) / longs;
                double x1 = radius * Math.cos(lng);
                double y1 = radius * Math.sin(lng);

//                glNormal3f(x * zr0, y * zr0, z0);
//                glVertex3f(x * zr0, y * zr0, z0);
//                glNormal3f(x * zr1, y * zr1, z1);
//                glVertex3f(x * zr1, y * zr1, z1);

                /** store after calculating positions */
                float _x1 = (float)(x * zr0) + position[0];
                float _x2 = (float) (x * zr1) + position[0];
                float _x3 = (float) (x1 * zr0)+ position[0];
                float _x4 = (float)(x1 * zr1) +position[0];

                float _y1 = (float)(y * zr0)+ position[1];
                float _y2 = (float) (y * zr1)+ position[1];
                float _y3 = (float) (y1 * zr0)+ position[1];
                float _y4 = (float)(y1 * zr1) + position[1];

                // the first triangle
                vertices[triIndex * 9 + 0 ] = _x1;    vertices[triIndex * 9 + 1 ] = _y1;   vertices[triIndex * 9 + 2 ] = (float) z0;
                vertices[triIndex * 9 + 3 ] = _x2;    vertices[triIndex * 9 + 4 ] = _y2;   vertices[triIndex * 9 + 5 ] = (float) z1;
                vertices[triIndex * 9 + 6 ] = _x3;   vertices[triIndex * 9 + 7 ] = _y3;  vertices[triIndex * 9 + 8 ] = (float) z0;

                triIndex ++;
                vertices[triIndex * 9 + 0 ] = _x3;   vertices[triIndex * 9 + 1 ] = _y3;    vertices[triIndex * 9 + 2 ] = (float) z0;
                vertices[triIndex * 9 + 3 ] = _x2;    vertices[triIndex * 9 + 4 ] = _y2;     vertices[triIndex * 9 + 5 ] = (float) z1;
                vertices[triIndex * 9 + 6 ] = _x4;    vertices[triIndex * 9 + 7 ] = _y4;   vertices[triIndex * 9 + 8 ] = (float) z1;

//                vertices[triIndex*9 + 0 ] = (float)(x * zr0) -1;    vertices[triIndex*9 + 1 ] = (float)(y * zr0);   vertices[triIndex*9 + 2 ] = (float) z0;
//                vertices[triIndex*9 + 3 ] = (float)(x * zr1) -1;    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   vertices[triIndex*9 + 5 ] = (float) z1;
//                vertices[triIndex*9 + 6 ] = (float)(x1 * zr0) -1;   vertices[triIndex*9 + 7 ] = (float)(y1 * zr0);  vertices[triIndex*9 + 8 ] = (float) z0;
//
//                triIndex ++;
//                vertices[triIndex*9 + 0 ] = (float)(x1 * zr0) -1;   vertices[triIndex*9 + 1 ] = (float)(y1 * zr0);    vertices[triIndex*9 + 2 ] = (float) z0;
//                vertices[triIndex*9 + 3 ] = (float)(x * zr1) -1;    vertices[triIndex*9 + 4 ] = (float)(y * zr1);     vertices[triIndex*9 + 5 ] = (float) z1;
//                vertices[triIndex*9 + 6 ] = (float)(x1 * zr1) -1;    vertices[triIndex*9 + 7 ] = (float)(y1 * zr1);   vertices[triIndex*9 + 8 ] = (float) z1;

                // in this case, the normal is the same as the vertex, plus the normalization;
//                for (int kk = -9; kk<9 ; kk++) {
//                    normals[triIndex * 9 + kk] = vertices[triIndex * 9+kk];
//                    if((triIndex * 9 + kk)%3 == 2)
//                        colors[triIndex * 9 + kk] = 1;
//                    else
//                        colors[triIndex * 9 + kk] = 0;
//                }
                triIndex ++;
            }
            //glEnd();
        }
        return vertices;
    }

    /**
     * creates buffers for Sphere shape objects
     * @param spherePositions
     * @param sphereColors
     * @param sphereRadii
     * @param steps
     */
    public void createBuffers(float[] spherePositions,float[] sphereColors, float[] sphereRadii, int steps){
        int length = spherePositions.length;
        int colorLength = sphereColors.length;

        // there are lats*longs number of quads, each requires two triangles with six vertices, each vertex takes 3 floats;
        int lats = steps, longs = steps;
        _vertices = new float[lats*longs* 6 * POSITION_DATA_SIZE * (length/ POSITION_DATA_SIZE)];
        _colors = new float[lats*longs*6* COLOR_DATA_SIZE * (colorLength/ COLOR_DATA_SIZE)];

        try {
            int offset = 0;
            for(int i = 0; i < length/3; i ++) {
                int index = i * POSITION_DATA_SIZE;
                float[] vertices = createSphere((2 * sphereRadii[i]), lats, longs, new float[]{spherePositions[index], spherePositions[index+1], spherePositions[index+2]});
                System.arraycopy(vertices, 0,_vertices, offset, vertices.length);
                offset += vertices.length;
                int idx = i * COLOR_DATA_SIZE;
                final float[] currColor = new float[]{sphereColors[idx],sphereColors[idx+1],sphereColors[idx+2],sphereColors[idx+3]};
                for (int j = 0; j < vertices.length/POSITION_DATA_SIZE; j++){
                    System.arraycopy(currColor, 0, _colors, (((vertices.length* idx)/POSITION_DATA_SIZE) + (j * COLOR_DATA_SIZE)), currColor.length);
                }
            }
            vertexCount = (_vertices.length/COORDS_PER_VERTEX) * (length/POSITION_DATA_SIZE);

//        float[] buff1 = createSphere(radius, lats, longs, new float[]{-1,-1,-1});
//        float[] buff2 = createSphere(radius, lats, longs, new float[]{1,1,1});
//        float[] buff3 = createSphere(radius, lats, longs, new float[]{1,.9f,1});
//        vertexCount = (_vertices.length/COORDS_PER_VERTEX) * 3;
//        System.arraycopy(buff1, 0,_vertices, 0, buff1.length);
//        System.arraycopy(buff2, 0,_vertices, buff1.length, buff2.length);
//        System.arraycopy(buff3, 0,_vertices, buff1.length*2, buff3.length);

        }catch (Exception e){
            Log.d("Tag","error: "+ e);
        }

//        FloatBuffer tempVertexBuffer = createSphere((float) radius,lats,longs);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(_vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        _vertexBuffer = bb.asFloatBuffer();
        _vertexBuffer.put(_vertices);
        _vertexBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(_colors.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        _colorBuffer = bb2.asFloatBuffer();
        _colorBuffer.put(_colors);
        _colorBuffer.position(0);

        vertexCount = getVertexCount();
        spheresPositionsLength = spherePositions.length;
        FloatBuffer spheresVerticesBuffer = _vertexBuffer;
        FloatBuffer spheresColorsBuffer = _colorBuffer;
        ShortBuffer indicesBuffer = getIndicesBuffer();
        indexCount = getIndexCount();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glSphereBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, spheresVerticesBuffer.capacity() * BYTES_PER_FLOAT, spheresVerticesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glSphereBuffer[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, spheresColorsBuffer.capacity() * BYTES_PER_FLOAT, spheresColorsBuffer, GLES20.GL_STATIC_DRAW);

        if(indicesBuffer != null) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, glSphereBuffer[2]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * 2, indicesBuffer,
                    GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        aSpheresVerticesBufferIdx = glSphereBuffer[0];
        aSpheresColorsBufferIdx = glSphereBuffer[1];
        aSphereIndicesBufferIdx = glSphereBuffer[2];

        spheresColorsBuffer.limit(0);
        spheresColorsBuffer = null;
        spheresVerticesBuffer.limit(0);
        spheresVerticesBuffer = null;
        if(indicesBuffer != null) {
            indicesBuffer.limit(0);
        }
    }
    protected ShortBuffer getIndicesBuffer(){
        return indicesBuffer;
    }

    protected int getVertexCount(){
        return vertexCount;
    }

    protected int getIndexCount(){return indexCount;}

    /**
     * draws Sphere objects
     * @param aMVPMatrix
     */
    public void render(float[] aMVPMatrix) {

        if(BLENDING) {
            // Enable blending
            GLES20.glEnable(GLES20.GL_BLEND);
//            GLES20.glBlendFuncSeparate(GLES20.GL_ONE_MINUS_SRC_COLOR, GLES20.GL_ONE_MINUS_DST_COLOR, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE_MINUS_DST_ALPHA);
            GLES20.glBlendFunc( GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE_MINUS_DST_ALPHA);

//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(aSphereProgramHandle);

        int aSphereMVPMatrixHandle = GLES20.glGetUniformLocation(aSphereProgramHandle, "u_MVPMatrix");
        int aSpherePositionHandle = GLES20.glGetAttribLocation(aSphereProgramHandle, "a_Position");
        int aSphereColorHandle = GLES20.glGetAttribLocation(aSphereProgramHandle, "a_Color");

        // Pass in the combined matrix.
        //GLES20.glUniformMatrix4fv(aMVPMatrixHandle, 1, false, aMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(aSphereMVPMatrixHandle, 1, false, aMVPMatrix, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aSpheresVerticesBufferIdx);
        GLES20.glEnableVertexAttribArray(aSpherePositionHandle);
        GLES20.glVertexAttribPointer(aSpherePositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aSpheresColorsBufferIdx);
        GLES20.glEnableVertexAttribArray(aSphereColorHandle);
        GLES20.glVertexAttribPointer(aSphereColorHandle, COLOR_DATA_SIZE , GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw the vertices.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // draw the vertices using indices
//            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, aSphereIndicesBufferIdx);
//            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
//            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        if(BLENDING) {
            // Enable blending
            GLES20.glDisable(GLES20.GL_BLEND);
//            GLES20.glBlendFuncSeparate(GLES20.GL_ONE_MINUS_SRC_COLOR, GLES20.GL_ONE_MINUS_DST_COLOR, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE_MINUS_DST_ALPHA);

//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }

    public void release() {
        // Delete buffers from OpenGL's memory
        final int[] buffersToDelete = new int[] { aSpheresVerticesBufferIdx, aSpheresColorsBufferIdx};
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }

}