//Network related includes
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/un.h>

#include <stdio.h>
#include <pthread.h>
#include <time.h>
#include <sys/file.h>

/* for daemon and for unlink*/
#include <unistd.h>

//Webkey related includes
#include "webkeynative.h"
#include "webkey_utile.h"
#include "webkey_auth.h"
#include "updater.h"
#include "wipc.pb.h"
#include "screencap.h"
#include "touchinject.h"
#include "keyinject.h"
#include "buttoninject.h"


using namespace google::protobuf::io;

bool halt = false;
int lockfd = -1;
int sockfd = -1;
Updater* updater;
auth_credentials auth_creds;
int argc;
char **argv;

void sig_handler(int signo) {
    if (signo == SIGINT) {
        ALOGD("Received SIGINT");
        halt = true;
        close(sockfd);
    }
}

void ignore_sigpipe() {
    struct sigaction act;
    memset(&act, 0, sizeof(act));
    act.sa_handler = SIG_IGN;
    act.sa_flags = SA_RESTART;
    sigaction(SIGPIPE, &act, NULL);
}

bool read_message(wipc::Message * message, CodedInputStream *coded_input) {
    google::protobuf::uint32 size;
    int perse_success;

    if(!coded_input->ReadVarint32(&size)) {
        ALOGD("Can't read message len.");
        return false;
    }

    CodedInputStream::Limit msgLimit = coded_input->PushLimit(size);
    perse_success = message->ParseFromCodedStream(coded_input);
    coded_input->PopLimit(msgLimit);

    if(!perse_success) {
        ALOGD("Can't parse protobuf message: %d", size);
        return false;
    }

    return true;
}

void *receiver(void *sockfd) {
    ALOGD("Started msg receiver pthread\n");
    bool running = true;

    int socket = *((int *)sockfd);    
    wipc::Message message;

    //prepare the network reading.
    ZeroCopyInputStream* raw_input = new FileInputStream(socket);
    CodedInputStream* coded_input = new CodedInputStream(raw_input);
    
    //Start the read procedure
    do{
        if(!read_message(&message, coded_input)) {            
            break;
        }

        switch(message.type()) {
            case wipc::Message::SCREEN:
                set_metrics(message.screen());
                break;
            case wipc::Message::TOUCH:
                touches_inject(&message);
                break;
            case wipc::Message::KEY:
                if(message.key().type() == wipc::Key::ENABLE){
                    enable_keyboard(message.key().defaultinputmethod());
                } else {
                    disable_keyboard();
                }
                break;
            case wipc::Message::BUTTON:
                int type;
                unsigned int button;

                switch(message.button().type()) {
                    case wipc::Button::UP:
                        type = BUTTON_UP;
                        break;
                    case wipc::Button::DOWN:
                        type = BUTTON_DOWN;
                        break;
                    case wipc::Button::LONGPRESS:
                        type = BUTTON_LONGPRESS;
                        break;
                }
                button = message.button().buttonid();
                button_inject(&type, &button);
                break;
            case wipc::Message::CMD:
                running=false;
                halt=true;
                ALOGD("Received HALT msg");
                break;
        }
    } while(running);
    
    //todo:
    //free(socket);    
    delete coded_input;
    delete raw_input;
    ALOGD("Stop msg receiver pthread");
    return NULL;
}

bool running_check_and_lock(char *lock_path) {
    lockfd = open(lock_path, O_CREAT, 0644);
    if(lockfd == -1) {
        ALOGE("Failed to open the lock file");
        return false;
    }

    if(flock(lockfd, LOCK_EX|LOCK_NB) != 0) {
        ALOGD("The backend already running");
        return false;
    }
    return true;
}

bool check_and_update() {
    if(!updater->has_update()) {
        return false;
    }

    ALOGD("Update available");
    if(updater->update()) {
        ALOGD("Update success to: %d", updater->current_version());
        return true;
    } else {
        ALOGD("Update failed");
        return false;
    }
}

void teardown(int exit_code) {
    close(sockfd);
    stop_stream();
    disable_keyboard();
    deinit_touch();
    close(lockfd);

    if(EXIT_SCREEN_INIT_ERR == exit_code) {
        halt = true;
    }

    if(!halt) {
        restart(argc, argv);
    }

    ALOGD("Bye");
    exit(exit_code);
}

