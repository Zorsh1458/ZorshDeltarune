#define M_PI 3.1415926535897932384626433832795

float neverlandintro_rand(vec2 co, float nin_time)
{
    return fract(sin(dot(co.xy + nin_time * 0.000001 ,vec2(12.9898,78.233))) * 43758.5453);
}

void neverlandintro_mainImage( out vec4 fragColor, in vec2 fragCoord, in float nin_time )
{
	float size = 30.0;
	float prob = 0.8;
	
	vec2 pos = floor(1.0 / size * fragCoord.xy);
	
	float color = 0.0;
	float starValue = neverlandintro_rand(pos, nin_time);
	
	if (starValue > prob)
	{
		vec2 center = size * pos + vec2(size, size) * 0.5;

		center += neverlandintro_rand(-center, nin_time);
		
		float t = 0.9 + 0.2 * sin(nin_time + (starValue - prob) / (1.0 - prob) * 45.0);
				
		color = 1.0 - distance(fragCoord.xy, center) / (0.5 * size);
		float ang = neverlandintro_rand(center, nin_time) + nin_time * (mod(neverlandintro_rand(-center, nin_time), 2.0) - 1.0);
		vec2 offset = fragCoord - center;
		offset = vec2(cos(ang) * offset.x + sin(ang) * offset.y, cos(ang) * offset.y - sin(ang) * offset.x);
		color = color * t / (abs(offset.y)) * t / (abs(offset.x));
		color *= 0.5 + sin(nin_time * 8 + ang * 5) * 0.5;
		color += 0.5 * max(0, 1.0 - length(offset) * 0.1);
	}
	else if (neverlandintro_rand(fragCoord.xy / ScreenSize.xy, nin_time) > 0.996)
	{
		float r = neverlandintro_rand(fragCoord.xy, nin_time);
		color = r * (0.25 * sin(nin_time * (r * 5.0) + 720.0 * r) + 0.75) * cos(nin_time * 5.234 + r * 5.152);
	}
	
	if (color > 1.0) {
		fragColor = vec4(vec3(1.0, color - 1.0, color - 0.75), 1.0);
	} else {
		fragColor = vec4(vec3(color, 0.0, 0.25 * color), 1.0);
	}
}

#define NEVERLANDINTRO_BURST
#define NEVERLANDINTRO_NUM_LAYERS 0.

mat2 neverlandintro_Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c,-s,s,c);
}

float neverlandintro_Star(vec2 uv, float a, float sparkle) {
    vec2 av1 = abs(uv);
 	vec2 av2 = abs(uv*neverlandintro_Rot(a));
    vec2 av = min(av1, av2);
    
    vec3 col = vec3(0);
    float d = length(uv);
    float star = av1.x*av1.y;
    star = max(av1.x*av1.y, av2.x*av2.y);
    star = max(0., 1.-star*1e3);
    
    float m = min(5., 1e-2/d);
    
    return m+pow(star, 4.)*sparkle;
}

float neverlandintro_Hash21(vec2 p) {
    p = fract(p*vec2(123.34,145.54));
    p += dot(p, p+45.23);
    return fract(p.x*p.y);
}

vec3 neverlandintro_StarLayer(vec2 uv, float t, float sparkle) {
    vec2 gv = fract(uv)-.5;
    vec2 id = floor(uv);
	vec3 col = vec3(0);
    
    #ifndef NEVERLANDINTRO_BURST
    t = 0.;
    #endif
    
    for(int y=-1; y<=1; y++) {
        for(int x=-1; x<=1; x++) {
            vec2 offs = vec2(x, y);
            float n = neverlandintro_Hash21(id-offs);
			vec3 N = fract(n*vec3(10,100,1000));
            vec2 p = (N.xy-.5)*.7;
            
            float brightness = neverlandintro_Star(gv-p+offs, n*6.2831+t, sparkle);
            vec3 star = brightness*vec3(.6+p.x, .4, .6+p.y)*N.z*N.z;
            
            
            
            star *= 1.+sin((t+n)*20.)*smoothstep(sin(t)*.5+.5, 1., fract(10.*n));
            
            float d = length(gv+offs);
            
            col += star*smoothstep(1.5, .8, d);
        }
    }
    return col;
}

void neverlandintro_burstImage( out vec4 fragColor, in vec2 fragCoord, in float nin_time )
{
    vec2 uv = (fragCoord-.5*ScreenSize.xy)/ScreenSize.y;
    vec2 M = vec2(0.0);
    
    M *= 10.;
    
	float t = -nin_time*.3;
	
    float twirl = sin(t*.1);
    twirl *= twirl*twirl*sin(dot(uv,uv));
    uv *= neverlandintro_Rot(-t*.2);
    
    uv *= 2.+sin(t*.05);
    
    vec3 col = vec3(0);
    float speed = -.2;
    #ifdef NEVERLANDINTRO_BURST
    speed = .1;
    float bla = sin(t+sin(t+sin(t)*.5))*.5+.5;
    float d = dot(uv,uv);
    
    float a = atan(uv.x, uv.y);
    uv /= d;
    float burst = sin(nin_time*.05);
    uv *= burst+.2;
    #endif
    
    float stp = 1./NEVERLANDINTRO_NUM_LAYERS;
        
    for(float i=0.; i<1.; i+=stp) {
    	float lt = fract(t*speed+i);
        float scale = mix(10., .25, lt);
        float fade = smoothstep(0., .4, lt)*smoothstep(1., .95, lt); 
        vec2 sv = uv*scale+i*134.53-M;
        //sv.x += t;
        col += neverlandintro_StarLayer(sv, t, fade)*fade;
    }
    
    #ifdef NEVERLANDINTRO_BURST
    //t = nin_time*.5;
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

vec2 neverlandintro_rrrot(vec2 uv, float r) {
    float sinX = sin (r);
    float cosX = cos (r);
    float sinY = sin (r);
    mat2 rotationMatrix = mat2( cosX, -sinX, sinY, cosX);
    return uv *rotationMatrix;
}

void neverlandintro_imageSwirl2( out vec4 fragColor, in vec2 fragCoord, in float nin_time )
{
    float s = 7.;
    float st = 0.2;

    vec2 uv = neverlandintro_rrrot(fragCoord/ScreenSize.xy, -0.2+sin(nin_time)*0.05);

    float osc = sin(uv.x*(uv.x+.5)*15.)*0.2;
    uv.y += osc * sin(nin_time+uv.x*2.);
    uv.y = fract(uv.y*s);
    
    vec3 bg = vec3(0.0);
    vec3 fg = vec3(.05,.05,.05);
    
    float mask = smoothstep(0.5, 0.55, uv.y);
    mask += smoothstep(0.5+st,0.55+st, 1.-uv.y);
    
    vec3 col = mask*bg + (1.-mask)*fg;

    fragColor = vec4(col,1.0);
}

void neverlandintro_imageSwirl( out vec4 fragColor, in vec2 fragCoord, in float nin_time ) {
	vec4 c1;
	vec4 c2;
	vec4 c3;
	vec4 c4;
	neverlandintro_imageSwirl2(c1, fragCoord, nin_time);
	neverlandintro_imageSwirl2(c2, vec2(ScreenSize.x - fragCoord.x, fragCoord.y), nin_time);
	neverlandintro_imageSwirl2(c3, vec2(ScreenSize.x - fragCoord.x, ScreenSize.y - fragCoord.y), nin_time);
	neverlandintro_imageSwirl2(c4, vec2(fragCoord.x, ScreenSize.y - fragCoord.y), nin_time);
	fragColor = (c1 + c2 + c3 + c4) * 0.5;
}