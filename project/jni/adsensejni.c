#include <string.h> 
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "urlcode.h"
#include "adsensejni.h"

#define DEBUG 1

#if DEBUG
#include <android/log.h>
#  define  D(x...)  __android_log_print(ANDROID_LOG_INFO,"adsenseclient",x)
#else
#  define  D(...)  do {} while (0)
#endif


jstring
Java_in_humbug_adsenseclient_AdsenseClient_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{

  CURLcode res;
  char buffer[10];

  if(curl_local_init() == 0) {
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
