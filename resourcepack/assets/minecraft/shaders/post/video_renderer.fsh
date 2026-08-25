#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;
uniform sampler2D ScreenSampler;

uniform sampler2D Video0Sampler;
uniform sampler2D Video1Sampler;
uniform sampler2D Video2Sampler;
uniform sampler2D Video3Sampler;
uniform sampler2D Video4Sampler;
uniform sampler2D Video5Sampler;
uniform sampler2D Video6Sampler;
uniform sampler2D Video7Sampler;
uniform sampler2D Video8Sampler;

in vec2 texCoord;
in vec2 oneTexel;

layout(std140) uniform VideoConfig {
    int CutsceneIdOffset;
};

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

int frameRate = 24;

vec4 parseFrame(float frame, vec4 screenTexel, int effectsCount, int cutsceneId, vec2 offset) {
    vec2 frameSize;
    vec2 videoSize;

    // РАЗМЕРЫ
    switch(cutsceneId / effectsCount - CutsceneIdOffset) {
    case 0:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video0Sampler, 0);
        break;
    case 1:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video1Sampler, 0);
        break;
    case 2:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video2Sampler, 0);
        break;
    case 3:
        frameSize = vec2(480.0, 270.0);
        videoSize = textureSize(Video3Sampler, 0);
        break;
    case 4:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video4Sampler, 0);
        break;
    case 5:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video5Sampler, 0);
        break;
    case 6:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video6Sampler, 0);
        break;
    case 7:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video7Sampler, 0);
        break;
    case 8:
        frameSize = vec2(320.0, 180.0);
        videoSize = textureSize(Video8Sampler, 0);
        break;
    }

    float xFrameCount = floor(videoSize.x / frameSize.x);
    float yFrameCount = floor(videoSize.y / frameSize.y);
    float xFrame = mod(frame, xFrameCount);
    float yFrame = mod((frame - xFrame) / xFrameCount, yFrameCount);

    vec2 videoCoord = texCoord;
    videoCoord.x *= frameSize.x / videoSize.x;
    videoCoord.y *= frameSize.y / videoSize.y;
    videoCoord.y *= -1;
    videoCoord.x += frameSize.x / videoSize.x * xFrame;
    videoCoord.y -= frameSize.y / videoSize.y * yFrame;
    vec4 videoTexel;


    // НУЖНЫЙ СЕМПЛЕР
    switch(cutsceneId / effectsCount - CutsceneIdOffset) {
    case 0:
        return texture(Video0Sampler, videoCoord + offset / videoSize);
    case 1:
        return texture(Video1Sampler, videoCoord + offset / videoSize);
    case 2:
        return texture(Video2Sampler, videoCoord + offset / videoSize);
    case 3:
        return texture(Video3Sampler, videoCoord + offset / videoSize);
    case 4:
        return texture(Video4Sampler, videoCoord + offset / videoSize);
    case 5:
        return texture(Video5Sampler, videoCoord + offset / videoSize);
    case 6:
        return texture(Video6Sampler, videoCoord + offset / videoSize);
    case 7:
        return texture(Video7Sampler, videoCoord + offset / videoSize);
    case 8:
        return texture(Video8Sampler, videoCoord + offset / videoSize);
    }

    return screenTexel;
}

void main() {
    vec4 inTexel = texture(InSampler, texCoord);
    fragColor = inTexel;
    vec4 prevTexel = texture(PrevSampler, texCoord);
    //vec4 screenTexel = texture(ScreenSampler, texCoord);
    vec4 controlTexel = texture(ScreenSampler, vec2(0.5, 0.5));
    int rawCutsceneId = int(controlTexel.g * 255.0 * 256.0 + controlTexel.b * 255.0);
    int cutsceneId = int(rawCutsceneId / 4.0) * 4;
    int effectsData = rawCutsceneId - cutsceneId;
    int effectsCount = 4;

    //fragColor = screenTexel * 0.5 + fragColor * 0.5;
    //return;

    //float frameRate = round(1.0 / texture(PrevSampler, vec2(0.0, 0.0)).g);
    float timeCycle = GameTime*1000.0 * frameRate - floor(GameTime*1000.0 * frameRate);
    if (round(controlTexel.r * 255.0) == 252.0)
    {
        float frame = round(prevTexel.r / prevTexel.a);
        vec4 mainFrame = vec4(0.0);
        mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(0.0, 0.0));
        if (effectsData == 0 || effectsData == 3) {
            float radius = 5;
            float mult = 0.1;
            for (float x = -radius; x <= radius; x++)
            {
                for (float y = -radius; y <= radius; y++)
                {
                    if (x != 0 || y != 0)
                    {
                        mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(x, y) * mult);
                    }
                }
            }
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(0.0, 0.5));
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(0.5, 0.0));
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(0.5, 0.5));
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(0.0, -0.5));
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(-0.5, 0.0));
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(-0.5, -0.5));
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(0.5, -0.5));
            //mainFrame += parseFrame(frame, inTexel, effectsCount, cutsceneId, vec2(-0.5, 0.5));
            //mainFrame /= 9;
            mainFrame /= (radius * 2 + 1) * (radius * 2 + 1) - 1;
            //mainFrame = vec4(1.0);
        }
        if (effectsData == 2) {
            float stOffset = prevTexel.g;
            float t = timeCycle - stOffset;
            if (t < 0.0) {
                t += 1.0;
            }
            vec4 nextFrame = vec4(0.0);
            nextFrame += parseFrame(frame + 1, inTexel, effectsCount, cutsceneId, vec2(0.0, 0.0));
            mainFrame += (nextFrame - mainFrame) * t;
            //mainFrame = vec4(vec3(t), 1.0);
        }
        if (effectsData == 3) {
            float stOffset = prevTexel.g;
            float t = timeCycle - stOffset;
            if (t < 0.0) {
                t += 1.0;
            }
            vec4 nextFrame = vec4(0.0);
            nextFrame += parseFrame(frame + 1, inTexel, effectsCount, cutsceneId, vec2(0.0, 0.0));
            nextFrame += parseFrame(frame + 1, inTexel, effectsCount, cutsceneId, vec2(0.0, 0.5));
            nextFrame += parseFrame(frame + 1, inTexel, effectsCount, cutsceneId, vec2(0.5, 0.0));
            nextFrame += parseFrame(frame + 1, inTexel, effectsCount, cutsceneId, vec2(0.5, 0.5));
            nextFrame /= 4;
            mainFrame += (nextFrame - mainFrame) * t;
            //mainFrame = vec4(vec3(t), 1.0);
        }
        fragColor = mainFrame;
    }
}
