#include "updater.h"
#include <sys/param.h> // For PATH_MAX (the maximum length of a path)
#include <sys/stat.h> // For the chmod
#include <unistd.h>
#include <fstream>
#include "webkey_utile.h"
#include "version.h"

Updater::Updater(char *current_file, char *update_file_path) {
    resolve_realpath(current_file);

    if(update_file_path == NULL) {
        this->update_file_path = "webkeynative_update";
    } else {
        this->update_file_path = update_file_path;
    }

    vcode_file_path = this->update_file_path + ".txt";
    ALOGD("version: %d", current_version());
}

bool Updater::has_update() {
    int availableVersion = available_version();

    if(availableVersion == -1) {
        ALOGD("can not read version of the update file");
        return false;
    }

    if(current_version() != available_version()) {
        return true;
    } else {
        return false;
    }
}

bool Updater::update() {
    if(!cp_update_file()) {
        ALOGE("Failed to copy the file: %s -> %s", update_file_path.c_str(), current_file.c_str());
        return false;
    }

    chmod(current_file.c_str(), S_IRWXU);
    ALOGD("The binary has been copied: %s -> %s", update_file_path.c_str(), current_file.c_str());
    return true;
}

int Updater::current_version() {
    return VERSION_CODE;
}

int Updater::available_version() {
    int vcode = 1;

    FILE *vfile = fopen(vcode_file_path.c_str(),"r");
    if(vfile == NULL) {
        return -1;
    }

    fscanf(vfile, "%d", &vcode);
    fclose(vfile);

    return vcode;
}

void Updater::resolve_realpath(char *src) {
    char resolved_path[PATH_MAX];

    if (realpath(src, resolved_path) == 0) {
        //ALOGE("Could not resolve realpath.\n");
        return;
    }

    current_file = resolved_path;
}

bool Updater::cp_update_file() {
    std::ifstream f1 (update_file_path.c_str(), std::fstream::binary);
    if (!f1) {
        ALOGD("Can not open the file for read: %s", update_file_path.c_str());
        return false;
    }
    unlink(current_file.c_str());

    std::ofstream f2 (current_file.c_str(), std::ios::out|std::fstream::trunc|std::fstream::binary);
    if(!f2) {
        ALOGD("Can not open the file for write: %s", current_file.c_str());
        return false;
    }

    f2 << f1.rdbuf();
    f2.close();
    //maybe should put back this line. Without it the adb type start is not works well
    //chmod(dst, S_IRWXU|S_IROTH);
    return true;
}
