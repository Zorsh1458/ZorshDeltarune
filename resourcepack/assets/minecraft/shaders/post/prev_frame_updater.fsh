#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D ScreenSampler;

in vec2 texCoord;
in vec2 oneTexel;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

float frameCount = 200;
int frameRate = 60;

void main() {
    vec4 prevTexel = texture(InSampler, texCoord);
    float prevFrame = round(prevTexel.b * frameCount);
    float curFrame = mod(floor(GameTime*1000.0 * frameRate), frameCount);
    if (curFrame != prevFrame) {
        prevTexel.g = GameTime*1000.0 * frameRate - floor(GameTime*1000.0 * frameRate);
        prevTexel.b = curFrame / frameCount;
        if (prevTexel.r > 0.7805 || texture(ScreenSampler, vec2(0.5, 0.5)).rgb == vec3(254.0, 254.0, 254.0) / 255.0) {
            prevTexel.r = 0.0;
        } else {
            prevTexel.r += 1.0 / frameCount;
            prevTexel.r = min(prevTexel.r, 1.0);
        }
    }
    prevTexel.a = 1.0 / frameCount;
    fragColor = prevTexel;
}