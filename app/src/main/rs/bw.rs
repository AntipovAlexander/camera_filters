#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.antipov.coroutines.idp_renderscript)

#include "rs_debug.rsh"

rs_allocation in;

uchar4 __attribute__((kernel)) root(uchar4 in) {
    //Convert input uchar4 to float4
    float4 f4 = rsUnpackColor8888(in);

    float red = f4.r;
    float green = f4.g;
    float blue = f4.b;

    float gray = (red + green + blue) / 3;

    //Put the values in the output uchar4, note that we keep the alpha value
    return rsPackColorTo8888(gray, gray, gray, f4.a);
}

uchar4 __attribute__((kernel)) identity(int x, int y) {

    uchar4 curPixel;

    curPixel.r = rsGetElementAtYuv_uchar_Y(in, x, y);
    curPixel.g = rsGetElementAtYuv_uchar_U(in, x, y);
    curPixel.b = rsGetElementAtYuv_uchar_V(in, x, y);
    curPixel.a = 255;

    //Put the values in the output uchar4, note that we keep the alpha value
    return rsPackColorTo8888(curPixel.r, curPixel.g, curPixel.b, curPixel.a);
}