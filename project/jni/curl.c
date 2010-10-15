#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "adsensejni.h"

static void *myrealloc(void *ptr, size_t size) {
  if (ptr)
    return realloc(ptr, size);
  else
    return calloc(1, size);
}

static size_t write_response(void *ptr, size_t size, size_t nmemb, void *stream) {
  struct write_result *mem = (struct write_result*)stream;
  size_t realsize = nmemb * size;

  mem->memory = myrealloc(mem->memory, mem->size + realsize + 1);
  if (mem->memory) {
    memcpy(&(mem->memory[mem->size]), ptr, realsize);
    mem->size += realsize;
    mem->memory[mem->size] = 0;
  }

  return realsize;
}

int curl_local_init() {
  curl = curl_easy_init();

  if (! curl)
    return 1;

  curl_easy_setopt(curl, CURLOPT_COOKIEJAR, COOKIEFILE);
  curl_easy_setopt(curl, CURLOPT_COOKIEFILE, COOKIEFILE);
  curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1);

  return 0;
}
