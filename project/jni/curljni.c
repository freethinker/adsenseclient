#include <string.h> 
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "curl/curl.h"

jstring
Java_com_banandroid_curljni_curljni_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{

  CURL *curl;
  CURLcode res;
        char buffer[10];

  curl = curl_easy_init();
  if(curl) {
    curl_easy_setopt(curl, CURLOPT_URL, "yahoo.com");
    res = curl_easy_perform(curl);
        /* always cleanup */
    curl_easy_cleanup(curl);
   if(res == 0)
                return (*env)->NewStringUTF(env, "0 response");
   else
        sprintf(buffer,"code: %i",res);
                return (*env)->NewStringUTF(env, buffer);

  } else {

return (*env)->NewStringUTF(env, "no curl");

  }
} 
