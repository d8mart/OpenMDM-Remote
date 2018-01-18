/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <errno.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>

#include <sys/ioctl.h>
#include <sys/mman.h>

#include <binder/ProcessState.h>

#include <binder/IMemory.h>
#include <gui/ISurfaceComposer.h>

#include <ui/PixelFormat.h>

#include <SkImageEncoder.h>
#include <SkData.h>
#include <SkStream.h>

#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <semaphore.h> 

#include <time.h>

#include "wipc.pb.h"
#include "webkey_utile.h"
#include "screencap.h"
#include "flinger_function.h"


using namespace android;

static uint32_t DEFAULT_DISPLAY_ID = ISurfaceComposer::eDisplayIdMain;

uint32_t FRAMEBUFFER = 0;
uint32_t VIABINDER = 1;
char MSG_TYPE_IMAGE = 0x1;

//It is the steps between bytes in the img diff algorithm
#define OFFSET 25

static uint32_t SCREENCAP_METHOD_TYPE = VIABINDER;

pthread_t screencap_thread = 0;

/* should protect this values */
static bool running = false;

struct screen_info {
    uint32_t device_width;
    uint32_t device_height;
    ssize_t bytesPerPixel;


    //it reprsesent what the browser ask
    uint32_t width;
    uint32_t height;

    //The asked sending frequency in millis
    uint32_t frequency; 
    bool orientation;

    //it is true if the data has changed
    bool updated_resolution;
    bool updated_frequency;
    bool check_diff;

    pthread_mutex_t lock;
} static screen_info;

static double now_ms(void) {
    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000.0 * res.tv_sec + (double) res.tv_nsec / 1e6;
}

//Binder base variables
sp<IBinder> display = SurfaceComposerClient::getBuiltInDisplay(DEFAULT_DISPLAY_ID);

/* debug function */
static bool write_to_file(const void *data, size_t len) {
    char name[100];
    snprintf(name,100,"/sdcard/webkey/%f.png",now_ms());

    int fd = open(name, O_WRONLY | O_CREAT | O_TRUNC, 0664);
    if (fd == -1) {
        ALOGE("Error opening file: %s (%s)", name, strerror(errno));
        return false;
    }

    write(fd, data, len);
    close(fd);
    return true;
}

/* debug function */
static void profile(int w, int h, int len, double start, double end) {
    double duration = end-start;
    ALOGD("Profile: w=%d, h=%d, size=%d, duration=%f", w, h, len, duration);
}


static bool write_n_bytes(int socket, const void *data, size_t len) {
    ssize_t out;

    while(len > 0) {
        out = write(socket, data, len);
        if (out <= 0) {
            return false;
        } else {
            len-=out;
        }
    }
    return true;
}

