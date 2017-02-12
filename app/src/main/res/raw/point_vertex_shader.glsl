uniform mat4 u_MVPMatrix;      		

attribute vec4 a_Position;
attribute vec4 a_Color;

varying vec4 v_Color;   // This will be passed into the fragment shader.


void main()                    
{
    v_Color = a_Color;

	gl_Position =  u_MVPMatrix * a_Position;
    gl_PointSize = 50.0;
}