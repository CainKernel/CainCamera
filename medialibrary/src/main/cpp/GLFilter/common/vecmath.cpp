//
// Created by CainHuang on 2019/3/13.
//

#include "vecmath.h"

//--------------------------------------------------------------------------------
// vec3
//--------------------------------------------------------------------------------
Vector3::Vector3( const Vector4& vec ) {
    f_[0] = vec.f_[0];
    f_[1] = vec.f_[1];
    f_[2] = vec.f_[2];
}

//--------------------------------------------------------------------------------
// vec4
//--------------------------------------------------------------------------------
Vector4 Vector4::operator*( const Matrix4& rhs ) const {
    Vector4 out;
    out.f_[0] = f_[0] * rhs.f_[0] + f_[1] * rhs.f_[1] + f_[2] * rhs.f_[2] + f_[3] * rhs.f_[3];
    out.f_[1] = f_[0] * rhs.f_[4] + f_[1] * rhs.f_[5] + f_[2] * rhs.f_[6] + f_[3] * rhs.f_[7];
    out.f_[2] = f_[0] * rhs.f_[8] + f_[1] * rhs.f_[9] + f_[2] * rhs.f_[10] + f_[3] * rhs.f_[11];
    out.f_[3] = f_[0] * rhs.f_[12] + f_[1] * rhs.f_[13] + f_[2] * rhs.f_[14] + f_[3] * rhs.f_[15];
    return out;
}

//--------------------------------------------------------------------------------
// Matrix4
//--------------------------------------------------------------------------------
Matrix4::Matrix4() {
    for ( int32_t i = 0; i < 16; ++i ) {
        f_[i] = 0.f;
    }
}

Matrix4::Matrix4( const float* mIn ) {
    for ( int32_t i = 0; i < 16; ++i ) {
        f_[i] = mIn[i];
    }
}

Matrix4 Matrix4::operator*( const Matrix4& rhs ) const {
    Matrix4 ret;
    ret.f_[0] = f_[0] * rhs.f_[0] + f_[4] * rhs.f_[1] + f_[8] * rhs.f_[2]
                + f_[12] * rhs.f_[3];
    ret.f_[1] = f_[1] * rhs.f_[0] + f_[5] * rhs.f_[1] + f_[9] * rhs.f_[2]
                + f_[13] * rhs.f_[3];
    ret.f_[2] = f_[2] * rhs.f_[0] + f_[6] * rhs.f_[1] + f_[10] * rhs.f_[2]
                + f_[14] * rhs.f_[3];
    ret.f_[3] = f_[3] * rhs.f_[0] + f_[7] * rhs.f_[1] + f_[11] * rhs.f_[2]
                + f_[15] * rhs.f_[3];

    ret.f_[4] = f_[0] * rhs.f_[4] + f_[4] * rhs.f_[5] + f_[8] * rhs.f_[6]
                + f_[12] * rhs.f_[7];
    ret.f_[5] = f_[1] * rhs.f_[4] + f_[5] * rhs.f_[5] + f_[9] * rhs.f_[6]
                + f_[13] * rhs.f_[7];
    ret.f_[6] = f_[2] * rhs.f_[4] + f_[6] * rhs.f_[5] + f_[10] * rhs.f_[6]
                + f_[14] * rhs.f_[7];
    ret.f_[7] = f_[3] * rhs.f_[4] + f_[7] * rhs.f_[5] + f_[11] * rhs.f_[6]
                + f_[15] * rhs.f_[7];

    ret.f_[8] = f_[0] * rhs.f_[8] + f_[4] * rhs.f_[9] + f_[8] * rhs.f_[10]
                + f_[12] * rhs.f_[11];
    ret.f_[9] = f_[1] * rhs.f_[8] + f_[5] * rhs.f_[9] + f_[9] * rhs.f_[10]
                + f_[13] * rhs.f_[11];
    ret.f_[10] = f_[2] * rhs.f_[8] + f_[6] * rhs.f_[9] + f_[10] * rhs.f_[10]
                 + f_[14] * rhs.f_[11];
    ret.f_[11] = f_[3] * rhs.f_[8] + f_[7] * rhs.f_[9] + f_[11] * rhs.f_[10]
                 + f_[15] * rhs.f_[11];

    ret.f_[12] = f_[0] * rhs.f_[12] + f_[4] * rhs.f_[13] + f_[8] * rhs.f_[14]
                 + f_[12] * rhs.f_[15];
    ret.f_[13] = f_[1] * rhs.f_[12] + f_[5] * rhs.f_[13] + f_[9] * rhs.f_[14]
                 + f_[13] * rhs.f_[15];
    ret.f_[14] = f_[2] * rhs.f_[12] + f_[6] * rhs.f_[13] + f_[10] * rhs.f_[14]
                 + f_[14] * rhs.f_[15];
    ret.f_[15] = f_[3] * rhs.f_[12] + f_[7] * rhs.f_[13] + f_[11] * rhs.f_[14]
                 + f_[15] * rhs.f_[15];

    return ret;
}

