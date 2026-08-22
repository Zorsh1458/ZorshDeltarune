#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>

in vec3 Position;

out float vid;
out vec4 Pos;

void main() {
    float id = floor(gl_VertexID / 4.0);
    //float move = pow(mod(GameTime * 100.0 + id * 0.03, 1.0), 2.0) * 5000;
    //gl_Position = ProjMat * ModelViewMat * vec4(Position + vec3(Position.x, 0.0, Position.z) * move / 2000 - vec3(0, 2000, 0) + vec3(0, move + 10 * (sin(GameTime * 1000.0 + gl_VertexID) - 1), 0), 1.0);
    vid = id;
    vec3 pos = Position;
    Pos = vec4(pos, 1.0);
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
