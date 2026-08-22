#version 150

#moj_import <minecraft:fog.glsl>


in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

void main() {
    vec4 color = vertexColor;
    //color.a *= 1.0f - linear_fog_value(vertexDistance, 0, FogCloudsEnd);
    color.a = 0.0;
    fragColor = color;

    fragColor = REMOVE_BLUE(fragColor);
}
