#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in float vid;
in vec4 Pos;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 8) & 1;
}

void main() {
    const vec4 col1 = vec4(0.7, 0.7, 1, 1);
    const vec4 col2 = vec4(0.7, 0.7, 1, 0.1);

    int timeData = getTimeData();

    fragColor = mix(col1, col2, (sin(vid * 11.32458643) + 1) * 0.5);
    if (timeData == 1) {
        const vec4 col3 = vec4(1.0, 0.6, 0.6, 1);
        const vec4 col4 = vec4(1.0, 0.6, 0.6, 0.1);
        fragColor = mix(col3, col4, (sin(vid * 11.32458643) + 1) * 0.5);
    }
    fragColor = REMOVE_BLUE(fragColor);
}