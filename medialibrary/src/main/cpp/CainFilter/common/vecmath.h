//
// Created by CainHuang on 2019/3/13.
//

#ifndef VECMATH_H
#define VECMATH_H

#include <math.h>
#include <AndroidLog.h>
#include <cstdint>

class Vector2;
class Vector3;
class Vector4;
class Matrix4;

/******************************************************************
 * 2 elements vector class
 *
 */
class Vector2 {
private:
    float f_[2];

public:
    friend class Vector3;
    friend class Vector4;
    friend class Matrix4;
    friend class Quaternion;

    Vector2() {
        f_[0] = f_[1] = 0.f;
    }

    Vector2( const float fX, const float fY ) {
        f_[0] = fX;
        f_[1] = fY;
    }

    Vector2( const Vector2& vec ) {
        f_[0] = vec.f_[0];
        f_[1] = vec.f_[1];
    }

    Vector2( const float* pVec ) {
        f_[0] = (*pVec++);
        f_[1] = (*pVec++);
    }

    //Operators
    Vector2 operator*( const Vector2& rhs ) const {
        Vector2 ret;
        ret.f_[0] = f_[0] * rhs.f_[0];
        ret.f_[1] = f_[1] * rhs.f_[1];
        return ret;
    }

    Vector2 operator/( const Vector2& rhs ) const {
        Vector2 ret;
        ret.f_[0] = f_[0] / rhs.f_[0];
        ret.f_[1] = f_[1] / rhs.f_[1];
        return ret;
    }

    Vector2 operator+( const Vector2& rhs ) const {
        Vector2 ret;
        ret.f_[0] = f_[0] + rhs.f_[0];
        ret.f_[1] = f_[1] + rhs.f_[1];
        return ret;
    }

    Vector2 operator-( const Vector2& rhs ) const{
        Vector2 ret;
        ret.f_[0] = f_[0] - rhs.f_[0];
        ret.f_[1] = f_[1] - rhs.f_[1];
        return ret;
    }

    Vector2& operator+=( const Vector2& rhs ) {
        f_[0] += rhs.f_[0];
        f_[1] += rhs.f_[1];
        return *this;
    }

    Vector2& operator-=( const Vector2& rhs ) {
        f_[0] -= rhs.f_[0];
        f_[1] -= rhs.f_[1];
        return *this;
    }

    Vector2& operator*=( const Vector2& rhs ) {
        f_[0] *= rhs.f_[0];
        f_[1] *= rhs.f_[1];
        return *this;
    }

    Vector2& operator/=( const Vector2& rhs ) {
        f_[0] /= rhs.f_[0];
        f_[1] /= rhs.f_[1];
        return *this;
    }

    //External operators
    friend Vector2 operator-( const Vector2& rhs ) {
        return Vector2( rhs ) *= -1;
    }

    friend Vector2 operator*( const float lhs, const Vector2& rhs ) {
        Vector2 ret;
        ret.f_[0] = lhs * rhs.f_[0];
        ret.f_[1] = lhs * rhs.f_[1];
        return ret;
    }

    friend Vector2 operator/( const float lhs, const Vector2& rhs ) {
        Vector2 ret;
        ret.f_[0] = lhs / rhs.f_[0];
        ret.f_[1] = lhs / rhs.f_[1];
        return ret;
    }

    //Operators with float
    Vector2 operator*( const float& rhs ) const {
        Vector2 ret;
        ret.f_[0] = f_[0] * rhs;
        ret.f_[1] = f_[1] * rhs;
        return ret;
    }

    Vector2& operator*=( const float& rhs ) {
        f_[0] = f_[0] * rhs;
        f_[1] = f_[1] * rhs;
        return *this;
    }

    Vector2 operator/( const float& rhs ) const {
        Vector2 ret;
        ret.f_[0] = f_[0] / rhs;
        ret.f_[1] = f_[1] / rhs;
        return ret;
    }

