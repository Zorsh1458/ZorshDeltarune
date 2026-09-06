#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in vec4 vertexColor;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 7) & 1;
}

void main() {
    if (getTimeData() != 0) {
        discard;
    }

    vec4 color = vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;

    fragColor = REMOVE_BLUE(fragColor);
}
