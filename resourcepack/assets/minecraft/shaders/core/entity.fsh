#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 7) & 1;
}

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (getTimeData() != 0) {
        if (gl_FragCoord.x / ScreenSize.x - 0.5 > 0.1 || gl_FragCoord.x / ScreenSize.x - 0.5 < -0.17) {
            discard;
        }
    }
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif
    color *= vertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    gl_FragDepth = gl_FragCoord.z;
    if (LinearizeDepth(gl_FragDepth) > 220.0 && textureSize(Sampler0, 0) == vec2(64.0, 64.0)) {
        gl_FragDepth = 0.0;
        vec4 tex = texture(Sampler0, mod(gl_FragCoord.xy, 64.0) / 64.0);
        fragColor = vec4(mix(vec3(0.0), tex.rgb, tex.a), 1.0);
    }

    fragColor = REMOVE_BLUE(fragColor);
}
