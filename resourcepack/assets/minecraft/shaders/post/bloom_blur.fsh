#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

void main() {
    vec4 blurred = vec4(0.0);
    float radius = 15.0;
    for (float a = -radius + 0.5; a <= radius; a += 2.0) {
        blurred += texture(InSampler, texCoord + sampleStep * a) * pow(abs(a / radius), 2.0);
    }
    blurred += texture(InSampler, texCoord + sampleStep * radius) / 2.0;
    //fragColor = vec4((blurred / (radius + 0.5)).rgb, blurred.a);
    fragColor = blurred / (radius + 0.5) * 4.0;
}
