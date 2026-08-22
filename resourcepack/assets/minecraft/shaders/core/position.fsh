#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

#moj_import<minecraft:remove_blue.glsl>

void main() {
    fragColor = apply_fog(ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    fragColor = REMOVE_BLUE(fragColor);
}
