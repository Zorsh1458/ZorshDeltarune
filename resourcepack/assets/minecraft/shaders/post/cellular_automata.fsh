#version 150

uniform sampler2D InSampler;

in vec2 oneTexel;
in vec2 texCoord;

out vec4 fragColor;

float rand(vec2 co)
{
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float randStep(vec2 co)
{
    float res = fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
    if (res > 0.5)
    {
        return 1.0;
    }
    return 0.0;
}

void main() {
    vec4 prev = texture(InSampler, texCoord);
    fragColor = prev;
    if (texCoord.x < 0.5 && texCoord.y < 0.5)
    {
        vec2 coord = texCoord / 0.2;
        if (prev == vec4(0.0, 0.0, 0.0, 0.2))
        {
            float rand = rand(coord);
            fragColor = vec4(0.0);
            if (rand > 0.5)
            {
                fragColor = vec4(randStep(coord * 0.3), randStep(coord * 0.3 + 0.3), randStep(coord * 0.3 + 0.7), 1.0);
            }
        }
        else
        {
            vec4 neigb = vec4(0);
            bool live = prev.a > 0.0;
            float bornMin = 9;
            float bornMax = 9;
            float liveMin = 6;
            float liveMax = 9;
            float kernelRadius = 2;
            for (float x = -kernelRadius; x <= kernelRadius; x++)
            {
                for (float y = -kernelRadius; y <= kernelRadius; y++)
                {
                    if (x != 0.0 || y != 0.0)
                    {
                        neigb += texture(InSampler, texCoord + oneTexel * vec2(x, y));
                    }
                }
            }
            fragColor = vec4(0.0);
            if (live && neigb.a >= liveMin && neigb.a <= liveMax)
            {
                fragColor = prev;
            }
            if (!live && neigb.a >= bornMin && neigb.a <= bornMax)
            {
                fragColor = neigb / neigb.a;
            }
        }
    }
}
