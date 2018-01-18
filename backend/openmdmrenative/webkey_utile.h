#ifndef WEBKEYNATIVE_UTILE_H
#define WEBKEYNATIVE_UTILE_H

#include <android/log.h>

void __webkey_log_print(int prio, const char *tag, const char *fmt, ...);
void restart(int argc, char** argv);

#define LOG_TAG "Webkey-backend"
#define ALOGD(...) __webkey_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __webkey_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif //WEBKEYNATIVE_UTILE_H
