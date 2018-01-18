#include <SkBitmap.h>
#include <gui/SurfaceComposerClient.h>

using namespace android;

//old type skia ( < 5.0 )
#ifdef NEWSKIA
SkColorType flinger2skia(PixelFormat f);
#else
SkBitmap::Config flinger2skia(PixelFormat f);
#endif

//New type skia
