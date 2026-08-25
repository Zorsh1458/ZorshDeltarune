#version 150

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in vec4 Position;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec2 texCoord;
out vec2 oneTexel;
out vec3 blockTexCoord;
out float plFov;

void main()
{
    //vec4 worldPosition = ModelViewMat * Position;
    //worldPos = Position.xyz + ChunkOffset;
    //plFov = 2.0*atan(1.0/ProjMat[1][1])*180.0/3.141592;
    plFov = 90;
    vec4 outPos = ProjMat * vec4(Position.xy * OutSize, 0.0, 1.0);
    blockTexCoord = ModelOffset;
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    oneTexel = 1.0 / InSize;

    texCoord = Position.xy;
}
