#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

vec4 getBrightned() {
    vec4 inTexel = texture(InSampler, texCoord);
    float brightness = dot(inTexel.rgb, vec3(0.2126, 0.7152, 0.0722));
    if (brightness > 0.7) {
        return inTexel;
    }
    return vec4(0.0, 0.0, 0.0, 1.0);
}

void main()
{
    fragColor = getBrightned();
}