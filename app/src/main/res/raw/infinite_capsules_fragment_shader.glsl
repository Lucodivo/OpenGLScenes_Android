#version 300 es

// define float precision
precision highp float;

out vec4 FragColor;

#define MAX_STEPS 100
#define MISS_DIST 50.0
#define HIT_DIST 0.01

float distPosToScene(vec3 pos);
float distanceRayToScene(vec3 rayOrigin, vec3 rayDir);
float distToInfiniteCapsules(vec3 rayPos);
float distToCapsule(vec3 rayPos, vec3 posA, vec3 posB, float radius);
float getLight(vec3 surfacePos);
vec3 getNormal(vec3 surfacePos);

uniform vec2 viewPortResolution;
uniform vec3 rayOrigin;
uniform float elapsedTime;
uniform mat4 viewRotationMat;
uniform vec3 lightPos;
uniform vec3 lightColor;

const vec3 missColor = vec3(0.2, 0.2, 0.2);
void main()
{
    vec2 uv = (gl_FragCoord.xy - (0.5 * viewPortResolution.xy))/viewPortResolution.y;

    vec3 rayDir = vec3(uv.x, uv.y, 1.0); // NOTE: Expected to be normalized!
    rayDir = vec3(vec4(rayDir, 0.0) * viewRotationMat);
    rayDir = normalize(rayDir);

    float dist = distanceRayToScene(rayOrigin, rayDir);

    if(dist > 0.0) { // hit
        vec3 surfacePos = rayOrigin + (rayDir * dist);
        float diffuse = getLight(surfacePos);
        vec3 col = vec3(diffuse);

        FragColor = vec4(col * lightColor, 1.0);
    } else { // miss
        FragColor = vec4(missColor, 1.0);
    }
}

const vec2 normalEpsilon = vec2(0.001, 0.0);
vec3 getNormal(vec3 surfacePos) {
    float dist = distPosToScene(surfacePos);
    float xDist = distPosToScene(surfacePos + normalEpsilon.xyy);
    float yDist = distPosToScene(surfacePos + normalEpsilon.yxy);
    float zDist = distPosToScene(surfacePos + normalEpsilon.yyx);
    vec3 normal = (vec3(xDist, yDist, zDist) - dist) / normalEpsilon.x;
    return normalize(normal);
}


float getLight(vec3 surfacePos) {

    vec3 lightDir = normalize(lightPos - surfacePos);
    vec3 normal = getNormal(surfacePos);

    float diff = clamp(dot(normal, lightDir), 0.0, 1.0);

    // calculate for shadows
    //float dist = distanceRayToScene(surfacePos + (normal * HIT_DIST * 2.0), lightDir);
    //if(dist < length(lightPos - surfacePos)) diff *= 0.1;

    if(lightPos == vec3(0.0, 0.0, 0.0)) return diff * 0.3;

    return diff;
}

// NOTE: ray dir is assumed to be normalized
float distanceRayToScene(vec3 rayOrigin, vec3 rayDir) {

    float dist = 0.0;

    for(int i = 0; i < MAX_STEPS; i++) {

        vec3 pos = rayOrigin + (dist * rayDir);
        float posToScene = distPosToScene(pos);
        dist += posToScene;
        if(abs(posToScene) < HIT_DIST) return dist;
        if(posToScene > MISS_DIST) break;
    }

    return -1.0f;
}

float distPosToScene(vec3 rayPos) {
    return distToInfiniteCapsules(rayPos);
}

const vec4 capsuleCenterPosA = vec4(-1.5, 0.0, 0.0, 0.0);
const vec4 capsuleCenterPosB = vec4(1.5, 0.0, 0.0, 0.0);
const vec3 offset = vec3(3.0, 3.0, 3.0);
float distToInfiniteCapsules(vec3 rayPos) {
    vec3 posA = vec3(capsuleCenterPosA * viewRotationMat);
    vec3 posB = vec3(capsuleCenterPosB * viewRotationMat);
    posA = posA + offset;
    posB = posB + offset;
    float infiniteCapsuleDistance = distToCapsule(mod(rayPos, 2.0 * offset), posA, posB, 1.0);
    float lightSphereDistance = distance(rayPos.xyz, lightPos) - 0.5;
    return min(infiniteCapsuleDistance, lightSphereDistance);
}

float distToCapsule(vec3 rayPos, vec3 posA, vec3 posB, float radius) {
    vec3 aToB = posB - posA;
    vec3 aToRayPos = rayPos - posA;
    float abCosTheta = dot(aToB, aToRayPos);
    float magnitudeAToB = length(aToB);
    float projectionAToRayOnAToB = abCosTheta / magnitudeAToB;
    vec3 closestPoint = posA + (clamp(projectionAToRayOnAToB / magnitudeAToB, 0.0, 1.0) * aToB);
    return length(rayPos - closestPoint) - radius;
}