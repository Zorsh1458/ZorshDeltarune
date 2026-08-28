#version 150

uniform sampler2D InSampler;
uniform sampler2D MenuUISampler;

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

void main() {
    vec4 col = texture(InSampler, texCoord);
    vec4 cMenu = texture(InSampler, mod(gl_FragCoord.xy, 64) / 64.0);
    fragColor = vec4(mix(col.rgb * 0.5, cMenu, cMenu.a), 1.0);
}
