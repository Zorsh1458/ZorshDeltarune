void soulintro_mainImage( out vec4 fragColor, in vec2 fragCoord, float sin_time )
{
	float s = 0.0, v = 0.0;
	vec2 uv = (fragCoord / ScreenSize.xy) * 2.0 - 1.;
    float time = (sin_time-2.0)*10.0;
	vec3 col = vec3(0);
    vec3 init = vec3(0.0, .35, time * 0.002);
	for (int r = 60; r < 80; r++) 
	{
		vec3 p = init + s * vec3(uv, 0.05);
		for (int i=0; i < 6; i++)	p = abs(p * 2.04) / dot(p, p) - .9;
		v += pow(dot(p, p), .7) * .06;
		col +=  vec3(v * 0.2+.4, 12.-s*2., .1 + v * 1.) * v * 0.00003;
		s += .025;
	}
	fragColor = vec4(tanh(col), 1.0);
}