    Vector2& operator/=( const float& rhs ) {
        f_[0] = f_[0] / rhs;
        f_[1] = f_[1] / rhs;
        return *this;
    }

    //Compare
    bool operator==( const Vector2& rhs ) const {
        if ( f_[0] != rhs.f_[0] || f_[1] != rhs.f_[1] ) {
            return false;
        }
        return true;
    }

    bool operator!=( const Vector2& rhs ) const {
        if ( f_[0] == rhs.f_[0] ) {
            return false;
        }

        return true;
    }

    float length() const {
        return sqrtf( f_[0] * f_[0] + f_[1] * f_[1] );
    }

    Vector2 normalize() {
        float len = length();
        f_[0] = f_[0] / len;
        f_[1] = f_[1] / len;
        return *this;
    }

    float dot( const Vector2& rhs ) {
        return f_[0] * rhs.f_[0] + f_[1] * rhs.f_[1];
    }

    bool validate() {
        if ( isnan( f_[0] ) || isnan( f_[1] ) ) {
            return false;
        }
        return true;
    }

    void value( float& fX, float& fY ) {
        fX = f_[0];
        fY = f_[1];
    }

    float getX() {
        return f_[0];
    }

    float getY() {
        return f_[1];
    }

    float* ptr() {
        return f_;
    }

    void dump() {
        ALOGI( "Vector2 %f %f", f_[0], f_[1] );
    }
};

/******************************************************************
 * 3 elements vector class
 *
 */
class Vector3 {
private:
    float f_[3];

public:
    friend class Vector4;
    friend class Matrix4;
    friend class Quaternion;

    Vector3() {
        f_[0] = f_[1] = f_[2] = 0.f;
    }

    Vector3( const float fX, const float fY, const float fZ ) {
        f_[0] = fX;
        f_[1] = fY;
        f_[2] = fZ;
    }

    Vector3( const Vector3& vec ) {
        f_[0] = vec.f_[0];
        f_[1] = vec.f_[1];
        f_[2] = vec.f_[2];
    }

    Vector3( const float* pVec ) {
        f_[0] = (*pVec++);
        f_[1] = (*pVec++);
        f_[2] = *pVec;
    }

    Vector3( const Vector2& vec, float f ) {
        f_[0] = vec.f_[0];
        f_[1] = vec.f_[1];
        f_[2] = f;
    }

    Vector3( const Vector4& vec );

    //Operators
    Vector3 operator*( const Vector3& rhs ) const {
        Vector3 ret;
        ret.f_[0] = f_[0] * rhs.f_[0];
        ret.f_[1] = f_[1] * rhs.f_[1];
        ret.f_[2] = f_[2] * rhs.f_[2];
        return ret;
    }

    Vector3 operator/( const Vector3& rhs ) const {
        Vector3 ret;
        ret.f_[0] = f_[0] / rhs.f_[0];
        ret.f_[1] = f_[1] / rhs.f_[1];
        ret.f_[2] = f_[2] / rhs.f_[2];
        return ret;
    }

    Vector3 operator+( const Vector3& rhs ) const {
        Vector3 ret;
        ret.f_[0] = f_[0] + rhs.f_[0];
        ret.f_[1] = f_[1] + rhs.f_[1];
        ret.f_[2] = f_[2] + rhs.f_[2];
        return ret;
    }

    Vector3 operator-( const Vector3& rhs ) const {
        Vector3 ret;
        ret.f_[0] = f_[0] - rhs.f_[0];
        ret.f_[1] = f_[1] - rhs.f_[1];
        ret.f_[2] = f_[2] - rhs.f_[2];
        return ret;
    }

    Vector3& operator+=( const Vector3& rhs ) {
        f_[0] += rhs.f_[0];
        f_[1] += rhs.f_[1];
        f_[2] += rhs.f_[2];
        return *this;
    }

