#pragma version(1)
#pragma rs java_package_name(cn.louispeng.imagefilter.renderscript)

#include "Clamp.rsh"

// set from the java SDK level
rs_allocation in;

// Magic factors

// Static variables
uint32_t _width;
uint32_t _height;

void setup() {
	_width = rsAllocationGetDimX(in);
	_height = rsAllocationGetDimY(in);
}

uchar4 __attribute__((kernel)) root(int x, int y) {
    uchar Y = rsGetElementAtYuv_uchar_Y(in, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(in, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(in, x, y);

    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    /* force the alpha channel to opaque - the conversion doesn't seem to do this */
    rgba.a = 0xFF;

    float4 f4 = rsUnpackColor8888(rgba);

    float3 f3;
    if (x == _width - 1) {
        f3 = f4.rgb;
    } else {
        float4 theF4 = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(in, x + 1, y));
        f3 = (f4.rgb - theF4.rgb) + 0.5f;
        f3 = FClamp01Float3(f3);
    }

    return rsPackColorTo8888(f3);
}

