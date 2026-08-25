vec4 REMOVE_BLUE(vec4 FROM_REMOVE) {
    vec4 result = FROM_REMOVE;
    result.b = (result.b * 255.0 - mod(result.b * 255.0, 2.0)) / 255.0;
    return result;
}