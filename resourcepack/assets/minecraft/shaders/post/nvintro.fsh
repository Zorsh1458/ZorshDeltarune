#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

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

#moj_import <minecraft:neverlandintro.glsl>
#moj_import <minecraft:soulintro.glsl>

void main() {
    vec4 controlTexel = texture(RawScreenSampler, vec2(0.5, 0.5));
    int effectId = int(round(controlTexel.b * 255.0));
    float additionEffect = controlTexel.g;
    vec2 baseCoord = texCoord;

    if (round(controlTexel.r * 255) != 253.0)
    {
        effectId = -1;
    } else {
        if (effectId == 16) {
            if (additionEffect == 0.0) {
                return;
            }

            float y = floor(texCoord.y * 128.0) / 2.0;

            float offset = cos(y * 0.25 + additionEffect * 6.28);
            offset +=  0.2 * cos(1.623 + y - 6.28 * 4.0 * additionEffect);
            offset +=  0.2 * cos(3.623 + y * 2.0 + 6.28 * 16.0 * additionEffect);

            float w = 0.003;

            float t = cos(additionEffect * 6.283) / -2.0 + 0.5;

            if (texCoord.x < 0.1 + 0.01 * offset) {
                neverlandintro_imageSwirl(fragColor, gl_FragCoord.xy, t * 3.0 + 1.0);
                return;
            }
            if (texCoord.x < 0.1 + w + 0.01 * offset) {
                fragColor = vec4(vec3(0.75), 1.0);
                return;
            }

            if (texCoord.x > 0.9 - 0.01 * offset) {
                neverlandintro_imageSwirl(fragColor, gl_FragCoord.xy, t * 3.0 + 1.0);
                return;
            }
            if (texCoord.x > 0.9 - w - 0.01 * offset) {
                fragColor = vec4(vec3(0.75), 1.0);
                return;
            }

            vec4 col1;
            vec4 col2;
            vec2 uv1 = ScreenSize.xy * 0.3 * ((texCoord - 0.5) * (1.0 - 0.5 * additionEffect) + 0.5);
            vec2 uv2 = ScreenSize.xy * 0.3 * ((texCoord - 0.5) * (1.5 - 0.5 * additionEffect) + 0.5);
            uv1 = floor(uv1 / 0.5) * 0.5;
            uv2 = floor(uv2 / 0.5) * 0.5;
            neverlandintro_mainImage(col1, uv1, t);
            neverlandintro_mainImage(col2, uv2, t);
            vec4 stars = mix(col1, col2, additionEffect);
            stars.r = min(stars.r, 1.0);
            stars.g = min(stars.g, 1.0);
            stars.b = min(stars.b, 1.0);
            vec4 col3;
            vec4 col4;
            neverlandintro_burstImage(col3, gl_FragCoord.xy, additionEffect * 16.0);
            neverlandintro_burstImage(col4, gl_FragCoord.xy, additionEffect * 16.0 + 16.0);
            fragColor = mix(col4, col3, additionEffect);
            fragColor.g = min(fragColor.g, 1.0) * 0.2;
            fragColor.r *= 0.5;
            fragColor.r = pow(fragColor.r, 0.3) * 0.2;
            fragColor.b = 0.1 + fragColor.g;
            fragColor += stars * 0.2;
        }
        if (effectId == 17) {
            if (additionEffect == 0.0) {
                return;
            }

            float y = floor(texCoord.y * 128.0) / 2.0;

            float offset = cos(y * 0.25 + additionEffect * 6.28);
            offset +=  0.2 * cos(1.623 + y - 6.28 * 4.0 * additionEffect);
            offset +=  0.2 * cos(3.623 + y * 2.0 + 6.28 * 16.0 * additionEffect);

            float w = 0.003;

            float t = cos(additionEffect * 6.283) / -2.0 + 0.5;

            if (texCoord.x < 0.1 + 0.01 * offset) {
                neverlandintro_imageSwirl(fragColor, gl_FragCoord.xy, t * 3.0 + 1.0);
                return;
            }
            if (texCoord.x < 0.1 + w + 0.01 * offset) {
                fragColor = vec4(vec3(0.75), 1.0);
                return;
            }

            if (texCoord.x > 0.9 - 0.01 * offset) {
                neverlandintro_imageSwirl(fragColor, gl_FragCoord.xy, t * 3.0 + 1.0);
                return;
            }
            if (texCoord.x > 0.9 - w - 0.01 * offset) {
                fragColor = vec4(vec3(0.75), 1.0);
                return;
            }

            vec4 col1;
            vec4 col2;
            //vec2 uv1 = ScreenSize.xy * 0.3 * ((texCoord - 0.5) * (1.0 - 0.5 * additionEffect) + 0.5);
            //vec2 uv2 = ScreenSize.xy * 0.3 * ((texCoord - 0.5) * (1.5 - 0.5 * additionEffect) + 0.5);
            //uv1 = floor(uv1 / 0.5) * 0.5;
            //uv2 = floor(uv2 / 0.5) * 0.5;
            soulintro_mainImage(col1, texCoord * ScreenSize.xy, additionEffect * 16.0);
            soulintro_mainImage(col2, texCoord * ScreenSize.xy, additionEffect * 16.0 + 16.0);
            fragColor = mix(col1, col2, additionEffect);
        }
    }
}
