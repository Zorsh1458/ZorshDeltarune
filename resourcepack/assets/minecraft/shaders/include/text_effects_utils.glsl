#version 150

#moj_import <minecraft:globals.glsl>

precision highp float;

#define hue(v)  ((.6+.6*cos(6.*(v)+vec4(0, 23, 21, 1)))+vec4(0., 0., 0., 1.) )

#define finalize() { \
    sphericalVertexDistance=length((ModelViewMat*vertex).xyz); \
    cylindricalVertexDistance=length((ModelViewMat*vertex).xyz); \
}

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453) * 0.01;
}

void applyProjection(inout vec4 vertex) {
    gl_Position = ProjMat * ModelViewMat * vertex;
}

void applyColorTexture() {
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
}

void applyHueColor() {
    vertexColor = hue(gl_Position.x + GameTime * 1000.) * texelFetch(Sampler2, UV2 / 16, 0);
}

vec4 lin_int(vec4 start, vec4 end, float t) {
    return start + (end - start) * t;
}

void applyGoldColor() {
    vertexColor = lin_int(vec4(255, 196, 0, 255.0) / 255., vec4(255, 255, 255, 255.0) / 255., (sin(gl_Position.y * 1000. + GameTime * 10000) + 1) / 2.);
}

void applyMegaWaveColor() {
    float Y = gl_Position.y + sin(GameTime * 4000. + (gl_Position.x * 18)) / 20.;
    vertexColor = hue(Y * 10 + GameTime * 10.) * texelFetch(Sampler2, UV2 / 16, 0);
}

void applyWaveEffect() {
    gl_Position.y += sin(GameTime * 12000. + (gl_Position.x * 12)) / 150.;
    //float modif = 0.03;
    //float time = GameTime + 1000 * float(int(gl_Position.x / modif)) * modif;
    //float t1 = round(time * 10000) * 0.0001;
    //float t2 = round((time + 100.0) * 10000) * 0.0001;
    //gl_Position.x += rand(vec2(t1, t1));
    //gl_Position.y += rand(vec2(t2, t2));
}

void applyMegaWaveEffect() {
    gl_Position.y += sin(GameTime * 4000. + (gl_Position.x * 18)) / 20. + sin(GameTime * 1000000) * 0.01;
    //gl_Position.x += cos(GameTime * 40000. + (gl_Position.x * 18)) / 100.;
}

void applyXWaveEffect() {
    gl_Position.x += cos(GameTime * 4000. + (gl_Position.x * 36)) / 50.;
}

void processMegaWave(inout vec4 vertex) {
    applyProjection(vertex);
    applyMegaWaveColor();
    applyMegaWaveEffect();
    finalize();
}

void processXWave(inout vec4 vertex) {
    applyProjection(vertex);
    applyGoldColor();
    //applyXWaveEffect();
    finalize();
}

void processCoolEffect(inout vec4 vertex) {
    applyGoldColor();
    float vertexId = mod(gl_VertexID, 4.);
    if (vertex.z > 0.) {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y += 1;
        }
        else {
            vertex.y -= 1;
        }
        if (vertexId == 0. || vertexId == 1.) {
            vertex.x += 1;
        }
        else {
            vertex.x -= 1;
        }
    }
    applyProjection(vertex);
    finalize();
}

void processPhysicEffect(inout vec4 vertex) {
    applyGoldColor();
    float vertexId = mod(gl_VertexID, 4.);
    float t = cos(GameTime * 12000. / 4 + (gl_VertexID - vertexId) * 0.1) * 1;
    if (vertex.z > 0.) {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y -= t;
        }
        else {
            vertex.y += t;
        }
        if (vertexId == 0. || vertexId == 1.) {
            vertex.x -= t;
        }
        else {
            vertex.x += t;
        }
    }
    applyProjection(vertex);
    finalize();
}

void processJellyEffect(inout vec4 vertex) {
    applyGoldColor();
    float vertexId = mod(gl_VertexID, 4.);
    float t = cos(GameTime * 40000. / 4 + (gl_VertexID - vertexId) * 0.05) * 2 + 2;
    if (vertex.z > 0.) {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y -= t;
        }
        else {
            vertex.y += t;
        }
    }
    applyProjection(vertex);
    finalize();
}

