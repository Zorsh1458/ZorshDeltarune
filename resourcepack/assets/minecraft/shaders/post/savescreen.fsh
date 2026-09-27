#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;
uniform sampler2D SavedSampler;
uniform sampler2D RawScreenSampler;

in vec2 texCoord;
in vec2 oneTexel;
in vec3 blockTexCoord;
in float plFov;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

void main() {
    fragColor = texture(SavedSampler, texCoord);
    vec4 controlTexel = texture(RawScreenSampler, vec2(0.5, 0.5));
    int effectId = int(round(controlTexel.b * 255.0));
    float additionEffect = controlTexel.g;
    
    if (round(controlTexel.r * 255) == 253.0)
    {
        if (effectId == 2) {
            if (round(additionEffect * 255.0) == 2.0) {
                fragColor = texture(InSampler, texCoord);
            }
        }
    }

    //fragColor = vec4(texCoord, 0.0, 1.0);
}