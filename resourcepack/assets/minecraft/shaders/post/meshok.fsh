#version 150

#moj_import <minecraft:globals.glsl>

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    fragColor = vec4(0.0);
    vec4 mainCol = vec4(0.0);
    vec2 uvScaled = texCoord * 30 * vec2(ScreenSize.x / ScreenSize.y, 1.0);
    uvScaled += vec2(sin(uvScaled.x + GameTime * 1000.0) * 0.1, cos(uvScaled.y + GameTime * 749.0) * 0.1);
    uvScaled += vec2(sin(texCoord.y * 2.5 + GameTime * 1000.0) * 0.15, cos(texCoord.x * 2.5 + GameTime * 749.0) * 0.15);
    uvScaled += vec2(cos(texCoord.y * 5.5 + GameTime * 1000.0) * 0.3, sin(texCoord.x * 5.5 + GameTime * 749.0) * 0.3);
    vec2 cell = vec2(floor(uvScaled.x), floor(uvScaled.y));
    if (mod(uvScaled.x, 1.0) < 0.75) {
        mainCol = vec4(vec3(168, 111, 50) / 255.0 * (0.85 + 0.15 * rand(texCoord * vec2(1.0, 0.0))), 1.0);
        if (abs(mod(uvScaled.x, 1.0) - 0.375) > 0.325) {
            mainCol = vec4(mainCol.rgb * 0.5, 1.0 - (abs(mod(uvScaled.x, 1.0) - 0.375) - 0.325) / 0.05);
        }
    }
    if (mod(uvScaled.x, 1.0) >= 0.75 || mod(cell.x + cell.y, 2) == 0.0) {
        if (mod(uvScaled.y, 1.0) < 0.75) {
            mainCol = vec4(vec3(168, 111, 50) / 255.0 * (0.85 + 0.15 * rand(texCoord * vec2(0.0, 1.0))), 1.0);
            if (abs(mod(uvScaled.y, 1.0) - 0.375) > 0.325) {
                mainCol = vec4(mainCol.rgb * 0.5, 1.0 - (abs(mod(uvScaled.y, 1.0) - 0.375) - 0.325) / 0.05);
            }
        }
    }
    if (mainCol.a > 0.0) {
        //mainCol = vec4(mainCol.rgb * (0.3 - 0.15 * length(texCoord - 0.5)), mainCol.a);
        fragColor = mix(vec4(0.0), mainCol, 1.0);
    }
}