void *screencap_binder(void *sockfd) {

    ProcessState::self()->startThreadPool();

    int socket = *((int *)sockfd);
    void const* base = 0;

    int* lastpic = NULL;
    size_t lastpic_size = 0;
    bool changed = false;

    size_t size = 0;
    uint32_t len = 0;
    uint32_t lenout = 0;
    uint32_t f, s;
    int width, height;

    /*
     * this variables for the sendig frequency
     * 
     */
    uint32_t send_frequency; //in millis
    double start_time;
    double delta;

    running = true;
    while(running) {
        start_time = now_ms();
        /** Do work **/
        pthread_mutex_lock(&screen_info.lock);

        /*
         * Read the changed only it is necessary.
         * Must guaranteed the first time running
         */
        if(screen_info.updated_resolution) {
            width = screen_info.width;
            height = screen_info.height;

            //free the prev img
            if(lastpic) {
                delete[] lastpic;
            }
            /*
             * caluclate the size of diff img
             */
            lastpic_size = width*height * screen_info.bytesPerPixel/sizeof(int)/OFFSET;
            lastpic = new int[lastpic_size]; 

            //Ha valtozik a meret akkor tuti, h uj kepet kell kikuldeni.
            screen_info.check_diff = false;
            screen_info.updated_resolution = false;
        }

        if(screen_info.updated_frequency) {
            send_frequency = screen_info.frequency;
            screen_info.updated_frequency = false;
        }

        pthread_mutex_unlock(&screen_info.lock);

        ScreenshotClient screenshot;
        
        #ifdef NEWSKIA
        if(screenshot.update(display, Rect(), width, height, false) != NO_ERROR) {
        #else
        if(screenshot.update(display, width, height) != NO_ERROR) {
        #endif
            ALOGE("Screenshot.update error");
            continue;
        }
        //Hogy legyen ideje a fb-nek is feltoltodni
        usleep((int) 40*1000);

        base = screenshot.getPixels();
        f = screenshot.getFormat();
        s = screenshot.getStride();
        size = screenshot.getSize();

        //Check the image diff
        if(base && screen_info.check_diff) {
            changed = false;
            for(size_t i=0; i < lastpic_size; i++) {
                if(lastpic[i] != ((int*) base)[i*OFFSET]) {
                    lastpic[i] = ((int*) base)[i*OFFSET];
                    changed = true;
                }
            }
        }
        //ha valtozott a kep vagy nem kell nezni a valtozast akkor akkor kuldjuk
        if(changed || !screen_info.check_diff) {
            if (base) {
                SkBitmap b;

                #ifdef NEWSKIA
                    const SkImageInfo info = SkImageInfo::Make(width, height, flinger2skia(f), kPremul_SkAlphaType);
                    b.installPixels(info, const_cast<void*>(base), s*bytesPerPixel(f));
                #else 
                    b.setConfig(flinger2skia(f), width, height, s*bytesPerPixel(f));
                    b.setPixels((void*)base);
                #endif

                SkDynamicMemoryWStream stream;
                SkImageEncoder::EncodeStream(&stream, b,
                        SkImageEncoder::kJPEG_Type, SkImageEncoder::kDefaultQuality);
                SkData* streamData = stream.copyToData();

                len = streamData->size();
                lenout = htonl(len);
                uint32_t send_frequency_htonl = htonl(send_frequency);

                /**
                 * TODO: check result and the siz of len
                 * If it is eq 0 then should do something
                 */
                //write out the img's size
                if( !write_n_bytes(socket, &MSG_TYPE_IMAGE, 1) ||
                    !write_n_bytes(socket, &lenout, sizeof(uint32_t)) ||
                    !write_n_bytes(socket, streamData->data(), len) ||
                    !write_n_bytes(socket, &send_frequency_htonl, sizeof(uint32_t))) {
                    running = false;
                }

                streamData->unref();
            } else {
                ALOGE("Base is empty");
                running=false;
            }
        }

        delta = send_frequency - (now_ms() - start_time);
        if( delta > 0 ) {
            usleep((int) delta*1000);
        }
    }

    //free the mem alloc
    delete[] lastpic;
    ALOGD("Stoped screencap pthread");
    return NULL;
}

void set_resolution(uint32_t width, uint32_t height) {
    bool new_orientation;
    /* if too small the screen size */
    if (width <= 0 || height <= 0) { 
        return;
    }

    if(width > height) {
        new_orientation = LANDSCAPE;
    } else {
        new_orientation = PORTRAIT;
    }

    pthread_mutex_lock(&screen_info.lock);
    if(new_orientation == screen_info.orientation) {
        /* protect if asked metrics heigher then device's size */
        if( width <= screen_info.device_width) {
            screen_info.width = width;
            screen_info.height = height;
        }
    } else {
        /* protect if asked metrics heigher then device's size */
        if(height <= screen_info.device_width) {
            screen_info.width = height;
            screen_info.height = width;
        }
    }
    screen_info.updated_resolution = true;
    pthread_mutex_unlock(&screen_info.lock);

}

void set_frequency(uint32_t frequency) {
    pthread_mutex_lock(&screen_info.lock);
    screen_info.frequency = frequency;
    screen_info.updated_frequency = true;
    pthread_mutex_unlock(&screen_info.lock);
}

void set_metrics(wipc::Screen message) {
    switch(message.type()) {
        case wipc::Screen::RESOLUTION:
            set_resolution(message.resolution().width(), message.resolution().height());
            break;
        case wipc::Screen::FREQUENCY:
            set_frequency(message.frequency().frequency());
            break;
        case wipc::Screen::IMGDIFF:
            screen_info.check_diff = message.imagediff().diff();
            break;
    }
}

bool screencap_init() {
    ALOGD("Init sceencap");
    ProcessState::self()->startThreadPool();

    ScreenshotClient screenshot;

    if(pthread_mutex_init(&screen_info.lock, NULL) != 0) {
        ALOGE("Dimension mutex init error");
    }
    
    #ifdef NEWSKIA
    if (display != NULL && screenshot.update(display, Rect(), false) == NO_ERROR && screenshot.getPixels()) {
    #else 
    if (display != NULL && screenshot.update(display) == NO_ERROR && screenshot.getPixels()) {
    #endif

        pthread_mutex_lock(&screen_info.lock);
        screen_info.frequency = 500;
        screen_info.device_width = screenshot.getWidth();
        screen_info.device_height = screenshot.getHeight();
        screen_info.width = screen_info.device_width;
        screen_info.height = screen_info.device_height;
        screen_info.bytesPerPixel = bytesPerPixel(screenshot.getFormat());
        screen_info.check_diff = false;
        screen_info.updated_frequency = true;
        screen_info.updated_resolution = true;

        if(screen_info.device_width > screen_info.device_height) {
            screen_info.orientation = LANDSCAPE;
        } else {
            screen_info.orientation = PORTRAIT;
        }

        SCREENCAP_METHOD_TYPE = VIABINDER;
        pthread_mutex_unlock(&screen_info.lock);

        ALOGD("Screencap method will be: binder (%d, %d)",screen_info.device_width, screen_info.device_height);
        return true;
    }

    return false;
}

/**
 * for touch device rotation
 */
bool get_screen_orientation(){
    return screen_info.orientation;
}

void stop_stream() {
    running = false;
}

void start_streamer(int *socket){
    //SCHED_NORMAL
    int policy = SCHED_OTHER;

    pthread_attr_t attr;

    pthread_attr_init(&attr);
    pthread_attr_setschedpolicy(&attr, policy);
    attr.sched_priority = sched_get_priority_min(policy);

    pthread_mutex_lock(&screen_info.lock);
    screen_info.check_diff = false;
    screen_info.updated_resolution = true;
    screen_info.updated_frequency = true;
    pthread_mutex_unlock(&screen_info.lock);
    pthread_create(&screencap_thread, &attr, screencap_binder, (void *) socket);
    ALOGD("Started screencap pthread");
}
