#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D NVIntroSampler;
uniform sampler2D RawScreenSampler;

uniform sampler2D PrevMainSampler;
uniform sampler2D FlashSampler;

uniform sampler2D MeshokSampler;
uniform sampler2D CookieSampler;
uniform sampler2D PrevSampler;

uniform sampler2D RotationSampler;
uniform sampler2D SavedSampler;
uniform sampler2D SkinmapSampler;

in vec2 texCoord;
in vec2 oneTexel;
in vec3 blockTexCoord;
in float plFov;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

#moj_import <minecraft:crt.glsl>

#define iTime (GameTime*1000.0)
vec3 iResolution = vec3(ScreenSize, 1.0);
const vec4 iMouse = vec4(0.0, 0.0, 0.0, 0.0);

#define S(a, b, t) smoothstep(a, b, t)
#define USE_POST_PROCESSING

vec3 N13(float p) {
    //  from DAVE HOSKINS
   vec3 p3 = fract(vec3(p) * vec3(.1031,.11369,.13787));
   p3 += dot(p3, p3.yzx + 19.19);
   return fract(vec3((p3.x + p3.y)*p3.z, (p3.x+p3.z)*p3.y, (p3.y+p3.z)*p3.x));
}

vec4 N14(float t) {
	return fract(sin(t*vec4(123., 1024., 1456., 264.))*vec4(6547., 345., 8799., 1564.));
}
float N(float t) {
    return fract(sin(t*12345.564)*7658.76);
}

float Saw(float b, float t) {
	return S(0., b, t)*S(1., b, t);
}


vec2 DropLayer2(vec2 uv, float t) {
    vec2 UV = uv;
    
    uv.y += t*0.75;
    vec2 a = vec2(6., 1.);
    vec2 grid = a*2.;
    vec2 id = floor(uv*grid);
    
    float colShift = N(id.x); 
    uv.y += colShift;
    
    id = floor(uv*grid);
    vec3 n = N13(id.x*35.2+id.y*2376.1);
    vec2 st = fract(uv*grid)-vec2(.5, 0);
    
    float x = n.x-.5;
    
    float y = UV.y*20.;
    float wiggle = sin(y+sin(y));
    x += wiggle*(.5-abs(x))*(n.z-.5);
    x *= .7;
    float ti = fract(t+n.z);
    y = (Saw(.85, ti)-.5)*.9+.5;
    vec2 p = vec2(x, y);
    
    float d = length((st-p)*a.yx);
    
    float mainDrop = S(.4, .0, d);
    
    float r = sqrt(S(1., y, st.y));
    float cd = abs(st.x-x);
    float trail = S(.23*r, .15*r*r, cd);
    float trailFront = S(-.02, .02, st.y-y);
    trail *= trailFront*r*r;
    
    y = UV.y;
    float trail2 = S(.2*r, .0, cd);
    float droplets = max(0., (sin(y*(1.-y)*120.)-st.y))*trail2*trailFront*n.z;
    y = fract(y*10.)+(st.y-.5);
    float dd = length(st-vec2(x, y));
    droplets = S(.3, 0., dd);
    float m = mainDrop+droplets*r*trailFront;
    
    //m += st.x>a.y*.45 || st.y>a.x*.165 ? 1.2 : 0.;
    return vec2(m, trail);
}

float StaticDrops(vec2 uv, float t) {
	uv *= 40.;
    
    vec2 id = floor(uv);
    uv = fract(uv)-.5;
    vec3 n = N13(id.x*107.45+id.y*3543.654);
    vec2 p = (n.xy-.5)*.7;
    float d = length(uv-p);
    
    float fade = Saw(.025, fract(t+n.z));
    float c = S(.3, 0., d)*fract(n.z*10.)*fade;
    return c;
}

vec2 Drops(vec2 uv, float t, float l0, float l1, float l2) {
    float s = StaticDrops(uv, t)*l0; 
    vec2 m1 = DropLayer2(uv, t)*l1;
    vec2 m2 = DropLayer2(uv*1.85, t)*l2;
    
    float c = s+m1.x+m2.x;
    c = S(.3, 1., c);
    
    return vec2(c, max(m1.y*l0, m2.y*l1));
}

