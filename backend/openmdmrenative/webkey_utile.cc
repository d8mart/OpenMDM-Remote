#include "webkey_utile.h"
#include <cstdarg>
#include <stdio.h>
#include <string.h>
#include <cstdlib>


using namespace std;

void __webkey_log_print(int prio, const char *tag, const char *fmt, ...) {
    std::va_list ap;
    va_start(ap, fmt);
    __android_log_vprint(prio, tag, fmt, ap);
    fprintf(stdout, "D: ");
    vprintf(fmt, ap);
    fprintf(stdout, "\n");
    va_end(ap);
}

void restart(int argc, char** argv) {
    ALOGD("Restarting the process...");
    int i;
    int needed = 2; // plus end of string mark and & (bg) mark

    for(i = 0; i < argc; i++) {
        needed += strlen(argv[i]) + 1; // plus space
    }

    char *storage = (char *) malloc( sizeof( char ) * needed );
    strcpy(storage, argv[0]);

    for(i = 1; i < argc; ++i) {
        strcat(storage, " ");
        strcat(storage, argv[i] );
    }
    strcat(storage, "&");
    system(storage);
    free(storage);
}
