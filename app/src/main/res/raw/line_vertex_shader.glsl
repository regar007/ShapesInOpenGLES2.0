//#version 120
// This matrix member variable provides a hook to manipulate
// the coordinates of the objects that use this vertex shader
uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;
attribute vec4 a_Color;

varying vec4 v_Color;

void main() {
// the matrix must be included as a modifier of gl_Position
  gl_Position = u_MVPMatrix * a_Position;

  v_Color = a_Color;

}
