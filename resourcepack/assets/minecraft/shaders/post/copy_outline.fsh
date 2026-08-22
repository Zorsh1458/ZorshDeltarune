#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 rawoutline = texture(InSampler, texCoord);
    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    float rad = 1.0;
    for (float x = -rad; x <= rad; x++) {
        for (float z = -rad; z <= rad; z++) {
            vec2 pos = texCoord + oneTexel * vec2(x, z);
            if (texture(InSampler, pos).rgb == vec3(0.0)) {
                return;
            }
        }
    }
    fragColor = rawoutline;
}
