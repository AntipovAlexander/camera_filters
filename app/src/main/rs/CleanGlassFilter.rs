#pragma version(1)
#pragma rs java_package_name(com.antipov.coroutines.idp_renderscript)

#include "Clamp.rsh"

// set from the java SDK level
rs_allocation in;

// Magic factors
float _Size = 0.5f;

// Static variables
uint32_t _width;
uint32_t _height;
float _ratio;
uint32_t _centerX;
uint32_t _centerY;
uint32_t _max;
uint32_t _min;

void setup() {
	_width = rsAllocationGetDimX(in);
	_height = rsAllocationGetDimY(in);
	_ratio = (_width >  _height) ?  ((float)_height / _width) : ((float)_width / _height);
 	_centerX = _width >> 1;
	_centerY = _height >> 1;
	_max = _centerX * _centerX + _centerY * _centerY;
	_min = _max * (1 - _Size) * (1 - _Size);
}

uchar4 __attribute__((kernel)) root(int x, int y) {
    uchar Y = rsGetElementAtYuv_uchar_Y(in, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(in, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(in, x, y);

    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    /* force the alpha channel to opaque - the conversion doesn't seem to do this */
    rgba.a = 0xFF;

    float4 f4 = rsUnpackColor8888(rgba);

    // Calculate distance to center and adapt aspect ratio
    int32_t distanceX = _centerX - x;
    int32_t distanceY = _centerY - y;
    if (_width > _height){
        distanceY = distanceY * _ratio * 2;
    } else {
        distanceX = distanceX * _ratio * 2;
    }

    uint32_t distSq = distanceX * distanceX + distanceY * distanceY;

    float3 f3;
    float4 theF4;
    if (distSq > _min) {
        int32_t k = rsRand(1, 123456);

        uint32_t theX = x + k % 19;
        uint32_t theY = y + k % 19;
        if (theX >= _width) {
            theX = _width - 1;
        }
        if (theY >= _height) {
            theY = _height - 1;
        }

        theF4 = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(in, theX, theY));
    } else {
        theF4 = f4;
    }

    f3 = theF4.rgb;

    return rsPackColorTo8888(f3);
}