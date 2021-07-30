#version 330

in vec2 outTexCoordinate;
in vec4 outDiffuseColour;

out vec4 fragColor;

uniform sampler2D diffuseTexture;
uniform float windowHeight;
uniform float fadeRange;

void main() {
    float yTopMax = windowHeight - fadeRange;
    float yTopMin = windowHeight - (2 * fadeRange);
    float yBottomMax = 2 * fadeRange;
    float yBottomMin = fadeRange;

    if ((gl_FragCoord.y < yBottomMin) || (gl_FragCoord.y > yTopMax)) {
        fragColor = texture(diffuseTexture, outTexCoordinate) * vec4(outDiffuseColour.xyz, 0);
    }
    else if (gl_FragCoord.y > yTopMin) {
        float range = ((gl_FragCoord.y - yTopMin) * 100) / fadeRange;
        fragColor = texture(diffuseTexture, outTexCoordinate) * vec4(outDiffuseColour.xyz, 1.0 - (range / 100.0));
    }
    else if (gl_FragCoord.y < yBottomMax) {
        float range = ((gl_FragCoord.y - yBottomMin) * 100) / fadeRange;
        fragColor = texture(diffuseTexture, outTexCoordinate) * vec4(outDiffuseColour.xyz, range / 100.0);
    }
    else {
        fragColor = texture(diffuseTexture, outTexCoordinate) * outDiffuseColour;
    }
}
