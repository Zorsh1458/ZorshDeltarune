#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

void main() {
    float t = (sin(GameTime * 1000.0) * 0.5 + 0.5) * 0.25 + 1.0;
    vec4 color = texture(Sampler0, clamp(texCoord0 * t - 0.5 * t + 0.5, 0.0, 1.0));
    t += 0.2;
    color.a += texture(Sampler0, clamp(texCoord0 * t - 0.5 * t + 0.5, 0.0, 1.0)).a * 0.5;
    color *= vertexColor * ColorModulator;
    //if (color.a > 0.1) {
    //    color = vec4(0.0, 0.0, 0.0, 1.0);
    //}
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    fragColor = REMOVE_BLUE(fragColor);
    discard;
}
