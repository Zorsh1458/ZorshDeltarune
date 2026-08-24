#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 uvPoint;
in vec4 whiteVertexColor;
in vec4 vertexColor;
in vec4 controlColor;
in vec2 texCoord0;
in vec2 texCoord1;
in vec3 Norm;
in vec4 clipNear;
in vec3 worldpos;

out vec4 fragColor;

#moj_import <minecraft:remove_blue.glsl>

#define iTime (GameTime*100)
vec3 iResolution = vec3(ScreenSize.xy, 1.0);
vec4 iMouse = vec4(0.0);




#moj_import <minecraft:nvmainscreen.glsl>







// Constants ----------
#define PI 3.14159265358979
#define P2 6.28318530717959

// -- tracing parameters
const int   MAX_TRACE_STEP = 40;
const float MAX_TRACE_DIST = 80.;
const float NO_HIT_DIST    = 100.; // must be NO_HIT_DIST > MAX_TRACE_DIST
const float TRACE_PRECISION = .001;
const float FUDGE_FACTOR = .9;
const int   RAY_TRACE_COUNT = 5;
const vec3  GAMMA = vec3(1./2.2);

// -- rendering parameters
const int   GI_TRACE_STEP = 5;
const float GI_LENGTH = 1.6;
const float GI_STRENGTH = .2;
const float AO_STRENGTH = .4;
const int   SS_MAX_TRACE_STEP = 4;
const float SS_MAX_TRACE_DIST = 10.;
const float SS_MIN_MARCHING = .4;
const float SS_SHARPNESS = 1.;
const float CS_STRENGTH = .4;
const float CS_SHARPNESS = .3;


// Structures ----------
struct Surface {
  float d;              // distance
  vec3  kd, tc, rl, rr; // diffusion, transparent-color, reflectance, refractive index
};
Surface near(Surface s,Surface t) { if (s.d<t.d) return s; return t; }

struct Ray {
  vec3  org, dir, col;     // origin, direction, color
  float len, stp, rr, sgn; // length, marching step, refractive index of current media, sign of distance function
};
Ray ray(vec3 o, vec3 d) { return Ray(o,d,vec3(1),0.,0.,1.,1.); }
Ray ray(vec3 o, vec3 d, vec3 c, float rr, float s) { return Ray(o,d,c,0.,0.,rr,s); }
vec3 _pos(Ray r) { return r.org+r.dir*r.len; }

struct Hit {
  vec3 pos,nml; // position, normal
  Ray ray;      // ray
  Surface srf;  // surface
  bool isTransparent, isReflect;  // = (len2(srf.tc) > 0.001, len2(srf.rl) > 0.001)
};
Hit nohit(Ray r) { return Hit(vec3(0), vec3(0), r, Surface(NO_HIT_DIST, vec3(1), vec3(0), vec3(0), vec3(0)), false, false); }

struct Camera {
  vec3 pos, tgt;  // position, target
  float rol, fcs; // roll, focal length
};
mat3 _mat3(Camera c) {
  vec3 w = normalize(c.pos-c.tgt);
  vec3 u = normalize(cross(w,vec3(sin(c.rol),cos(c.rol),0)));
  return mat3(u,normalize(cross(u,w)),w);
}

struct AABB { vec3 bmin, bmax; };
struct Light { vec3 dir, col; };
vec3 _diff(vec3 n, Light l){ return clamp((dot(n, l.dir)+1.)*.5, 0., 1.)*l.col; }


// Grobal valiables ----------
const float bpm = 126.0;
const Light amb = Light(vec3(0,-1,0), vec3(0.4));
const Light dif = Light(normalize(vec3(0,-1,0)), vec3(1));
float phase;


// Utilities ----------
vec3  _hsv(float h, float s, float v) { return ((clamp(abs(fract(h+vec3(0,2,1)/3.)*6.-3.)-1.,0.,1.)-1.)*s+1.)*v; }
mat3  _smat(vec2 a) { float x=cos(a.y),y=cos(a.x),z=sin(a.y),w=sin(a.x); return mat3(y,w*z,-w*x,0,x,z,w,-y*z,y*x); }
mat3  _pmat(vec3 a) {
  float sx=sin(a.x),sy=sin(a.y),sz=sin(a.z),cx=cos(a.x),cy=cos(a.y),cz=cos(a.z);
  return mat3(cz*cy,sz*cy,-sy,-sz*cx+cz*sy*sx,cz*cx+sz*sy*sx,cy*sx,sz*sx+cz*sy*cx,-cz*sx+sz*sy*cx,cy*cx);
}
float _checker(vec2 uv, vec2 csize) { return mod(floor(uv.x/csize.x)+floor(uv.y/csize.y),2.); }
float _checker3(vec3 uvt, vec3 csize) { return mod(floor(uvt.x/csize.x)+floor(uvt.y/csize.y)+floor(uvt.z/csize.z),2.); }
float len2(vec3 v) { return dot(v,v); }
float smin(float a, float b, float k) { return -log(exp(-k*a)+exp(-k*b))/k; }
float smax(float a, float b, float k) { return log(exp(k*a)+exp(k*b))/k; }
float vmin(vec3 v) { return min(v.x, min(v.y, v.z)); }
float vmax(vec3 v) { return max(v.x, max(v.y, v.z)); }
vec2  cycl(float t, vec2 f, vec2 r) { return vec2(cos(t*f.x)*r.x+cos(t*f.y)*r.y,sin(t*f.x)*r.x+sin(t*f.y)*r.y); }
vec3  fresnel(vec3 f0, float dp) { return f0+(1.-f0)*pow(1.-abs(dp),5.); }
float rr2rl(float rr) { float v=(rr-1.)/(rr+1.); return v*v; }
float rand(vec2 co){ return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453); }

