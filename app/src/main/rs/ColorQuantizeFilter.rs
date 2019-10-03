#pragma version(1)
#pragma rs java_package_name(cn.louispeng.imagefilter.renderscript)

// 颜色量化特效

#include "Clamp.rsh"

// set from the java SDK level
rs_allocation in;

// Magic factors
static const float Levels = 5.0f;
static const float Mult = 0.003921569f;

uchar4 __attribute__((kernel)) root(int x, int y)  {
    uchar Y = rsGetElementAtYuv_uchar_Y(in, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(in, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(in, x, y);

    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    /* force the alpha channel to opaque - the conversion doesn't seem to do this */
    rgba.a = 0xFF;

	float4 f4 = rsUnpackColor8888(rgba);	// extract RGBA values, see rs_core.rsh
	
	float R = (((int) (f4.r * 255.0f * Mult * Levels)) / Levels) * 255.0f;
	float G = (((int) (f4.g * 255.0f * Mult * Levels)) / Levels) * 255.0f;
	float B = (((int) (f4.b * 255.0f * Mult * Levels)) / Levels) * 255.0f;
        
    float3 f3 = {R / 255.0f, G / 255.0f, B / 255.0f};
    return rsPackColorTo8888(f3);
}