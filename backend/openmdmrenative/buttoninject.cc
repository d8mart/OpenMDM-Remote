#include <errno.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>

#include "wipc.pb.h"

#include "buttoninject.h"
#include "webkeynative.h"
#include <sys/stat.h>

void button_inject(int *type, unsigned int *key_code) {
    char key_code_buffer [33];
    char cmd[80];
    snprintf(key_code_buffer, sizeof(key_code_buffer), "%d", *key_code);
    strcpy(cmd, "/system/bin/input keyevent ");
    
    if(*type == BUTTON_LONGPRESS) {
        strcat(cmd, "--longpress ");
    }

    strcat(cmd, key_code_buffer);
    strcat(cmd, "&");
    system(cmd);
}