    Vector3& operator-=( const Vector3& rhs ) {
        f_[0] -= rhs.f_[0];
        f_[1] -= rhs.f_[1];
        f_[2] -= rhs.f_[2];
        return *this;
    }

    Vector3& operator*=( const Vector3& rhs ) {
        f_[0] *= rhs.f_[0];
        f_[1] *= rhs.f_[1];
        f_[2] *= rhs.f_[2];
        return *this;
    }

    Vector3& operator/=( const Vector3& rhs ) {
        f_[0] /= rhs.f_[0];
        f_[1] /= rhs.f_[1];
        f_[2] /= rhs.f_[2];
        return *this;
    }

    //External operators
    friend Vector3 operator-( const Vector3& rhs ) {
        return Vector3( rhs ) *= -1;
    }

    friend Vector3 operator*( const float lhs, const Vector3& rhs ) {
        Vector3 ret;
        ret.f_[0] = lhs * rhs.f_[0];
        ret.f_[1] = lhs * rhs.f_[1];
        ret.f_[2] = lhs * rhs.f_[2];
        return ret;
    }

    friend Vector3 operator/( const float lhs, const Vector3& rhs ) {
        Vector3 ret;
        ret.f_[0] = lhs / rhs.f_[0];
        ret.f_[1] = lhs / rhs.f_[1];
        ret.f_[2] = lhs / rhs.f_[2];
        return ret;
    }

    //Operators with float
    Vector3 operator*( const float& rhs ) const {
        Vector3 ret;
        ret.f_[0] = f_[0] * rhs;
        ret.f_[1] = f_[1] * rhs;
        ret.f_[2] = f_[2] * rhs;
        return ret;
    }

    Vector3& operator*=( const float& rhs ) {
        f_[0] = f_[0] * rhs;
        f_[1] = f_[1] * rhs;
        f_[2] = f_[2] * rhs;
        return *this;
    }

    Vector3 operator/( const float& rhs ) const {
        Vector3 ret;
        ret.f_[0] = f_[0] / rhs;
        ret.f_[1] = f_[1] / rhs;
        ret.f_[2] = f_[2] / rhs;
        return ret;
    }

    Vector3& operator/=( const float& rhs ) {
        f_[0] = f_[0] / rhs;
        f_[1] = f_[1] / rhs;
        f_[2] = f_[2] / rhs;
        return *this;
    }

    //Compare
    bool operator==( const Vector3& rhs ) const {
        if ( f_[0] != rhs.f_[0] || f_[1] != rhs.f_[1] || f_[2] != rhs.f_[2] ) {
            return false;
        }
        return true;
    }

    bool operator!=( const Vector3& rhs ) const {
        if ( f_[0] == rhs.f_[0] ) {
            return false;
        }

        return true;
    }

    float length() const {
        return sqrtf( f_[0] * f_[0] + f_[1] * f_[1] + f_[2] * f_[2] );
    }

    Vector3 normalize() {
        float len = length();
        f_[0] = f_[0] / len;
        f_[1] = f_[1] / len;
        f_[2] = f_[2] / len;
        return *this;
    }

    float dot( const Vector3& rhs ) {
        return f_[0] * rhs.f_[0] + f_[1] * rhs.f_[1] + f_[2] * rhs.f_[2];
    }

    Vector3 cross( const Vector3& rhs ) {
        Vector3 ret;
        ret.f_[0] = f_[1] * rhs.f_[2] - f_[2] * rhs.f_[1];
        ret.f_[1] = f_[2] * rhs.f_[0] - f_[0] * rhs.f_[2];
        ret.f_[2] = f_[0] * rhs.f_[1] - f_[1] * rhs.f_[0];
        return ret;
    }

    bool validate() {
        if ( isnan( f_[0] ) || isnan( f_[1] ) || isnan( f_[2] ) ) {
            return false;
        }
        return true;
    }

    void value( float& fX, float& fY, float& fZ ) {
        fX = f_[0];
        fY = f_[1];
        fZ = f_[2];
    }