// Intersect functions ----------
vec2 hitAABB(Ray r, AABB b) { 
  vec3 t1=(b.bmin-r.org)/r.dir, t2=(b.bmax-r.org)/r.dir;
  return vec2(vmax(min(t1, t2)), vmin(max(t1, t2)));
}

float ifBox(Ray r, vec3 b) {
  vec2 v = hitAABB(r, AABB(-b,b));
  return (0.<=v.y&&v.x<=v.y)?v.x:NO_HIT_DIST;
}

// Distance functions ----------
float dfPln(vec3 p, vec3 n, float d) { return dot(p,n) + d; }
float dfBox(vec3 p, vec3 b, float r) { return r-length(max(abs(p)-b,0.));}

const float vtx[11] = float[11](1.10, 1.39, 1.75, 2.32, 2.68, 2.08, 1.44, 1.12, 0.98, 0.95, 1.00);
const float vcnt = 5.0;
float dfFreeDSBC(in vec3 p, in float r, in float t) {
  p = vec3(p.x, abs(p.y)-t, abs(p.z*2.0));
  float at = atan(p.z,p.x)/(PI/vcnt);
  float a = floor(at+.5)*(PI/vcnt), c = cos(a), s = sin(a);
  int   i = int(at*2.);
  float v0 = vtx[i];
  float v1 = vtx[i+1];
  float rf = mix(v0,v1,fract(at*2.)) * r;
  vec3  q = vec3((c*p.x+s*p.z)/rf, p.y/r, abs(-c*p.z+s*p.x)/rf);
  float fcBezel = dot(q, vec3(.544639035, .8386705679, 0))           - .544639035;
  float fcUGird = dot(q, vec3(.636291199, .7609957358, .1265661887)) - .636291199;
  float fcStar  = dot(q, vec3(.332894535, .9328278154, .1378894313)) - .448447409;
  float fcTable =   q.y - .2727511892 - .05;
  float fcCulet = - q.y - .8692867378 * .96;
  float fcGirdl = length(q.xz) - .975;
  return max(fcGirdl, max(fcCulet, max(fcTable, max(fcBezel, max(fcStar, fcUGird)))));
}

float sdChain(vec3 p, float r, float l, float w){
  float a=(l+r-w)*2., x0=mod(p.x, a*2.), x1=mod(p.x+a, a*2.);
  return min(length(vec2(length(vec2(max(abs(x0-a)-l,0.),p.z))-r,p.y))-w,
             length(vec2(length(vec2(max(abs(x1-a)-l,0.),p.y))-r,p.z))-w);
}


// Domain Operations ----------
vec3 doXZ(vec3 p, vec2 r){
  vec2 hr = r*.5;
  return vec3(mod(p.x+hr.x, r.x)-hr.x, p.y, mod(p.z+hr.y, r.y)-hr.y);
}


// Deforimng function ----------
vec3 foXZCircle(vec3 p, float l, float r){
  return vec3(atan(p.z,p.x)*l/P2,p.y,length(p.xz)-r);
}


// Easing Functions ----------


// Ray calcuatoins ----------
Ray rayScreen(in vec2 p, in Camera c) {
  return ray(c.pos, normalize(_mat3(c) * vec3(p.xy, -c.fcs)));
}

Ray rayReflect(in Hit h, in vec3 rl) {
  return ray(h.pos + h.nml*.01, reflect(h.ray.dir, h.nml), h.ray.col*rl, h.ray.rr, h.ray.sgn);
}

Ray rayRefract(in Hit h, in float rr) {
  vec3 r = refract(h.ray.dir, h.nml, h.ray.rr/rr);
  if (len2(r)<.001) return rayReflect(h, vec3(1));
  return ray(h.pos - h.nml*.01, r, h.ray.col*h.srf.tc, rr, -h.ray.sgn);
}


// Sphere tracing ----------.
Surface map(in vec3 p){
  float t = exp(cos(phase/2.0)*5.0)*0.001;
  float isGlass = sign(p.x) * 0.5 + 0.5; // glass=1, mirror=0
  vec3 rr = vec3(isGlass*1.8),
       rl = vec3(rr2rl(rr.x)),
       col = vec3(0),
       tc = vec3(1.0,0.2,0.2)*isGlass;
  return Surface(dfFreeDSBC(vec3(abs(p.x)-2.7,p.y-1.5,p.z)*_pmat(vec3(phase/32.0,0.0,0.5*PI)), 1.6, 0.1), col, tc, rl, rr);
}
//doXZ(p, vec2(1.5*8.-0.4*16.))*mat

