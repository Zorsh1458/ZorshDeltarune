#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D DepthSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;

out vec4 fragColor;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main() {
    vec2 uv = texCoord * 2.0 - 1.0;
    uv.x *= ScreenSize.x / ScreenSize.y;
    float t = LinearizeDepth(texture(DepthSampler, texCoord).r);
    vec4 inTexel = vec4(mix(vec3(t / 100.0), vec3(0.0), smoothstep(0.5, 1.0, length(uv))), 1.0);
    if (t > 100.0) {
        inTexel = vec4(0.0, 0.0, 0.0, 1.0);
    }
    vec4 prevTexel = texture(PrevSampler, texCoord);
    fragColor = inTexel * 0.1 + prevTexel * 0.9;
    //fragColor = vec4(max(inTexel.rgb, prevTexel.rgb * 0.99), 1.0);
}
