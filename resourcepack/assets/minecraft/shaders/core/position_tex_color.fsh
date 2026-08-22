#version 150

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
    float LineWidth;
};

layout(std140) uniform Globals {
    vec2 ScreenSize;
    float GlintAlpha;
    float GameTime;
    int MenuBlurRadius;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

#moj_import<minecraft:remove_blue.glsl>

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 7) & 1;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a == 0.0 || getTimeData() != 0) {
        discard;
    }
    fragColor = color * ColorModulator;
    fragColor = REMOVE_BLUE(fragColor);
}