// Lighting ----------
vec4 _cs(in vec3 pos, in vec3 dir) {
  vec4 col;
  float len = SS_MIN_MARCHING;
  for (int i=SS_MAX_TRACE_STEP; i!=0; --i) {
    Surface s = map(pos + dir*len);
    col = vec4(s.tc, min(col.a, SS_SHARPNESS*s.d/len));
    len += max(s.d, SS_MIN_MARCHING);
    if (s.d<TRACE_PRECISION || len>SS_MAX_TRACE_DIST) break;
  }
  col.a = clamp(col.a, 0., 1.);
  col.rgb = pow((1.-col.a), CS_SHARPNESS) * col.rgb * CS_STRENGTH;
  return col;
}

vec4 _gi(in vec3 pos, in vec3 nml) {
  vec4 col = vec4(0);
  for (int i=GI_TRACE_STEP; i!=0; --i) {
    float hr = .01 + float(i) * GI_LENGTH / 4.;
    Surface s = map(nml * hr + pos);
    col += vec4(s.kd, 1.) * (hr - s.d);
  }
  col.rgb *= GI_STRENGTH / GI_LENGTH;
  col.a = clamp(1.-col.a * AO_STRENGTH / GI_LENGTH, 0., 1.);
  return col;
}

vec3 _back(in Ray ray) {
  ray.len = ifBox(ray, vec3(22));
  vec3 p = _pos(ray);
  vec3 ap = abs(p);
  float c = len2(fract(p*0.5)-0.5)-0.25;
  float ct = 0.7 + p.y/33.0;
  return (fract(c*cos(phase/16.)*16.0)*0.2+ct)*vec3(1.0-(ap.x+ap.y+ap.z-vmin(ap))/44.2);
}

vec3 lighting(in Hit h) {
  if (h.ray.len > MAX_TRACE_DIST) return _back(h.ray);
  vec4 fgi = _gi(h.pos, h.nml);    // Fake Global Illumination
  vec4 fcs = _cs(h.pos, dif.dir);  // Fake Caustic Shadow
  //   lin = ([Ambient]        + [Diffuse]        * [SS]  + [CS])    * [AO]  + [GI]
  vec3 lin = (_diff(h.nml, amb) + _diff(h.nml, dif) * fcs.w + fcs.rgb) * fgi.w + fgi.rgb;
  return  h.srf.kd * lin;
}


// Ray tracing ----------
vec3 _calcNormal(in vec3 p){
  vec3 v=vec3(.001,0,map(p).d);
  return normalize(vec3(map(p+v.xyy).d-v.z,map(p+v.yxy).d-v.z,map(p+v.yyx).d-v.z));
}

Hit sphereTrace(in Ray r) {
  Surface s;
  for(int i=0; i<MAX_TRACE_STEP; i++) {
    s = map(_pos(r));
    s.d *= r.sgn;
    r.len += s.d * FUDGE_FACTOR;
    r.stp = float(i);
    if (s.d < TRACE_PRECISION) break;
    if (r.len > MAX_TRACE_DIST) return nohit(r);
  }
  vec3 p = _pos(r);
  float interior = .5-r.sgn*.5;
  s.rr = mix(s.rr, vec3(1), interior);
  s.tc = max(s.tc, interior);
  return Hit(p, _calcNormal(p)*r.sgn, r, s, (len2(s.tc)>.001), (len2(s.rl)>.001));
}

Hit trace(in Ray r) {
  return sphereTrace(r);
}


// Rendering ----------
vec3 _difColor(inout Hit h) {
  if (len2(h.srf.kd) < .001) return vec3(0);
  vec3 col = lighting(h) * h.ray.col;
  h.ray.col *= 1. - h.srf.kd;
  return col;
}

Ray _nextRay(Hit h) {
  if (h.isTransparent) return rayRefract(h, h.srf.rr.x);
  return rayReflect(h, fresnel(h.srf.rl, dot(h.ray.dir, h.nml)));
}

vec4 render(in Ray r){
  vec3 col = vec3(0), rl, c;
  Hit h0, h1;
  float l0;

  // first trace
  h0 = trace(r);
  l0 = h0.ray.len;

  // first diffusion surface
  col += _difColor(h0);
  if (!h0.isReflect) return vec4(col, l0);

  // first reflection
  rl = fresnel(h0.srf.rl, dot(h0.ray.dir, h0.nml));
  h1 = trace(rayReflect(h0, rl));
  col += _difColor(h1);
  h0.ray.col *= 1. - rl;
  if (!h0.isTransparent) h0 = h1;
 
  // repeating trace
  for (int i=RAY_TRACE_COUNT; i!=0; --i) {
    if (!h0.isReflect) return vec4(col, l0);
    h0 = trace(_nextRay(h0));
    col += _difColor(h0);
  }

  // cheap trick
  c = h0.ray.col;
  if (len2(c) >= .25) col += _back(h0.ray) * c * c;

  return vec4(col, l0);
}

