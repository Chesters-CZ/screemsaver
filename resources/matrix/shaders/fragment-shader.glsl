#version 330 core

out vec4 FragColor;

in vec3 outColor;
in vec2 outTexture;

uniform sampler2D sampler;

void main()
{
FragColor = texture(sampler, outTexture);
    // FragColor = vec4(outColor, 1.0f);
}
