#version 330

in vec2 outTexCoordinate;

out vec4 fragColor;

uniform sampler2D diffuseTexture;

void main()
{
    fragColor = texture(diffuseTexture, outTexCoordinate);
}
