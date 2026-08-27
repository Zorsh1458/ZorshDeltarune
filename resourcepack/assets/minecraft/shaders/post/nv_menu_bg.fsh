#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 col = texture(InSampler, texCoord) * ColorModulate;
    fragColor = vec4(col.rgb * 0.25, col.a);
}
