#ifndef TOUCH_INC
#define TOUCH_INC

//this variables come from protobuf
#define TOUCH_UP 0
#define TOUCH_DOWN 1
#define TOUCH_MOVE 2
#define LANDSCAPE true
#define PORTRAIT false

bool init_touch();
void deinit_touch();
void touches_inject(wipc::Message*);

static const char DEVICE_CONFIG[] =
	"touch.deviceType = touchScreen\n"
    "touch.orientationAware = 1\n"
    "keyboard.layout = Webkey\n"
    "keyboard.characterMap = Webkey\n"
    "keyboard.orientationAware = 1\n"
    "keyboard.builtIn = 0\n"
    "cursor.mode = navigation\n"
    "cursor.orientationAware = 0\n";

/* Ez sosincs hasznalva. Egy workaround miatt szukseges.
 * Enelkul a Webkey touch-ot fizikai devicenak detektaljak
 * es defaulton el lesz hideolva a soft keyboard
 */
static const char KL_CONFIG[] =
    "key 1   SOFT_LEFT   WAKE\n"
    "key 2   SOFT_RIGHT   WAKE\n"
    "key 3   HOME   WAKE\n"
    "key 4   BACK   WAKE\n"
    "key 5   CALL   WAKE\n"
    "key 6   ENDCALL   WAKE\n"
    "key 7   0   WAKE\n"
    "key 8   1   WAKE\n"
    "key 9   2   WAKE\n"
    "key 10   3   WAKE\n"
    "key 11   4   WAKE\n"
    "key 12   5   WAKE\n"
    "key 13   6   WAKE\n"
    "key 14   7   WAKE\n"
    "key 15   8   WAKE\n"
    "key 16   9   WAKE\n"
    "key 17   STAR   WAKE\n"
    "key 18   POUND   WAKE\n"
    "key 19   DPAD_UP   WAKE\n"
    "key 20   DPAD_DOWN   WAKE\n"
    "key 21   DPAD_LEFT   WAKE\n"
    "key 22   DPAD_RIGHT   WAKE\n"
    "key 23   DPAD_CENTER   WAKE\n"
    "key 24   VOLUME_UP   WAKE\n"
    "key 25   VOLUME_DOWN   WAKE\n"
    "key 26   POWER   WAKE\n"
    "key 27   CAMERA   WAKE\n"
    "key 28   CLEAR   WAKE\n"
    "key 29   A   WAKE\n"
    "key 30   B   WAKE\n"
    "key 31   C   WAKE\n"
    "key 32   D   WAKE\n"
    "key 33   E   WAKE\n"
    "key 34   F   WAKE\n"
    "key 35   G   WAKE\n"
    "key 36   H   WAKE\n"
    "key 37   I   WAKE\n"
    "key 38   J   WAKE\n"
    "key 39   K   WAKE\n"
    "key 40   L   WAKE\n"
    "key 41   M   WAKE\n"
    "key 42   N   WAKE\n"
    "key 43   O   WAKE\n"
    "key 44   P   WAKE\n"
    "key 45   Q   WAKE\n"
    "key 46   R   WAKE\n"
    "key 47   S   WAKE\n"
    "key 48   T   WAKE\n"
    "key 49   U   WAKE\n"
    "key 50   V   WAKE\n"
    "key 51   W   WAKE\n"
    "key 52   X   WAKE\n"
    "key 53   Y   WAKE\n"
    "key 54   Z   WAKE\n"
    "key 55   COMMA   WAKE\n"
    "key 56   PERIOD   WAKE\n"
    "key 57   ALT_LEFT   WAKE\n"
    "key 58   ALT_RIGHT   WAKE\n"
    "key 59   SHIFT_LEFT   WAKE\n"
    "key 60   SHIFT_RIGHT   WAKE\n"
    "key 61   TAB   WAKE\n"
    "key 62   SPACE   WAKE\n"
    "key 63   SYM   WAKE\n"
    "key 64   EXPLORER   WAKE\n"
    "key 65   ENVELOPE   WAKE\n"
    "key 66   ENTER   WAKE\n"
    "key 67   DEL   WAKE\n"
    "key 68   GRAVE   WAKE\n"
    "key 69   MINUS   WAKE\n"
    "key 70   EQUALS   WAKE\n"
    "key 71   LEFT_BRACKET   WAKE\n"
    "key 72   RIGHT_BRACKET   WAKE\n"
    "key 73   BACKSLASH   WAKE\n"
    "key 74   SEMICOLON   WAKE\n"
    "key 75   APOSTROPHE   WAKE\n"
    "key 76   SLASH   WAKE\n"
    "key 77   AT   WAKE\n"
    "key 78   NUM   WAKE\n"
    "key 79   HEADSETHOOK   WAKE\n"
    "key 80   FOCUS   WAKE\n"
    "key 81   PLUS   WAKE\n"
    "key 82   MENU   WAKE\n"
    "key 83   NOTIFICATION   WAKE\n"
    "key 84   SEARCH   WAKE\n"
    "key 85   MEDIA_PLAY_PAUSE   WAKE\n"
    "key 86   MEDIA_STOP   WAKE\n"
    "key 87   MEDIA_NEXT   WAKE\n"
    "key 88   MEDIA_PREVIOUS   WAKE\n"
    "key 89   MEDIA_REWIND   WAKE\n"
    "key 90   MEDIA_FAST_FORWARD   WAKE\n"
    "key 91   MUTE   WAKE\n";

static const char DEVICE_NAME[] = "Webkey_touch";
static const char IDC_PATH_IN_SYSTEM[] = "/system/usr/idc/Webkey_touch.idc";
static const char IDC_PATH_IN_DATA[] = "/data/system/devices/idc/Webkey_touch.idc";
static const char KL_PATH_IN_SYSTEM[] = "/system/usr/keylayout/Webkey.kl";
#endif