vec4 gamma(in vec4 i) {
  return vec4(pow(i.xyz, GAMMA), i.w);
}


// Entry point ----------
void crystalHeart(out vec4 fragColor, in vec2 fragCoord) {
  phase = 0.0;

  vec2   m = vec2(cos(phase/128.), sin(phase/128.))*15.;
  Camera c = Camera(vec3(m.x,sin(phase/32.)*10.,m.y), vec3(0,2,0), 0., 1.73205081);
  //Camera c = Camera(vec3(0,10,1),vec3(0,1,0), 0., 1.73205081);
  c.pos.x = 9.0 + sin(GameTime * 4000.0) * 0.1;
  c.pos.y = 1.5;
  Ray    r = rayScreen((fragCoord.xy * 2. - iResolution.xy) / iResolution.x, c);

  vec4 res = render(r);
  //res.w = min(abs(res.w - length(c.pos)+15.)/100., 1.);

  fragColor = gamma(res);
  if (res.w > 17.0) {
    fragColor = vec4(0.0);
  }
}




























int getTimeData() {
    return (int(floor(GameTime * 16383.0)) >> 7) & 1;
}





float randFire(vec2 n) {
    return fract(sin(cos(dot(n, vec2(12.9898,12.1414)))) * 83758.5453);
}

float noise(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n), f = smoothstep(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(randFire(b), randFire(b + d.yx), f.x), mix(randFire(b + d.xy), randFire(b + d.yy), f.x), f.y);
}

float fbm(vec2 n) {
    float total = 0.0, amplitude = 1.0;
    for (int i = 0; i <5; i++) {
        total += noise(n) * amplitude;
        n += n*1.7;
        amplitude *= 0.47;
    }
    return total;
}

