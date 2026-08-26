#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
flat out ivec3 control;
out vec2 uvIndex;
out vec2 fontUV;
flat out int isUI;

mat4 changeFov(mat4 projection) {
    float fov = 90.0;
    if (projection[2][3] != 0.0 && fov >= 1.0F) {
        float invTanHF = 1.0 / tan(radians(fov * 0.5));
        float aspectInv = projection[0][0] / projection[1][1];
        projection[0][0] = invTanHF * aspectInv;
        projection[1][1] = invTanHF;
    }
    return projection;
}

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 8) & 1;
}

//#moj_import <minecraft:text_effects_utils.glsl>

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    isUI = 0;
//    if (getTimeData() == 1) {
//        gl_Position = changeFov(ProjMat) * ModelViewMat * vec4(Position, 1.0);
//    }

    //applyTextEffects();
    float vertexId = mod(gl_VertexID, 4.);
    if (vertexId == 0.) {
        fontUV = vec2(0.0, 1.0);
    }
    if (vertexId == 1.) {
        fontUV = vec2(0.0, 0.0);
    }
    if (vertexId == 2.) {
        fontUV = vec2(1.0, 0.0);
    }
    if (vertexId == 3.) {
        fontUV = vec2(1.0, 1.0);
    }
    uvIndex = Position.xz * vec2(-0.045, 0.05) + vec2(0.5);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
    control = ivec3(Color.xyz * 255 + vec3(.5));

    texCoord0 = UV0;

    float opacity = round(Color.a * 255.0);
    if (opacity == 253.0 || opacity == 252.0 || opacity == 251.0) {
        vertexColor = Color;
        isUI = 1;
        float RESOLUTION_Y = 480;
        float scaling_factor = floor(ScreenSize.y / RESOLUTION_Y);
        float scaling_remainder = mod(ScreenSize.y, RESOLUTION_Y);
        scaling_factor = ScreenSize.y / RESOLUTION_Y;
        vec2 screenTexel = vec2(1.0) / ScreenSize;
        vec2 pixelPos = Position.xy * 16.0;
        if (opacity == 251.0) {
            pixelPos = Position.xz * -16.0;
        }
        float x_remainder = ScreenSize.x - RESOLUTION_Y * scaling_factor;
        vec2 finalPos = pixelPos * screenTexel;
        gl_Position = vec4((finalPos * scaling_factor * 2.0) - vec2(0.25, 0.25) * screenTexel, Position.z, 1.0);
        if (opacity == 251.0) {
            gl_Position.z = 0.0;
        }
        sphericalVertexDistance = 0.1;

        // Mouse cursor
        if (opacity == 252.0) {
            float fx = -ModelViewMat[0][2];
            float fy = -ModelViewMat[1][2];
            float fz = -ModelViewMat[2][2];

            vec3 dir = normalize(vec3(fx, fy, fz));

            float pitch = max(min(asin(dir.y), 3.1415 / 2.0), -3.1415 / 2.0);

            float yaw = atan(dir.x, dir.z);
            gl_Position += vec4(yaw / -3.1415, pitch * 2.0 / 3.1415, 0.0, 0.0);
        }
    }
}