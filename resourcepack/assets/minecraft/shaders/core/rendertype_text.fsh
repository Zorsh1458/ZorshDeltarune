#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
flat in ivec3 control;
in vec2 uvIndex;
in vec2 fontUV;
flat in int isUI;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

#moj_import <minecraft:globals.glsl>
#define iTime GameTime*1000.0
const vec4 iMouse = vec4(0.0, 0.0, 0.0, 0.0);
//const vec3 iResolution = vec3(1920.0, 1080.0, 1.0);
vec3 iResolution = vec3(ScreenSize, 1.0);

#define TAU 6.28318530718
#define MAX_ITER 5

//moj_import <minecraft:deltarune.glsl>
//moj_import <minecraft:gold_ball.glsl>
#moj_import <minecraft:deltarune.glsl>

vec4 waterEffect( in vec2 fragCoord ) 
{
    vec4 fragColor;
	float time = iTime * .5+23.0;
    // uv should be the 0-1 uv of texture...
	vec2 uv = fragCoord.xy / iResolution.xy;
    

    vec2 p = mod(uv*TAU, TAU)-250.0;
	vec2 i = vec2(p);
	float c = 1.0;
	float inten = .005;

	for (int n = 0; n < MAX_ITER; n++) 
	{
		float t = time * (1.0 - (3.5 / float(n+1)));
		i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
		c += 1.0/length(vec2(p.x / (sin(i.x+t)/inten),p.y / (cos(i.y+t)/inten)));
	}
	c /= float(MAX_ITER);
	c = 1.17-pow(c, 1.4);
	vec3 colour = vec3(pow(abs(c), 8.0));
    //colour = clamp(colour + vec3(0.0, 0.35, 0.5), 0.0, 1.0);
    
	fragColor = vec4(colour, colour.x);
    return fragColor;
}

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 7) & 1;
}


vec4 finalWater(vec2 uv) {
    vec4 fragColor = vec4(0.0);
    float t = sin(GameTime * 100.0) * ScreenSize.x;
    fragColor += waterEffect(uv * ScreenSize * 7.657 + vec2(t, t));
    fragColor += waterEffect(uv * ScreenSize * 0.4543 + vec2(t * 0.4, -t));
    fragColor += waterEffect(uv * ScreenSize * 1.4543 + vec2(-t, t * 0.745));
    fragColor += waterEffect(uv * ScreenSize * 2.4543 + vec2(-t, -t));
    fragColor += waterEffect(uv * ScreenSize * 3.5645 + vec2(t, -t));
    fragColor += waterEffect(uv * ScreenSize * 4.3543 + vec2(-t, t));
    return fragColor / 3.0;
}

