#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in float Z;
in vec3 D;
in vec3 Pos;

out vec4 fragColor;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

float parseDepth(vec2 texCoord, float depth)
{
    float _FOV = 90.0;
    //float _FOV = 3.1415 / 2.0;
    //float distance = length(vec3(1., (2.*texCoord - 1.) * vec2(ScreenSize.x/ScreenSize.y,1.) * tan(radians(_FOV / 2.))) * depth);
    float distance = length(vec3(1., (2.*texCoord - 1.) * vec2(ScreenSize.x/ScreenSize.y,1.) * tan(radians(_FOV / 2.))) * depth);
    return distance;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = vec4(ColorModulator.rgb * vertexColor.rgb, ColorModulator.a);
    //if (Z != 0.0 && round(vertexColor.r * 255) == 254.0)
    //{
    //    //float depth = parseDepth(texCoord1, LinearizeDepth(Z));
    //    float depth = length(D);
    //    //float depth = length(vec4(ModelViewMat * vec4(Pos, 1.0)).xyz);
    //    float red = 0.9 * clamp(depth / 100.0, 0.0, 1.0);
    //    float redRound = floor(red * 255.0) / 255.0;
    //    float redLeft = red * 255.0 - floor(red * 255.0);
    //    //fragColor = vec4(red, fragColor.g, fragColor.b, 1.0);
    //    fragColor = vec4(redRound, redLeft, fragColor.b, 1.0);
    //}
}