    float getX() {
        return f_[0];
    }

    float getY() {
        return f_[1];
    }

    float getZ() {
        return f_[2];
    }

    float* ptr() {
        return f_;
    }

    void dump() {
        ALOGI( "Vector3 %f %f %f", f_[0], f_[1], f_[2] );
    }
};

/******************************************************************
 * 4 elements vector class
 *
 */
class Vector4 {
private:
    float f_[4];

public:
    friend class Vector3;
    friend class Matrix4;
    friend class Quaternion;

    Vector4() {
        f_[0] = f_[1] = f_[2] = f_[3] = 0.f;
    }

    Vector4( const float fX, const float fY, const float fZ, const float fW ) {
        f_[0] = fX;
        f_[1] = fY;
        f_[2] = fZ;
        f_[3] = fW;
    }

    Vector4( const Vector4& vec ) {
        f_[0] = vec.f_[0];
        f_[1] = vec.f_[1];
        f_[2] = vec.f_[2];
        f_[3] = vec.f_[3];
    }

    Vector4( const Vector3& vec, const float fW ) {
        f_[0] = vec.f_[0];
        f_[1] = vec.f_[1];
        f_[2] = vec.f_[2];
        f_[3] = fW;
    }

    Vector4( const float* pVec ) {
        f_[0] = (*pVec++);
        f_[1] = (*pVec++);
        f_[2] = *pVec;
        f_[3] = *pVec;
    }

    //Operators
    Vector4 operator*( const Vector4& rhs ) const {
        Vector4 ret;
        ret.f_[0] = f_[0] * rhs.f_[0];
        ret.f_[1] = f_[1] * rhs.f_[1];
        ret.f_[2] = f_[2] * rhs.f_[2];
        ret.f_[3] = f_[2] * rhs.f_[3];
        return ret;
    }

    Vector4 operator/( const Vector4& rhs ) const {
        Vector4 ret;
        ret.f_[0] = f_[0] / rhs.f_[0];
        ret.f_[1] = f_[1] / rhs.f_[1];
        ret.f_[2] = f_[2] / rhs.f_[2];
        ret.f_[3] = f_[2] / rhs.f_[3];
        return ret;
    }

    Vector4 operator+( const Vector4& rhs ) const {
        Vector4 ret;
        ret.f_[0] = f_[0] + rhs.f_[0];
        ret.f_[1] = f_[1] + rhs.f_[1];
        ret.f_[2] = f_[2] + rhs.f_[2];
        ret.f_[3] = f_[2] + rhs.f_[3];
        return ret;
    }

    Vector4 operator-( const Vector4& rhs ) const {
        Vector4 ret;
        ret.f_[0] = f_[0] - rhs.f_[0];
        ret.f_[1] = f_[1] - rhs.f_[1];
        ret.f_[2] = f_[2] - rhs.f_[2];
        ret.f_[3] = f_[2] - rhs.f_[3];
        return ret;
    }

    Vector4& operator+=( const Vector4& rhs ) {
        f_[0] += rhs.f_[0];
        f_[1] += rhs.f_[1];
        f_[2] += rhs.f_[2];
        f_[3] += rhs.f_[3];
        return *this;
    }

    Vector4& operator-=( const Vector4& rhs ) {
        f_[0] -= rhs.f_[0];
        f_[1] -= rhs.f_[1];
        f_[2] -= rhs.f_[2];
        f_[3] -= rhs.f_[3];
        return *this;
    }

    Vector4& operator*=( const Vector4& rhs ) {
        f_[0] *= rhs.f_[0];
        f_[1] *= rhs.f_[1];
        f_[2] *= rhs.f_[2];
        f_[3] *= rhs.f_[3];
        return *this;
    }

