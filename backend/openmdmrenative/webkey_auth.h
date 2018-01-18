#ifndef AUTH_INC
#define AUTH_INC

#define KEYLEN 5
struct auth_credentials {
    std::string session_key;
    std::string backend_key;
};
bool read_and_check_auth(int *socket, auth_credentials *auth_creds);
void send_auth_key(int *sockfd, auth_credentials *auth_creds);
#endif
