#include <errno.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include "suinput.h"
#include "wipc.pb.h"
#include "touchinject.h"
#include "webkey_utile.h"
#include <linux/input.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <sys/stat.h> //for chmod

/* this macro is used to tell if "bit" is set in "array"
 * it selects a byte from the array, and does a boolean AND
 * operation with a byte that only has the relevant bit set.
 * eg. to check for the 12th bit, we do (array[1] & 1<<4)
 */
#define test_bit(bit, array)    (array[bit/8] & (1<<(bit%8)))

using namespace google::protobuf;

struct touch_info {
    touch_info() : xmax(0), ymax(0) {}    
    __s32 xmin, xmax;
    __s32 ymin, ymax;
    bool multi;
    bool orientation;
    //it tepend from the display resolution
    bool altered_orientation;
    char name[256];
} static touch_info;

static bool touch_released = true;
int tid = 0;
int suinput_fd = -1;

bool read_dev_info(char *touch_device) {
    //for multi touch detection
    uint8_t abs_bitmask[(ABS_MAX+1)/8];
    memset(abs_bitmask, 0, sizeof(abs_bitmask));

    uint8_t key_bitmask[(KEY_MAX / 8) + 1];
    memset(key_bitmask, '\0', sizeof (key_bitmask));

    struct input_absinfo info;
    suinput_fd = open(touch_device, O_RDWR);
    if(suinput_fd == -1) {
        ALOGD("Can not open touch device %s: %s", touch_device, strerror(errno));
        return false;
    }

    //parse name
    if (ioctl(suinput_fd, EVIOCGNAME(sizeof(touch_info.name)), touch_info.name) < 0) {
        ALOGD("Touch device: %s, parse name error.",touch_device);
        goto err;
    }

    ioctl(suinput_fd, EVIOCGBIT(EV_KEY, sizeof(key_bitmask)), key_bitmask);
    ioctl(suinput_fd, EVIOCGBIT(EV_ABS, sizeof(abs_bitmask)), abs_bitmask);

    if (test_bit(BTN_TOUCH, key_bitmask) && 
        test_bit(ABS_X, abs_bitmask) && 
        test_bit(ABS_Y, abs_bitmask)) {
        touch_info.multi=false;
        ALOGD("Touch device: Detected single touch device: %s,",touch_device);

    } else if (test_bit(ABS_MT_POSITION_X, abs_bitmask) && 
        test_bit(ABS_MT_POSITION_Y, abs_bitmask)) {
        touch_info.multi=true;
        ALOGD("Touch device: Detected multi touch device: %s,",touch_device);
    } else {
        goto err;
    }
    
    if(touch_info.multi) {
        //if device class is multi touch
        if(ioctl(suinput_fd, EVIOCGABS(ABS_MT_POSITION_X), &info) == 0) {
            touch_info.xmin = info.minimum;
            touch_info.xmax = info.maximum;
        } else {
            ALOGD("Touch device: %s, parse ABS_MT_POSITION_X error");
            goto err;
        }

        if(ioctl(suinput_fd, EVIOCGABS(ABS_MT_POSITION_Y), &info) == 0) {
            touch_info.ymin = info.minimum;
            touch_info.ymax = info.maximum;
        } else {
            ALOGD("Touch device: %s, parse ABS_MT_POSITION_Y error",touch_device);
            goto err;
        }
    } else {
        //if device class is single touch
        if(ioctl(suinput_fd, EVIOCGABS(ABS_X), &info) == 0) {
            touch_info.xmin = info.minimum;
            touch_info.xmax = info.maximum;
        } else {
            ALOGD("Touch device: %s, parse ABS_X error",touch_device);
            goto err;
        }

        if(ioctl(suinput_fd, EVIOCGABS(ABS_Y), &info) == 0) {
            touch_info.ymin = info.minimum;
            touch_info.ymax = info.maximum;
        } else {
            ALOGD("Touch device: %s, parse ABS_Y error",touch_device);
            goto err;
        }            
    }

    if((touch_info.xmax-touch_info.xmin) > (touch_info.ymax-touch_info.ymin)) {
        touch_info.orientation = LANDSCAPE;
        touch_info.altered_orientation = true;
    } else {
        touch_info.orientation = PORTRAIT;
        touch_info.altered_orientation = false;
    }
    return true;

    err:
    close(suinput_fd);
    return false;
}

void write_out_config(const char* file_path, const char* content) {
    FILE* f = fopen(file_path, "w");
    if(f) {
        fprintf(f, "%s", content);
        fclose(f);
        chmod(file_path, S_IRUSR|S_IWUSR|S_IRGRP|S_IROTH);
    }
}

static void prepare_device_configs() {
    system("mount -o remount,rw /system");

    //write config fule in the /system/usr/idc..
    write_out_config(IDC_PATH_IN_SYSTEM, DEVICE_CONFIG);
    write_out_config(KL_PATH_IN_SYSTEM, KL_CONFIG);
    system("mount -o remount,ro /system");

    // in case the remount fails    
    mkdir("/data/system/devices",S_IRUSR|S_IWUSR|S_IXUSR);
    mkdir("/data/system/devices/idc",S_IRUSR|S_IWUSR|S_IXUSR);
    write_out_config(IDC_PATH_IN_DATA, DEVICE_CONFIG);
}

