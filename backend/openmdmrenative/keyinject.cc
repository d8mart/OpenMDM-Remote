#include <errno.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>

#include "wipc.pb.h"

#include "keyinject.h"
#include "webkey_utile.h"
#include <sys/stat.h>

//concatenating strings
#include <iostream>
#include <string>

std::string java_package ("com.webkey");

const std::string IME_PATH ("/system/bin/ime");
const std::string WK_SERVICE (".service.keyboard.KeyinjectService");

std::string cmd_enable;
std::string cmd_set;
std::string cmd_disable;
std::string cmd_restore_ime;

void prepare_reset_cmd(const std::string& ime) {
    cmd_restore_ime = IME_PATH +" set "+ime;
}

void init_keyboard(const std::string& keyboard_command_prefix) {
    java_package = keyboard_command_prefix;

    cmd_enable = IME_PATH+" enable "+java_package+"/"+WK_SERVICE;
    cmd_set = IME_PATH+" set "+java_package+"/"+WK_SERVICE;
    cmd_disable = IME_PATH+" disable "+java_package+"/"+WK_SERVICE;
}

void enable_keyboard(const std::string& default_input_method) {
    prepare_reset_cmd(default_input_method);

    if(system(cmd_enable.c_str()) != 0) {
        ALOGD("Ime enable error: %s\n", strerror(errno));
    }

    if(system(cmd_set.c_str()) != 0) {
        ALOGD("Ime set error: %s\n", strerror(errno));
    }
}

void disable_keyboard() {
    if(system(cmd_disable.c_str()) != 0) {
        ALOGD("Ime disable error: %s\n", strerror(errno));
    }

    if(system(cmd_restore_ime.c_str()) != 0) {
        ALOGD("Ime reseted error: %s\n", strerror(errno));
    }
}


