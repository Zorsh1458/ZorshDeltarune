#version 150

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

void main() {
    vec4 color = vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;

    fragColor = REMOVE_BLUE(fragColor);
}