Vector4 Matrix4::operator*( const Vector4& rhs ) const {
    Vector4 ret;
    ret.f_[0] = rhs.f_[0] * f_[0] + rhs.f_[1] * f_[4] + rhs.f_[2] * f_[8] + rhs.f_[3] * f_[12];
    ret.f_[1] = rhs.f_[0] * f_[1] + rhs.f_[1] * f_[5] + rhs.f_[2] * f_[9] + rhs.f_[3] * f_[13];
    ret.f_[2] = rhs.f_[0] * f_[2] + rhs.f_[1] * f_[6] + rhs.f_[2] * f_[10] + rhs.f_[3] * f_[14];
    ret.f_[3] = rhs.f_[0] * f_[3] + rhs.f_[1] * f_[7] + rhs.f_[2] * f_[11] + rhs.f_[3] * f_[15];
    return ret;
}

Matrix4 Matrix4::inverse() {
    Matrix4 ret;
    float det_1;
    float pos = 0;
    float neg = 0;
    float temp;

    temp = f_[0] * f_[5] * f_[10];
    if ( temp >= 0 ) {
        pos += temp;
    } else {
        neg += temp;
    }
    temp = f_[4] * f_[9] * f_[2];
    if ( temp >= 0 ) {
        pos += temp;
    } else {
        neg += temp;
    }
    temp = f_[8] * f_[1] * f_[6];
    if ( temp >= 0 ) {
        pos += temp;
    } else {
        neg += temp;
    }
    temp = -f_[8] * f_[5] * f_[2];
    if ( temp >= 0 ) {
        pos += temp;
    } else {
        neg += temp;
    }
    temp = -f_[4] * f_[1] * f_[10];
    if ( temp >= 0 ) {
        pos += temp;
    } else {
        neg += temp;
    }
    temp = -f_[0] * f_[9] * f_[6];
    if ( temp >= 0 ) {
        pos += temp;
    } else {
        neg += temp;
    }
    det_1 = pos + neg;

    if ( det_1 == 0.0 ) {
        //Error
    } else {
        det_1 = 1.0f / det_1;
        ret.f_[0] = (f_[5] * f_[10] - f_[9] * f_[6]) * det_1;
        ret.f_[1] = -(f_[1] * f_[10] - f_[9] * f_[2]) * det_1;
        ret.f_[2] = (f_[1] * f_[6] - f_[5] * f_[2]) * det_1;
        ret.f_[4] = -(f_[4] * f_[10] - f_[8] * f_[6]) * det_1;
        ret.f_[5] = (f_[0] * f_[10] - f_[8] * f_[2]) * det_1;
        ret.f_[6] = -(f_[0] * f_[6] - f_[4] * f_[2]) * det_1;
        ret.f_[8] = (f_[4] * f_[9] - f_[8] * f_[5]) * det_1;
        ret.f_[9] = -(f_[0] * f_[9] - f_[8] * f_[1]) * det_1;
        ret.f_[10] = (f_[0] * f_[5] - f_[4] * f_[1]) * det_1;

        /* Calculate -C * inverse(A) */
        ret.f_[12] = -(f_[12] * ret.f_[0] + f_[13] * ret.f_[4] + f_[14] * ret.f_[8]);
        ret.f_[13] = -(f_[12] * ret.f_[1] + f_[13] * ret.f_[5] + f_[14] * ret.f_[9]);
        ret.f_[14] = -(f_[12] * ret.f_[2] + f_[13] * ret.f_[6] + f_[14] * ret.f_[10]);

        ret.f_[3] = 0.0f;
        ret.f_[7] = 0.0f;
        ret.f_[11] = 0.0f;
        ret.f_[15] = 1.0f;
    }

    *this = ret;
    return *this;
}

