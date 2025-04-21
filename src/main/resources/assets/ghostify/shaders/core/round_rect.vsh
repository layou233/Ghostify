#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;

out vec2 f_Position;

void main() {
    f_Position = Position.xy;

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}