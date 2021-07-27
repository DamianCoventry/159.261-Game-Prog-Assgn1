#version 330

in vec2 outTexCoordinate;
in vec4 outDiffuseColour;
in vec3 outPosition;
in vec3 outNormal;

out vec4 fragColor;

uniform sampler2D diffuseTexture;
uniform vec3 ambientLight;
uniform vec3 lightDirection;
uniform vec3 lightColour;
uniform float lightIntensity;
uniform float shininess;

vec4 diffuseDirectionalLight(vec4 materialDiffuse, vec3 lightDirection, vec3 lightColour, float lightIntensity, vec3 vertexNormal)
{
    float diffuseFactor = max(dot(vertexNormal, lightDirection), 0.0);
    return materialDiffuse * vec4(lightColour, 1.0) * lightIntensity * diffuseFactor;
}

vec4 specularDirectionalLight(vec4 materialSpecular, vec3 lightDirection, vec3 lightColour, float lightIntensity, vec3 vertexPosition, vec3 vertexNormal)
{
    vec3 cameraDirection = normalize(-vertexPosition);
    vec3 reflectedLight = normalize(reflect(-lightDirection, vertexNormal));
    float shinyLightFactor = max(dot(cameraDirection, reflectedLight), 0.0);
    float specularFactor = pow(shinyLightFactor, shininess);
    return materialSpecular * lightIntensity * specularFactor * vec4(lightColour, 1.0);
}

void main()
{
    vec4 textureColour = texture(diffuseTexture, outTexCoordinate) * outDiffuseColour;

    vec4 ambient = textureColour * vec4(ambientLight, 1.0);

    vec4 diffuse = diffuseDirectionalLight(textureColour, lightDirection, lightColour, lightIntensity, outNormal);

    vec4 specular = specularDirectionalLight(textureColour, lightDirection, lightColour, lightIntensity, outPosition, outNormal);

    fragColor = ambient + diffuse + specular;
}
