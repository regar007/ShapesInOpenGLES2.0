precision mediump float;       	// Set the default precision to medium. We don't need as high of a 
								// precision in the fragment shader.
uniform vec3 u_LightPos;       	// The position of the light in eye space.
uniform sampler2D u_Texture;    // The input texture.
uniform float u_UseColor;
  
varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec4 v_Color;
varying vec3 v_Normal;         	// Interpolated normal for this fragment.
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

  
// The entry point for our fragment shader.
void main()                    		
{                              
	// Will be used for attenuation.
    float distance = length(u_LightPos - v_Position);                  
	
	// Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(u_LightPos - v_Position);              	

	// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
	// pointing in the same direction then it will get max illumination.
    float diffuse = max(dot(v_Normal, lightVector), 0.0);               	  		  													  

	// Add attenuation. 
    diffuse = diffuse * (1.0 / distance);
    
    // Add ambient lighting
    diffuse = diffuse + 0.2;  

    // replace the black color with the color for background
    vec4 texure = (texture2D(u_Texture, v_TexCoordinate));
    if(diffuse != 0.){
       // texure = vec4(1.0,1.0,1.0,1.0);
    }

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
	vec4 _out;
	if(u_UseColor != 0.){
        _out = v_Color;
    }else{
        _out = texure;
        if(texure.r > 0.){
            _out = texure;
        }else{
           _out = vec4(1, 0, 0, 1);
        }
    }
    gl_FragColor = _out;
  }

