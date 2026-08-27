#define T GameTime*1000.0
#define PI 3.141596
#define S smoothstep
const float EPSILON = 1e-6;

vec4 applyShader( vec2 uv )
{
    vec4 fragColor;
    
    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);
    
    float strength = 16.0;
    
    float x = (uv.x + 4.0 ) * (uv.y + 4.0 ) * (iTime * 10.0);
	  vec4 grain = vec4(mod((mod(x, 13.0) + 1.0) * (mod(x, 123.0) + 1.0), 0.01)-0.005) * strength;
    
    if(abs(uv.x - 0.5) < 0.002)
        color = vec4(0.0);

	  fragColor = color + grain;
    return fragColor;
}

vec3 dl_HueShift(vec3 color, float hueAdjust) {
    
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

vec3 city( vec2 p, vec2 realP, float time ) {
    float a = 0.;
    float hue = 0;
    for (float f = 8.; f < 24.; f++) {
        float stepp = floor(3e2*p.x/f+40.*f+time*20.0/pow(f, 0.5));
        a = p.y + f*0.017 < sin(stepp) ? f / 31.:a;
        if (f*0.017 < sin(stepp)) {
            hue = mod(stepp * 32137.0, 31.0) / 31.0;
        }
    }
    vec3 colorMult = vec3(0.0, 0.0, 0.5);
    vec3 colorBase = vec3(0.05, 0.2, 0.0);
    vec3 bg = colorBase + colorMult * p.y +p.x*.2;
    float bgShade = realP.y * 1.5 - 1.0;
    vec3 bg1 = mix(vec3(0.0), bg * 0.3 * vec3(1.0, 2, 1.5), realP.y * 1.5 - 1.0);
    vec3 bg2 = mix(vec3(0.0), bg * 0.3 * vec3(1.0, 2, 1.5), floor(bgShade * 6.0) / 6.0);
    if (a < 0.01) return mix(bg1, bg2, 0.5);
    return mix(vec3(0.1), dl_HueShift(bg, round(pow(hue, 4.0)) * 2.0), 1.0 - pow(1.0 - a, 1.0));
}

vec3 mainCityBg( vec2 c, float time )
{
  vec2 p = c.xy * vec2(2.0, 1.5);
  p.y = p.y * 1.5 - 0.5;
  vec2 cc = vec2(p.x, max(0.0, p.y) - 0.1 * abs(sin(p.x * 10.0 - time)));
  vec3 r = city(p, p, time) * 0.6;
  r += city(cc, p, time) * 0.4;
  return mix(vec3(0.0), r.rgb, min(1.2, p.y + 0.7)) * 0.8;
}

vec3 deltaruneBg2( vec2 uv, float timeOffset )
{
    vec2 trueUv = vec2(1.0) - gl_FragCoord.xy / ScreenSize.xy;
    return mainCityBg(trueUv, (GameTime + timeOffset) * 1000.0);
    //vec3 c2 = mainCityBg(trueUv, GameTime * 1000.0);
}

vec3 deltaruneBgPart(vec2 uv, vec2 dir, vec2 offset, float time) {
    vec3 col = vec3(0.0);
    uv.x *= ScreenSize.x / ScreenSize.y;
    uv = mod(uv * 12.0 + offset + dir * time * 2.35, 1.0);
    if (uv.x < 0.03 || uv.y < 0.03) {
        col = vec3(1.0, 0.0, 1.0);
    }
    return col;
}

vec3 deltaruneBg( vec2 uv, float timeOffset )
{
    float time = (min(mod(GameTime * 16383.0, 32.0), 15.0) / 16383.0 + timeOffset) * 1000.0;
    vec3 col1 = deltaruneBgPart(uv, vec2(0.5, -0.5), vec2(0.0), time);
    vec3 col2 = deltaruneBgPart(uv, vec2(-0.5, 0.5), vec2(-0.2), time);
    vec3 col = mix(col1, col2, 0.25);
    return col * 0.25;
}

vec4 ScreenShaders2( vec2 uv_raw ) {
    float t = GameTime * 1000.0;
    //float t = vertexColor.a * 10.0;
	vec3 c;
	float l, z = t;
	for(int i = 0; i < 3; i++) {
		vec2 uv, p = uv_raw;
		uv = p;
		p -= .5;
		p.x *= 1920.0/1080.0; // ASPECT RATIO
		z += .07;
		l = length(p);
		uv += p / l * (sin(z) + 1.) * abs(sin(l * 9. - z - z));
		c[i] = .01 / length(mod(uv, 1.)- .5);
	}
	fragColor = vec4(c / l, 1.0);

    return fragColor;
}

mat2 rotate(float a){
  float s = sin(a);
  float c = cos(a);
  return mat2(c,-s,s,c);
}

float sdRoundBox( vec3 p, vec3 b, float r )
{
  vec3 q = abs(p) - b + r;
  return length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0) - r;
}

