float nvmainscreen_random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

void nvmainscreen_iteration(vec2 uv, float i, out vec3 col, float size, float width, float nv_time) {
    float seed = 0.3;
    float r = nvmainscreen_random(vec2(i, seed * 2.0));
    vec2 p = vec2(nvmainscreen_random(vec2(i, seed)), nvmainscreen_random(vec2(seed, i)));
    vec2 dir = vec2(nvmainscreen_random(vec2(i, -seed)), nvmainscreen_random(vec2(-seed, i))) - 0.5;
    p = mod(p + dir * nv_time * 0.01, 1.0);
    vec2 offset = uv - p;
    offset.x *= iResolution.x / iResolution.y;
    float dist = length(offset) * size * (1.0 + sin(nv_time + r * 3.1415) * 0.3);
    if (dist < 1.0) {
        float angle = nvmainscreen_random(vec2(seed * 2.0, i)) * 3.1415 + nv_time * (nvmainscreen_random(vec2(i, seed * 3.0)) - 0.5);
        offset = vec2(cos(angle) * offset.x + sin(angle) * offset.y, cos(angle) * offset.y - sin(angle) * offset.x);
        vec2 ddist = vec2(abs(offset.x), abs(offset.y));
        if (dist < 1.0 && (ddist.x < width || ddist.y < width)) {
            col += vec3(1.0 - dist);
        }
        col += vec3(1.0 - sqrt(dist)) * 0.4;
    }
}

void nvmainscreen_mainImage( out vec4 fragColor, in vec2 fragCoord, float nv_time )
{
    vec2 uv = fragCoord/iResolution.xy;

    vec3 col = vec3(0.0);
    
    for (float i = 0.0; i < 8.0; i++) {
        nvmainscreen_iteration(uv, i, col, 45, 0.001, nv_time);
    }
    
    for (float i = 8.0; i < 12.0; i++) {
        nvmainscreen_iteration(uv, i, col, 25, 0.001, nv_time);
    }
    
    for (float i = 12.0; i < 20.0; i++) {
        nvmainscreen_iteration(uv, i, col, 85, 0.0003, nv_time);
    }
    
    col = vec3(min(col.r, 1.0));
    fragColor = vec4(col * 0.75, 1.0);
}


#define SCALE 0.95

float nvmainscreen_iSphere( in vec3 ro, in vec3 rd, in vec4 sph )
{
	vec3 oc = ro - sph.xyz;
	float b = dot( oc, rd );
	float c = dot( oc, oc ) - sph.w*sph.w;
	float h = b*b - c;
	if( h<0.0 ) return h;
	return -b - sqrt( h );
}

float nvmainscreen_map(vec3 p, sampler2D texSampler, float nv_time, vec2 texCoord) {
    float PI = 3.1415;
    float lat = 90. - acos(p.y / length(p)) * 180./PI;
    float lon = atan(p.x, p.z) * 180./PI;
    vec2 uv = vec2(lon/360., lat/180.) + 0.5;
    vec2 TEX_SIZE = vec2(320, 320);
    vec2 coord = mod(uv + vec2(nv_time * 0.02, 0.0), 1.0);
    coord.x = 1.0 - coord.x;
    vec2 fincoord = texCoord + coord * TEX_SIZE / textureSize(texSampler, 0);
    return texture(texSampler, fincoord).x;
}

vec3 nvmainscreen_normal(vec3 p, sampler2D texSampler, float nv_time, vec2 texCoord) {
	vec2 e = vec2(1,0)/1e3;
    p += 0.000 * vec3(
        nvmainscreen_map(p + e.xyy, texSampler, nv_time, texCoord) - nvmainscreen_map(p - e.xyy, texSampler, nv_time, texCoord),
        nvmainscreen_map(p + e.yxy, texSampler, nv_time, texCoord) - nvmainscreen_map(p - e.yxy, texSampler, nv_time, texCoord),
        nvmainscreen_map(p + e.yyx, texSampler, nv_time, texCoord) - nvmainscreen_map(p - e.yyx, texSampler, nv_time, texCoord))/ (2. * length(e));
	return normalize(p);
}

void nvmainscreen_moon( out vec4 fragColor, in vec2 fragCoord, sampler2D texSampler, float nv_time, vec2 texCoord ) {
    vec2 p = (2. * fragCoord.xy - iResolution.xy) / iResolution.y;
    float lat = 0.;
    float lon = 10.;
    float PI = 3.1415;
    vec3 camPos = 10. * vec3(sin(lon*PI/180.) * cos(lat*PI/180.), sin(lat*PI/180.), cos(lon*PI/180.) * cos(lat*PI/180.));
    vec3 w = normalize(-camPos);
    vec3 u = normalize(cross(w, vec3(0,1,0)));
    vec3 v = normalize(cross(u, w));
    mat3 camera = mat3(u, v, w);
    
    vec3 dir = normalize(camera * vec3(p / SCALE, length(camPos)));
    //float dist = nvmainscreen_iSphere(camPos, dir, vec4(0,0,0,0.97 + sin(nv_time) * 0.03));
    float dist = nvmainscreen_iSphere(camPos, dir, vec4(0,0,0,1));
    fragColor = vec4(0);
    if (dist > 0.) {
        vec3 q = camPos + dir * dist;
        float c = nvmainscreen_map(q, texSampler, nv_time, texCoord);
        vec3 n = nvmainscreen_normal(q, texSampler, nv_time, texCoord);
        float light = clamp(dot(n, normalize(vec3(-4,1,2))), 0., 1.);
        //float heat = clamp(2. / pow(nv_time, 2.), 0., 1.);
        //heat = 0.0;
        //fragColor = light * 0.5 * mix(vec4(0.58, 0.57, 0.55, 1), vec4(0.15, 0.13, 0.1, 1), smoothstep(0., 3., c));
        //fragColor += 5. * c * heat * vec4(1., 0.15, 0.05, 1.);
        fragColor = vec4(vec3(1.0) * min(c, 1.0) * (0.005 + light * 0.9), 1.0);
    }
    fragColor.rgb = mix(fragColor.rgb, vec3(0), smoothstep(SCALE - 4./iResolution.y, SCALE + 1./iResolution.y, length(p)));
    fragColor.rgb = pow(fragColor.rgb, vec3(1./2.2));
    fragColor.a = 1.0;
    if (dist < 0.0) {
        fragColor.a = 0.0;
        if (dist > -0.06 && dist < -0.045) {
            fragColor = vec4(vec3(0.5, 0.5, 0.75) * 0.33, 1.0);
        }
    }
}
	
