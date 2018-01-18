#include "flinger_function.h"

SkBitmap::Config flinger2skia(android::PixelFormat f)
{
    switch (f) {
        case PIXEL_FORMAT_A_8:
            return SkBitmap::kA8_Config;
        case PIXEL_FORMAT_RGB_565:
            return SkBitmap::kRGB_565_Config;
        case PIXEL_FORMAT_RGBA_4444:
            return SkBitmap::kARGB_4444_Config;
        default:
            return SkBitmap::kARGB_8888_Config;
    }
}
