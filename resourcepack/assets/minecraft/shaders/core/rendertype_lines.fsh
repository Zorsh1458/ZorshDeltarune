#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 7) & 1;
}

void main() {
    vec4 color = vertexColor * ColorModulator;
    if (getTimeData() != 0) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    fragColor = REMOVE_BLUE(fragColor);
    fragColor.a = 1.0;
}
