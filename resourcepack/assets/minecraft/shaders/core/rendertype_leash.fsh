#version 150

#moj_import <minecraft:fog.glsl>


in float sphericalVertexDistance;
in float cylindricalVertexDistance;
flat in vec4 vertexColor;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

void main() {
    fragColor = apply_fog(vertexColor, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    fragColor = REMOVE_BLUE(fragColor);
}
