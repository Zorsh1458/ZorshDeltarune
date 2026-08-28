#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D MenuUISampler;

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 7) & 1;
}

void main() {
    vec4 col = texture(InSampler, texCoord);
    col.rgb *= 0.5;
    if (getTimeData() == 1) {
        vec2 uv = gl_FragCoord.xy;
        uv.y = ScreenSize.y - uv.y;
        uv -= 32;
        uv *= 0.5;
        vec2 size = textureSize(MenuUISampler, 0);
        if (uv.x > 0 && uv.x < size.x && uv.y > 0 && uv.y < size.y) {
            vec4 cMenu = texture(MenuUISampler, uv / size);
            col.rgb = mix(col.rgb, cMenu.rgb, cMenu.a);
        }
    }
    fragColor = vec4(col.rgb, 1.0);
}