    Vector4& operator/=( const Vector4& rhs ) {
        f_[0] /= rhs.f_[0];
        f_[1] /= rhs.f_[1];
        f_[2] /= rhs.f_[2];
        f_[3] /= rhs.f_[3];
        return *this;
    }

    //External operators
    friend Vector4 operator-( const Vector4& rhs ) {
        return Vector4( rhs ) *= -1;
    }

    friend Vector4 operator*( const float lhs, const Vector4& rhs ) {
        Vector4 ret;
        ret.f_[0] = lhs * rhs.f_[0];
        ret.f_[1] = lhs * rhs.f_[1];
        ret.f_[2] = lhs * rhs.f_[2];
        ret.f_[3] = lhs * rhs.f_[3];
        return ret;
    }

    friend Vector4 operator/( const float lhs, const Vector4& rhs ) {
        Vector4 ret;
        ret.f_[0] = lhs / rhs.f_[0];
        ret.f_[1] = lhs / rhs.f_[1];
        ret.f_[2] = lhs / rhs.f_[2];
        ret.f_[3] = lhs / rhs.f_[3];
        return ret;
    }

    //Operators with float
    Vector4 operator*( const float& rhs ) const {
        Vector4 ret;
        ret.f_[0] = f_[0] * rhs;
        ret.f_[1] = f_[1] * rhs;
        ret.f_[2] = f_[2] * rhs;
        ret.f_[3] = f_[3] * rhs;
        return ret;
    }

    Vector4& operator*=( const float& rhs ) {
        f_[0] = f_[0] * rhs;
        f_[1] = f_[1] * rhs;
        f_[2] = f_[2] * rhs;
        f_[3] = f_[3] * rhs;
        return *this;
    }

    Vector4 operator/( const float& rhs ) const {
        Vector4 ret;
        ret.f_[0] = f_[0] / rhs;
        ret.f_[1] = f_[1] / rhs;
        ret.f_[2] = f_[2] / rhs;
        ret.f_[3] = f_[3] / rhs;
        return ret;
    }

    Vector4& operator/=( const float& rhs ) {
        f_[0] = f_[0] / rhs;
        f_[1] = f_[1] / rhs;
        f_[2] = f_[2] / rhs;
        f_[3] = f_[3] / rhs;
        return *this;
    }

    //Compare
    bool operator==( const Vector4& rhs ) const {
        if ( f_[0] != rhs.f_[0] || f_[1] != rhs.f_[1] || f_[2] != rhs.f_[2] || f_[3] != rhs.f_[3] ) {
            return false;
        }
        return true;
    }

    bool operator!=( const Vector4& rhs ) const {
        if( f_[0] == rhs.f_[0] ) {
            return false;
        }

        return true;
    }

    Vector4 operator*( const Matrix4& rhs ) const;

    float length() const {
        return sqrtf( f_[0] * f_[0] + f_[1] * f_[1] + f_[2] * f_[2] + f_[3] * f_[3] );
    }

    Vector4 normalize() {
        float len = length();
        f_[0] = f_[0] / len;
        f_[1] = f_[1] / len;
        f_[2] = f_[2] / len;
        f_[3] = f_[3] / len;
        return *this;
    }

    float dot( const Vector3& rhs ) {
        return f_[0] * rhs.f_[0] + f_[1] * rhs.f_[1] + f_[2] * rhs.f_[2];
    }

    Vector3 cross( const Vector3& rhs ) {
        Vector3 ret;
        ret.f_[0] = f_[1] * rhs.f_[2] - f_[2] * rhs.f_[1];
        ret.f_[1] = f_[2] * rhs.f_[0] - f_[0] * rhs.f_[2];
        ret.f_[2] = f_[0] * rhs.f_[1] - f_[1] * rhs.f_[0];
        return ret;
    }

    bool validate() {
        if ( isnan( f_[0] ) || isnan( f_[1] ) || isnan( f_[2] ) || isnan( f_[3] ) ) {
            return false;
        }
        return true;
    }

