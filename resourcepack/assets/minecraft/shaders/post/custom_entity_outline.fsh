#version 150

uniform sampler2D OutlineSampler;
uniform sampler2D RawOutlineSampler;
uniform sampler2D MainSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

vec4 parseColor(vec2 coord) {
    vec4 blurred = vec4(0.0);
    float radius = 2.0;
    vec2 sampleStep = vec2(oneTexel.x, 0.0);
    for (float a = -radius + 0.5; a <= radius; a += 2.0) {
        blurred += vec4(texture(OutlineSampler, coord + sampleStep * a).rgb, 1.0);
    }
    blurred += vec4(texture(OutlineSampler, coord + sampleStep * radius).rgb / 2.0, 1.0);
    return vec4((blurred / (radius + 0.5)).rgb, blurred.a);
}

void main() {
    vec4 rawoutline = texture(RawOutlineSampler, texCoord);
    vec4 outline = texture(OutlineSampler, texCoord);
    vec4 main = texture(MainSampler, texCoord);
    fragColor = main;
    //fragColor = main * 0.5 + rawoutline * 0.5;

    float rad = 2.0;
    for (float x = -rad; x <= rad; x++) {
        for (float z = -rad; z <= rad; z++) {
            vec2 pos = texCoord + oneTexel * vec2(x, z);
            float r = round(texture(RawOutlineSampler, pos).r * 255);
            if (r == 254.0 || r == 253.0 || r == 252.0) {
                return;
            }
        }
    }

    if (rawoutline.rgb != outline.rgb) {
        float r = round(rawoutline.r * 255);
        if (r != 252.0 && r != 253.0 && r != 254.0) {
            fragColor = outline;
        }
    }

//
    //vec4 blurred = vec4(0.0);
    //float radius = 2.0;
    //vec2 sampleStep = vec2(0.0, oneTexel.y);
    //for (float a = -radius; a <= radius; a += 1.0) {
    //    blurred += vec4(texture(OutlineSampler, texCoord + sampleStep * a).rgb, 1.0);
    //}
    //blurred /= radius * 2 + 1;
    //
    ////fragColor = outline;
//
    //if (blurred.rgb != outline.rgb) {
    //    fragColor = blurred;
    //}
}