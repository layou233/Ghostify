#version 150

uniform vec4 u_Rect;
uniform vec4 u_Radii; // The radius of each corner, (r1, r2, r3, r4)
uniform float u_edgeSoftness; // Softness of edges, free antialiasing, but breaks rounded corners over certain values.
uniform vec4 u_colorRect; // First color of gradient
uniform vec4 u_colorRect2; // Second color for gradient
uniform vec2 u_gradientDirectionVector; // Direction of the gradient based on which variable to use, for example a gradient from left to right will use (1.0, 0.0) to use the positive x value of the length from the middle of the rectangle
uniform vec4 u_colorShadow; // Color of the shadow.
uniform float u_shadowSoftness; // Softness of shadow. At 0 this will make shadow invisible.

#define u_rectCenter u_Rect.xy // center of rectangle, (x, y)
#define u_rectSize u_Rect.zw // size of rectangle, (width, height)

in vec2 f_Position;

out vec4 fragColor;

///**
//  * Signed Distance Function for a rounded rectangle
//  * @param CenterPosition Vec2 for the center of the rounded rectangle
//  * @param Size Vec2 containing the width and height of a rounded rectangle
//  * @param Radius Vec4 of all the radii in the rounded rectangle
//*/
float roundedBoxSDF(vec2 CenterPosition, vec2 Size, vec4 Radius) {
    Radius.xy = (CenterPosition.x > 0.0) ? Radius.xy : Radius.zw;
    Radius.x  = (CenterPosition.y > 0.0) ? Radius.x  : Radius.y;

    vec2 q = abs(CenterPosition)-Size+Radius.x;
    return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - Radius.x;
}

void main() {
    vec2 uv = (f_Position - u_rectCenter) / u_rectSize;
    vec2 strength = vec2(uv.x * u_gradientDirectionVector.x, uv.y * u_gradientDirectionVector.y);
    // Interpolate colors based on the distance
    vec4 gradientColor = mix(u_colorRect, u_colorRect2, (strength.x == 0.0) ? strength.y + 0.5 : strength.x + 0.5);

    vec4 u_colorBg = vec4(0.0); // The color of background
    vec2 halfSize = (u_rectSize / 2.0); // Rectangle extents (half of the size)

    // Calculate distance to edge.
    float distance = roundedBoxSDF(f_Position.xy - u_rectCenter, halfSize, u_Radii);
    // Smooth the result (free antialiasing).
    float smoothedAlpha = 1.0 - smoothstep(0.0, u_edgeSoftness, distance);

    // Apply a drop shadow effect.
    float shadowAlpha = 1.0 - smoothstep(-u_shadowSoftness, u_shadowSoftness, distance);

    // Blend background with shadow
    vec4 res_shadow_color =
        mix(
            u_colorBg,
            vec4(u_colorShadow.rgb, shadowAlpha),
            shadowAlpha
        );

    fragColor = mix(res_shadow_color, gradientColor, smoothedAlpha);
}