    void value( float& fX, float& fY, float& fZ, float& fW ) {
        fX = f_[0];
        fY = f_[1];
        fZ = f_[2];
        fW = f_[3];
    }

    float getX() {
        return f_[0];
    }

    float getY() {
        return f_[1];
    }

    float getZ() {
        return f_[2];
    }

    float getW() {
        return f_[3];
    }

    float* ptr() {
        return f_;
    }
};

/******************************************************************
 * 4x4 matrix
 *
 */
class Matrix4 {
private:
    float f_[16];

public:
    friend class Vector3;
    friend class Vector4;
    friend class Quaternion;

    Matrix4();
    Matrix4( const float* );

    Matrix4 operator*( const Matrix4& rhs ) const;
    Vector4 operator*( const Vector4& rhs ) const;

    Matrix4 operator+( const Matrix4& rhs ) const {
        Matrix4 ret;
        for( int32_t i = 0; i < 16; ++i ) {
            ret.f_[i] = f_[i] + rhs.f_[i];
        }
        return ret;
    }

    Matrix4 operator-( const Matrix4& rhs ) const {
        Matrix4 ret;
        for ( int32_t i = 0; i < 16; ++i ) {
            ret.f_[i] = f_[i] - rhs.f_[i];
        }
        return ret;
    }

    Matrix4& operator+=( const Matrix4& rhs ) {
        for ( int32_t i = 0; i < 16; ++i ) {
            f_[i] += rhs.f_[i];
        }
        return *this;
    }

    Matrix4& operator-=( const Matrix4& rhs ) {
        for ( int32_t i = 0; i < 16; ++i ) {
            f_[i] -= rhs.f_[i];
        }
        return *this;
    }

    Matrix4& operator*=( const Matrix4& rhs ) {
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

        *this = ret;
        return *this;
    }

    Matrix4 operator*( const float rhs ) {
        Matrix4 ret;
        for ( int32_t i = 0; i < 16; ++i ) {
            ret.f_[i] = f_[i] * rhs;
        }
        return ret;
    }

    Matrix4& operator*=( const float rhs ) {
        for ( int32_t i = 0; i < 16; ++i ) {
            f_[i] *= rhs;
        }
        return *this;
    }

    Matrix4& operator=( const Matrix4& rhs ) {
        for ( int32_t i = 0; i < 16; ++i ) {
            f_[i] = rhs.f_[i];
        }
        return *this;
    }

    Matrix4 inverse();

    Matrix4 transpose() {
        Matrix4 ret;
        ret.f_[0] = f_[0];
        ret.f_[1] = f_[4];
        ret.f_[2] = f_[8];
        ret.f_[3] = f_[12];
        ret.f_[4] = f_[1];
        ret.f_[5] = f_[5];
        ret.f_[6] = f_[9];
        ret.f_[7] = f_[13];
        ret.f_[8] = f_[2];
        ret.f_[9] = f_[6];
        ret.f_[10] = f_[10];
        ret.f_[11] = f_[14];
        ret.f_[12] = f_[3];
        ret.f_[13] = f_[7];
        ret.f_[14] = f_[11];
        ret.f_[15] = f_[15];
        *this = ret;
        return *this;
    }

    Matrix4& postTranslate( float tx, float ty, float tz ) {
        f_[12] += (tx * f_[0]) + (ty * f_[4]) + (tz * f_[8]);
        f_[13] += (tx * f_[1]) + (ty * f_[5]) + (tz * f_[9]);
        f_[14] += (tx * f_[2]) + (ty * f_[6]) + (tz * f_[10]);
        f_[15] += (tx * f_[3]) + (ty * f_[7]) + (tz * f_[11]);
        return *this;
    }

    void put(const float *mIn) {
        for ( int32_t i = 0; i < 16; ++i ) {
            f_[i] = mIn[i];
        }
    }

    float* ptr() {
        return f_;
    }

