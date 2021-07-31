#version 330

in vec2 outTexCoordinate;
in vec4 outDiffuseColour;
in vec3 outNormal;

out vec4 fragColor;

uniform sampler2D diffuseTexture;
uniform vec3 ambientLight;
uniform vec3 lightDirection;
uniform vec3 lightColour;
uniform float lightIntensity;

// NOTE: Lights are in eye space
vec4 diffuseDirectionalLight(vec4 materialDiffuse, vec3 lightDirection, vec3 lightColour, float lightIntensity, vec3 vertexNormal)
{
    float diffuseFactor = max(dot(vertexNormal, lightDirection), 0.0);
    return materialDiffuse * vec4(lightColour, 1.0) * lightIntensity * diffuseFactor;
}

void main()
{
    vec4 textureColour = texture(diffuseTexture, outTexCoordinate) * outDiffuseColour;

    vec4 ambient = textureColour * vec4(ambientLight, 1.0);

    vec4 diffuse = diffuseDirectionalLight(textureColour, lightDirection, lightColour, lightIntensity, outNormal);

    fragColor = ambient + diffuse;
}
