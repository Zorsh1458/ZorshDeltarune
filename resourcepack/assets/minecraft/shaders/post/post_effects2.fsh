#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D GlowDepthSampler;
uniform sampler2D RawScreenSampler;

uniform sampler2D PrevMainSampler;
uniform sampler2D FlashSampler;
uniform sampler2D CrtSampler;
uniform sampler2D BloomSampler;

uniform sampler2D MeshokSampler;
uniform sampler2D CellularSampler;
uniform sampler2D CookieSampler;
uniform sampler2D PrevSampler;

//uniform vec3 ModelOffset;

in vec2 texCoord;
in vec2 oneTexel;
in vec3 blockTexCoord;
in float plFov;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

#define iTime (GameTime*1000.0)
vec3 iResolution = vec3(ScreenSize, 1.0);
const vec4 iMouse = vec4(0.0, 0.0, 0.0, 0.0);

out vec4 fragColor;

#define TAU 6.28318530718
#define MAX_ITER 5

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

vec3 hueShift( vec3 color, float hueAdjust ){

    const vec3  kRGBToYPrime = vec3 (0.299, 0.587, 0.114);
    const vec3  kRGBToI      = vec3 (0.596, -0.275, -0.321);
    const vec3  kRGBToQ      = vec3 (0.212, -0.523, 0.311);

    const vec3  kYIQToR     = vec3 (1.0, 0.956, 0.621);
    const vec3  kYIQToG     = vec3 (1.0, -0.272, -0.647);
    const vec3  kYIQToB     = vec3 (1.0, -1.107, 1.704);

    float   YPrime  = dot (color, kRGBToYPrime);
    float   I       = dot (color, kRGBToI);
    float   Q       = dot (color, kRGBToQ);
    float   hue     = atan (Q, I);
    float   chroma  = sqrt (I * I + Q * Q);

    hue += hueAdjust;

    Q = chroma * sin (hue);
    I = chroma * cos (hue);

    vec3    yIQ   = vec3 (YPrime, I, Q);

    return vec3( dot (yIQ, kYIQToR), dot (yIQ, kYIQToG), dot (yIQ, kYIQToB) );
}

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float maxStrength = 0.5;
float minStrength = 0.125;

float speed = 10.00;

float random (vec2 noise)
{
    //--- Noise: Low Static (X axis) ---
    //return fract(sin(dot(noise.yx,vec2(0.000128,0.233)))*804818480.159265359);
    
    //--- Noise: Low Static (Y axis) ---
    //return fract(sin(dot(noise.xy,vec2(0.000128,0.233)))*804818480.159265359);
    
  	//--- Noise: Low Static Scanlines (X axis) ---
    //return fract(sin(dot(noise.xy,vec2(98.233,0.0001)))*925895933.14159265359);
    
   	//--- Noise: Low Static Scanlines (Y axis) ---
    //return fract(sin(dot(noise.xy,vec2(0.0001,98.233)))*925895933.14159265359);
    
    //--- Noise: High Static Scanlines (X axis) ---
    //return fract(sin(dot(noise.xy,vec2(0.0001,98.233)))*12073103.285);
    
    //--- Noise: High Static Scanlines (Y axis) ---
    //return fract(sin(dot(noise.xy,vec2(98.233,0.0001)))*12073103.285);
    
    //--- Noise: Full Static ---
    return fract(sin(dot(noise.xy,vec2(10.998,98.233)))*12433.14159265359);
}

/*
float random_colour (float noise)
{
    return fract(sin(noise));   
}
*/

void vhs(out vec4 fragColour, in vec2 fragCoord, vec3 background)
{
    
    vec2 uv = fragCoord.xy / iResolution.xy;
    vec2 uv2 = fract(fragCoord.xy/iResolution.xy*fract(sin(iTime*speed)));
    
    //--- Strength animate ---
    maxStrength = clamp(sin(iTime/2.0),minStrength,maxStrength);
    //-----------------------
    
    //--- Black and white ---
    vec3 colour = vec3(random(uv2.xy))*maxStrength;
    //-----------------------
        
    /*
    //--- Colour ---
    colour.r *= random_colour(sin(iTime*speed));
    colour.g *= random_colour(cos(iTime*speed));
    colour.b *= random_colour(tan(iTime*speed));
    //--------------
    */
    
    //--- Background ---
    //vec3 background = vec3(texture(InSampler, uv));
    //--------------
    
    fragColour = vec4(background-colour,1.0);
}


#define S(a, b, t) smoothstep(a, b, t)
//#define CHEAP_NORMALS
//#define HAS_HEART
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
    float T = iTime+M.x*2.;
    
    #ifdef HAS_HEART
    T = mod(iTime, 102.);
    T = mix(T, M.x*102., M.z>0.?1.:0.);
    #endif
    
    
    float t = T*.2;
    
    float rainAmount = iMouse.z>0. ? M.y : sin(T*.05)*.3+.7;
    
    float maxBlur = mix(3., 6., rainAmount);
    float minBlur = 2.;
    
    float story = 0.;
    float heart = 0.;
    
    #ifdef HAS_HEART
    story = S(0., 70., T);
    
    t = min(1., T/70.);						// remap drop time so it goes slower when it freezes
    t = 1.-t;
    t = (1.-t*t)*70.;
    
    float zoom= mix(.3, 1.2, story);		// slowly zoom out
    uv *=zoom;
    minBlur = 4.+S(.5, 1., story)*3.;		// more opaque glass towards the end
    maxBlur = 6.+S(.5, 1., story)*1.5;
    
    vec2 hv = uv-vec2(.0, -.1);				// build heart
    hv.x *= .5;
    float s = S(110., 70., T);				// heart gets smaller and fades towards the end
    hv.y-=sqrt(abs(hv.x))*.5*s;
    heart = length(hv);
    heart = S(.4*s, .2*s, heart)*s;
    rainAmount = heart;						// the rain is where the heart is
    
    maxBlur-=heart;							// inside the heart slighly less foggy
    uv *= 1.5;								// zoom out a bit more
    t *= .25;
    #else
    float zoom = -cos(T*.2);
    uv *= .7+zoom*.3;
    #endif
    UV = (UV-.5)*(.9+zoom*.1)+.5;
    
    float staticDrops = S(-.5, 1., rainAmount)*2.;
    float layer1 = S(.25, .75, rainAmount);
    float layer2 = S(.0, .5, rainAmount);
    
    
    vec2 c = Drops(uv, t, staticDrops, layer1, layer2);
   #ifdef CHEAP_NORMALS
    	vec2 n = vec2(dFdx(c.x), dFdy(c.x));// cheap normals (3x cheaper, but 2 times shittier ;))
    #else
    	vec2 e = vec2(.001, 0.);
    	float cx = Drops(uv+e, t, staticDrops, layer1, layer2).x;
    	float cy = Drops(uv+e.yx, t, staticDrops, layer1, layer2).x;
    	vec2 n = vec2(cx-c.x, cy-c.x);		// expensive normals
    #endif
    
    
    #ifdef HAS_HEART
    n *= 1.-S(60., 85., T);
    c.y *= 1.-S(80., 100., T)*.8;
    #endif
    
    float focus = mix(maxBlur-c.y, minBlur, S(.1, .2, c.x));
    vec3 col = textureLod(InSampler, UV+n, focus).rgb;
    
    
    #ifdef USE_POST_PROCESSING
    t = (T+3.)*.5;										// make time sync with first lightnoing
    float colFade = sin(t*.2)*.5+.5+story;
    col *= mix(vec3(1.), vec3(.8, .9, 1.3), colFade);	// subtle color shift
    float fade = S(0., 10., T);							// fade in at the start
    float lightning = sin(t*sin(t*10.));				// lighting flicker
    lightning *= pow(max(0., sin(t+sin(t))), 10.);		// lightning flash
    col *= 1.+lightning*fade*mix(1., .1, story*story);	// composite lightning
    col *= 1.-dot(UV-=.5, UV);							// vignette
    											
    #ifdef HAS_HEART
    	col = mix(pow(col, vec3(1.2)), col, heart);
    	fade *= S(102., 97., T);
    #endif
    
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


