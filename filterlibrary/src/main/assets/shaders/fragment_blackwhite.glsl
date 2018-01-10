precision mediump float;
uniform sampler2D inputTexture;
varying vec2 textureCoordinate;

vec4 calVignette(vec2 coord,vec4 color,float texture_width,float texture_height) {
	float shade = 0.6;
	float slope = 20.0;
	float range = 1.30 - sqrt(0.8) * 0.7;
	vec2 scale;
	if(texture_width > texture_height) {
		scale.x = 1.0;
		scale.y = texture_height / texture_width;
	} else {
		scale.x = texture_width / texture_height;
		scale.y = 1.0;
	}
	float inv_max_dist = 2.0 / length(scale);
	float dist = length((coord - vec2(0.5, 0.5)) * scale);
	float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);
	return vec4(color.rgb * lumen,color.a);
}
vec4 calNewVignette(vec2 coord,vec4 color,float texture_width,float texture_height,float value) {
	float shade = 0.85;
	float slope = 20.0;
	float range = 1.30 - sqrt(value) * 0.7;
	vec2 scale;
	if(texture_width > texture_height) {
		scale.x = 1.0;
		scale.y = texture_height / texture_width;
	} else {
		scale.x = texture_width / texture_height;
		scale.y = 1.0;
	}
	float inv_max_dist = 2.0 / length(scale);
	float dist = length((coord - vec2(0.5, 0.5)) * scale);
	float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);
	return vec4(color.rgb * lumen,color.a);
}
vec4 calVignette2(vec4 color, vec2 coord, float strength) {
	float distance = (coord.x - 0.5) * (coord.x - 0.5) + (coord.y - 0.5) * (coord.y - 0.5);
	float scale = distance / 0.5 * strength;
	color.r =  color.r - scale;
	color.g = color.g - scale;
	color.b = color.b - scale;
	return color;
}
vec4 calBrightnessContract(vec4 color,float brightness, float contrast,float threshold) {
	float cv = contrast <= -255.0 ? -1.0 : contrast / 255.0;
	if (contrast > 0.0 && contrast < 255.0) {
		cv = 1.0 / (1.0 - cv) - 1.0;
	}
	float r  = color.r + brightness / 255.0;
	float g = color.g + brightness / 255.0;
	float b = color.b + brightness / 255.0;
	if (contrast >= 255.0) {
		r = r >= threshold / 255.0 ? 1.0 : 0.0;
		g = g >= threshold / 255.0 ? 1.0 : 0.0;
 		b = b >= threshold / 255.0 ? 1.0 : 0.0;
	} else {
		r =  r + (r - threshold / 255.0) * cv;
		g = g + (g - threshold / 255.0) * cv;
		b = b + (b - threshold / 255.0) * cv;
	}
	color.r = r;
	color.g = g;
	color.b = b;
	return color;
}
void main() {
	vec4 color = texture2D(inputTexture, textureCoordinate);
	float gray = dot(color.rgb, vec3(0.229, 0.587, 0.114));
	float exposure = gray * 1.33;
	color.r = exposure;
	color.g = exposure;
	color.b = exposure;
	color = calVignette2(color, textureCoordinate, 0.5);
	color = calBrightnessContract(color, 0.0, 16.0, 128.0);
	gl_FragColor = color;
}

