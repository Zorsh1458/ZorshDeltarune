#version 150

uniform sampler2D InSampler;

in vec2 oneTexel;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    fragColor = texture(InSampler, vec2(mod(texCoord.x, 0.5), mod(texCoord.y, 0.5)));
}