void processSpinEffect(inout vec4 vertex) {
    applyGoldColor();
    float vertexId = mod(gl_VertexID, 4.);
    //float t = cos(GameTime * 12000. / 4 + (gl_VertexID - vertexId) * 0.1) * 1;
    float t = sin(GameTime * 12000. / 4 + (gl_VertexID - vertexId) * 0.1) * 0.1;
    t += 3.926875;
    float mod = 4;
    if (vertex.z > 0.) {
        vec4 offset = vec4(0, 0, 0, 0);
        if (vertexId == 0.) {
            offset = vec4(mod, mod, 0, 0);
        }
        if (vertexId == 1.) {
            offset = vec4(mod, -mod, 0, 0);
        }
        if (vertexId == 2.) {
            offset = vec4(-mod, -mod, 0, 0);
        }
        if (vertexId == 3.) {
            offset = vec4(-mod, mod, 0, 0);
        }
        vertex += offset;
        float scale = 1.5;
        if (vertexId == 0.) {
            vertex.x += sin(t) * mod * scale;
            vertex.y += cos(t) * mod * scale;
        }
        if (vertexId == 1.) {
            vertex.x += cos(t) * mod * scale;
            vertex.y += -sin(t) * mod * scale;
        }
        if (vertexId == 2.) {
            vertex.x += -sin(t) * mod * scale;
            vertex.y += -cos(t) * mod * scale;
        }
        if (vertexId == 3.) {
            vertex.x += -cos(t) * mod * scale;
            vertex.y += sin(t) * mod * scale;
        }
    }
    applyProjection(vertex);
    finalize();
}

void processShakeEffect(inout vec4 vertex) {
    applyGoldColor();
    float vertexId = mod(gl_VertexID, 4.);
    float t = cos(GameTime * 120000. / 4 + (gl_VertexID - vertexId) * 1) * 1;
    //vertex.x += t;
    vertex.y += t;
    applyProjection(vertex);
    finalize();
}

void processRainbowEffect(inout vec4 vertex) {
    applyProjection(vertex);
    applyHueColor();
    finalize();
}

void processWavyEffect(inout vec4 vertex) {
    applyProjection(vertex);
    applyColorTexture();
    applyWaveEffect();
    finalize();
}

void processWavyRainbowEffect(inout vec4 vertex) {
    applyProjection(vertex);
    applyWaveEffect();
    applyHueColor();
    finalize();
}

void processBouncyEffect(inout vec4 vertex) {
    applyColorTexture();
    float vertexId = mod(gl_VertexID, 4.);
    if (vertex.z <= 0.) {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y += cos(GameTime * 12000. / 4) * .1;
            vertex.y += max(cos(GameTime * 12000. / 4) * .1, 0.);
        }
    } else {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y -= cos(GameTime * 12000. / 4) * 3;
            vertex.y -= max(cos(GameTime * 12000. / 4) * 4, 0.);
        }
    }
    applyProjection(vertex);
    finalize();
}

void processBouncyRainbowEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    if (vertex.z <= 0.) {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y += cos(GameTime * 12000. / 4) * .1;
            vertex.y += max(cos(GameTime * 12000. / 4) * .1, 0.);
        }
    } else {
        if (vertexId == 3. || vertexId == 0.) {
            vertex.y -= cos(GameTime * 12000. / 4) * 3;
            vertex.y -= max(cos(GameTime * 12000. / 4) * 4, 0.);
        }
    }
    applyHueColor();
    applyProjection(vertex);
    finalize();
}

void processBlinkingEffect(inout vec4 vertex, float speed) {
    applyProjection(vertex);
    float blink = abs(sin(GameTime * 12000. * speed));
    vertexColor = Color * blink * texelFetch(Sampler2, UV2 / 16, 0);
    finalize();
}

void processNoShadow(inout vec4 vertex) {
    applyProjection(vertex);
    applyColorTexture();
    vertexColor = vec4(1, 1, 1, vertexColor.a); 
    finalize();
}

