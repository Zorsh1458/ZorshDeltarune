#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 col = texture(InSampler, texCoord) * ColorModulate;
    fragColor = vec4(col.rgb * 0.5, 1.0);
}
