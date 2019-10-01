#pragma version(1)
#pragma rs java_package_name(cn.louispeng.imagefilter.renderscript)

// set from the java SDK level
rs_allocation in;

// Magic factors

// Static variables
static uint32_t _width;
static uint32_t _height;
static const int32_t DOT_AREA = 10;
static const int32_t arrDither[DOT_AREA * DOT_AREA] = {        
    167,200,230,216,181,94,72,193,242,232,
    36,52,222,167,200,181,126,210,94,72,
    232,153,111,36,52,167,200,230,216,181,
    94,72,193,242,232,36,52,222,167,200,
    181,126,210,94,72,232,153,111,36,52,
    167,200,230,216,181,94,72,193,242,232,
    36,52,222,167,200,181,126,210,94,72,
    232,153,111,36,52,167,200,230,216,181,
    94,72,193,242,232,36,52,222,167,200,
    181,126,210,94,72,232,153,111,36,52
};

void setup() {
	_width = rsAllocationGetDimX(in);
	_height = rsAllocationGetDimY(in);
}

uchar4 __attribute__((kernel)) root(int x, int y)  {
    uchar Y = rsGetElementAtYuv_uchar_Y(in, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(in, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(in, x, y);

    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    /* force the alpha channel to opaque - the conversion doesn't seem to do this */
    rgba.a = 0xFF;


	float4 f4 = rsUnpackColor8888(rgba);	// extract RGBA values, see rs_core.rsh
	
    float3 f3;
    uint32_t offset_x = x % DOT_AREA;
    uint32_t offset_y = y % DOT_AREA;
    uint32_t index = offset_y * DOT_AREA + offset_x; 

    int32_t l_grayIntensity = (1.0f - f4.b) * 255;
    if (l_grayIntensity > arrDither[index]) {
        f3.r = 0;
        f3.g = 0;
        f3.b = 0;
    } else {
        f3.r = 1;
        f3.g = 1;
        f3.b = 1;
    }
            
    return rsPackColorTo8888(f3);
}