float fbm(vec3 p){
  float amp = 1.;
  float fre = 1.;
  float n = 0.;
  for(float i=0.;i<5.;i++){
    n += amp*abs(dot(cos(p*fre), vec3(.06)));
    amp *= .5;
    fre *= 2.;
  }
  return n;
}

vec3 path(float v){
  return vec3(cos(v*.2+sin(v*.1+T))*4.,
              sin(v*.2+cos(v*.1)+T)*4., v);
}

float hash12(vec2 p)
{
	vec3 p3  = fract(vec3(p.xyx) * .1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hash11(float v){
    return abs(cos(v*2.+sin(v*4.)/4.)/2.);
}

vec4 ScreenShaders( vec2 uv_raw ) {
    vec4 O;
    vec2 uv = uv_raw * 2.0 - vec2(1.0, 1.0);
    uv.x *= 1920.0/1080.0;
  
    O.rgb *= 0.;
    O.a = 1.;
  
    // vec3 ro = vec3(0);
    // vec3 rd = normalize(vec3(uv, 1));
    float t = T * 4.;
    vec3 ro = path(t);
    vec3 front = normalize(path(t+2.) - ro);
    vec3 up = vec3(0,1,0);
    vec3 right = normalize(cross(front, up));
    vec3 rd = mat3(right, up, front) * normalize(vec3(uv, 1));
  
    float zMax = 50.;
    float z = .1;
  
    vec3 col = vec3(0);
    for(float i=0.;i<100.;i++){
      vec3 p = ro + rd * z;
  
      float d = length(p.xy-path(p.z).xy)-3.;
      d = d * (1.-hash12(uv.xy*100.+T)*.2);  // Using hash noise reduction artifacts
  
      d = abs(d)+.01;
      d += fbm(p*2.);
  
      float k = sin(p.z+p.x*.5+p.y*.3)*.5+.5;   //glow factor
  
      col += k * (1.1+sin(vec3(3,2,1)+p.x+p.z))/d;
      
      if(d<EPSILON || z>zMax) break;
      z += d;
    }
  
    col = tanh(col / 1e2);
    O.rgb = col;
    return O;
}

void squarePortal(out vec4 O, in vec2 I){
  vec2 R = iResolution.xy;
  vec2 uv = (I*2.-R)/R.y;

  O.rgb *= 0.;
  O.a = 1.;

  vec3 ro = vec3(0.);

  vec3 rd = normalize(vec3(uv, 1.));

  float zMax = 50.;
  float z = .1;

  vec3 col = vec3(0);
  vec3 p;
  for(float i=0.;i<100.;i++){
    p = ro + rd * z;
    p.z += T*2.;
    p.xy *= rotate(p.z*.1);
    p.y = -abs(p.y)+10.;
    
    float d0 = abs(p.y)*.1;
    d0 = abs(d0)+.1;

    vec3 q = p;
    float s = 2.;
    vec2 id = round(q.xz/s);
    q.xz -= id*s;
    float d1 = sdRoundBox(q, vec3(.8, .2, .8), .1);
    d1 = max(0.01, d1*.6);
    float d = min(d0, d1);

    d = d * (1.-hash12(uv.xy*30.+T)*.1);  // 使用噪点消除伪影

    float n = hash12(id+T*3e-4);
    float glow = n >.5 ? (n*2.) : 0.;



    //float n = hash11(hash12(id*14.39)*31.49+T);
    //float glow = clamp(n, .01, 1.);

    col += glow*(.5+.5*sin(vec3(3,2,1)+id.x+id.y))/d0;
    // col += glow*(vec3(0,2,0))/d0;

    if(d<EPSILON || z>zMax) break;
    z += d;
  }
  col = tanh(col / 2e2);
  //col *= S(zMax,zMax-10.,z);
  //col = tanh(col / 1.-exp(-2.*z*z));

  O.rgb = col;
}