//--------------------------------------------------------------------------------
// Misc
//--------------------------------------------------------------------------------
Matrix4 Matrix4::rotationX( const float fAngle ) {
    Matrix4 ret;
    float fCosine, fSine;

    fCosine = cosf( fAngle );
    fSine = sinf( fAngle );

    ret.f_[0] = 1.0f;
    ret.f_[4] = 0.0f;
    ret.f_[8] = 0.0f;
    ret.f_[12] = 0.0f;
    ret.f_[1] = 0.0f;
    ret.f_[5] = fCosine;
    ret.f_[9] = fSine;
    ret.f_[13] = 0.0f;
    ret.f_[2] = 0.0f;
    ret.f_[6] = -fSine;
    ret.f_[10] = fCosine;
    ret.f_[14] = 0.0f;
    ret.f_[3] = 0.0f;
    ret.f_[7] = 0.0f;
    ret.f_[11] = 0.0f;
    ret.f_[15] = 1.0f;
    return ret;
}

Matrix4 Matrix4::rotationY( const float fAngle ) {
    Matrix4 ret;
    float fCosine, fSine;

    fCosine = cosf( fAngle );
    fSine = sinf( fAngle );

    ret.f_[0] = fCosine;
    ret.f_[4] = 0.0f;
    ret.f_[8] = -fSine;
    ret.f_[12] = 0.0f;
    ret.f_[1] = 0.0f;
    ret.f_[5] = 1.0f;
    ret.f_[9] = 0.0f;
    ret.f_[13] = 0.0f;
    ret.f_[2] = fSine;
    ret.f_[6] = 0.0f;
    ret.f_[10] = fCosine;
    ret.f_[14] = 0.0f;
    ret.f_[3] = 0.0f;
    ret.f_[7] = 0.0f;
    ret.f_[11] = 0.0f;
    ret.f_[15] = 1.0f;
    return ret;

}

Matrix4 Matrix4::rotationZ( const float fAngle ) {
    Matrix4 ret;
    float fCosine, fSine;

    fCosine = cosf( fAngle );
    fSine = sinf( fAngle );

    ret.f_[0] = fCosine;
    ret.f_[4] = fSine;
    ret.f_[8] = 0.0f;
    ret.f_[12] = 0.0f;
    ret.f_[1] = -fSine;
    ret.f_[5] = fCosine;
    ret.f_[9] = 0.0f;
    ret.f_[13] = 0.0f;
    ret.f_[2] = 0.0f;
    ret.f_[6] = 0.0f;
    ret.f_[10] = 1.0f;
    ret.f_[14] = 0.0f;
    ret.f_[3] = 0.0f;
    ret.f_[7] = 0.0f;
    ret.f_[11] = 0.0f;
    ret.f_[15] = 1.0f;
    return ret;
}

