#version 330

in vec2 outTexCoordinate;
in vec4 outDiffuseColour;

out vec4 fragColor;

uniform sampler2D diffuseTexture;

void main()
{
    fragColor = texture(diffuseTexture, outTexCoordinate) * outDiffuseColour;
}