void processWaterRippleEffect(inout vec4 vertex) {
    vec2 center = vec2(0.0, 0.0); // Центр экрана
    vec2 pos = (ModelViewMat * vertex).xy;
    float dist = distance(pos, center);
    float wave = sin(dist * 30.0 - GameTime * 10000.) * 1;
    vertex.xy += wave * normalize(pos - center);
    
    applyProjection(vertex);
    applyColorTexture();
    finalize();
}

void processBurningEffect(inout vec4 vertex) {
    applyProjection(vertex);
    float noise = fract(sin(vertex.x * 1000. + GameTime * 12000.) * 43758.5453);
    float burn = smoothstep(0.3, 0.7, noise);
    vec4 fireColor = mix(vec4(1.0, 0.3, 0.0, 1.0), vec4(1.0, 0.8, 0.0, 1.0), noise);
    vertexColor = mix(texelFetch(Sampler2, UV2 / 16, 0), fireColor, burn);
    finalize();
}

void processExplodeEffect(inout vec4 vertex) {
    float charId = float(gl_VertexID / 4);
    float t = mod(GameTime * 1000., 5.0); // Цикл каждые 5 секунд
    if (t < 1.0) {
        // Нормальное состояние
        applyProjection(vertex);
    } else {
        // Разлетание
        float angle = charId * 1.618; // Золотое сечение для распределения
        float distance = (t - 1.0) * 3;
        vertex.x += cos(angle) * distance;
        vertex.y += sin(angle) * distance;
        applyProjection(vertex);
    }
    applyColorTexture();
    finalize();
}

void processNeonEffect(inout vec4 vertex) {
    applyProjection(vertex);
    float glow = 0.7 + 0.3 * sin(GameTime * 15000. + gl_Position.x * 50.);
    vec4 baseColor = texelFetch(Sampler2, UV2 / 16, 0);
    vertexColor = vec4(baseColor.rgb * glow * 1.5, baseColor.a);
    finalize();
}

void processTypewriterEffect(inout vec4 vertex) {
    applyProjection(vertex);
    float pulse = 0.5 + 0.5 * sin(GameTime * 8000.);
    vertexColor = Color * pulse * texelFetch(Sampler2, UV2 / 16, 0);
    finalize();
}

void processMetalGlint(inout vec4 vertex) {
    applyProjection(vertex);
    float lightPos = sin(GameTime * 5000. + vertex.x * 10.);
    vec3 metalColor = mix(vec3(0.3, 0.3, 0.3), vec3(0.9, 0.9, 0.9), smoothstep(-0.5, 0.5, lightPos));
    vertexColor = vec4(metalColor, 1.0) * texelFetch(Sampler2, UV2 / 16, 0);
    finalize();
}

void processStarField(inout vec4 vertex) {
    applyProjection(vertex);
    float star = step(0.95, fract(sin(vertex.x * 1000. + vertex.y * 1000.) * 43758.5453));
    float twinkle = sin(GameTime * 10000. + vertex.x * 100.) * 0.5 + 0.5;
    vertexColor = mix(
        texelFetch(Sampler2, UV2 / 16, 0),
        vec4(1.0, 1.0, 1.0, 1.0),
        star * twinkle
    );
    finalize();
}

void processVintageFilm(inout vec4 vertex) {
    applyProjection(vertex);
    float filmGrain = fract(sin(vertex.x * 100. + vertex.y * 100. + GameTime * 5000.) * 43758.5453) * 0.1;
    float scanLine = sin(vertex.y * 1000. + GameTime * 10000.) * 0.05;
    vec4 texColor = texelFetch(Sampler2, UV2 / 16, 0);
    vertexColor = vec4(
        texColor.r * (0.9 + filmGrain + scanLine),
        texColor.g * (0.8 + filmGrain),
        texColor.b * (0.7 + filmGrain - scanLine),
        texColor.a
    );
    finalize();
}

void processOutlineEffect(inout vec4 vertex) {
    applyProjection(vertex);
    // Создаем эффект контура с помощью позиции вершины
    float outline = smoothstep(0.4, 0.6, abs(sin(vertex.x * 30.0 + GameTime * 3000.)) * abs(cos(vertex.y * 30.0 + GameTime * 2500.)));
    
    vec4 texColor = texelFetch(Sampler2, UV2 / 16, 0);
    vertexColor = mix(
        texColor,
        vec4(0.0, 1.0, 1.0, 1.0), // Цвет контура
        outline
    );
    finalize();
}

