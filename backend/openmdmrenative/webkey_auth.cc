//Network related includes
#include <unistd.h>

#include <google/protobuf/io/zero_copy_stream_impl.h>
#include <google/protobuf/io/coded_stream.h>

#include "webkeynative.h"
#include "webkey_utile.h"
#include "webkey_auth.h"

using namespace google::protobuf::io;

//It protect from the fake client
static bool check_auth(wipc::Message * message, std::string * key) {
    if(message->type() != wipc::Message::CMD) {
        return false;
    }

    if(!message->command().has_initkey()) {
        return false;        
    }

    std::string received_key = message->command().initkey();

    if(received_key.compare(*key) != 0) {
        return false;        
    } else {
        return true;
    }
}

bool read_and_check_auth(int *socket, auth_credentials *auth_creds) {
    wipc::Message message;

    ZeroCopyInputStream* raw_input = new FileInputStream(*socket);
    CodedInputStream* coded_input = new CodedInputStream(raw_input);

    bool result = true;
    
    //Authentication steps
    if(!read_message(&message , coded_input)) {
        result = false;
        goto stop;
    }

    if(!check_auth(&message, &auth_creds->backend_key)) {
        result = false;
        goto stop;
    }

    stop:
    delete coded_input;
    delete raw_input;
    return result;
}

//send key to java
//http://blog.ajhodges.com/
void send_auth_key(int *sockfd, auth_credentials *auth_creds) {
	wipc::Message message;
	message.set_type(wipc::Message::CMD);

	wipc::Command * command = message.mutable_command();
	command->set_type(wipc::Command::INIT_KEY);
	command->set_initkey(auth_creds->session_key);

    //make a buffer that can hold message + room for a 32bit delimiter
    int varintsize = CodedOutputStream::VarintSize32(message.ByteSize());
    int messageSize = message.ByteSize()+varintsize;
    char* messageBuf = new char[messageSize];
         
    //write varint delimiter to buffer
    ArrayOutputStream* arrayOut = new ArrayOutputStream(messageBuf, messageSize);
    CodedOutputStream* codedOut = new CodedOutputStream(arrayOut);

    codedOut->WriteVarint32(message.ByteSize());
     
    //write protobuf message to buffer
    message.SerializeToCodedStream(codedOut);
     
    //send buffer to client
    if(write(*sockfd, messageBuf, messageSize) <= 0) {
        ALOGE("Auth key sending failed");
    }
	delete messageBuf;
	delete arrayOut;
    delete codedOut; 
}
