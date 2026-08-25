#define CRT_CURVE 
#define CRT_SCANS
//#define CRT_FLICKS
#define CRT_GRAINS 
//#define CRT_YBUG 
//#define CRT_DIRTY
//#define CRT_STRIP
//#define CRT_COLOR
//#define CRT_BLINK
#define CRT_VIG

float CRT_FREQUENCY = 11.0;








float crt_rand2d(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float crt_rand(float n) {
    return fract(sin(n) * 43758.5453123);
}

float crt_noise(float p) {
	float fl = floor(p);
  	float fc = fract(p);
	return mix(crt_rand(fl), crt_rand(fl + 1.0), fc);
}

float crt_map(float val, float amin, float amax, float bmin, float bmax) {
    float n = (val - amin) / (amax-amin);
    float m = bmin + n * (bmax-bmin);
    return m;
}

float crt_snoise(float p){
    return crt_map(crt_noise(p),0.0,1.0,-1.0,1.0);
}

float crt_threshold(float val,float cut){
    float v = clamp(abs(val)-cut,0.0,1.0);
    v = sign(val) * v;
    float scale = 1.0 / (1.0 - cut);
    return v * scale;
}















vec2 crt_uv_curve(vec2 uv) {
	uv = (uv - 0.5) * 2.0;
	uv *= 1.2;	
	uv.x *= 1.0 + 1.5 * pow((abs(uv.y) / 5.0), 2.0);
	uv.y *= 1.0 + 1.5 * pow((abs(uv.x) / 4.0), 2.0);
    uv /= 1.25;
	uv  = (uv / 2.0) + 0.5;
    if (uv.x < 0.0) {
        uv.x = 0.0;
    }
    if (uv.x > 1.0) {
        uv.x = 1.0;
    }
    if (uv.y < 0.0) {
        uv.y = 0.0;
    }
    if (uv.y > 1.0) {
        uv.y = 1.0;
    }
	return uv;
}

vec3 crt_color(sampler2D tex, vec2 uv){        
    vec3 color = texture(tex,uv).rgb;
    #ifdef CRT_COLOR
    float bw = (color.r + color.g + color.b) / 3.0;
    color = mix(color,vec3(bw,bw,bw),.95);
    float p = 1.5;
    color.r = pow(color.r,p);
    color.g = pow(color.g,p-0.1);
    color.b = pow(color.b,p);
    #endif
    return color;
}

vec3 crt_ghost(sampler2D tex, vec2 uv, float crt_time){
    #ifdef CRT_FLICKS
    
    float n1 = crt_threshold(crt_snoise(crt_time*10.),.85);
    float n2 = crt_threshold(crt_snoise(2000.0+crt_time*10.),.85);
    float n3 = crt_threshold(crt_snoise(3000.0+crt_time*10.),.85);
    
    vec2 or = vec2(0.,0.);
    vec2 og = vec2(0,0.);
    vec2 ob = vec2(0.,0);

    float os = .05;
    or += vec2(n1*os,0.);
    og += vec2(n2*os,0.);
    ob += vec2(0.,n3*os);
  
    float r = crt_color(tex,uv + or).r;
    float g = crt_color(tex,uv + og).g;
    float b = crt_color(tex,uv + ob).b;
    vec3 color = vec3(r,g,b);
    return color;
    #else 
    return texture(tex,uv).rgb;
    #endif
}

vec2 crt_uv_ybug(vec2 uv, float crt_time){
    float n4 = clamp(crt_noise(200.0+crt_time*2.)*14.,0.,2.);
    uv.y += n4;
    uv.y = mod(uv.y,1.);
    return uv;
}

vec2 crt_uv_hstrip(vec2 uv, float crt_time){
    float vnoise = crt_snoise(crt_time*6.);
    float hnoise = crt_threshold(crt_snoise(crt_time*10.),.5);

    float line = (sin(uv.y*10.+vnoise)+1.)/2.;
    line = (clamp(line,.9,1.)-.9)*10.;
    
    uv.x += line * 0.03 * hnoise;
    uv.x = mod(uv.x,1.);
    return uv;
}



void crt_mainImage( out vec4 fragColor, in vec2 fragCoord, in float crt_time, in sampler2D iChannel0 )
{
    float t = float(int(crt_time * CRT_FREQUENCY));
    
    vec2 true_uv = fragCoord / ScreenSize.xy;
    vec2 uv = fragCoord / ScreenSize.xy;

    #ifdef CRT_CURVE
    uv = crt_uv_curve(uv);
    #endif

    vec2 ouv = uv;
    
    #ifdef CRT_GRAINS
    float xn = crt_threshold(crt_snoise(crt_time*10.),.7) * 0.05;
    float yn = crt_threshold(crt_snoise((500.0+crt_time)*10.),.7) * 0.05;
    
    float r = crt_rand2d(uv+(t+100.0)*.01);
    uv = uv + vec2(xn,yn) * r;
    #endif
    
     
    #ifdef CRT_YBUG
    uv = crt_uv_ybug(uv, crt_time);
    #endif

    #ifdef CRT_STRIP
    uv = crt_uv_hstrip(uv, crt_time);
    #endif
    
   
    vec2 onePixel = vec2(0.0, 1.0) / ScreenSize.xy * 3.;
    //#ifdef CRT_BLUR
    //vec3 colorA = crt_ghost(iChannel0,uv + onePixel,or,og,ob, crt_time);
    //vec3 colorB = crt_ghost(iChannel0,uv - onePixel,or,og,ob, crt_time);
    //vec3 colorC = crt_ghost(iChannel0,uv,or,og,ob, crt_time);
    //vec3 color = (colorA+colorB+colorC)/3.0;
    //#else
    vec3 color = crt_ghost(iChannel0,uv, crt_time);
    //#endif

    //color = colorC;
    
    #ifdef CRT_BLINK
    float blink = .96 + .04*(sin(crt_time*100.)+1.)/2.;
    color *= blink;
    #endif
    
    #ifdef CRT_VIG
    float vig = 44.0 * (ouv.x * (1.0-ouv.x) * ouv.y * (1.0-ouv.y));
	vig *= mix( 0.7, 1.0, crt_rand(t + 0.5));
    color *= .6 + .4*vig;
    #endif
     
    #ifdef CRT_DIRTY
    color *= 1.0 + crt_rand2d(uv+t*.01) * 0.2;	
    #endif
    
    float scanA = (sin(true_uv.y*3.1415*ScreenSize.y/5.4 + crt_time * 400)+1.)/2.;
    float scanB = (sin(true_uv.y*3.1415*1.)+1.)/2.;
    #ifdef CRT_SCANS
    color *= .75 + scanA * .25;
    //color *= .5 + scanC * .5;
    //color *= scanB;    
    #endif

    vec3 backColor = vec3(.4,.4,.4);
    if (ouv.x < 0.0 || ouv.x > 1.0)
		color = backColor;
	if (ouv.y < 0.0 || ouv.y > 1.0)
		color = backColor;

    fragColor = vec4(color,1.0);
}