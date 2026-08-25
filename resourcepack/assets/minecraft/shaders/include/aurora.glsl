mat2 aurora_mm2(in float a){float c = cos(a), s = sin(a);return mat2(c,s,-s,c);}
mat2 aurora_m2 = mat2(0.95534, 0.29552, -0.29552, 0.95534);
float aurora_tri(in float x){return clamp(abs(fract(x)-.5),0.01,0.49);}
vec2 aurora_tri2(in vec2 p){return vec2(aurora_tri(p.x)+aurora_tri(p.y),aurora_tri(p.y+aurora_tri(p.x)));}

float aurora_triNoise2d(in vec2 p, float spd, float aurora_time)
{
    float z=1.8;
    float z2=2.5;
	float rz = 0.;
    p *= aurora_mm2(p.x*0.06);
    vec2 bp = p;
	for (float i=0.; i<3.; i++ )
	{
        vec2 dg = aurora_tri2(bp*1.85)*.75;
        dg *= aurora_mm2(aurora_time*spd);
        p -= dg/z2;

        bp *= 1.3;
        z2 *= .45;
        z *= .42;
		p *= 1.21 + (rz-1.0)*.02;
        
        rz += aurora_tri(p.x+aurora_tri(p.y))*z;
        p*= -aurora_m2;
	}
    return clamp(1./pow(rz*29., 1.3),0.,.55);
}

float aurora_hash21(in vec2 n){ return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453); }
vec4 aurora_aurora(vec3 ro, vec3 rd, float aurora_time)
{
    vec4 col = vec4(0);
    vec4 avgCol = vec4(0);
    
    for(float _i=0.;_i<8.;_i++)
    {
        float i = _i * 3.0;
        float of = 0.006*aurora_hash21(gl_FragCoord.xy)*smoothstep(0.,15., i);
        float pt = ((.8+pow(i,1.4)*.002)-ro.y)/(rd.y*2.+0.4);
        pt -= of;
    	vec3 bpos = ro + pt*rd;
        vec2 p = bpos.zx;
        float rzt = aurora_triNoise2d(p, 0.06, aurora_time);
        vec4 col2 = vec4(0,0,0, rzt);
        col2.rgb = (sin(1.-vec3(2.15,-.5, 1.2)+i*0.043)*0.5+0.5)*rzt;
        avgCol =  mix(avgCol, col2, .5);
        col += avgCol*exp2(-i*0.065 - 2.5)*smoothstep(0.,5., i);
        
    }
    
    col *= (clamp(rd.y*15.+.4,0.,1.));
    
    
    //return clamp(pow(col,vec4(1.3))*1.5,0.,1.);
    //return clamp(pow(col,vec4(1.7))*2.,0.,1.);
    //return clamp(pow(col,vec4(1.5))*2.5,0.,1.);
    //return clamp(pow(col,vec4(1.8))*1.5,0.,1.);
    
    //return smoothstep(0.,1.1,pow(col,vec4(1.))*1.5);
    return col*2.8;
    //return pow(col,vec4(1.))*2.
}


//-------------------Background and Stars--------------------

vec3 aurora_nmzHash33(vec3 q)
{
    uvec3 p = uvec3(ivec3(q));
    p = p*uvec3(374761393U, 1103515245U, 668265263U) + p.zxy + p.yzx;
    p = p.yzx*(p.zxy^(p >> 3U));
    return vec3(p^(p >> 16U))*(1.0/vec3(0xffffffffU));
}

vec3 aurora_stars(in vec3 p)
{
    vec3 c = vec3(0.);
    float res = iResolution.x*1.;
    
	for (float i=0.;i<4.;i++)
    {
        vec3 q = fract(p*(.15*res))-0.5;
        vec3 id = floor(p*(.15*res));
        vec2 rn = aurora_nmzHash33(id).xy;
        float c2 = 1.-smoothstep(0.,.6,length(q));
        c2 *= step(rn.x,.0005+i*i*0.001);
        c += c2*(mix(vec3(1.0,0.49,0.1),vec3(0.75,0.9,1.),rn.y)*0.1+0.9);
        p *= 1.3;
    }
    return c*c*.8;
}

vec3 aurora_bg(in vec3 rd)
{
    float sd = dot(normalize(vec3(-0.5, -0.6, 0.9)), rd)*0.5+0.5;
    sd = pow(sd, 5.);
    vec3 col = mix(vec3(0.05,0.1,0.2), vec3(0.1,0.05,0.2), sd);
    return col*.63;
}
//-----------------------------------------------------------


vec3 aurora_hueShift( vec3 color, float hueAdjust ){

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

void aurora_mainImage( out vec4 fragColor, in vec3 ro, in vec3 rd, float aurora_time )
{
    vec3 col = vec3(0.);
    vec3 brd = rd;
    float fade = smoothstep(0.,0.01,abs(brd.y))*0.1+0.9;
    
    col = aurora_bg(rd)*fade * 0.1;
    
    if (rd.y > 0.){
        vec4 aur = smoothstep(0.,1.5,aurora_aurora(ro,rd, aurora_time))*fade;
        //col += aurora_stars(rd);
        col = col*(1.-aur.a) + aur.rgb;
    }
    //else //Reflections
    //{
    //    rd.y = abs(rd.y);
    //    col = aurora_bg(rd)*fade*0.6;
    //    vec4 aur = smoothstep(0.0,2.5,aurora_aurora(ro,rd, aurora_time));
    //    col += aurora_stars(rd)*0.1;
    //    col = col*(1.-aur.a) + aur.rgb;
    //    vec3 pos = ro + ((0.5-ro.y)/rd.y)*rd;
    //    float nz2 = aurora_triNoise2d(pos.xz*vec2(.5,.7), 0., aurora_time);
    //    col += mix(vec3(0.2,0.25,0.5)*0.08,vec3(0.3,0.3,0.5)*0.7, nz2*0.4);
    //}
    
	fragColor = vec4(aurora_hueShift(col, 2.85), 1.);
}
