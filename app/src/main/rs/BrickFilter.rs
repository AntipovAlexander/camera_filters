#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.antipov.coroutines.idp_renderscript)


// set from the java SDK level
rs_allocation in;

// magic factor
const static float ThreshHold = 0.5f;

uchar4 __attribute__((kernel)) root(int x, int y) {
    uchar Y = rsGetElementAtYuv_uchar_Y(in, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(in, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(in, x, y);
    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    /* force the alpha channel to opaque - the conversion doesn't seem to do this */
    rgba.a = 0xFF;

	float mean = (rgba.r + rgba.g + rgba.b) / 3;
	mean = (mean >= ThreshHold ? 1.0f : 0.0f);

    float3 f3 = {mean, mean, mean};

    return rsPackColorTo8888(f3);
}