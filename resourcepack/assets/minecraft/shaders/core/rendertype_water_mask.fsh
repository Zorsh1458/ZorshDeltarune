#version 150

#moj_import <minecraft:dynamictransforms.glsl>

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

void main() {
    fragColor = ColorModulator;
    fragColor = REMOVE_BLUE(fragColor);
}