void fireImage( out vec4 fragColor, in vec2 fragCoord ) {

    const vec3 c1 = vec3(0.5, 0.0, 0.1);
    const vec3 c2 = vec3(0.9, 0.1, 0.0);
    const vec3 c3 = vec3(0.2, 0.1, 0.7);
    const vec3 c4 = vec3(1.0, 0.9, 0.1);
    const vec3 c5 = vec3(0.1);
    const vec3 c6 = vec3(0.9);

    vec2 speed = vec2(0.1, 0.9);
    float shift = 1.327+sin(iTime*2.0)/2.4;
    float alpha = 1.0;
    
	float dist = 3.5-sin(iTime*0.4)/1.89;
    
    vec2 uv = fragCoord.xy / iResolution.xy;
    vec2 p = fragCoord.xy * dist / iResolution.xx;
    p += sin(p.yx*4.0+vec2(.2,-.3)*iTime)*0.04;
    p += sin(p.yx*8.0+vec2(.6,+.1)*iTime)*0.01;
    
    p.x -= iTime/1.1;
    float q = fbm(p - iTime * 0.3+1.0*sin(iTime+0.5)/2.0);
    float qb = fbm(p - iTime * 0.4+0.1*cos(iTime)/2.0);
    float q2 = fbm(p - iTime * 0.44 - 5.0*cos(iTime)/2.0) - 6.0;
    float q3 = fbm(p - iTime * 0.9 - 10.0*cos(iTime)/15.0)-4.0;
    float q4 = fbm(p - iTime * 1.4 - 20.0*sin(iTime)/14.0)+2.0;
    q = (q + qb - .4 * q2 -2.0*q3  + .6*q4)/3.8;
    vec2 r = vec2(fbm(p + q /2.0 + iTime * speed.x - p.x - p.y), fbm(p + q - iTime * speed.y));
    vec3 c = mix(c1, c2, fbm(p + r)) + mix(c3, c4, r.x) - mix(c5, c6, r.y);
    vec3 color = vec3(1.0/(pow(c+1.61,vec3(4.0))) * cos(shift * fragCoord.y / iResolution.y));
    
    color=vec3(1.0,.2,.05)/(pow((r.y+r.y)* max(.0,p.y)+0.1, 4.0));;
    color += vec3(0.055)*mix( vec3(.9,.4,.3),vec3(.7,.5,.2), uv.y);
    color = color/(1.0+max(vec3(0),color));
    alpha = max(color.x, max(color.y, color.z));
    alpha = pow(alpha, 1.4);
    alpha *= 1 - uv.y;
    fragColor = vec4(color.x, color.y, color.z, alpha);
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

float rand2(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

















vec4 raytrace(vec3 ray_origin, vec3 ray_direction, float red, float green) {
    float t = -ray_origin.y / ray_direction.y;
    vec3 col = vec3(0.0);
    float x = 0;
    float z = 0;
    if (t >= 0.0) {
        vec3 rawpos = ray_origin + ray_direction * t;
        vec3 pos = floor(rawpos);
        col = vec3(0.5) + 0.5 * vec3(mod(pos.x + pos.z, 2.0));
        col = mix(vec3(0.0), col, 1 / (1.0 + t * 0.1));
        x = pos.x;
        z = rawpos.z;
    } else {
        float t2 = -(ray_origin.y - 9.5) / ray_direction.y;
        if (t2 >= 0.0) {
            vec3 rawpos = ray_origin + ray_direction * t2;
            vec3 pos = floor(rawpos);
            col = vec3(0.5) + 0.5 * vec3(mod(pos.x + pos.z + 1, 2.0));
            col = mix(vec3(0.0), col, 1 / (1.0 + t2 * 0.1));
            x = pos.x;
            z = rawpos.z;
        }
    }
    if (abs(x-2.5) > 2.5) {
        t = -ray_origin.x / ray_direction.x;
        if (t >= 0.0) {
            vec3 rawpos = ray_origin + ray_direction * t;
            vec3 pos = floor(rawpos);
            col = vec3(0.5) + 0.5 * vec3(mod(pos.y + pos.z + 1, 2.0));
            col = mix(vec3(0.0), col, 1 / (1.0 + t * 0.1));
            z = rawpos.z;
        } else {
            float t2 = -(ray_origin.x - 6) / ray_direction.x;
            if (t2 >= 0.0) {
                vec3 rawpos = ray_origin + ray_direction * t2;
                vec3 pos = floor(rawpos);
                col = vec3(0.5) + 0.5 * vec3(mod(pos.y + pos.z, 2.0));
                col = mix(vec3(0.0), col, 1 / (1.0 + t2 * 0.1));
                z = rawpos.z;
            }
        }
    }
    z = abs(z);
    return mix(vec4(red, red, red, 1.0), vec4(green, green, green, 0.0), 1 / (1.0 + (z * z)));
}





vec4 raytraceBox(vec3 ray_origin, vec3 ray_direction, float red, float green) {
    float t = -ray_origin.y / ray_direction.y;
    float t2 = -(ray_origin.y - 2) / ray_direction.y;
    float t3 = -ray_origin.x / ray_direction.x;
    float t4 = -(ray_origin.x - 2) / ray_direction.x;
    float t5 = -(ray_origin.z + 2) / ray_direction.z;
    vec3 pos = vec3(0.0);
    float hit = 99999.0;
    float brightness = 1.0;
    if (t > 0.0 && t < hit) {
      hit = t;
      brightness = 1.0;
    }
    if (t2 > 0.0 && t2 < hit) {
      hit = t2;
      brightness = 1.0;
    }
    if (t3 > 0.0 && t3 < hit) {
      hit = t3;
      brightness = 0.75;
    }
    if (t4 > 0.0 && t4 < hit) {
      hit = t4;
      brightness = 0.75;
    }
    if (t5 > 0.0 && t5 < hit) {
      hit = t5;
      brightness = 0.75;
    }

    vec3 sph_center = vec3(1.0, 1.0, -1.0);
    float sph_rad = 0.5;

    float t_sph = -1.0;

    vec3 L = sph_center - ray_origin;
    float tca = dot(L, ray_direction);
    if (tca >= 0) {
      float d2 = dot(L, L) - tca * tca;
      if (d2 <= sph_rad * sph_rad) {
        float thc = sqrt(sph_rad * sph_rad - d2);
        float t0 = tca - thc;
        float t1 = tca + thc;
        if (t0 > 0.0) {
          t_sph = t0;
        }
        if (t1 > 0.0 && t1 < t0) {
          t_sph = t1;
        }
      }
    }
    
    vec3 col = hueShift(vec3(1.0, 0.5, 0.5), green * 6.283);
    bool hit_sph = false;
    if (t_sph > 0.0 && t_sph < hit) {
      hit = t_sph;
      hit_sph = true;
      vec3 normall = normalize(ray_origin + ray_direction * hit - sph_center);
      float light = max(dot(normall, vec3(0.0, 1.0, 0.0)), 0.1);
      pos = ray_origin + ray_direction * hit;
      pos.z = abs(pos.z);
      pos -= 1.0;
      float angle = red * 6.283;
      pos = vec3(pos.x * cos(angle) + pos.z * sin(angle), pos.y, pos.z * cos(angle) - pos.x * sin(angle));
      pos = round(pos * 8.0);
      col = mix(col, vec3(1.0), 0.25);
      if (mod(pos.x + pos.y + pos.z, 2.0) > 0.5) {
        return vec4(light * col * 0.75, 1.0);
      }
      return vec4(light * col, 1.0);
    }

    pos = ray_origin + ray_direction * hit;
    pos.z = abs(pos.z);
    pos -= 1.0;
    if (pos.y < 0.0) {
      brightness = 0.2 + 0.55 * smoothstep(0.5, 0.6, length(vec2(pos.x, pos.z)));
    }

    
    if (abs(pos.x) > 0.97 && abs(pos.y) > 0.97) {
      brightness = 0.0;
    }
    if (abs(pos.z) > 0.97 && abs(pos.y) > 0.97) {
      brightness = 0.0;
    }
    if (abs(pos.x) > 0.97 && abs(pos.z) > 0.97) {
      brightness = 0.0;
    }


    pos = round(pos * 4.0);
    if (mod(pos.x + pos.y + pos.z, 2.0) > 0.5) {
      return vec4(col * 0.75 * brightness, 1.0);
    }
    return vec4(col * brightness, 1.0);
}





void main() {
    if (getTimeData() != 0) {
        if (gl_FragCoord.x / ScreenSize.x - 0.5 > 0.1 || gl_FragCoord.x / ScreenSize.x - 0.5 < -0.17) {
            discard;
        }
    }

    vec2 size = textureSize(Sampler0, 0);
    vec2 oneTexel = vec2(1.0) / size;
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    vec4 texcolor = texture(Sampler0, texCoord0);
    vec2 rounded = floor(texCoord0 * size / 16.0);
    vec2 uv2 = texCoord0 * size / 16.0 - rounded;
    if (round(texcolor.a * 255) == 254.0) {
        fragColor = vec4(gl_FragCoord.xy / ScreenSize, 0.0, 1.0);
        return;
    }
    if (int(round(texcolor.a * 255) + 0.5) == 254) {
        fragColor = vec4(0.0, 1.0, 0.0, 1.0);
        return;
    }
    if (round(texcolor.a * 255) == 254.0 && length(controlColor.xyz) < sqrt(3.0) - 0.1) {
        fragColor = controlColor;
            //vec2 uvPoint = texcolor.rg;
            vec2 uv = uvPoint;
            int param_r = int(round(controlColor.r * 255) + 0.5);
            int param_g = int(round(controlColor.g * 255) + 0.5);
            float effect_id = round(controlColor.b * 255);
            //if (effect_id == 1)
            //{
            //    fireImage(color, uv * iResolution.xy * vec2(1.0, 0.6));
            //    fragColor = REMOVE_BLUE(fragColor);
            //}
            //if (effect_id == 2)
            //{
            //    float rounded = floor(uv.y * 64) * 0.5;
            //    uv.x += sin(GameTime * 400.0 + 0.4 * rounded) * 0.25;
            //    fireImage(color, uv * iResolution.xy * vec2(1.0, 0.5));
            //    fragColor = REMOVE_BLUE(fragColor);
            //}
            if (effect_id == 3)
            {
                float rounded = floor(uv.y * 64) * 1.0;
                uv.x += sin(GameTime * 4000.0 + -0.4 * rounded) * 0.25 * 0.1;
                fireImage(color, uv * iResolution.xy * vec2(10.0, 0.75));
                fragColor = REMOVE_BLUE(fragColor);
            }
            if (effect_id == 4)
            {
                crystalHeart(color, uv * iResolution.xy);
                color *= whiteVertexColor;
                color *= 1.5;
                fragColor = REMOVE_BLUE(fragColor);
            }
            if (effect_id == 5)
            {
                uv = round(uv * 128) / 128;
                crystalHeart(color, uv * iResolution.xy);
                fragColor = REMOVE_BLUE(fragColor);
            }
        //    //if (effect_id == 6)
        //    //{
        //    //    if (mod(uv.y + GameTime * 100.0, 1.0) < 1. / 32)
        //    //    {
        //    //        float t = mod(uv.y + GameTime * 100.0, 1.0) - 1. / 64;
        //    //        t *= 64;
        //    //        t = 1.0 - abs(t);
        //    //        uv.y = round(uv.y * 32) / 32;
        //    //        crystalHeart(color, uv * iResolution.xy);
        //    //        color = vec4(hueShift(color.rgb, 0.2), color.a);
        //    //        color *= 1 + t;
        //    //    }
        //    //    else
        //    //    {
        //    //        crystalHeart(color, uv * iResolution.xy);
        //    //    }
        //    //    fragColor = REMOVE_BLUE(fragColor);
        //    //}
        //    //if (effect_id == 7)
        //    //{
        //    //    if (mod(uv.y * 16 + GameTime * 1000.0, 1.0) < 1. / 4)
        //    //    {
        //    //        float t = mod(uv.y * 16 + GameTime * 1000.0, 1.0) - 1. / 8;
        //    //        t *= 8;
        //    //        t = 1.0 - abs(t);
        //    //        if (uv.y < 0.625)
        //    //        {
        //    //            uv.y = floor(uv.y * 32) / 32;
        //    //        }
        //    //        else
        //    //        {
        //    //            uv.y = ceil(uv.y * 32) / 32;
        //    //        }
        //    //        crystalHeart(color, uv * iResolution.xy);
        //    //        color = vec4(hueShift(color.rgb, 3), color.a);
        //    //        color *= 1 + t;
        //    //    }
        //    //    else
        //    //    {
        //    //        crystalHeart(color, uv * iResolution.xy);
        //    //        color.g = color.r;
        //    //        color.b = color.r;
        //    //    }
        //    //    fragColor = REMOVE_BLUE(fragColor);
        //    //}
        //    //if (effect_id == 8)
        //    //{
        //    //    uv = uv * 2.0 - 1.0;
        //    //    float t = mod(length(uv) * 4 - GameTime * 1000.0 + abs(sin(uv.x * 30) * 0.2), 1.0);
        //    //    if (t > 0.5)
        //    //    {
        //    //        discard;
        //    //    }
        //    //    else
        //    //    {
        //    //        fragColor = vec4(1.0 * clamp(1.0 - length(uv) * 0.5, 0.0, 1.0), 0.0, 0.0,(t * 2) * clamp(1.0 - length(uv), 0.0, 1.0));
        //    //        fragColor = REMOVE_BLUE(fragColor);
        //    //        return;
        //    //    }
        //    //}
        //    //if (effect_id == 9)
        //    //{
        //    //    fragColor = vec4(Norm, 1.0);
        //    //    fragColor = REMOVE_BLUE(fragColor);
        //    //    return;
        //    //}
            if (effect_id == 10)
            {
                vec3 tangent = normalize(cross(vec3(0.,1.,0.), Norm));
                vec3 bitangent = normalize(cross(Norm, tangent));
                mat3 localMat = mat3(tangent, bitangent, Norm);

                vec3 nearPos = clipNear.xyz / clipNear.w;

                vec3 raydir = normalize(worldpos - nearPos) * localMat;
                vec3 rayorigin = vec3(vec2(6, 9.5) * uv, 0);
                fragColor = raytrace(rayorigin, raydir, controlColor.r, controlColor.g);
                fragColor = REMOVE_BLUE(fragColor);
                return;
            }
            if (effect_id == 11)
            {
                float x = floor(uv.x * 32) / 32.0;
                float squish = sin(GameTime * 1000.0 + x * 10.0) * 0.5 + 0.5;
                squish = squish * 0.05 + 0.95;
                float preY = clamp((uv.y - (1.0 - squish)) / squish, 0.0, 1.0);
                float y = floor(preY * 32) / 32.0;
                fragColor = vec4(1.0, 0.5, 0.0, 0.0);
                if (abs(x - 0.5) * 2 < 1 - y) {
                    float xScale = pow(abs(x - 0.5) * 2, 2.0) + 0.5;
                    fragColor = vec4(mix(vec3(1.0, 0.75, 0.0), vec3(0.0, 0.85, 1.0), controlColor.g), y * xScale);
                }
                //fragColor = vec4(uvPoint, 0.0, 1.0);
                if (fragColor.a == 0.0) {
                    discard;
                }
                fragColor = REMOVE_BLUE(fragColor);
                return;
            }
            if (effect_id == 12)
            {
                float xCount = round(controlColor.r * 255)+1;
                float yCount = round(controlColor.g * 255)+1;
                float x = floor(uv.x * xCount);
                float y = floor(uv.y * yCount);
                float px = mod(uv.x * xCount, 1.0) * 2.0 - 1.0;
                float py = mod(uv.y * yCount, 1.0) * 2.0 - 1.0;
                fragColor = vec4(0.0);
                if (x == 0.0 || y == 0.0 || x == xCount - 1 || y == yCount - 1) {
                    float d = px * px * px * px + py * py * py * py ;
                    if (d < 1.0) {
                        vec3 c = mix(vec3(1.0, 1.0, 0.0), vec3(1.0, 0.0, 1.0), mod(x + y + floor(GameTime * 2000.0), 2.0));
                        c = mix(c * 0.8, mix(c, vec3(1.0), 0.25), 1.0 - d);
                        vec4 finalColor = vec4(c, 0.5);
                        fragColor = apply_fog(finalColor, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
                    }
                }
                fragColor = REMOVE_BLUE(fragColor);
                return;
            }
            if (effect_id == 13)
            {
                fragColor = vec4(0.75, 0.75, 0.75, 0.15 + 0.3 * (sin(floor(GameTime * 1000.0 * 8.0) / 8.0) * 0.5 + 0.5));
                if (uv.x < 0.05 || uv.y < 0.1 || uv.x > 0.95 || uv.y > 0.875) {
                    fragColor = vec4(0.75, 0.75, 0.75, 0.5);
                    if (uv.x > 0.04 && uv.y > 0.09 && uv.x < 0.96 && uv.y < 0.885) {
                        fragColor = vec4(0.2, 0.2, 0.2, 0.75);
                    }
                }
                fragColor = REMOVE_BLUE(fragColor);
                return;
            }
            if (effect_id == 14)
            {
                float y = floor(uv.y * 16) / 16.0;
                fragColor = vec4(vec3(0.0, 1.0, 0.0), (1 - y) * ((sin(GameTime * 1500.0) * 0.5 + 0.5) * 0.25 + 0.25));
                fragColor = REMOVE_BLUE(fragColor);
                return;
            }
            if (effect_id == 15)
            {
                float xCount = round(controlColor.r * 255)+1;
                float yCount = round(controlColor.g * 255)+1;
                float x = floor(uv.x * xCount);
                float y = floor(uv.y * yCount);
                float px = mod(uv.x * xCount, 1.0) * 2.0 - 1.0;
                float py = mod(uv.y * yCount, 1.0) * 2.0 - 1.0;
                fragColor = vec4(0.0);
                if (x == 0.0 || y == 0.0 || x == xCount - 1 || y == yCount - 1) {
                    float d = px * px * px * px + py * py * py * py ;
                    if (d < 1.0) {
                        vec3 c = vec3(1.0, 1.0, 0.0);
                        if (mod(x + y + floor(GameTime * 8000.0), 3.0) == 1.0) {
                            c = vec3(1.0, 0.0, 1.0);
                        }
                        if (mod(x + y + floor(GameTime * 8000.0), 3.0) == 2.0) {
                            c = vec3(0.0, 1.0, 1.0);
                        }
                        c = mix(c * 0.8, mix(c, vec3(1.0), 0.25), 1.0 - d);
                        vec4 finalColor = vec4(c, 0.5);
                        fragColor = apply_fog(finalColor, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
                    }
                }
                fragColor = REMOVE_BLUE(fragColor);
                return;
            }
            if (effect_id == 16)
            {
                fragColor = vec4(uv, 0.0, 1.0);
                //gl_FragDepth = 0.0;
                fragColor = REMOVE_BLUE(fragColor);
                return;
            }
            if (effect_id == 17)
            {
              vec3 tangent = normalize(cross(vec3(0.,1.,0.), Norm));
              vec3 bitangent = normalize(cross(Norm, tangent));
              mat3 localMat = mat3(tangent, bitangent, Norm);
              vec3 nearPos = clipNear.xyz / clipNear.w;
              vec3 raydir = normalize(worldpos - nearPos) * localMat;
              vec3 rayorigin = vec3(vec2(2.0) * uv, 0);
              fragColor = raytraceBox(rayorigin, raydir, controlColor.r, controlColor.g);
              fragColor = REMOVE_BLUE(fragColor);
              return;
            }
            if (effect_id == 18)
            {
              vec2 tcor = gl_FragCoord.xy / ScreenSize.xy;
              tcor.y = 1.0 - tcor.y;
              vec2 scale = vec2(0.0);
              if (param_g == 0) {
                scale = vec2(320, 180);
              }
              vec2 tcor1 = tcor;
              vec2 tcor2 = tcor;
              vec2 tcor3 = tcor;
              float strength = 0.04 + 0.04 * cos(param_r / 255.0 * 6.283 + 3.1415);
              tcor1.x += sin(tcor.y * 10.0 + param_r * 10.0 * 6.28) * strength * (1.0 - tcor.y);
              tcor2.x += sin(tcor.y * 10.0 + 6.283 * 0.33 + param_r * 10.0 * 6.28) * strength * (1.0 - tcor.y);
              tcor3.x += sin(tcor.y * 10.0 + 6.283 * 0.66 + param_r * 10.0 * 6.28) * strength * (1.0 - tcor.y);
              tcor1.x = min(max(tcor1.x, 0.0), 1.0);
              tcor2.x = min(max(tcor2.x, 0.0), 1.0);
              tcor3.x = min(max(tcor3.x, 0.0), 1.0);
              fragColor = vec4(0.0);
              fragColor += texture(Sampler0, texCoord0 + tcor1 * oneTexel * (scale - vec2(1.0)));
              fragColor += texture(Sampler0, texCoord0 + tcor2 * oneTexel * (scale - vec2(1.0)));
              fragColor += texture(Sampler0, texCoord0 + tcor3 * oneTexel * (scale - vec2(1.0)));
              fragColor *= 0.333;
              fragColor.a = 1.0;
              fragColor = REMOVE_BLUE(fragColor);
              return;
            }
            if (effect_id == 19)
            {
              vec4 c1 = vec4(0.0);
              vec4 c2 = vec4(0.0);
              nvmainscreen_mainImage(c1, gl_FragCoord.xy, controlColor.r * 20.0);
              nvmainscreen_mainImage(c2, gl_FragCoord.xy, (controlColor.r + 1.0) * 20.0);
              fragColor = vec4(mix(c2.rgb, c1.rgb, controlColor.r), 1.0);
              vec4 moon = vec4(0.0);
              vec2 uv = gl_FragCoord.xy / ScreenSize;
              uv -= vec2(0.5);
              uv *= 1.5;
              uv += vec2(0.1, 0.5);
              nvmainscreen_moon(moon, uv * ScreenSize, Sampler0, sin(controlColor.r * 6.283), texCoord0);
              fragColor = vec4(mix(fragColor.rgb, moon.rgb, moon.a), 1.0);
              fragColor = vec4(gl_FragCoord.xy / ScreenSize, 0.0, 1.0);
              //fragColor = REMOVE_BLUE(fragColor);
              return;
            }
            if (effect_id == 20)
            {
              fragColor = vec4(uv.xy, 1.0 / 255.0, 1.0);
              return;
            }
    }
    else
    {
        if (color.a < 0.3) {
            discard;
        }
    }
    if (color.a < 0.1) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    fragColor = REMOVE_BLUE(fragColor);
}