bool accept_client() {
    bool myerr = false;
    int clientfd;
    pthread_t receiver_thread;

    do {
        ALOGD("Waiting for the client...");
        clientfd = accept(sockfd, (struct sockaddr*)NULL, NULL);
        if(clientfd < 0) {
            perror("Accpet error: ");
            myerr = true;
            ALOGD("Socket accept error");
            break;
        }

        if(check_and_update()) {
            ALOGD("Update available");
            break;
        } 

        //send the authentication code
        send_auth_key(&clientfd, &auth_creds);

        //Read backend key from java
        if(!read_and_check_auth(&clientfd, &auth_creds)) {
            ALOGD("The backend auth is failed");
            halt = true;
            break;
        }
        ALOGD("The backend authentication is ok");

        //start the streamer
        start_streamer(&clientfd);

        //Start the message loop
        pthread_create(&receiver_thread, NULL, receiver, (void *) &clientfd);
        pthread_join(receiver_thread, NULL);
        close(clientfd);

    } while (!halt);
    
    close(clientfd);
    return myerr;
}

bool init_socket() {
    int port = 8888;
    struct sockaddr_in serv_address;

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if(sockfd == -1) {
        perror("socketfd: \n");
        return false;
    }

    memset(&serv_address, 0, sizeof(struct sockaddr_in));
    serv_address.sin_family = AF_INET;
    serv_address.sin_port = htons(port);
    serv_address.sin_addr.s_addr = inet_addr("127.0.0.1");

    //Close the connection if the queue is full. Valojaban nem ugy mukodik ahogy gondolom.
    struct linger lo = { 1, 0 };
    setsockopt(sockfd, SOL_SOCKET, SO_LINGER, &lo, sizeof(lo));

    if(bind(sockfd,(struct sockaddr *) &serv_address, sizeof(serv_address)) < 0) {        
        return false;
    }

    listen(sockfd, 1);
    return true;
}

int main(int m_argc, char** m_argv) {
    ALOGD("Backend starting ...");

    //for restart
    argc = m_argc;
    argv = m_argv;

    //The length is KEYLEN (5);
    auth_creds.session_key = "budaf";
    auth_creds.backend_key = "budaf";
   
    //path of lock file 
    char *lock_file = strdup("webkeynative.lock");

    //path of update file
    char *update_file_path = NULL;

    //path of the current file
    char *current_file = argv[0];

    //the java package name (for keyboard)
    std::string java_package("com.webkey");


    int c;
    while ((c = getopt (argc, argv, "s:l:u:j:")) != -1) {
        switch (c) {
            case 's':
                auth_creds.session_key = std::string(optarg, KEYLEN);
                auth_creds.backend_key = std::string(optarg+KEYLEN, KEYLEN);
                break;
            case 'l':
                free(lock_file);
                lock_file = optarg;
                break;
            case 'u':
                update_file_path = optarg;
                break;
            case 'j':
                java_package = optarg;
                break;
            case '?':
                ALOGE("Unsupported cmd arg?\n");
                exit(EXIT_SUCCESS);
        }
    }

    updater = new Updater(current_file, update_file_path);

    //Ez nem mukodik ujratelepites utan (ha kozben futott a backend)
    if(!running_check_and_lock(lock_file)) {
        ALOGD("Exit because already running");
        exit(EXIT_ALREADY_RUNNING);
    }

    if(check_and_update()) {
        teardown(EXIT_SUCCESS);
    }

    //init services
    init_touch();
    init_keyboard(java_package);
    if(!screencap_init()) {
        ALOGE("Screencap initialization error");
        teardown(EXIT_SCREEN_INIT_ERR);
    }

    //init the ipc communication
    if(!init_socket()) {
        ALOGE("Socket initialization error");
        teardown(EXIT_SOCKET_INIT_ERR);
    }

    //sig handling
    ignore_sigpipe();
    signal(SIGINT, sig_handler);

    if(!accept_client()) {
        teardown(EXIT_SOCKET_INIT_ERR);
    }
    
    teardown(EXIT_SUCCESS);
    return 0;
}
