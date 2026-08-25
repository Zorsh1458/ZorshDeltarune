#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec3 Pos;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

#define iTime (GameTime*1000.0)
vec4 iMouse = vec4(0.0);
vec3 iResolution = vec3(ScreenSize.xy, 1.0);

#moj_import <minecraft:aurora.glsl>

#define BURST
#define NUM_LAYERS 5.

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c,-s,s,c);
}

float Star(vec2 uv, float a, float sparkle) {
    vec2 av1 = abs(uv);
 	vec2 av2 = abs(uv*Rot(a));
    vec2 av = min(av1, av2);
    
    vec3 col = vec3(0);
    float d = length(uv);
    float star = av1.x*av1.y;
    star = max(av1.x*av1.y, av2.x*av2.y);
    star = max(0., 1.-star*1e3);
    
    float m = min(5., 1e-2/d);
    
    return m+pow(star, 4.)*sparkle;
}

float Hash21(vec2 p) {
    p = fract(p*vec2(123.34,145.54));
    p += dot(p, p+45.23);
    return fract(p.x*p.y);
}

vec3 StarLayer(vec2 uv, float t, float sparkle) {
    vec2 gv = fract(uv)-.5;
    vec2 id = floor(uv);
	vec3 col = vec3(0);
    
    #ifndef BURST
    t = 0.;
    #endif
    
    for(int y=-1; y<=1; y++) {
        for(int x=-1; x<=1; x++) {
            vec2 offs = vec2(x, y);
            float n = Hash21(id-offs);
			vec3 N = fract(n*vec3(10,100,1000));
            vec2 p = (N.xy-.5)*.7;
            
            float brightness = Star(gv-p+offs, n*6.2831+t, sparkle);
            vec3 star = brightness*vec3(.6+p.x, .4, .6+p.y)*N.z*N.z;
            
            
            
            star *= 1.+sin((t+n)*20.)*smoothstep(sin(t)*.5+.5, 1., fract(10.*n));
            
            float d = length(gv+offs);
            
            col += star*smoothstep(1.5, .8, d);
        }
    }
    return col;
}

void mainImageStar( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = (fragCoord-.5*iResolution.xy)/iResolution.y;
    vec2 M = iMouse.xy/iResolution.xy;
    
    M *= 10.;
    
	float t = -iTime*.3;
	
    float twirl = sin(t*.1);
    twirl *= twirl*twirl*sin(dot(uv,uv));
    uv *= Rot(-t*.2);
    
    uv *= 2.+sin(t*.05);
    
    vec3 col = vec3(0);
    float speed = -.2;
    #ifdef BURST
    speed = .1;
    float bla = sin(t+sin(t+sin(t)*.5))*.5+.5;
    float d = dot(uv,uv);
    
    float a = atan(uv.x, uv.y);
    uv /= d;
    float burst = sin(iTime*.05);
    uv *= burst+.2;
    #endif
    
    float stp = 1./NUM_LAYERS;
        
    for(float i=0.; i<1.; i+=stp) {
    	float lt = fract(t*speed+i);
        float scale = mix(10., .25, lt);
        float fade = smoothstep(0., .4, lt)*smoothstep(1., .95, lt); 
        vec2 sv = uv*scale+i*134.53-M;
        //sv.x += t;
        col += StarLayer(sv, t, fade)*fade;
    }
    
    #ifdef BURST
    //t = iTime*.5;
    float burstFade = smoothstep(0., .02, abs(burst));
    float size = .9*sin(t)+1.;
    size = max(size, sqrt(size));
    float fade = size/d;
    col *= mix(1., fade, burstFade);
    col += fade*.2*vec3(1., .5, .1)*bla*burstFade;
    
    t*=1.5;
    
    a -= M.x*.1;
    float rays = sin(a*5.+t*3.)-cos(a*7.-t);
    rays *= sin(a+t+sin(a*4.)*10.)*.5+.5;
    col += rays*bla*.1*burstFade;
    col += 1.-burstFade;
    #else
    col *= 4.;
    #endif
    
    fragColor = vec4(col,1.0);
}

int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 8) & 1;
}

#define NUM_LAYERS_ZV 6.
#define ITER_ZV 14

vec4 tex(vec3 p, float timeOffset)
{
    float t = (GameTime + timeOffset) * 5.0 * 1000.0 + 78;
    t = floor(t * 2.0) / 2.0;
    vec4 o = vec4(p.xyz,3.*sin(t*.1));
    //vec4 o = vec4(p.xyz / (1.0 + (GameTime + timeOffset) * 1000.0),3.*sin(t*.1));
    vec4 dec = vec4 (1.,.9,.1,.15) + vec4(.06*cos(t*.1),0,0,.14*cos(t*.23));
    for (int i=0 ; i++ < ITER_ZV;) o.xzyw = abs(o/dot(o,o)- dec);
    return o;
}

float hueFromRgb(vec3 c) {
    float R = c.r / 255.0;
    float G = c.g / 255.0;
    float B = c.b / 255.0;
    float Min = min(R, min(G, B));
    float Max = max(R, max(G, B));
    if (Max == R) {
        return (G-B)/(Max-Min);
    }
    if (Max == G) {
        return 2.0 + (B-R)/(Max-Min);
    }
    return 4.0 + (R-G)/(Max-Min);
}