#define AA 1
const float tau =atan(1.)*8.;

float radarSize = 0.3;

vec3 iRes = vec3(vec2(1.0) * ScreenSize.y * radarSize, 1.0);

highp float hash(float x) {
    return fract(sin(x)*43758.5453);
}
highp float rand2(vec2 co) {
    return fract(sin(mod(dot(co.xy ,vec2(12.9898,78.233)),3.14)) * 43758.5453);
}
float mark(int n,float ang2,vec2 uv,float mn,float mx){
	float aa=float(AA)/iRes.y;
    ang2-=tau/float(n)/2.;
	return smoothstep(mn,mx,distance(uv,vec2(0)))*smoothstep(aa*2.,0.,abs(fract(ang2/tau*float(n))-0.5)/float(n)*tau*distance(uv,vec2(0)));
}
float circle(float dist,vec2 uv){
	float aa=float(AA)/iRes.y;
	return (smoothstep(dist-aa*2.,dist-aa,distance(uv,vec2(0)))-smoothstep(dist,dist+aa,distance(uv,vec2(0))))/10.;
}
float point(vec2 coord,vec2 uv,float ang){
	float aa=float(AA)/iRes.y;
	return smoothstep(0.004+aa,0.002,distance(uv,coord))*smoothstep(tau*0.7,0.,ang)/2.;
}

float tm(float o) {
    return (sin(iTime + o) * 0.5 + 0.5) + 0.5;
}

