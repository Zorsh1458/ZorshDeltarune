#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

void main() {
    float r = round(texture(InSampler, texCoord).r * 255.0);
    if (r == 252 || r == 253 || r == 254)
    {
        fragColor = texture(InSampler, texCoord);
    }
    else
    {
        vec4 blurred = vec4(0.0);
        float radius = 2.0;
        for (float a = -radius + 0.5; a <= radius; a += 2.0) {
            blurred += texture(InSampler, texCoord + sampleStep * a);
        }
        blurred += texture(InSampler, texCoord + sampleStep * radius) / 2.0;
        fragColor = vec4((blurred / (radius + 0.5)).rgb, blurred.a);
        //fragColor = vec4(0.0, 1.0, 0.0, blurred.a);
    }
}
