#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord1;
out float Z;
out vec3 D;
out vec3 Pos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color;
    texCoord0 = UV0;
    texCoord1 = gl_Position.xy;
    Z = 0.0;
    Pos = Position;
    if (round(Color.rgb * 255.0).r <= 254.0)
    {
        Z = gl_Position.z / gl_Position.w;
        D = vec4(ModelViewMat * vec4(Position, 1.0)).xyz;
    }
}