void mainImageRadar( out vec4 o, in vec2 fragCoord ){
    vec2 uv = (fragCoord-iRes.xy/2.)/iRes.y;
	float aa=float(AA)/iRes.y;
	
    //uv.x+=(rand2(uv*iTime)-0.5)/100.*smoothstep(0.7,1.,hash(floor(iTime)));
    
    float dist=distance(uv,vec2(0));
	float sdist=smoothstep(0.5+aa,0.5,dist);
    
    float ang=iTime*tau/5.;
	float ang2=atan(uv.x,uv.y);
    ang=mod(ang2+ang,tau);
    
    float col=smoothstep(1.,0.,ang);
    col/=2.;
    col+=smoothstep(tau-aa/distance(uv,vec2(0)),tau,ang)/2.;
	col+=sdist/10.;
    col+=smoothstep(aa,0.,abs(uv.x))/4.;
    col+=smoothstep(aa,0.,abs(uv.y))/4.;
    col+=smoothstep(aa,0.,abs(uv.y - uv.x * 0.5774))/4.;
    col+=smoothstep(aa,0.,abs(uv.y * 0.5774 - uv.x))/4.;
    col+=smoothstep(aa,0.,abs(uv.y + uv.x * 0.5774))/4.;
    col+=smoothstep(aa,0.,abs(uv.y * 0.5774 + uv.x))/4.;
	
    col+=mark(9*4,ang2,uv,0.45,0.5)/2.;
    col+=mark(9*4*5,ang2,uv,0.475,0.5)/5.;
    col+=circle(0.0 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.1 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.2 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.3 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.4 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.5 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.6 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.7 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.8 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(0.9 + mod(iTime * 0.05, 0.1), uv);
    col+=circle(1.0 + mod(iTime * 0.05, 0.1), uv);
   	//col+=point(vec2(0.3,0.1),uv,ang);
    //col+=point(vec2(-0.2,-0.3),uv,ang);
    //col+=point(vec2(0.2,-0.1),uv,ang);
    
    o = mix(vec4(0,col,0,0),vec4(0.055,0.089,0.0,0)/1.3,1.-sdist);
	o=o;
    
    float l=cos(fragCoord.y);
    l*=l;
    l/=3.;
    l+=0.6+rand2(uv*iTime);
    
    o*=l;
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

vec4 smearedScreen(vec2 texCoord) {
    vec2 pixel = round(texCoord * ScreenSize);
    if (mod(pixel.x, 2.0) == 0.0) {
        return texture(InSampler, texCoord + vec2(oneTexel.x, 0.0));
    }
    return texture(InSampler, texCoord - vec2(oneTexel.x, 0.0));
}









float map(float value, float min1, float max1, float min2, float max2) {
  return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

vec2 curve(vec2 uv)
{
	uv = (uv - 0.5) * 2.0;
	uv *= 1.1;	
	uv.x *= 1.0 + pow((abs(uv.y) / 5.0), 2.0);
	uv.y *= 1.0 + pow((abs(uv.x) / 4.0), 2.0);
	uv  = (uv / 2.0) + 0.5;
	uv =  uv *0.92 + 0.04;
	return uv;
}

void CRT( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 q = fragCoord.xy / iResolution.xy;
    vec2 uv = q;
    uv = curve( uv );
    vec3 oricol = texture( InSampler, vec2(q.x,q.y) ).xyz;
    vec3 col;
	float x =  sin(0.3*iTime+uv.y*21.0)*sin(0.7*iTime+uv.y*29.0)*sin(0.3+0.33*iTime+uv.y*31.0)*0.0017;

    col.r = texture(InSampler,vec2(x+uv.x+0.001,uv.y+0.001)).x+0.05;
    col.g = texture(InSampler,vec2(x+uv.x+0.000,uv.y-0.002)).y+0.05;
    col.b = texture(InSampler,vec2(x+uv.x-0.002,uv.y+0.000)).z+0.05;
    col.r += 0.08*texture(InSampler,0.75*vec2(x+0.025, -0.027)+vec2(uv.x+0.001,uv.y+0.001)).x;
    col.g += 0.05*texture(InSampler,0.75*vec2(x+-0.022, -0.02)+vec2(uv.x+0.000,uv.y-0.002)).y;
    col.b += 0.08*texture(InSampler,0.75*vec2(x+-0.02, -0.018)+vec2(uv.x-0.002,uv.y+0.000)).z;

    col = clamp(col*0.6+0.4*col*col*1.0,0.0,1.0);

    float vig = (0.0 + 1.0*16.0*uv.x*uv.y*(1.0-uv.x)*(1.0-uv.y));
	col *= vec3(pow(vig,0.3));

    col *= vec3(0.95,1.05,0.95);
	col *= 2.8;

	float scans = clamp( 0.35+0.35*sin(3.5*iTime+uv.y*iResolution.y*1.5), 0.0, 1.0);
	
	float s = pow(scans,1.7);
	col = col*vec3( 0.4+0.7*s) ;

    col *= 1.0+0.01*sin(110.0*iTime);
	if (uv.x < 0.0 || uv.x > 1.0)
		col *= 0.0;
	if (uv.y < 0.0 || uv.y > 1.0)
		col *= 0.0;
	
	col*=1.0-0.65*vec3(clamp((mod(fragCoord.x, 2.0)-1.0)*2.0,0.0,1.0));
	
    float comp = smoothstep( 0.1, 0.9, sin(iTime) );
 
	// Remove the next line to stop cross-fade between original and postprocess
//	col = mix( col, oricol, comp );

    fragColor = vec4(col,1.0);
}

bool isOutlineVisible()
{
    vec4 screenTexel = texture(RawScreenSampler, texCoord);
    float redRound = screenTexel.r;
    float redLeft = screenTexel.g;
    float outlineDepth = (redRound + redLeft / 255.0) / 0.9;
    float inDepth = parseDepth(texCoord) / 100.0;
    bool result = outlineDepth < inDepth;
    float sampleDistX = oneTexel.x * 5;
    float sampleDistY = oneTexel.y * 5;
    if (texture(RawScreenSampler, texCoord + vec2(sampleDistX, 0.0)).rgb == vec3(0.0)) {
        return false;
    }
    if (texture(RawScreenSampler, texCoord + vec2(-sampleDistX, 0.0)).rgb == vec3(0.0)) {
        return false;
    }
    if (texture(RawScreenSampler, texCoord + vec2(0.0, sampleDistY)).rgb == vec3(0.0)) {
        return false;
    }
    if (texture(RawScreenSampler, texCoord + vec2(0.0, -sampleDistY)).rgb == vec3(0.0)) {
        return false;
    }
    return result;
}

vec4 glassEffect2(vec2 texCoord, float strength)
{
    vec4 color = vec4(0.0);
    int radius = 5;
    int count = 0;
    for (int x = -radius; x <= radius; x++)
    {
        for (int y = -radius; y <= radius; y++)
        {
            color += vec4(rand(texCoord + vec2(x * oneTexel.x, y * oneTexel.y)), rand(texCoord + vec2(1.0, 1.0) + vec2(x * oneTexel.x, y * oneTexel.y)), 0.0, 1.0);
            count++;
        }
    }
    color /= count;
    color = color * 2 - 1.0;
    float x = color.r * strength;
    float y = color.g * strength;
    return texture(InSampler, texCoord + vec2(x * oneTexel.x, y * oneTexel.y));
}

vec4 glassEffect(vec2 texCoord)
{
    vec4 color = vec4(0.0);
    int radius = 5;
    int count = 0;
    for (int x = -radius; x <= radius; x++)
    {
        for (int y = -radius; y <= radius; y++)
        {
            color += vec4(rand(texCoord + vec2(x * oneTexel.x, y * oneTexel.y)), rand(texCoord + vec2(1.0, 1.0) + vec2(x * oneTexel.x, y * oneTexel.y)), 0.0, 1.0);
            count++;
        }
    }
    color /= count;
    color = color * 2 - 1.0;
    float x = color.r * 50.0;
    float y = color.g * 50.0;
    return texture(InSampler, texCoord + vec2(x * oneTexel.x, y * oneTexel.y));
}

void main() {
    vec4 inTexel = texture(InSampler, texCoord);
    vec4 controlTexel = texture(RawScreenSampler, vec2(0.5, 0.5));
    vec4 screenTexel = texture(RawScreenSampler, texCoord);
    int contrCutsceneId = int(int(round(controlTexel.g * 255.0 * 256.0 + controlTexel.b * 255.0)) / 4.0) * 4;
    int effectsCount = 4;
    int effectId = int(contrCutsceneId / effectsCount) - 1000;
    int screenEffectId = int(round(screenTexel.b * 255.0));
    vec4 prevTexel = texture(PrevSampler, texCoord);
    float time = round(prevTexel.r / prevTexel.a);
    fragColor = inTexel;
    //fragColor = vec4(0.0, 0.0, 0.0, 1.0);

    if (round(controlTexel.r * 255) != 253.0)
    {
        effectId = -1;
    } else {
        if (effectId == 0)
        {
            fragColor = vec4(vec3(1.0) - inTexel.rgb, 1.0);
        }
        if (effectId == 1)
        {
            fragColor = vec4(hueShift(inTexel.rgb, -0.5), 1.0);
        }
        if (effectId == 2)
        {
            fragColor = vec4(hueShift(inTexel.rgb, GameTime * 1000.0), 1.0);
        }
        if (effectId == 3)
        {
            int count = int((sin(GameTime * 1000) + 1) * 4) + 12;
            inTexel.r = floor(inTexel.r * count) / count;
            inTexel.g = floor(inTexel.g * count) / count;
            inTexel.b = floor(inTexel.b * count) / count;
            fragColor = inTexel;
        }
        if (effectId == 4)
        {
            float a = sin(GameTime * 1000.0) * 0.15;
            float x = texCoord.x - 0.5;
            float y = texCoord.y - 0.5;
            x *= ScreenSize.x / ScreenSize.y;
            vec2 v = vec2(x * cos(a) + y * sin(a), x * -sin(a) + y * cos(a));
            v.x /= ScreenSize.x / ScreenSize.y;
            v *= 1.0 - abs(a);
            v += 0.5;
            fragColor = texture(InSampler, v);
        }
        if (effectId == 5)
        {
            fragColor = texture(InSampler, texCoord + vec2(rand(vec2(GameTime, 0.0)), rand(vec2(0.0, GameTime))) * 0.003);
        }
        if (effectId == 6)
        {
            fragColor = texture(InSampler, texCoord + vec2(rand(vec2(GameTime, 0.0)), rand(vec2(0.0, GameTime))) * 0.008);
        }
        if (effectId == 7)
        {
            fragColor = texture(InSampler, texCoord + vec2(rand(vec2(GameTime, 0.0)), rand(vec2(0.0, GameTime))) * 0.011);
        }
        if (effectId == 8)
        {
            fragColor = texture(InSampler, texCoord + vec2(rand(vec2(GameTime, 0.0)), rand(vec2(0.0, GameTime))) * 0.02);
        }
        if (effectId == 9)
        {
            fragColor = texture(InSampler, texCoord + vec2(rand(vec2(GameTime, 0.0)), rand(vec2(0.0, GameTime))) * (sin(GameTime * 1000.0) + 1) * 0.01);
        }
        if (effectId == 10)
        {
            fragColor = texture(InSampler, vec2(1.0) - texCoord);
            return;
        }
        if (effectId == 11)
        {
            fragColor = vec4(texture(InSampler, texCoord - vec2(0.002, 0.0) * abs(sin(GameTime * 1000.0))).r, inTexel.g, texture(InSampler, texCoord + vec2(0.002, 0.0) * abs(sin(GameTime * 1000.0))).b, inTexel.a);
        }
        if (effectId == 12)
        {
            vec2 coord = texCoord;
            coord.x -= 0.5;
            coord.x *= 1.15 - cos(abs(coord.y - 0.5) * 2.0) * 0.15;
            if (abs(coord.x) > 0.5) {
                fragColor = vec4(vec3(0.0), 1.0);
            } else {
                coord.x += 0.5;
                vec3 background = vec3(texture(InSampler, coord - vec2(0.002, 0.0)).r, texture(InSampler, coord).g, texture(InSampler, coord + vec2(0.002, 0.0)).b) * 0.8;
                vhs(fragColor, texCoord * ScreenSize, background);
            }
        }
        if (effectId == 13)
        {
            raindrop(fragColor, texCoord * ScreenSize);
        }
        if (effectId == 14)
        {
            fragColor = vec4(0.0, 0.0, 0.0, 1.0);
            if (length(inTexel.rgb - texture(InSampler, vec2(0.5, 0.5)).rgb) < 0.2) {
                fragColor = inTexel;
            }
        }
        if (effectId == 15)
        {
            float t = LinearizeDepth(texture(DepthSampler, texCoord).r) * 0.1;
            vec3 fogColor = hueShift(vec3(181, 229, 245) / 255.0, GameTime*1000.0);
            fragColor = vec4(mix(inTexel.rgb, fogColor, min(t, 1.0)), 1.0);
        }
        if (effectId == 16)
        {
            float t = LinearizeDepth(texture(DepthSampler, texCoord).r) * 0.1;
            vec3 fogColor = vec3(82, 188, 100) / 255.0;
            fragColor = vec4(mix(inTexel.rgb, fogColor, min(t, 1.0)), 1.0);
        }
        if (effectId == 17)
        {
            float t = LinearizeDepth(texture(DepthSampler, texCoord).r) * 0.1;
            vec3 fogColor = vec3(39, 95, 49) / 255.0;
            fragColor = vec4(mix(inTexel.rgb, fogColor, min(t, 1.0)), 1.0);
        }
        if (effectId == 18)
        {
            float t = LinearizeDepth(texture(DepthSampler, texCoord).r) * 0.1;
            vec3 fogColor = vec3(108, 123, 150) / 255.0;
            fragColor = vec4(mix(inTexel.rgb, fogColor, min(t, 1.0)), 1.0);
        }
        if (effectId == 19)
        {
            fragColor = inTexel;
            float t = LinearizeDepth(texture(DepthSampler, texCoord).r);
            if (t < LinearizeDepth(texture(DepthSampler, texCoord - vec2(0.002, 0.0)).r) && t < LinearizeDepth(texture(DepthSampler, texCoord + vec2(0.002, 0.0)).r)) {
                fragColor = vec4(0.0, 0.0, 0.0, 1.0);
            }
            if (t < LinearizeDepth(texture(DepthSampler, texCoord - vec2(0.0, 0.002)).r) && t < LinearizeDepth(texture(DepthSampler, texCoord + vec2(0.0, 0.002)).r)) {
                fragColor = vec4(0.0, 0.0, 0.0, 1.0);
            }
        }
        if (effectId == 20)
        {
            float add = 0.008;
            fragColor = inTexel;
            float depthMod = 30.0;
            float t = parseDepth(texCoord);
            if (t < 100.0 && mod(t - GameTime * 10000.0, depthMod) < 3.0) {
                float m = mod(t - GameTime * 10000.0, depthMod) / 3.0;
                fragColor = mix(inTexel, vec4(0.0, 1.0, 1.0, 1.0), m);
            }
            if (t < 100.0 && mod(t - GameTime * 10000.0 + 7.0, depthMod) < 10.0) {
                float m = mod(t - GameTime * 10000.0 + 7.0, depthMod) / 10.0;
                float t2 = parseDepth(texCoord);
                if (t2 < parseDepth(texCoord - vec2(0.002, 0.0) * m) && t2 < parseDepth(texCoord + vec2(0.002, 0.0) * m)) {
                    fragColor = mix(fragColor, vec4(0.0, 1.0, 1.0, 1.0), m);
                }
                if (t2 < parseDepth(texCoord - vec2(0.0, 0.002) * m) && t2 < parseDepth(texCoord + vec2(0.0, 0.002) * m)) {
                    fragColor = mix(fragColor, vec4(0.0, 1.0, 1.0, 1.0), m);
                }
            }
            vec2 cent = vec2(radarSize / 2.0 + add, 1.0 - radarSize / 2.0 - add);
            vec2 coord = texCoord;
            coord.x *= ScreenSize.x / ScreenSize.y;
            if (length(coord - cent) < (radarSize / 2.0 + add)) {
                mainImageRadar(fragColor, (texCoord + vec2(-add * ScreenSize.y / ScreenSize.x, add) - vec2(0.0, 1.0 - radarSize)) * ScreenSize);
            }
        }
        if (effectId == 21)
        {
            float add = 0.008;
            fragColor = inTexel;
            float depthMod = 30.0;
            float t = LinearizeDepth(texture(DepthSampler, texCoord).r);
            float fov = 80.0;
            float xPos = texCoord.x * 2.0 - 1.0;
            float center = 1.0 / tan(fov / 2.0);
            float xAngle = atan(xPos / center);
            t /= cos(xAngle);
            vec2 uv = texCoord * 2.0 - 1.0;
            uv.x *= ScreenSize.x / ScreenSize.y;
            float dist = length(uv);
            float exposure = 2.;
            float AOE = 8.;
            vec2 uv2 = texCoord;
            float d = sqrt(pow((uv2.x - 0.5),2.0) + pow((uv2.y - 0.5),2.0));
            d = exp(-(d * AOE)) * exposure / pow(t, 1.2);
            fragColor = vec4(mix(inTexel.rgb*clamp(d,0.0,10.0) * 10.0, vec3(0.0), smoothstep(0.5, 1.0, dist * 0.5)), 1.0);
        }
        if (effectId == 22)
        {
            fragColor = texture(FlashSampler, texCoord);
            float depth = LinearizeDepth(texture(DepthSampler, texCoord).r);
            vec2 uv = texCoord * 2.0 - 1.0;
            if (screenTexel.rgb == vec3(1.0)) {
                fragColor = mix(vec4(1.0), vec4(1.0, 0.0, 0.0, 1.0), sin(GameTime * 5000.0) * 0.5 + 0.5);
            }
            for (int a = 1; a <= 30; a++) {
                vec2 offset = vec2(sin(a * 12), cos(a * 12));
                float scal = 40.0 + 10.0 * sin(GameTime * 2000.0);
                if (texture(RawScreenSampler, texCoord + oneTexel * offset * scal).rgb == vec3(1.0) &&
                    texture(RawScreenSampler, texCoord + oneTexel * offset * (scal-1)).rgb != vec3(1.0)) {
                    fragColor = mix(vec4(1.0), vec4(1.0, 0.0, 0.0, 1.0), sin(GameTime * 5000.0) * 0.5 + 0.5);
                }
            }
        }
        if (effectId == 23)
        {
            vec4 inTexel = texture(InSampler, floor(texCoord * ScreenSize / 4.0) / ScreenSize * 4.0);
            vec2 uv = texCoord * 2.0 - 1.0;
            fragColor = vec4(mix(vec3(inTexel.r * 2.5, inTexel.g * 7.0, inTexel.b * 2.5), vec3(0.0), length(uv)), inTexel.a);
        }
        if (effectId == 24)
        {
            float t = LinearizeDepth(texture(DepthSampler, texCoord).r);
            float fov = 80.0;
            float xPos = texCoord.x * 2.0 - 1.0;
            float center = 1.0 / tan(fov / 2.0);
            float xAngle = atan(xPos / center);
            t /= cos(xAngle);
            fragColor = vec4(hueShift(inTexel.rgb, GameTime * 1000.0 + t * 5.0), 1.0);
        }
        if (effectId == 25)
        {
            vec2 uv = texCoord * 2.0 - 1.0;
    
            float speed = 4.0;
            float turbulence = 10.0;
            float dist = length(uv);
            vec2 center = vec2(0.5,0.5);

            uv += uv/dist * cos(dist*turbulence-GameTime*1000.0*speed)*0.08;

            //map back from -1 to 1 back to 0 1 for texture sample
            uv = uv * 0.5 + 0.5;
            fragColor = vec4(hueShift(texture(InSampler, uv).rgb, GameTime * 1000.0), 1.0);
        }
        if (effectId == 26)
        {
            fragColor = texture(InSampler, vec2(texCoord.x * -1 + 1, texCoord.y));
        }
        if (effectId == 27)
        {
            vec2 uv = texCoord;
            uv *= 2.0;
            uv -= 0.5;
            uv.y += 0.15;
            //fragColor = texture(InSampler, uv);
            fragColor = mix(inTexel, vec4(1.0, 0.75, 0.0, 1.0), 0.1);
            if (uv.x < 0.0) {
                fragColor = vec4(mix(vec3(0.2), vec3(0.0), smoothstep(0.0, 0.05, uv.x * -1)), 1.0);
            }
            if (uv.x > 1.0) {
                fragColor = vec4(mix(vec3(0.2), vec3(0.0), smoothstep(1.0, 1.05, uv.x)), 1.0);
            }
            if (uv.y < 0.0) {
                fragColor = vec4(mix(vec3(0.2), vec3(0.0), smoothstep(0.0, 0.05, uv.y * -1)), 1.0);
            }
            if (uv.y > 1.0) {
                fragColor = vec4(mix(vec3(0.2), vec3(0.0), smoothstep(1.0, 1.05, uv.y)), 1.0);
            }
        }
        if (effectId == 28)
        {
            vec2 pixel = round(texCoord * ScreenSize);
            if (mod(pixel.y, 2.0) == 0.0) {
                fragColor = smearedScreen(texCoord + vec2(0.0, oneTexel.y));
            } else {
                fragColor = smearedScreen(texCoord - vec2(0.0, oneTexel.y));
            }
        }
        if (effectId == 29) {
            float distance = parseDepth(texCoord);
            distance *= 7.9;
            float scale = 3.0;
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, 1.0));
            fragColor = vec4(mix(inTexel.rgb, vec3(0.0), clamp(distance, 0.0, 1.0)), 1.0);
        }
        if (effectId == 30) {
            float distance = parseDepth(texCoord);
            distance *= -7.9;
            float scale = 3.0;
            distance += parseDepth(texCoord + oneTexel * scale * vec2(1.0, 1.0));
            distance += parseDepth(texCoord + oneTexel * scale * vec2(1.0, 0.0));
            distance += parseDepth(texCoord + oneTexel * scale * vec2(1.0, -1.0));
            distance += parseDepth(texCoord + oneTexel * scale * vec2(0.0, -1.0));
            distance += parseDepth(texCoord + oneTexel * scale * vec2(-1.0, -1.0));
            distance += parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 0.0));
            distance += parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 1.0));
            distance += parseDepth(texCoord + oneTexel * scale * vec2(0.0, 1.0));
            fragColor = vec4(mix(inTexel.rgb, vec3(0.0), clamp(distance, 0.0, 1.0)), 1.0);
        }
        if (effectId == 31)
        {
            float distance = parseDepth(texCoord);
            distance *= 7.9;
            float scale = 3.0;
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, 1.0));
            int count = 8;
            inTexel.r = floor(inTexel.r * count) / count;
            inTexel.g = floor(inTexel.g * count) / count;
            inTexel.b = floor(inTexel.b * count) / count;
            vec3 final = mix(inTexel.rgb, vec3(0.0), clamp(distance, 0.0, 1.0));
            vec3 fogColor = vec3(183, 211, 235) / 255.0;
            fragColor = vec4(mix(final, fogColor, clamp(parseDepth(texCoord) / 50.0, 0.0, 1.0)), 1.0);
        }
        if (effectId == 32) {
            vec2 uv = texCoord;
            uv *= 2.0;
            uv -= 0.5;
            //uv.y += 0.15;
            //fragColor = texture(InSampler, uv);
            //fragColor = mix(inTexel, vec4(1.0, 0.75, 0.0, 1.0), 0.1);
            vec4 tex = texture(CrtSampler, texCoord * vec2(1.0, -1.0));
            if (tex.g > 0.2 && tex.r < 0.2 && tex.b < 0.2) {
                CRT(fragColor, uv * ScreenSize);
                int count = 4;
                fragColor.r = floor(fragColor.r * count) / count;
                fragColor.g = floor(fragColor.g * count) / count;
                fragColor.b = floor(fragColor.b * count) / count;
            } else {
                fragColor = tex;
            }
        }
        if (effectId == 33)
        {
            float gray = 0.2989 * inTexel.r + 0.5870 * inTexel.g + 0.1140 * inTexel.b;
            float dist = parseDepth(texCoord);
            fragColor = vec4(mix(inTexel.rgb * 2.0, vec3(gray), clamp(map(dist, 5.0, 6.0, 0.0, 1.0), 0.0, 1.0)), 1.0);
            fragColor = vec4(mix(inTexel.rgb, fragColor.rgb, clamp(map(dist, 4.0, 5.0, 0.0, 1.0), 0.0, 1.0)), 1.0);
        }
        if (effectId == 34)
        {
            vec4 bloom = texture(BloomSampler, texCoord);
            //fragColor = bloom;
            //return;
            float brightness = dot(bloom.rgb, vec3(0.2126, 0.7152, 0.0722));
            fragColor.r = max(bloom.r, inTexel.r);
            fragColor.g = max(bloom.g, inTexel.g);
            fragColor.b = max(bloom.b, inTexel.b);
        }
        if (effectId == 35)
        {
            float depth = pow(parseDepth(texCoord), 1.0);
            if (depth < 100)
            {
                fragColor = texture(InSampler, vec2(texCoord.x, texCoord.y - 0.01 * depth));
            }
        }
        if (effectId == 36)
        {
            float depth = pow(parseDepth(texCoord), 1.0);
            if (depth > 10)
            {
                fragColor = vec4(0.0);
                //fragColor += vec4(0.0);
            }
        }
        if (effectId == 37)
        {
            float gray = 0.2989 * inTexel.r + 0.5870 * inTexel.g + 0.1140 * inTexel.b;
            float depth = pow(parseDepth(texCoord), 1.0);
            fragColor = vec4(mix(inTexel.rgb, vec3(gray * 0.35), clamp(depth / 30.0, 0.0, 1.0)), 1.0);
        }
        if (effectId == 38)
        {
            float depth = pow(parseDepth(texCoord), 1.0);
            vec4 final = finalWater((texCoord - 0.5) * depth * 0.3 + 0.5);
            fragColor = inTexel * (1.0 + final.x * 5.0 / depth);
        }
        if (effectId == 39)
        {
            fragColor = inTexel;
            if (round(fragColor.b * 255.0) > 250 && round(fragColor.r * 255.0) <= 31 && round(fragColor.g * 255.0) <= 31)
            {
                //fragColor = vec4(1.0, 0.0, 0.0, 1.0);
                float x = fragColor.r * 8.0;
                float y = fragColor.g * 8.0;
                fragColor = vec4(x, y, 0.0, 1.0);
            }
        }
        if (effectId == 40)
        {
            fragColor = inTexel;
            fragColor.r = mod(blockTexCoord.x, 128.0) / 128.0;
            fragColor.g = mod(blockTexCoord.x, 16.0) / 16.0;
        }
        if (effectId == 42)
        {
            float outlineDepth = screenTexel.r * 2;
            float inDepth = parseDepth(texCoord) / 100.0;
            fragColor = vec4(inDepth);
            if (screenTexel.rgb != vec3(0.0))
            {
                fragColor = vec4(outlineDepth);
            }
        }
        if (effectId == 43) {
            float depth = parseDepth(texCoord);
            float distance = depth;
            distance *= 7.9;
            float scale = 100.0 / (20.0 + depth);
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, 1.0));
            fragColor = vec4(mix(inTexel.rgb, inTexel.rgb * 1.5, clamp(distance, 0.0, 1.0)), 1.0);
        }
        if (effectId == 44)
        {
            fragColor = vec4(hueShift(inTexel.rgb, 0.85 * 3.1415), 1.0);
        }
        if (effectId == 45)
        {
            fragColor = vec4(0.5 * fragColor.r + 0.5 * fragColor.g, 0.5 * fragColor.r + 0.5 * fragColor.g, fragColor.b, fragColor.a);
        }
        if (effectId == 46)
        {
            fragColor = vec4(0.5 * fragColor.b + 0.5 * fragColor.g, 0.5 * fragColor.r + 0.5 * fragColor.b, 0.5 * fragColor.r + 0.5 * fragColor.g, fragColor.a);
        }
        if (effectId == 47)
        {
            fragColor = glassEffect(texCoord);
        }
        if (effectId == 48)
        {
            vec2 uv = texCoord * 2.0 - 1.0;
            //uv.x *= ScreenSize.x / ScreenSize.y;
            float maxlength = sqrt(pow(ScreenSize.x / ScreenSize.y, 2.0) + 1.0);
            float l = 1.41 - length(uv);
            float spin = sin(GameTime * 5000.0) * pow(l, 2.0) * 3.0;
            float X = cos(spin) * uv.x + sin(spin) * uv.y;
            float Y = -sin(spin) * uv.x + cos(spin) * uv.y;
            vec2 newUv = vec2(X, Y);
            newUv += 1.0;
            newUv /= 2.0;
            fragColor = texture(InSampler, newUv);
        }
        if (effectId == 49)
        {
            vec2 uv = texCoord * 2.0 - 1.0;
            //uv.x *= ScreenSize.x / ScreenSize.y;
            float maxlength = sqrt(pow(ScreenSize.x / ScreenSize.y, 2.0) + 1.0);
            float l = 1.41 - length(uv);
            float spin = sin(GameTime * 7500.0) * pow(l, 2.0) * 0.5;
            vec2 newUv = uv * (1.0 + spin);
            newUv += 1.0;
            newUv /= 2.0;
            fragColor = texture(InSampler, newUv);
        }
        if (effectId == 50)
        {
            fragColor = vec4(vec3(parseDepth(texCoord) * 0.01), 1.0);
        }
        if (effectId == 51)
        {
            vec4 mainCol = texture(MeshokSampler, texCoord);
            float brightness = pow(mainCol.r * 255 / 160, 0.2);
            fragColor = mix(inTexel, vec4(vec3(brightness * 0.2), mainCol.a) * (0.3 - 0.5 * length(texCoord - 0.5)), brightness);
            for (float x = -2; x <= 2; x++)
            {
                for (float y = -2; y <= 2; y++)
                {
                    if (texture(MeshokSampler, texCoord + oneTexel * vec2(x, y)).a == 0.0)
                    {
                        fragColor = mix(inTexel, mainCol, 0.25);
                    }
                }
            }
        }
        if (effectId == 52)
        {
            fragColor = inTexel;
            for (int i = 0; i < 3; i++)
            {
                fragColor[i] = max(0.0, fragColor[i] - 0.1);
                fragColor[i] /= (1.0 - 0.1);
            }
            fragColor = vec4(mix(inTexel.rgb, fragColor.rgb, clamp(time / 20, 0.0, 1.0)), 1.0);
        }
        if (effectId == 53)
        {
            fragColor = vec4(0.0, 0.0, 0.0, 1.0);
            vec2 uv = texCoord * 2.0 - 1.0;
            //uv.x *= ScreenSize.x / ScreenSize.y;
            if (length(uv - vec2(0.3, 0)) < 0.25 || length(uv + vec2(0.3, 0)) < 0.25)
            {
                fragColor = inTexel;
            }
        }
        if (effectId == 54) {
            float distance = parseDepth(texCoord);
            distance *= 7.9;
            float scale = 3.0;
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, 1.0));
            fragColor = vec4(mix(vec3(0.9), vec3(0.0), clamp(distance, 0.0, 1.0)), 1.0);
        }
        if (effectId == 55) {
            fragColor = texture(CellularSampler, texCoord);
        }
        if (effectId == 56) {
            vec4 color = vec4(0.0);
            const int l = 9;
            vec3 weights[] = vec3[l](
                vec3(0, 0, 5),
                vec3(0, 1, -0.5),
                vec3(0, -1, -0.5),
                vec3(1, 0, -0.5),
                vec3(-1, 0, -0.5),
                vec3(1, 1, -0.5),
                vec3(1, -1, -0.5),
                vec3(-1, 1, -0.5),
                vec3(-1, -1, -0.5)
            );
            for (int i = 0; i < l; i++)
            {
                color += texture(InSampler, texCoord + oneTexel * weights[i].xy) * weights[i].z;
            }
            for (int i = 0; i < 3; i++)
            {
                color[i] = max(0.0, color[i] - 0.15);
                color[i] /= (1.0 - 0.15);
            }
            fragColor = color;
        }
        if (effectId == 57)
        {
            fragColor = vec4(0.0);
            if (length(inTexel.rgb - texture(InSampler, texCoord + oneTexel).rgb) > 0.2)
            {
                fragColor = vec4(0.0, inTexel.g, 0.0, inTexel.a);
            }
            float distance = parseDepth(texCoord);
            distance *= 7.9;
            float scale = 3.0;
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, -1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 0.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 1.0));
            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, 1.0));
            fragColor = vec4(mix(fragColor.rgb, vec3(0.0, 1.0, 0.0), clamp(distance, 0.0, 1.0)), 1.0);
        }
        if (effectId == 58)
        {
            fragColor = inTexel;
            vec2 uv = texCoord * 2.0 - 1.0;
            uv.x *= ScreenSize.x / ScreenSize.y;
            if (length(uv) < 0.5)
            {
                float dist = parseDepth(texCoord);
                fragColor.rgb *= 1.0 + 5 * (100 - clamp(dist * dist, 0.0, 100.0)) / 100 * (1.0 - smoothstep(0.25, 0.5, length(uv)));
            }
        }
        if (effectId == 59)
        {
            fragColor = inTexel;
            vec2 uv = texCoord * 2.0 - 1.0;
            uv.x *= ScreenSize.x / ScreenSize.y;
            uv.y += sin(GameTime * 1000.0) * 0.01;
            uv *= 0.7;
            float dist = parseDepth(texCoord);
            //fragColor.rgb *= 1.0 + 1 *(100 - clamp(dist * dist, 0.0, 100.0)) / 100;
            if (length(uv) < 10)
            {
                //vec4 cookie = texture(CookieSampler, uv + 0.5);
                vec4 cookie = texture(CookieSampler, uv * (1.0 + dist) / 8 + 0.5);
                fragColor.rgb *= 1.0 + clamp(cookie.r * 5.5 * (400 - clamp(dist * dist, 0.0, 400)) / 400 * (1.0 - smoothstep(0.25, 0.5, length(uv))), 0.0, 5);
                //float t = parseDepth(texCoord);
                //vec2 uv_ = texCoord * 2.0 - 1.0;
                //uv_.x *= ScreenSize.x / ScreenSize.y;
                //float dist_ = length(uv_);
                //float exposure = 5.;
                //float AOE = 8.;
                //vec2 uv2 = texCoord;
                //float d = sqrt(pow((uv2.x - 0.5),2.0) + pow((uv2.y - 0.5),2.0));
                //d = exp(-(d * AOE)) * exposure / pow(t, 1.2);
                //fragColor = vec4(mix(fragColor.rgb*clamp(d,0.1,10.0) * 10.0, fragColor.rgb, smoothstep(0.5, 1.0, dist_ * 0.5)), 1.0);
            }
            float brightness = fragColor.g * 0.6 + fragColor.r * 0.25 + fragColor.b * 0.15;
            for (int i = 0; i < 3; i++)
            {
                fragColor[i] = max(0.0, fragColor[i] - 0.1);
                fragColor[i] /= (1.0 - 0.1);
            }
            fragColor = vec4(mix(fragColor.rgb, vec3(brightness), 0.2), fragColor.a);
            //fragColor = vec4(mix(inTexel.rgb, fragColor.rgb, clamp(time / 20, 0.0, 1.0)), 1.0);
        }
        if (effectId == 60)
        {
            fragColor = vec4(hueShift(inTexel.rgb, 3.1415 * 1.0), 1.0);
            fragColor = vec4(fragColor.b, fragColor.g, fragColor.r, 1.0);
            //float brg = 0.6 * fragColor.g + 0.25 * fragColor.r + 0.15 * fragColor.b;
            //brg = pow(brg, 0.5);
            //fragColor *= 1.0 - brg;
        }
        if (effectId == 61)
        {
            if (time > 130)
            {
                fragColor = vec4(hueShift(inTexel.rgb, 3.1415 * 1.0), 1.0);
                fragColor = vec4(fragColor.b, fragColor.g, fragColor.r, 1.0);
                //float brg = 0.6 * fragColor.g + 0.25 * fragColor.r + 0.15 * fragColor.b;
                //fragColor *= 1.0 - brg;
            }
            else
            {
                vec2 uv = texCoord * 2.0 - 1.0;
                float t = sin(clamp(time - length(uv) * 20, 0.0, 100.0) / 200 * 3.1415);
                float a = sin(clamp(time - length(uv) * 20, 0.0, 100.0) / 100 * 3.1415);
                uv += vec2(rand(vec2(a, 0)), rand(vec2(0, a))) * a * 0.3;
                float t2 = (1.0 - t);
                t2 = pow(t2, 2.0);
                t = pow(t, 2.0);
                vec4 intex = texture(InSampler, uv / 2.0 + 0.5);
                vec3 def = intex.rgb * t2;
                vec3 dark = hueShift(intex.rgb, 3.1415 * 1.0 * t) * t;
                dark = vec3(dark.b, dark.g, dark.r);
                vec3 res = vec3(0.0);
                for (int i = 0; i < 3; i++)
                {
                    res[i] = max(def[i], dark[i]);
                }
                vec3 glassed = glassEffect2(texCoord, a * 500).rgb;
                float glassBright = 0.6 * glassed.g + 0.25 * glassed.r + 0.15 * glassed.b;
                float bright = 0.6 * res.g + 0.25 * res.r + 0.15 * res.b;
                fragColor = vec4(mix(res, res / bright * glassBright, a), 1.0);
                //float brg = 0.6 * fragColor.g + 0.25 * fragColor.r + 0.15 * fragColor.b;
                //fragColor *= 1.0 - brg;
            }
        }
        if (effectId == 62)
        {
            float d = length((texCoord * 2 - 1) * vec2(1.0, ScreenSize.y / ScreenSize.x)) * 0.75;
            float t = time;
            t = 0.95 - sin(t / 100.0 * 3.1415);
            if (d > t)
            {
                float col = mod(d*10 - GameTime*200.0, 1.0);
                fragColor = vec4(vec3(col * 0.3), 0.0);
                if (mod(d*10 - GameTime*200.0, 1.0) < 0.02 && d > 0.1)
                {
                    fragColor = vec4(vec3(0.5), 1.0);
                }
            }
            else if (d > t-0.005)
            {
                fragColor = vec4(vec3(0.75), 1.0);
            }
        }
    }
    //if (round(screenTexel.r * 255) == 254.0)
    //{
    //    if (screenEffectId == 0)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            //vec4 inversed = vec4(vec3(1.0) - inTexel.rgb, 1.0);
    //            float yoff = sin(GameTime * 2000.0);
    //            float xoff = cos(GameTime * 2000.0);
    //            vec4 inversed = glassEffect(vec2(1.0 - texCoord.x, texCoord.y) + vec2(xoff * 0.1, yoff * 0.1));
    //            inversed += glassEffect(vec2(texCoord.x, 1.0 - texCoord.y) + vec2(xoff * 0.1, yoff * 0.1));
    //            inversed /= 2.0;
    //            inversed = vec4(hueShift(inversed.rgb, 0.25 * 3.1415 * GameTime * 3000.0), inversed.a);
    //            float t = sin(GameTime * 4000.0 + texCoord.x * 2.0) * 0.5 + 0.5;
    //            t = 1.0;
    //            fragColor.r = max(vec4(t * inversed).r, vec4((1.0 - t) * inTexel).r);
    //            fragColor.g = max(vec4(t * inversed).g, vec4((1.0 - t) * inTexel).g);
    //            fragColor.b = max(vec4(t * inversed).b, vec4((1.0 - t) * inTexel).b);
    //        }
    //    }
    //    if (screenEffectId == 1)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            fragColor = vec4(hueShift(inTexel.rgb, GameTime * 1000.0), 1.0);
    //        }
    //    }
    //    if (screenEffectId == 2)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            fragColor = texture(InSampler, texCoord + vec2(rand(vec2(GameTime, 0.0)), rand(vec2(0.0, GameTime))) * 0.003);
    //        }
    //    }
    //    if (screenEffectId == 3)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            vec2 coord = texCoord;
    //            coord.x -= 0.5;
    //            coord.x *= 1.15 - cos(abs(coord.y - 0.5) * 2.0) * 0.15;
    //            if (abs(coord.x) > 0.5) {
    //                fragColor = vec4(vec3(0.0), 1.0);
    //            } else {
    //                coord.x += 0.5;
    //                vec3 background = vec3(texture(InSampler, coord - vec2(0.002, 0.0)).r, texture(InSampler, coord).g, texture(InSampler, coord + vec2(0.002, 0.0)).b) * 0.8;
    //                vhs(fragColor, texCoord * ScreenSize, background);
    //            }
    //        }
    //    }
    //    if (screenEffectId == 4)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            float distance = parseDepth(texCoord);
    //            distance *= 7.9;
    //            float scale = 3.0;
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 1.0));
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, 0.0));
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(1.0, -1.0));
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, -1.0));
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, -1.0));
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 0.0));
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(-1.0, 1.0));
    //            distance -= parseDepth(texCoord + oneTexel * scale * vec2(0.0, 1.0));
    //            int count = 8;
    //            inTexel.r = floor(inTexel.r * count) / count;
    //            inTexel.g = floor(inTexel.g * count) / count;
    //            inTexel.b = floor(inTexel.b * count) / count;
    //            vec3 final = mix(inTexel.rgb, vec3(0.0), clamp(distance, 0.0, 1.0));
    //            vec3 fogColor = vec3(183, 211, 235) / 255.0;
    //            fragColor = vec4(mix(final, fogColor, clamp(parseDepth(texCoord) / 50.0, 0.0, 1.0)), 1.0);
    //        }
    //    }
    //    if (screenEffectId == 5)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            fragColor = vec4(vec3(1.0) - inTexel.rgb, 1.0);
    //        }
    //    }
    //    if (screenEffectId == 6)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            fragColor = glassEffect(texCoord);
    //        }
    //    }
    //    if (screenEffectId == 7)
    //    {
    //        if (isOutlineVisible())
    //        {
    //            float depth = pow(parseDepth(texCoord), 1.0);
    //            if (depth < 100)
    //            {
    //                fragColor = texture(InSampler, vec2(texCoord.x, texCoord.y - 0.01 * depth));
    //            }
    //        }
    //    }
    //}
}