    //--------------------------------------------------------------------------------
    // Misc
    //--------------------------------------------------------------------------------
    static Matrix4 perspective( float width, float height, float nearPlane, float farPlane );

    static Matrix4 lookAt( const Vector3& vEye, const Vector3& vAt, const Vector3& vUp );

    static Matrix4 translation( const float fX, const float fY, const float fZ );
    static Matrix4 translation( const Vector3 vec );

    static Matrix4 rotationX( const float angle );

    static Matrix4 rotationY( const float angle );

    static Matrix4 rotationZ( const float angle );

    static Matrix4 identity() {
        Matrix4 ret;
        ret.f_[0] = 1.f;
        ret.f_[1] = 0;
        ret.f_[2] = 0;
        ret.f_[3] = 0;
        ret.f_[4] = 0;
        ret.f_[5] = 1.f;
        ret.f_[6] = 0;
        ret.f_[7] = 0;
        ret.f_[8] = 0;
        ret.f_[9] = 0;
        ret.f_[10] = 1.f;
        ret.f_[11] = 0;
        ret.f_[12] = 0;
        ret.f_[13] = 0;
        ret.f_[14] = 0;
        ret.f_[15] = 1.f;
        return ret;
    }

    void dump() {
        ALOGI( "%f %f %f %f", f_[0], f_[1], f_[2], f_[3] );
        ALOGI( "%f %f %f %f", f_[4], f_[5], f_[6], f_[7] );
        ALOGI( "%f %f %f %f", f_[8], f_[9], f_[10], f_[11] );
        ALOGI( "%f %f %f %f", f_[12], f_[13], f_[14], f_[15] );
    }
};

/******************************************************************
 * Quaternion class
 *
 */
class Quaternion {
private:
    float f_[4];

public:
    friend class Vector3;
    friend class Vector4;
    friend class Matrix4;

    Quaternion() {
        f_[0] = 0.f;
        f_[1] = 0.f;
        f_[2] = 0.f;
        f_[3] = 1.f;
    }

    Quaternion( const float fX, const float fY, const float fZ, const float fW ) {
        f_[0] = fX;
        f_[1] = fY;
        f_[2] = fZ;
        f_[3] = fW;
    }

    Quaternion( const Vector3 vec, const float fW ) {
        f_[0] = vec.f_[0];
        f_[1] = vec.f_[1];
        f_[2] = vec.f_[2];
        f_[3] = fW;
    }

    Quaternion( const float* p ) {
        f_[0] = *p++;
        f_[1] = *p++;
        f_[2] = *p++;
        f_[3] = *p++;
    }

    Quaternion operator*( const Quaternion rhs ) {
        Quaternion ret;
        ret.f_[0] = f_[0] * rhs.f_[3] + f_[1] * rhs.f_[2] - f_[2] * rhs.f_[1] + f_[3] * rhs.f_[0];
        ret.f_[1] = -f_[0] * rhs.f_[2] + f_[1] * rhs.f_[3] + f_[2] * rhs.f_[0] + f_[3] * rhs.f_[1];
        ret.f_[2] = f_[0] * rhs.f_[1] - f_[1] * rhs.f_[0] + f_[2] * rhs.f_[3] + f_[3] * rhs.f_[2];
        ret.f_[3] = -f_[0] * rhs.f_[0] - f_[1] * rhs.f_[1] - f_[2] * rhs.f_[2] + f_[3] * rhs.f_[3];
        return ret;
    }

    Quaternion& operator*=( const Quaternion rhs ) {
        Quaternion ret;
        ret.f_[0] = f_[0] * rhs.f_[3] + f_[1] * rhs.f_[2] - f_[2] * rhs.f_[1] + f_[3] * rhs.f_[0];
        ret.f_[1] = -f_[0] * rhs.f_[2] + f_[1] * rhs.f_[3] + f_[2] * rhs.f_[0] + f_[3] * rhs.f_[1];
        ret.f_[2] = f_[0] * rhs.f_[1] - f_[1] * rhs.f_[0] + f_[2] * rhs.f_[3] + f_[3] * rhs.f_[2];
        ret.f_[3] = -f_[0] * rhs.f_[0] - f_[1] * rhs.f_[1] - f_[2] * rhs.f_[2] + f_[3] * rhs.f_[3];
        *this = ret;
        return *this;
    }

