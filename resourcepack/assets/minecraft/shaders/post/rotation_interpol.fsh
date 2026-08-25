#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D RawScreenSampler;
uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 glowing = texture(RawScreenSampler, vec2(0.5));
    fragColor = texture(InSampler, texCoord);
    int effectId = int(round(glowing.b * 255.0));
    if (effectId == 5)
    {
        float maxAnglePerFrameSpeed = 10.0;

        float targetAngle = glowing.g;
        float prevAngle = fragColor.b;
        float offset = targetAngle - prevAngle;
        if (offset >= 0.0)
        {
            fragColor.b = prevAngle + min(offset, maxAnglePerFrameSpeed / 255.0);
        }
        else
        {
            fragColor.b = prevAngle - min(abs(offset), maxAnglePerFrameSpeed / 255.0);
        }
    }
}