void raindrop( out vec4 fragColor, in vec2 fragCoord )
{
	vec2 uv = (fragCoord.xy-.5*iResolution.xy) / iResolution.y;
    vec2 UV = fragCoord.xy/iResolution.xy;
    vec3 M = iMouse.xyz/iResolution.xyz;
    float T = iTime * 3.0;
    
    float t = T*.2;
    
    float rainAmount = sin(T*.05)*.3+.7;
    
    rainAmount *= 0.75;

    float maxBlur = mix(3., 6., rainAmount);
    float minBlur = 2.;
    
    float story = 0.;
    float heart = 0.;
    float zoom = 1.0;
    uv *= .7+zoom*.3;
    UV = (UV-.5)*(.9+zoom*.1)+.5;
    
    float staticDrops = S(-.5, 1., rainAmount)*2.;
    float layer1 = S(.25, .75, rainAmount);
    float layer2 = S(.0, .5, rainAmount);
    
    
    vec2 c = Drops(uv, t, staticDrops, layer1, layer2);
    #ifdef CHEAP_NORMALS
    	vec2 n = vec2(dFdx(c.x), dFdy(c.x));
    #else
    	vec2 e = vec2(.001, 0.);
    	float cx = Drops(uv+e, t, staticDrops, layer1, layer2).x;
    	float cy = Drops(uv+e.yx, t, staticDrops, layer1, layer2).x;
    	vec2 n = vec2(cx-c.x, cy-c.x);		// expensive normals
    #endif
    
    float focus = mix(maxBlur-c.y, minBlur, S(.1, .2, c.x));
    vec3 col = textureLod(InSampler, UV+n, focus).rgb;
    
    
    #ifdef USE_POST_PROCESSING
    t = (T+3.)*.5;										// make time sync with first lightnoing
    //float colFade = sin(t*.2)*.5+.5+story;
    //col *= mix(vec3(1.), vec3(.8, .9, 1.3), colFade);	// subtle color shift
    float fade = S(0., 10., T);							// fade in at the start
    //float lightning = sin(t*sin(t*10.));				// lighting flicker
    //lightning *= pow(max(0., sin(t+sin(t))), 10.);		// lightning flash
    //col *= 1.+lightning*fade*mix(1., .1, story*story);	// composite lightning
    col *= 1.-dot(UV-=.5, UV);							// vignette
    
    col *= fade;										// composite start and end fade
    #endif
    
    //col = vec3(heart);
    fragColor = vec4(col, 1.);
}

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

float parseDepth(vec2 texCoord) {
    //float _FOV = 2.0*atan(1.0/ProjMat[1][1])*180.0/3.141592;
    //float _FOV = 2.0*atan(1.0/gl_ProjectionMatrix[1][1]) / 3.141592 * 180.0;
    //float _FOV = 90.0;
    float _FOV = plFov;
    float depth = LinearizeDepth(texture(DepthSampler, texCoord).r);
    float distance = length(vec3(1., (2.*texCoord - 1.) * vec2(OutSize.x/OutSize.y,1.) * tan(radians(_FOV / 2.))) * depth);
    //float distance = length(vec3(1., (2.*texCoord - 1.) * vec2(OutSize.x/OutSize.y,1.) * tan(_FOV / 2.)) * depth);
    return distance;
}

void filmGrain( out vec4 fragColor, in vec2 fragCoord )
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;

    // Calculate noise and sample texture
    float mdf = 0.1; // increase for noise amount 
    float noise = (fract(sin(dot(uv + vec2(mod(iTime * 1000.0, 10.0), 0.0), vec2(12.9898,78.233)*2.0)) * 43758.5453));
    vec4 tex = texture(InSampler, uv);
    
    mdf *= 3;
    
    vec4 col = tex * (1.1 - noise * mdf);

    // Output to screen
    fragColor = col;
}

void filmGrainRaindrop( out vec4 fragColor, in vec2 fragCoord ) {
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;

    // Calculate noise and sample texture
    float mdf = 0.1; // increase for noise amount 
    float noise = (fract(sin(dot(uv + vec2(mod(iTime * 1000.0, 10.0), 0.0), vec2(12.9898,78.233)*2.0)) * 43758.5453));
    vec4 tex = texture(InSampler, uv);
    raindrop(tex, fragCoord);
    
    mdf *= 3;
    
    vec4 col = tex * (1.1 - noise * mdf);

    // Output to screen
    fragColor = col;
}



