#pragma version(1)
#pragma rs java_package_name(cn.louispeng.imagefilter.renderscript)

// 高亮对比度特效

#include "Clamp.rsh"

// set from the java SDK level
rs_allocation in;

// The brightness factor.
// Should be in the range [-1.0f, 1.0f].
float gBrightnessFactor  = 0.25f;

// The contrast factor.
// Should be in the range [-1.0f, 1.0f].
float gContrastFactor  = 0.5f;

// magic factor
float ContrastFactor1;



void setup() {
	ContrastFactor1  = (1.0f + gContrastFactor) * (1.0f + gContrastFactor);
}

uchar4 __attribute__((kernel)) root(int x, int y)  {
    uchar Y = rsGetElementAtYuv_uchar_Y(in, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(in, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(in, x, y);

    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    /* force the alpha channel to opaque - the conversion doesn't seem to do this */
    rgba.a = 0xFF;

	float4 f4 = rsUnpackColor8888(rgba);	// extract RGBA values, see rs_core.rsh
	
    float3 f3 = f4.rgb;
    
	// Modify brightness (addition)
	if (gBrightnessFactor != 0.0f) {
	   	// Add brightness
	   	f3 = f3 + gBrightnessFactor;
	   	f3 = FClamp01Float3(f3);
	}
	
	// Modifiy contrast (multiplication)
 	if (ContrastFactor1 != 1.0f){
	    // Transform to range [-0.5f, 0.5f]
	    f3 = f3 - 0.5f;
	
	    // Multiply contrast factor
	    f3 = f3 * ContrastFactor1;
	
	    // Transform back to range [0.0f, 1.0f]
	    f3 = f3 + 0.5f;
	    f3 = FClamp01Float3(f3);
	}
    
    return rsPackColorTo8888(f3);
}