Matrix4 Matrix4::translation( const float fX, const float fY, const float fZ ) {
    Matrix4 ret;
    ret.f_[0] = 1.0f;
    ret.f_[4] = 0.0f;
    ret.f_[8] = 0.0f;
    ret.f_[12] = fX;
    ret.f_[1] = 0.0f;
    ret.f_[5] = 1.0f;
    ret.f_[9] = 0.0f;
    ret.f_[13] = fY;
    ret.f_[2] = 0.0f;
    ret.f_[6] = 0.0f;
    ret.f_[10] = 1.0f;
    ret.f_[14] = fZ;
    ret.f_[3] = 0.0f;
    ret.f_[7] = 0.0f;
    ret.f_[11] = 0.0f;
    ret.f_[15] = 1.0f;
    return ret;
}

Matrix4 Matrix4::translation( const Vector3 vec ) {
    Matrix4 ret;
    ret.f_[0] = 1.0f;
    ret.f_[4] = 0.0f;
    ret.f_[8] = 0.0f;
    ret.f_[12] = vec.f_[0];
    ret.f_[1] = 0.0f;
    ret.f_[5] = 1.0f;
    ret.f_[9] = 0.0f;
    ret.f_[13] = vec.f_[1];
    ret.f_[2] = 0.0f;
    ret.f_[6] = 0.0f;
    ret.f_[10] = 1.0f;
    ret.f_[14] = vec.f_[2];
    ret.f_[3] = 0.0f;
    ret.f_[7] = 0.0f;
    ret.f_[11] = 0.0f;
    ret.f_[15] = 1.0f;
    return ret;
}

Matrix4 Matrix4::perspective( float width, float height, float nearPlane, float farPlane ) {
    float n2 = 2.0f * nearPlane;
    float rcpnmf = 1.f / (nearPlane - farPlane);

    Matrix4 result;
    result.f_[0] = n2 / width;
    result.f_[4] = 0;
    result.f_[8] = 0;
    result.f_[12] = 0;
    result.f_[1] = 0;
    result.f_[5] = n2 / height;
    result.f_[9] = 0;
    result.f_[13] = 0;
    result.f_[2] = 0;
    result.f_[6] = 0;
    result.f_[10] = (farPlane + nearPlane) * rcpnmf;
    result.f_[14] = farPlane * rcpnmf * n2;
    result.f_[3] = 0;
    result.f_[7] = 0;
    result.f_[11] = -1.0f;
    result.f_[15] = 0;

    return result;
}

Matrix4 Matrix4::lookAt( const Vector3& vec_eye, const Vector3& vec_at, const Vector3& vec_up ) {
    Vector3 vec_forward, vec_up_norm, vec_side;
    Matrix4 result;

    vec_forward.f_[0] = vec_eye.f_[0] - vec_at.f_[0];
    vec_forward.f_[1] = vec_eye.f_[1] - vec_at.f_[1];
    vec_forward.f_[2] = vec_eye.f_[2] - vec_at.f_[2];

    vec_forward.normalize();
    vec_up_norm = vec_up;
    vec_up_norm.normalize();
    vec_side = vec_up_norm.cross( vec_forward );
    vec_up_norm = vec_forward.cross( vec_side );

    result.f_[0] = vec_side.f_[0];
    result.f_[4] = vec_side.f_[1];
    result.f_[8] = vec_side.f_[2];
    result.f_[12] = 0;
    result.f_[1] = vec_up_norm.f_[0];
    result.f_[5] = vec_up_norm.f_[1];
    result.f_[9] = vec_up_norm.f_[2];
    result.f_[13] = 0;
    result.f_[2] = vec_forward.f_[0];
    result.f_[6] = vec_forward.f_[1];
    result.f_[10] = vec_forward.f_[2];
    result.f_[14] = 0;
    result.f_[3] = 0;
    result.f_[7] = 0;
    result.f_[11] = 0;
    result.f_[15] = 1.0;

    result.postTranslate( -vec_eye.f_[0], -vec_eye.f_[1], -vec_eye.f_[2] );
    return result;
}