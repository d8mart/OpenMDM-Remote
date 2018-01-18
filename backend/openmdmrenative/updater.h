#ifndef UNTITLED1_UPDATER_H
#define UNTITLED1_UPDATER_H

#include <string>

class Updater {
private:
    std::string current_file;
    std::string update_file_path;
    std::string vcode_file_path;
    int available_version();
    bool cp_update_file();
    void resolve_realpath(char *src);
public:
    Updater(char *current_file, char *update_file_path);
    bool has_update();
    bool update();
    int current_version();
};

#endif //UNTITLED1_UPDATER_H
