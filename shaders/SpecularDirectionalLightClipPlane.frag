#version 330

in vec2 outTexCoordinate;
in vec4 outDiffuseColour;
in vec3 outWorldPosition;
in vec3 outModelPosition;
in vec3 outNormal;

out vec4 fragColor;

uniform sampler2D diffuseTexture;
uniform sampler2D noiseTexture;

uniform vec3 ambientLight;
uniform vec3 lightDirection;
uniform vec3 lightColour;
uniform float lightIntensity;
uniform float shininess;
uniform vec3 planeNormal;
uniform vec3 pointOnPlane;

// NOTE: Lights are in eye space
vec4 diffuseDirectionalLight(vec4 materialDiffuse, vec3 lightDirection, vec3 lightColour, float lightIntensity, vec3 vertexNormal)
{
    float diffuseFactor = max(dot(vertexNormal, lightDirection), 0.0);
    return materialDiffuse * vec4(lightColour, 1.0) * lightIntensity * diffuseFactor;
}

// NOTE: Lights are in eye space
vec4 specularDirectionalLight(vec4 materialSpecular, vec3 lightDirection, vec3 lightColour, float lightIntensity, vec3 vertexPosition, vec3 vertexNormal)
{
    vec3 cameraDirection = normalize(-vertexPosition);
    vec3 reflectedLight = normalize(reflect(-lightDirection, vertexNormal));
    float shinyLightFactor = max(dot(cameraDirection, reflectedLight), 0.0);
    float specularFactor = pow(shinyLightFactor, shininess);
    return materialSpecular * lightIntensity * specularFactor * vec4(lightColour, 1.0);
}

// ax + by + cz + d = 0
float planeEquation(vec3 vertexPosition) {
    float d = -((pointOnPlane.x * planeNormal.x) + (pointOnPlane.y * planeNormal.y) + (pointOnPlane.z * planeNormal.z));
    return (vertexPosition.x * planeNormal.x) + (vertexPosition.y * planeNormal.y) + (vertexPosition.z * planeNormal.z) + d;
}

vec3 sampleNoiseTexture() {
    return texture(noiseTexture,
                vec2(outModelPosition.x,// * textureSize(noiseTexture, 0),
                     outModelPosition.z /** textureSize(noiseTexture, 0)*/)
            ).rgb;
}

void discardIfOnOrUnderPlanePlusNoise() {
    float arcTangent = atan(outModelPosition.z, outModelPosition.x);
    float scaledSinWave = sin(arcTangent * 5.0f) / 20.0f;
    vec3 noise = sampleNoiseTexture();
    float averageNoiseScaled = ((noise.r + noise.g + noise.b) / 3.0f) * 0.1f;
    float yFinal = outModelPosition.y + scaledSinWave + averageNoiseScaled;
    if (planeEquation(vec3(outModelPosition.x, yFinal, outModelPosition.z)) <= 0.0) {
        // then the fragment is on or behind the plane
        discard;
    }
}

void main()
{
    discardIfOnOrUnderPlanePlusNoise();

    vec4 textureColour = texture(diffuseTexture, outTexCoordinate) * outDiffuseColour;

    vec4 ambient = textureColour * vec4(ambientLight, 1.0);

    vec4 diffuse = diffuseDirectionalLight(textureColour, lightDirection, lightColour, lightIntensity, outNormal);

    vec4 specular = specularDirectionalLight(textureColour, lightDirection, lightColour, lightIntensity, outWorldPosition, outNormal);

    fragColor = ambient + diffuse + specular;
}
