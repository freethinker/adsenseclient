#ifndef _ADSENSECLIENT_CURL_H
#define _ADSENSECLIENT_CURL_H

#include "curl/curl.h"
#include "curl/easy.h"

#define COOKIEFILE "/sdcard/humbug_adsense_client/cookies.txt"

struct write_result {
  char *memory;
  size_t size;
};

int curl_local_init();

CURL *curl;
extern CURL *curl;

#endif /* _ADSENSECLIENT_CURL_H */
