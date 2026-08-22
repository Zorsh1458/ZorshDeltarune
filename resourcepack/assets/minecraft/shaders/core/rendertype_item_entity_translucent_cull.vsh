#version 150

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;


out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec2 uvPoint;
out vec4 vertexColor;
out vec4 whiteVertexColor;
out vec4 controlColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec2 texCoord2;
out vec3 Norm;
out vec4 clipNear;
out vec3 worldpos;

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 8) & 1;
}

void main() {
    float id = mod(gl_VertexID, 4);
    mat4x4 pMat = ProjMat;
    //if (getTimeData() == 1) {
    //    pMat[2][0] *= 0.0;
    //    pMat[2][1] *= 0.0;
    //    pMat[3][0] *= 0.0;
    //    pMat[3][1] *= 0.0;
    //    pMat[0][1] *= 0.0;
    //    pMat[1][0] *= 0.0;
    //    pMat[1][3] *= 0.0;
    //    pMat[0][3] *= 0.0;
    //}
    mat4x4 mvMat = ModelViewMat;
    if (getTimeData() == 1) {
        mvMat[0][1] *= -1.0;
        mvMat[2][1] *= -1.0;
        mvMat[1][2] *= -1.0;
    }
    gl_Position = pMat * mvMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color) * texelFetch(Sampler2, UV2 / 16, 0);
    whiteVertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, vec4(1.0)) * texelFetch(Sampler2, UV2 / 16, 0);
    texCoord0 = UV0;
    texCoord1 = UV1;
    texCoord2 = UV2;
    controlColor = Color;
    Norm = Normal;
    worldpos = Position;
    clipNear = inverse(ProjMat * ModelViewMat) * vec4(gl_Position.xy,  -gl_Position.w, gl_Position.w);
    if (id == 0)
    {
        uvPoint = vec2(0.0, 1.0);
    }
    if (id == 1)
    {
        uvPoint = vec2(0.0, 0.0);
    }
    if (id == 2)
    {
        uvPoint = vec2(1.0, 0.0);
    }
    if (id == 3)
    {
        uvPoint = vec2(1.0, 1.0);
    }
    //uvPoint = UV2 / 16.0;
}