    Quaternion conjugate() {
        f_[0] = -f_[0];
        f_[1] = -f_[1];
        f_[2] = -f_[2];
        return *this;
    }

    //Non destuctive version
    Quaternion conjugated() {
        Quaternion ret;
        ret.f_[0] = -f_[0];
        ret.f_[1] = -f_[1];
        ret.f_[2] = -f_[2];
        ret.f_[3] = f_[3];
        return ret;
    }

    void toMatrix( Matrix4& mat ) {
        float x2 = f_[0] * f_[0] * 2.0f;
        float y2 = f_[1] * f_[1] * 2.0f;
        float z2 = f_[2] * f_[2] * 2.0f;
        float xy = f_[0] * f_[1] * 2.0f;
        float yz = f_[1] * f_[2] * 2.0f;
        float zx = f_[2] * f_[0] * 2.0f;
        float xw = f_[0] * f_[3] * 2.0f;
        float yw = f_[1] * f_[3] * 2.0f;
        float zw = f_[2] * f_[3] * 2.0f;

        mat.f_[0] = 1.0f - y2 - z2;
        mat.f_[1] = xy + zw;
        mat.f_[2] = zx - yw;
        mat.f_[4] = xy - zw;
        mat.f_[5] = 1.0f - z2 - x2;
        mat.f_[6] = yz + xw;
        mat.f_[8] = zx + yw;
        mat.f_[9] = yz - xw;
        mat.f_[10] = 1.0f - x2 - y2;

        mat.f_[3] = mat.f_[7] = mat.f_[11] = mat.f_[12] = mat.f_[13] = mat.f_[14] = 0.0f;
        mat.f_[15] = 1.0f;
    }

    void toMatrixPreserveTranslate( Matrix4& mat ) {
        float x2 = f_[0] * f_[0] * 2.0f;
        float y2 = f_[1] * f_[1] * 2.0f;
        float z2 = f_[2] * f_[2] * 2.0f;
        float xy = f_[0] * f_[1] * 2.0f;
        float yz = f_[1] * f_[2] * 2.0f;
        float zx = f_[2] * f_[0] * 2.0f;
        float xw = f_[0] * f_[3] * 2.0f;
        float yw = f_[1] * f_[3] * 2.0f;
        float zw = f_[2] * f_[3] * 2.0f;

        mat.f_[0] = 1.0f - y2 - z2;
        mat.f_[1] = xy + zw;
        mat.f_[2] = zx - yw;
        mat.f_[4] = xy - zw;
        mat.f_[5] = 1.0f - z2 - x2;
        mat.f_[6] = yz + xw;
        mat.f_[8] = zx + yw;
        mat.f_[9] = yz - xw;
        mat.f_[10] = 1.0f - x2 - y2;

        mat.f_[3] = mat.f_[7] = mat.f_[11] = 0.0f;
        mat.f_[15] = 1.0f;
    }

    static Quaternion rotationAxis( const Vector3 axis, const float angle ) {
        Quaternion ret;
        float s = sinf( angle / 2 );
        ret.f_[0] = s * axis.f_[0];
        ret.f_[1] = s * axis.f_[1];
        ret.f_[2] = s * axis.f_[2];
        ret.f_[3] = cosf( angle / 2 );
        return ret;
    }

    void value( float& fX, float& fY, float& fZ, float& fW ) {
        fX = f_[0];
        fY = f_[1];
        fZ = f_[2];
        fW = f_[3];
    }
};

#endif //VECMATH_H
