#version 150

uniform sampler2D InSampler;

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

void main() {
    vec4 col = texture(InSampler, texCoord);
    fragColor = vec4(col.rgb * 0.5, 1.0);
}