void processEdgeGlow(inout vec4 vertex) {
    applyProjection(vertex);
    vec2 uv = vertex.xy * 10.0; // Масштабируем для эффекта
    float edge = max(
        abs(sin(uv.x + GameTime * 3000.)),
        abs(cos(uv.y + GameTime * 2500.))
    );
    
    vec4 texColor = texelFetch(Sampler2, UV2 / 16, 0);
    vertexColor = mix(
        texColor,
        vec4(1.0, 0.5, 0.0, 1.0), // Цвет свечения
        pow(edge, 3.0) // Усиливаем края
    );
    finalize();
}

void processSparkEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    float gId = gl_VertexID - vertexId;
    float t = abs(sin(GameTime * 10000.0 + gId * 0.3)) * 3.6;
    if (vertexId <= 1.) {
        vertex.x -= t - 3.6;
    } else {
        vertex.x += t - 3.6;
    }
    applyHueColor();
    applyProjection(vertex);
    finalize();
}

void processGoldSparkEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    float gId = gl_VertexID - vertexId;
    float t = abs(sin(GameTime * 10000.0 + gId * 0.3)) * 3.6;
    if (vertexId <= 1.) {
        vertex.x -= t - 3.6;
    } else {
        vertex.x += t - 3.6;
    }
    applyGoldColor();
    applyProjection(vertex);
    finalize();
}

// /ex narrate |<element[<&chr[F802]>].font[space:default]><&color[#fd6400]>|<element[<&chr[F802]>].font[space:default]><&color[#fd6600]>|<element[<&chr[F802]>].font[space:default]><&color[#fd6800]>|<element[<&chr[F802]>].font[space:default]><&color[#fd7000]>|<element[<&chr[F802]>].font[space:default]><&color[#fd7200]>|<element[<&chr[F802]>].font[space:default]><&color[#fd7400]>|<element[<&chr[F802]>].font[space:default]><&color[#fd7600]>|<element[<&chr[F802]>].font[space:default]><&color[#fd7800]>

void processGhostEffect(inout vec4 vertex, int z, int offset) {
    if (z != 255) {
        float angle = z / 255.0 * 2.0 * 3.1415;
        float t = mod(GameTime * 1000.0 + offset / 20.0, 1.0);
        t = pow(t, 2.0);
        vertex.x += cos(angle) * 1.2 * t * 0.1;
        vertex.y += sin(angle) * 1.2 * t * 0.1;
    }
    
    applyProjection(vertex);
    if (z == 255) {
        vertexColor = vec4(1.0, 1.0, 1.0, 1.0);
    } else {
        float t = mod(GameTime * 1000.0 + offset / 20.0, 1.0);
        vertexColor = vec4(1.0, 1.0, 1.0, 1.0 - t);
    }
    finalize();
}

void processPixelateEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    if (vertex.z > 0.) {
        if (vertexId == 0.) {
            vertex.x = 0;
            vertex.y = -4000;
            vertexColor.xyz = vec3(0.0, 1.0, 0.134);
        }
        if (vertexId == 1.) {
            vertex.x = 0;
            vertex.y = 1200;
            vertexColor.xyz = vec3(0.0, 0.0, 0.134);
        }
        if (vertexId == 2.) {
            vertex.x = 10000;
            vertex.y = 1200;
            vertexColor.xyz = vec3(1.0, 0.0, 0.134);
        }
        if (vertexId == 3.) {
            vertex.x = 10000;
            vertex.y = -4000;
            vertexColor.xyz = vec3(1.0, 1.0, 0.134);
        }
    }
    else {
        vertex.x = 0;
        vertex.y = 0;
        vertexColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
    
    applyProjection(vertex);
    finalize();
}

void processShaderEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    float g = GameTime * 1000.0;
    float t = g - floor(g);
    applyProjection(vertex);
    if (vertexId == 0.) {
        vertexColor.xyz = vec3(0.0, 1.0, 0.134);
    }
    if (vertexId == 1.) {
        vertexColor.xyz = vec3(0.0, 0.0, 0.134);
    }
    if (vertexId == 2.) {
        vertexColor.xyz = vec3(1.0, 0.0, 0.134);
    }
    if (vertexId == 3.) {
        vertexColor.xyz = vec3(1.0, 1.0, 0.134);
    }
    finalize();
}

void processWaterShaderEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    float g = GameTime * 1000.0;
    float t = g - floor(g);
    applyProjection(vertex);
    if (vertexId == 0.) {
        vertexColor.xyz = vec3(0.0, 1.0, 0.234);
    }
    if (vertexId == 1.) {
        vertexColor.xyz = vec3(0.0, 0.0, 0.234);
    }
    if (vertexId == 2.) {
        vertexColor.xyz = vec3(1.0, 0.0, 0.234);
    }
    if (vertexId == 3.) {
        vertexColor.xyz = vec3(1.0, 1.0, 0.234);
    }
    finalize();
}

void processNoiseShaderEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    if (vertexId == 0.) {
        vertex.x = 0.0;
        vertex.y = 0.0;
        vertexColor.xyz = vec3(0.0, 1.0, 0.174);
    }
    if (vertexId == 1.) {
        vertex.x = 0.0;
        vertex.y *= 2.0;
        vertexColor.xyz = vec3(0.0, 0.0, 0.174);
    }
    if (vertexId == 2.) {
        vertex.x *= 2.0;
        vertex.y *= 2.0;
        vertexColor.xyz = vec3(1.0, 0.0, 0.174);
    }
    if (vertexId == 3.) {
        vertex.x *= 2.0;
        vertex.y = 0.0;
        vertexColor.xyz = vec3(1.0, 1.0, 0.174);
    }
    applyProjection(vertex);
    finalize();
}

void processScreenShaderEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    if (vertexId == 0.) {
        vertex.x = 0.0;
        vertex.y = 0.0;
        vertexColor.xyz = vec3(0.0, 1.0, 0.154);
        applyProjection(vertex);
    }
    if (vertexId == 1.) {
        vertex.x = 0.0;
        vertex.y = ScreenSize.y * 0.25;
        vertexColor.xyz = vec3(0.0, 0.0, 0.154);
        applyProjection(vertex);
    }
    if (vertexId == 2.) {
        vertex.xy = ScreenSize * 0.25;
        vertexColor.xyz = vec3(1.0, 0.0, 0.154);
        applyProjection(vertex);
    }
    if (vertexId == 3.) {
        vertex.x = ScreenSize.x * 0.25;
        vertex.y = 0.0;
        vertexColor.xyz = vec3(1.0, 1.0, 0.154);
        applyProjection(vertex);
    }
    finalize();
}

void processBlackScreenEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    if (vertexId == 0.) {
        vertex.x = 0.0;
        vertex.y = 0.0;
        vertexColor.xyz = vec3(0.0, 0.0, 0.0);
        applyProjection(vertex);
    }
    if (vertexId == 1.) {
        vertex.x = 0.0;
        vertex.y = ScreenSize.y;
        vertexColor.xyz = vec3(0.0, 0.0, 0.0);
        applyProjection(vertex);
    }
    if (vertexId == 2.) {
        vertex.xy = ScreenSize;
        vertexColor.xyz = vec3(0.0, 0.0, 0.0);
        applyProjection(vertex);
    }
    if (vertexId == 3.) {
        vertex.x = ScreenSize.x;
        vertex.y = 0.0;
        vertexColor.xyz = vec3(0.0, 0.0, 0.0);
        applyProjection(vertex);
    }
    finalize();
}

void processHologramShaderEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    if (vertexId == 0.) {
        vertexColor.xyz = vec3(0.0, 1.0, 0.194);
        applyProjection(vertex);
    }
    if (vertexId == 1.) {
        vertexColor.xyz = vec3(0.0, 0.0, 0.194);
        applyProjection(vertex);
    }
    if (vertexId == 2.) {
        vertexColor.xyz = vec3(1.0, 0.0, 0.194);
        applyProjection(vertex);
    }
    if (vertexId == 3.) {
        vertexColor.xyz = vec3(1.0, 1.0, 0.194);
        applyProjection(vertex);
    }
    vec4 projected = ProjMat * ModelViewMat * vertex;
    vec2 col = projected.xy / projected.z + 0.5;
    //col.x = clamp(col.x, 0.0, 1.0);
    //col.y = clamp(col.y, 0.0, 1.0);
    //vertexColor.xyz = vec3(col, 0.194);
    //applyProjection(vertex);
    finalize();
}

void processSpriteChatEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    float g = GameTime * 3000.0;
    if (vertexId == 0. || vertexId == 3.) {
        vertex.y += sin(g) * 2;
    } else {
        vertex.y += sin(g + 150) * 2;
    }
    applyProjection(vertex);
    vertexColor.xyz = vec3(1.0, 1.0, 1.0);
    finalize();
}

void processSpriteEffect(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    float g = GameTime * 3000.0;
    if (vertexId == 0. || vertexId == 3.) {
        vertex.y += sin(g) * 0.2;
    } else {
        vertex.y += sin(g + 150) * 0.2;
    }
    applyProjection(vertex);
    vertexColor.xyz = vec3(1.0, 1.0, 1.0);
    finalize();
}

void processRemoveShadow(inout vec4 vertex) {
    float vertexId = mod(gl_VertexID, 4.);
    if (vertexId == 0.0) {
        vertex.xy = vec2(0.0, 0.0);
    }
    applyProjection(vertex);
    finalize();
}

ivec3 applyTextEffects() {
    vec4 vertex = vec4(Position, 1.);
    ivec3 iColor = ivec3(Color.xyz * 255 + vec3(.5));

    if(fract(Position.z) < .1) {
        if(iColor==ivec3(19, 23, 9)) {
            gl_Position=vec4(2, 2, 2, 1);
            applyColorTexture();
            finalize();
            return iColor;
        }
        if(iColor==ivec3(57, 63, 63)) {
            applyProjection(vertex);
            applyColorTexture();
            finalize();
            return iColor;
        }
        if(iColor==ivec3(57, 63, 62)) {
            processWavyEffect(vertex);
            return iColor;
        }
        if(iColor==ivec3(57, 62, 63)) {
            processWavyEffect(vertex);
            return iColor;
        }
        if(iColor==ivec3(57, 62, 62)) {
            processBouncyEffect(vertex);
            return iColor;
        }
        if(iColor==ivec3(57, 61, 63)) {
            processBouncyEffect(vertex);
            return iColor;
        }
        if(iColor==ivec3(57, 61, 62)) {
            processBlinkingEffect(vertex, .5);
            return iColor;
        }
    }

    if (iColor == ivec3(78, 92, 36)) {
        processNoShadow(vertex);
        return iColor;
    }
    if (iColor == ivec3(230, 255, 254)) {
        processRainbowEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 0)) {
        processMegaWave(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 1)) {
        processXWave(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 2)) {
        processPhysicEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 3)) {
        processCoolEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 4)) {
        processShakeEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 5)) {
        processJellyEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 6)) {
        processSpinEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 7)) {
        processPixelateEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 8)) {
        processSparkEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 9)) {
        processGoldSparkEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 10)) {
        processShaderEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 11)) {
        processSpriteEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 12)) {
        processScreenShaderEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 13)) {
        processNoiseShaderEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 14)) {
        processHologramShaderEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 15)) {
        processBlackScreenEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(255, 119, 16)) {
        processWaterShaderEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(62, 29, 3)) {
        processRemoveShadow(vertex);
        return iColor;
    }
    if (iColor.x == 253 && iColor.y >= 100 && iColor.y <= 120) {
        processGhostEffect(vertex, iColor.z, iColor.y - 100);
        return iColor;
    }
    //////////////////////////////////////////////////////////
    if (iColor == ivec3(230, 255, 250)) {
        processWavyEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(230, 251, 254)) {
        processWavyRainbowEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(230, 251, 250)) {
        processBouncyEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(230, 247, 254)) {
        processBouncyRainbowEffect(vertex);
        return iColor;
    }
    if (iColor == ivec3(230, 247, 250)) {
        processBlinkingEffect(vertex, .5);
        return iColor;
    }

    applyProjection(vertex);
    applyColorTexture();
    finalize();

    return iColor;
}
