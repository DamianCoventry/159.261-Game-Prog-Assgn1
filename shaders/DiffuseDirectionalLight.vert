#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoordinate;
layout (location = 2) in vec3 normal;

out vec2 outTexCoordinate;
out vec4 outDiffuseColour;
out vec3 outNormal;

uniform vec4 diffuseColour;
uniform mat4 mvMatrix;
uniform mat4 projectionMatrix;

void main()
{
    vec4 mvPosition = mvMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPosition;
    outNormal = normalize(mvMatrix * vec4(normal, 0.0)).xyz;
    outTexCoordinate = texCoordinate;
    outDiffuseColour = diffuseColour;
}
