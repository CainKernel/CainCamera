//
// Created by admin on 2018/4/4.
//

#include "GLSharpnessImageFilter.h"

static const char vertex_sharpness[] = SHADER_STRING(
        uniform mat4 uMVPMatrix;                                       
        attribute vec4 aPosition;                                      
        attribute vec4 aTextureCoord;                                  
                                                                       
        uniform float imageWidthFactor;                                
        uniform float imageHeightFactor;                               
        uniform float sharpness;                                       
                                                                       
        varying vec2 textureCoordinate;                                
        varying vec2 leftTextureCoordinate;                            
        varying vec2 rightTextureCoordinate;                           
        varying vec2 topTextureCoordinate;                             
        varying vec2 bottomTextureCoordinate;                          
                                                                       
        varying float centerMultiplier;                                
        varying float edgeMultiplier;                                  
        void main()                                                    
        {                                                              
            gl_Position = aPosition;                                   
                                                                       
            mediump vec2 widthStep = vec2(imageWidthFactor, 0.0);      
            mediump vec2 heightStep = vec2(0.0, imageHeightFactor);    
                                                                       
            textureCoordinate = aTextureCoord.xy;                      
            leftTextureCoordinate = aTextureCoord.xy - widthStep;      
            rightTextureCoordinate = aTextureCoord.xy + widthStep;     
            topTextureCoordinate = aTextureCoord.xy + heightStep;      
            bottomTextureCoordinate = aTextureCoord.xy - heightStep;   
                                                                       
            centerMultiplier = 1.0 + 4.0 * sharpness;                  
            edgeMultiplier = sharpness;                                
        }
);

static const char fragment_sharpness[] = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        varying highp vec2 leftTextureCoordinate;
        varying highp vec2 rightTextureCoordinate;
        varying highp vec2 topTextureCoordinate;
        varying highp vec2 bottomTextureCoordinate;

        varying highp float centerMultiplier;
        varying highp float edgeMultiplier;

        uniform sampler2D inputImageTexture;

        void main()
        {
            mediump vec3 textureColor = texture2D(inputImageTexture, textureCoordinate).rgb;
            mediump vec3 leftTextureColor = texture2D(inputImageTexture, leftTextureCoordinate).rgb;
            mediump vec3 rightTextureColor = texture2D(inputImageTexture, rightTextureCoordinate).rgb;
            mediump vec3 topTextureColor = texture2D(inputImageTexture, topTextureCoordinate).rgb;
            mediump vec3 bottomTextureColor = texture2D(inputImageTexture, bottomTextureCoordinate).rgb;

            gl_FragColor = vec4((textureColor * centerMultiplier
                       - (leftTextureColor * edgeMultiplier
                       + rightTextureColor * edgeMultiplier
                       + topTextureColor * edgeMultiplier
                       + bottomTextureColor * edgeMultiplier)),
                       texture2D(inputImageTexture, bottomTextureCoordinate).w);
        }
);

GLSharpnessImageFilter::GLSharpnessImageFilter() {
    GLSharpnessImageFilter(getVertexShader(), getFragmentShader());
}

GLSharpnessImageFilter::GLSharpnessImageFilter(const char *vertexShader, const char *fragmentShader)
        : GLImageFilter(vertexShader, fragmentShader) {
    widthFactor = 0.0;
    heightFactor = 0.0;
}

const char *GLSharpnessImageFilter::getVertexShader(void) {
    return vertex_sharpness;
}
const char *GLSharpnessImageFilter::getFragmentShader(void) {
    return fragment_sharpness;
}

void GLSharpnessImageFilter::initHandle(void) {
    GLImageFilter::initHandle();
    mImageWidthLoc = glGetUniformLocation(programHandle, "imageWidthFactor");
    mImageHeightLoc = glGetUniformLocation(programHandle, "imageHeightFactor");
    mSharpnessLoc = glGetUniformLocation(programHandle, "sharpness");
    setSharpness(0.0);
}

void GLSharpnessImageFilter::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
    GLImageFilter::bindValue(texture, vertices, textureCoords);
    glUniform1f(mImageWidthLoc, widthFactor);
    glUniform1f(mImageHeightLoc, heightFactor);
    glUniform1f(mSharpnessLoc, sharpness);
}

void GLSharpnessImageFilter::onInputSizeChanged(int width, int height) {
    GLImageFilter::onInputSizeChanged(width, height);
    widthFactor = 1.0f / width;
    heightFactor = 1.0f / height;
}


