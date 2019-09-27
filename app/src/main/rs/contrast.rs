#pragma version(1)
#pragma rs java_package_name(com.antipov.coroutines.idp_renderscript)

rs_allocation in;
uint32_t gW;
uint32_t gH;

uchar4 __attribute__((kernel)) root(int x, int y) {
    uchar Y = rsGetElementAtYuv_uchar_Y(in, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(in, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(in, x, y);

    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    /* force the alpha channel to opaque - the conversion doesn't seem to do this */
    rgba.a = 0xFF;

    float4 f4 = rsUnpackColor8888(rgba);

    float red = f4.r;
    float green = f4.g;
    float blue = f4.b;

    float gray = (red + green + blue) / 3;

    //Put the values in the output uchar4, note that we keep the alpha value
    return rsPackColorTo8888(gray, gray, gray, f4.a);
}