float randvv(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float rand(float x, float y) {
    return randvv(vec2(x, y));
}

vec3 getNeverlandLogoBlur(float PARAMETER) {
    vec2 LOGO_SIZE = vec2(231, 96);
    vec2 oneTexel = vec2(1.0) / textureSize(Sampler0, 0);
    vec2 coord = texCoord0;
    vec2 pixelUV = fontUV * LOGO_SIZE;
    vec3 result = texture(Sampler0, coord).rgb;
    float count = 1.0;
    for (float ox = -round(256.0 * PARAMETER); ox <= round(256.0 * PARAMETER); ox += 1.0) {
        if (ox != 0.0) {
            if (ox + pixelUV.x > 0 && ox + pixelUV.x < LOGO_SIZE.x) {
                result += texture(Sampler0, coord + vec2(oneTexel.x * ox * 0.25, 0.0)).rgb;
                count += 1.0;
            }
        }
    }
    return result / count;
}

vec3 getNeverlandLogoSwirl(float PARAMETER, float scale) {
    vec2 LOGO_SIZE = vec2(231, 96);
    float tpar = pow(PARAMETER * 256.0 / 40.0, 0.05);
    float pixelization = 1.1 + 28.0 * max(0.0, 1.0 - tpar * 1.5);
    vec2 oneTexel = vec2(1.0) / textureSize(Sampler0, 0);
    vec2 randDir = vec2(rand(fontUV.x + tpar, fontUV.y - tpar), rand(-fontUV.x - tpar, -fontUV.y + tpar));
    vec2 cor = texCoord0 + randDir * oneTexel * (1.0 - tpar) * 60.0;
    vec2 coord = floor(cor * textureSize(Sampler0, 0) * 4.0 / pixelization) / 4.0 * pixelization / textureSize(Sampler0, 0);
    vec2 pixelUV = fontUV * LOGO_SIZE;
    float offsetPX = scale * 4.0 * (1.0 - tpar) * sin(PARAMETER * 3.1415 * 3.0 + scale * 0.2 * fontUV.y + coord.y * 3.1415 * 24.0);
    float offset = oneTexel.x * offsetPX;
    coord.x += offset;
    float picCoordX = pixelUV.x + offsetPX;
    vec3 returnCol = mix(vec3(0.0), texture(Sampler0, coord).rgb, min(PARAMETER * 256.0 / 40.0 * 2.0, 1.0));
    if (picCoordX > LOGO_SIZE.x) {
        returnCol = vec3(0.0);
    }
    if (picCoordX < 0) {
        returnCol = vec3(0.0);
    }
    if (pixelUV.x < 3.0) {
        returnCol = vec3(0.0);
    }
    if (pixelUV.x > 228.0) {
        returnCol = vec3(0.0);
    }
    if (abs(pixelUV.y - 48.0) > 32.0) {
        returnCol = vec3(0.0);
    }
    return returnCol;
}

void main() {
    if (getTimeData() != 0 && sphericalVertexDistance > 10) {
        discard;
    }

    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    vec4 baseCol = texture(Sampler0, texCoord0);

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);

    if (isUI == 1) {
        fragColor = vec4(baseCol.rgb * vertexColor.rgb, baseCol.a);
    }

    if (isUI == 2) {
        fragColor = vec4(0.0, 1.0, 0.0, 1.0);
    }

    if (control.x == 253) {
        if (control.y == 1) {
            float z = control.z * 1.0;
            vec2 uv = gl_FragCoord.xy / ScreenSize;

            vec3 col1 = deltaruneBg(uv, 14.5 / 16383.0);
            vec3 col2 = deltaruneBg(uv, 0.0);
            float t = mod(floor(GameTime * 16383.0), 32.0) / 14.0;
            vec3 col = mix(col1, col2, min(max(t, 0.0), 1.0));
            fragColor = vec4(mix(col, vec3(0.0), z / 255.0), 1.0);
            fragColor = REMOVE_BLUE(fragColor);
            return;
        }
        if (control.y == 2) {
            vec4 def = apply_fog(texture(Sampler0, texCoord0) * ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
            fragColor = mix(def, vec4(1.0), sin(GameTime * 16383.0 / 14.0 * 6.283 * 2.0) * 0.5 + 0.5);
            fragColor = REMOVE_BLUE(fragColor);
            return;
        }
    }

    // RED (x):
    //     EVEN = LEAVE COLOR
    //     ODD = EFFECT CLASS
    // BLUE (z):
    //     EFFECT TYPE
    // GREEN (y):
    //     EFFECT PARAMETER

    int CLASS = control.x;
    float PARAMETER = float(control.y) / 255.0;
    int PARAMETER_INT = control.y;
    int EFFECT = control.z;

    if (CLASS == 1) {
        if (EFFECT == 0) {
            vec3 c1 = getNeverlandLogoSwirl(PARAMETER, 30.0);
            vec3 c2 = getNeverlandLogoSwirl(PARAMETER, 40.0);
            vec3 c3 = getNeverlandLogoSwirl(PARAMETER, 50.0);
            fragColor = vec4((c1 + c2 + c3) / 3.0, 1.0);
            fragColor = REMOVE_BLUE(fragColor);
            return;
        }
        if (EFFECT == 1) {
            vec3 c = getNeverlandLogoBlur(PARAMETER * 128.0 / 60.0);
            fragColor = vec4(mix(c, vec3(0.0), PARAMETER * 256.0 / 60.0), 1.0);
            fragColor = REMOVE_BLUE(fragColor);
            return;
        }
        // Flat map on screen
        // Parameter = specify scale
        if (EFFECT == 2) {
            vec2 scale = vec2(0.0);
            if (PARAMETER_INT == 0) {
                scale = vec2(160, 90);
            }
            vec2 pixelUV = fontUV * scale;
            vec2 oneTexel = textureSize(Sampler0, 0);
            oneTexel = vec2(1.0 / oneTexel.x, -1.0 / oneTexel.y);
            vec2 startPix = texCoord0 - pixelUV * oneTexel;
            vec2 tcor = gl_FragCoord.xy / ScreenSize.xy;
            tcor.x -= 0.5;
            tcor.x *= ScreenSize.x / ScreenSize.y;
            tcor.x += 0.5 * scale.x / scale.y;
            vec2 lookupoffset = tcor * scale.y * oneTexel;
            if (lookupoffset.x < 0.0) {
                lookupoffset.x = 0.0;
            }
            if (lookupoffset.x > scale.x * oneTexel.x) {
                lookupoffset.x = scale.x * oneTexel.x;
            }
            vec2 lookupcor = startPix + lookupoffset;
            fragColor = texture(Sampler0, lookupcor);
            fragColor = REMOVE_BLUE(fragColor);
            return;
        }
        if (EFFECT == 3) {
            vec2 oneTexel = vec2(1.0) / textureSize(Sampler0, 0);
            //vec2 offset = vec2(0.0, cos(fontUV.y * 3.1415 + PARAMETER * 1.0 * 6.283) * 1.0) * oneTexel;
            vec2 offset = vec2(0.0);
            fragColor = texture(Sampler0, texCoord0 + offset);
            if (fragColor.a < 0.5) {
                bool applyEffect = false;
                if (texture(Sampler0, texCoord0 + offset + vec2(1.0, 0.0) * oneTexel).a >= 0.5) {
                    applyEffect = true;
                } else if (texture(Sampler0, texCoord0 + offset + vec2(-1.0, 0.0) * oneTexel).a >= 0.5) {
                    applyEffect = true;
                } else if (texture(Sampler0, texCoord0 + offset + vec2(0.0, 1.0) * oneTexel).a >= 0.5) {
                    applyEffect = true;
                } else if (texture(Sampler0, texCoord0 + offset + vec2(0.0, -1.0) * oneTexel).a >= 0.5) {
                    applyEffect = true;
                }
                if (applyEffect) {
                    fragColor = vec4(vec3(0.33), PARAMETER);
                }
            }
            fragColor.a *= vertexColor.a;
        }
    }

    if (floor(fragColor.a * 255.99) < 1.5) {
        fragColor.a = 0.0;
        discard;
    }

    fragColor = REMOVE_BLUE(fragColor);
}