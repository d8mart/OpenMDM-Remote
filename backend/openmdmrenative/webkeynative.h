#include <android/log.h>
#include "wipc.pb.h"

//protobuf parse
#include <google/protobuf/io/zero_copy_stream_impl.h>
#include <google/protobuf/io/coded_stream.h>

bool read_message(wipc::Message * message, google::protobuf::io::CodedInputStream *coded_input);

#define EXIT_SUCCESS 0
#define EXIT_ALREADY_RUNNING 10
#define EXIT_SCREEN_INIT_ERR 11
#define EXIT_SOCKET_INIT_ERR 12
