#ifndef SCREENCAP_INC
#define SCREENCAP_INC

#define LANDSCAPE true
#define PORTRAIT false

bool screencap_init();
bool get_screen_orientation();
void start_streamer(int*);
void stop_stream();
void set_metrics(const wipc::Screen);

#endif