void mainImageField( out vec4 fragColor, in vec2 fragCoord, float timeOffset)
{
    vec2 uv = fragCoord;
    vec3 col = vec3(0);
    float t= (GameTime + timeOffset) * 1000.0 * .3;
    //t = 3034;
    
	for(float i=0.; i<=1.; i+=1./NUM_LAYERS_ZV)
    {
        float d = fract(i+t); // depth
        float s = mix(5.,.5,d); // scale
        float f = d * smoothstep(1.,.9,d); //fade
        col+= tex(vec3(uv*s,i*4.), timeOffset).xyz*f;
    }
    
    col/=NUM_LAYERS_ZV;
    col*=vec3(2,1.,2.);
   	col=pow(col,vec3(.5 ));

    float hue = hueFromRgb(col);
    float tParam = sin(hue * 6.283) * 0.2 + 0.5;
    col = vec3(tParam, tParam, (1.0 - tParam));
    col = vec3(0.0, 0.0, (1.0 - tParam));
    if (max(tParam - 0.3, 0.0) < 0.01) {
        col = vec3(1.0);
    }

    fragColor = vec4(col,0.0);
}

void main()
{
    int timeData = getTimeData();
    fragColor = apply_fog(ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, 0.0, FogSkyEnd, FogSkyEnd, FogSkyEnd, FogColor);
    //if (timeData == 1) {
    //    vec4 color1 = vec4(0.0);
    //    vec4 color2 = vec4(0.0);
    //    mainImageField(color1, (Pos.xz - vec2(0.5)) * 0.2, 14.0 / 16383.0);
    //    mainImageField(color2, (Pos.xz - vec2(0.5)) * 0.2, 0.0);
    //    float t = mod(floor(GameTime * 16383.0), 32.0) / 14.0;
    //    vec4 color = mix(color1, color2, min(max(t, 0.0), 1.0));
    //    if (length(Pos.xz) > 10) {
    //        fragColor = vec4(mix(fragColor.rgb, color.rgb, 10.0 / length(Pos.xz)), 1.0);
    //    } else {
    //        fragColor = vec4(color.rgb, 1.0);
    //    }
    //}
    if (timeData == 1) {
        vec3 p = Pos;
        vec4 color1 = vec4(0.0);
        vec4 color2 = vec4(0.0);
        float toff = 14.0 / 16383.0;
        float t1 = GameTime * 5000.0;
        float t2 = (GameTime + toff) * 5000.0;
        aurora_mainImage(color1, vec3(0.0), normalize(p), t2);
        aurora_mainImage(color2, vec3(0.0), normalize(p), t1);
        float t = mod(floor(GameTime * 16383.0), 32.0) / 14.0;
        fragColor = mix(color1, color2, min(max(t, 0.0), 1.0));
        fragColor.rgb = mix(vec3(0.0), fragColor.rgb, 1.0 / (1.0 + max(0.0, length(Pos.xz) - 64.0) * 0.1));
    }
    //vec2 pp = Pos.xz;
    //float ang = asin(pp.x / length(pp));
    //if (pp.y < 0)
    //{
    //    ang *= -1;
    //}
    //float t = 1.0;
    //pp *= 1.0 + sin(ang * 30 + t * 1000.0) * 0.01;
    //pp *= 1.0 + sin(ang * 30 + t * -1500.0) * 0.005;
    //pp *= 1.0 + sin(ang * 30 + t * 10000.0) * 0.001;
    //float x = pp.x;
    //float z = pp.y;
    //float dx = round(x * 4) / 4;
    //float dz = round(z * 4) / 4;
    //float tdist = sqrt(x*x + z*z);
    //float dist = sqrt(dx*dx + dz*dz);
    //float a = asin(dx / dist);
    //if (z < 0)
    //{
    //    a *= -1;
    //}
    //float appearance = (sin(t * 200.0) + 1.0) * 0.5;
    //dist += appearance * 20.0;
    //if (dist <= 10)
    //{
    //    mainImageStar(fragColor, vec2(x, z) / 20 * (1.0 + appearance * 10) + 0.5);
    //    //fragColor = mainCol * (1.0 - tdist / 5.0);
    //}
    //vec4 fogCol = apply_fog(ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, 0.0, FogSkyEnd, FogSkyEnd, FogSkyEnd, FogColor);
    //if (dist > 10)
    //{
    //    float d2 = dist - 10;
    //    //fragColor = mix(fogCol, vec4(fogCol.rgb * 0.4, 1.0), 1.0 / (1.0 + d2 * 0.5));
    //    float scale = sin(a * 60 + t * 5000.0) * 0.5 + 0.5;
    //    fragColor = mix(fogCol, vec4(vec3(30, 77, 80) * 3, 255) / 255.0, 1.0 / (1.0 + d2 * (scale * 0.5 + 1.0)));
    //    fragColor *= 1.0 + (1.0 + pow(sin(dist - t * 4000), 3)) * 0.5 / (1.0 + d2 * (scale * 0.5 + 1.0));
    //    fragColor *= 1.0 - appearance;
    //}
    //float time = floor(t * 1000 * 4) / 1000 / 4;
    //vec2 offseted = vec2(dx, dz) + (vec2(rand(vec2(dx, time)), rand(vec2(time, dz))) * 2 - 1) * 0.5;
    //vec2 offseted2 = vec2(dx, dz) + (vec2(rand(vec2(dz, time)), rand(vec2(time, dx))) * 2 - 1) * 0.5;
    //float offDist = length(offseted);
    //float offDist2 = length(offseted2);
    //offDist += appearance * 20.0;
    //offDist2 += appearance * 20.0;
    //if (offDist > 10 && offDist < 10.5)
    //{
    //    fragColor = vec4(fragColor.rgb * 1.2, 1.0);
    //    //fragColor = mix(fragColor, vec4(1.0, 0.0, 0.0, 1.0), 0.2);
    //}
    //if (offDist2 > 10 && offDist2 < 10.5)
    //{
    //    fragColor = vec4(fragColor.rgb * 0.8, 1.0);
    //    //fragColor = mix(fragColor, vec4(0.0, 1.0, 0.0, 1.0), 0.2);
    //}
    fragColor = REMOVE_BLUE(fragColor);
}