static void set_own_touch_infos() {
    touch_info.multi=false;
    touch_info.xmin = TOUCH_X_MIN;
    touch_info.xmax = TOUCH_X_MAX;
    touch_info.ymin = TOUCH_Y_MIN;
    touch_info.ymax = TOUCH_X_MAX;
    touch_info.orientation = PORTRAIT;
    touch_info.altered_orientation = false;
}

static bool init_own_device() {
    prepare_device_configs();
    suinput_fd = suinput_open(DEVICE_NAME);
    if(suinput_fd == -1) {
        return false;
    }
    set_own_touch_infos();

    return true;
}

static bool looking_for_touch_device() {
    char touch_device[19]="/dev/input/event0";

    for (int i=50; i >= 0; i--) {
        if (i < 10) {
            touch_device[sizeof(touch_device)-3] = '0'+(char)(i);
            touch_device[sizeof(touch_device)-2] = 0;
        } else {
            touch_device[sizeof(touch_device)-3] = '0'+(char)(i/10);
            touch_device[sizeof(touch_device)-2] = '0'+(char)(i%10);
            touch_device[sizeof(touch_device)-1] = 0;
        }
        if(read_dev_info(touch_device)) {
            return true;
        }
    }
    return false;
}

bool init_touch() {

    if(init_own_device()) {
        ALOGD("Own %s inited success", DEVICE_NAME);
        return true;
    } else {
        ALOGD("Own %s init failed: %s", DEVICE_NAME, strerror(errno));
    }

    if(looking_for_touch_device()) {
        ALOGD("Touch device is %s, xmax: %d, ymax: %d",touch_info.name, touch_info.xmax, touch_info.ymax);
        return true;
    }

    return false;

}

void deinit_touch() {
    suinput_close(suinput_fd);
}

int write_event(__u16 type, __u16 code, __s32 value) {
    int result = -1;
    struct input_event event;

    memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, 0); /* This should not be able to fail ever.. */
    event.type = type;
    event.code = code;
    //TODO: Handle the tid max size!!!!!
    event.value = value;

    result = write(suinput_fd, &event, sizeof(event));
    if (result != sizeof(event)) {
        ALOGD("Touch inject error: %s", strerror(errno));
    }

    return result;
}

int touch_inject(uint32_t x, uint32_t y, uint32_t touch_type, bool mirror_touch, bool flip_touch) {
    uint32_t xx;
    uint32_t yy;

    /*
     * protect the touch flow
     * it filter out the double down events
     *
     * This method not works properly when the
     * touch inject return with error
     */
    if ((touch_type == TOUCH_DOWN)  && (touch_released == false) ) {
        //inject touch release event
        touch_inject(x, y, TOUCH_UP, mirror_touch, flip_touch);
    }

    if (touch_type == TOUCH_UP) { 
        touch_released = true;
    }

    if (touch_type == TOUCH_DOWN) {
        touch_released = false;
    }

    //handle the invalid size
    if (x > 65535) x = 65535;
    if (y > 65535) y = 65535;

    if (mirror_touch) {
        x = 65535 - x;        
        y = 65535 - y;
    }
    if (touch_info.altered_orientation || flip_touch) {
        int t = y;
        yy = touch_info.ymin + (x * (touch_info.ymax - touch_info.ymin)) / 65535;
        xx = touch_info.xmax - (t * (touch_info.xmax - touch_info.xmin)) / 65535;
    } else {
        xx = touch_info.xmin + (x * (touch_info.xmax - touch_info.xmin)) / 65535;
        yy = touch_info.ymin + (y * (touch_info.ymax - touch_info.ymin)) / 65535;
    }

    if(touch_type == TOUCH_UP) {
        if(touch_info.multi) {
            write_event(EV_ABS, ABS_MT_TRACKING_ID, -1);        
        }
        write_event(EV_KEY, BTN_TOUCH, 0);            
        write_event(EV_SYN, SYN_REPORT, 0);
        return 0;
    }

    if(touch_type == TOUCH_DOWN) {
        if(touch_info.multi) {
            write_event(EV_ABS, ABS_MT_TRACKING_ID, tid);
            tid++;
        }
        write_event(EV_KEY, BTN_TOUCH, 1);        
    }

    if(touch_info.multi) {
        write_event(EV_ABS, ABS_MT_POSITION_X, xx);
        write_event(EV_ABS, ABS_MT_POSITION_Y, yy);
        write_event(EV_ABS, ABS_MT_PRESSURE, 100);
    } else {
        write_event(EV_ABS, ABS_X, xx);
        write_event(EV_ABS, ABS_Y, yy);             
    }

    write_event(EV_SYN, SYN_REPORT, 0);

    return 0;
}

void touches_inject(wipc::Message * t) {
    wipc::Touch touch;
    for(int i =0; i < t->touch_size(); i++) {
        touch = t->touch(i);
        touch_inject(
                touch.width(),
                touch.height(),
                touch.type(),
                touch.mirror(),
                touch.flip());
    }
}
