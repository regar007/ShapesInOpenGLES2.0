precision mediump float;       	// Set the default precision to medium. We don't need as high of a 
								// precision in the fragment shader.
uniform sampler2D u_Texture;    // The input texture.

varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec4 v_Color;          	// This is the color from the vertex shader interpolated across the 
  								// triangle per fragment.
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

// The entry point for our fragment shader.
void main()                    		
{                              

    // rotate the coordinates to rotate texture on render
    vec2 coord = v_TexCoordinate;
    // 4.712385 radian value for 90 degrees
    float sin_factor = sin(4.712385 * 2.0); // rotates by 180 degrees
    float cos_factor = cos(4.712385 * 2.0);
    mat2 rotation = mat2(cos_factor, -sin_factor, sin_factor, cos_factor);
    coord = (coord - .5) * rotation;
    coord += 0.5;

    // use coord in place of v_TexCoordinate to rotate your textures
    vec4 texure = (texture2D(u_Texture, v_TexCoordinate));

    gl_FragColor = texure;
}

