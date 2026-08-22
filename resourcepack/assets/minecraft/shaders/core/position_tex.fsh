#version 150

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;

out vec4 fragColor;

#moj_import<minecraft:remove_blue.glsl>

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
    fragColor = REMOVE_BLUE(fragColor);
}