void main() {
    vec4 inTexel = texture(InSampler, texCoord);
    vec4 controlTexel = texture(RawScreenSampler, vec2(0.5, 0.5));
    vec4 screenTexel = texture(RawScreenSampler, texCoord);
    //int contrCutsceneId = int(int(round(controlTexel.b * 255.0)) / 4.0) * 4;
    //int effectsCount = 4;
    int effectId = int(round(controlTexel.b * 255.0));
    float additionEffect = controlTexel.g;
    int screenEffectId = int(round(screenTexel.b * 255.0));
    vec4 prevTexel = texture(PrevSampler, texCoord);
    float time = round(prevTexel.r / prevTexel.a);
    //fragColor = vec4(0.0, 0.0, 0.0, 1.0);

    vec2 baseCoord = texCoord;
    fragColor = texture(InSampler, baseCoord);
    fragColor = texture(RawScreenSampler, baseCoord);
    return;

    if (round(screenTexel.r * 255) == 253.0)
    {
        int ef = int(round(screenTexel.b * 255.0));
        if (ef == 7) {
            //vec2 pixels = round(ScreenSize / vec2(640, 360));
            vec2 pixels = round(ScreenSize / vec2(320, 180));
            baseCoord = floor(texCoord * ScreenSize / pixels) * pixels / ScreenSize;
            fragColor = texture(InSampler, baseCoord);
        }
        if (ef == 8) {
            float t = time * 0.5;
            vec2 offset = vec2(cos(t), sin(t)) * oneTexel * 1.2;
            vec2 coord = baseCoord - offset * 5;
            vec4 col = vec4(0.0);
            for (int i = 0; i < 11; i++)
            {
                col += texture(InSampler, coord + offset * i);
            }
            col = col / 11;
            float param = (sin(time / 200.0 * 6.28) + 1.0) / 2.0;
            vec4 c1 = col * param;
            vec4 c2 = vec4(1.0 - col.r, 1.0 - col.g, 1.0 - col.b, 1.0) * (1.0 - param);
            vec4 final = vec4(1.0);
            final.r = max(c1.r, c2.r);
            final.g = max(c1.g, c2.g);
            final.b = max(c1.b, c2.b);
            fragColor = final;
        }
        if (ef == 9) {
            fragColor = texture(InSampler, vec2(1.0 - texCoord.x, texCoord.y));
        }
    }

    if (round(controlTexel.r * 255) != 253.0)
    {
        effectId = -1;
    } else {
        if (effectId == 1) {
            vec2 coord = texCoord;
            float depth = parseDepth(coord);
            vec4 col = inTexel;
            int check_blue = int(round(col.b * 255) + 0.5);
            if (check_blue % 2 == 1 && depth < 64.0) {
                // BLUE WORLD EFFECTS
                if (check_blue == 1) {
                    float zatemnenie = 0.0;
                    for (float i = 0; i < 16.0; i++) {
                        vec4 smpl = col;
                        smpl.r -= mod(smpl.r, oneTexel.r);
                        smpl.g -= mod(smpl.g, oneTexel.g);
                        int blue = int(round(smpl.b * 255) + 0.5);
                        float coldepth = parseDepth(coord);
                        if (blue % 2 == 0 || coldepth > 32.0) {
                            fragColor = col;
                            fragColor.rgb = mix(fragColor.rgb, vec3(0.0), zatemnenie);
                            return;
                        }
                        coord = texture(InSampler, coord).rg;
                        col = texture(InSampler, coord);
                        zatemnenie += 0.05;
                    }
                    fragColor = col;
                    int blue = int(round(col.b * 255) + 0.5);
                    if (blue % 2 == 1) {
                        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
                    }
                    fragColor.rgb = mix(fragColor.rgb, vec3(0.0), zatemnenie);
                }
            }
        }
        if (effectId == 2) {
            vec2 coord = texCoord;
            float depth = parseDepth(coord);
            vec4 col = inTexel;
            int check_blue = int(round(col.b * 255) + 0.5);
            if (check_blue % 2 == 1 && depth < 64.0) {
                // BLUE WORLD EFFECTS
                if (check_blue == 1) {
                    fragColor = vec4(1.0, 0.0, 0.0, 1.0);
                }
            }
        }
    }
}
