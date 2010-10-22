#include <string.h> 
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "urlcode.h"
#include "adsensejni.h"




jint
Java_in_humbug_adsenseclient_AdsenseDataService_genAdsenseReports( JNIEnv* env, jobject thiz, jstring username, jstring password ) {
	CURLcode res;
	const char *nativeUserName = (*env)->GetStringUTFChars(env, username, 0);
	const char *nativePassWord = (*env)->GetStringUTFChars(env, password, 0);


  if(curl_local_init() != 0) {
  	  curl_easy_cleanup(curl);
  	  return 1;
  }
  if((res = adsense_login(nativeUserName, nativePassWord)) != 0) {
  	  curl_easy_cleanup(curl);
  	  return 2;
  }
  if((res = get_adsense_reports()) != 0) {
  	  curl_easy_cleanup(curl);
  	  return 3;
  }	  
  process_adsense_reports();
  curl_easy_cleanup(curl);
  return 0;
}

jint
Java_in_humbug_adsenseclient_AccountSetupCheckSettings_genAdsenseReports( JNIEnv* env, jobject thiz, jstring username, jstring password ) {
	CURLcode res;
	const char *nativeUserName = (*env)->GetStringUTFChars(env, username, 0);
	const char *nativePassWord = (*env)->GetStringUTFChars(env, password, 0);


  if(curl_local_init() != 0) {
  	  curl_easy_cleanup(curl);
  	  return 1;
  }
  if((res = adsense_login(nativeUserName, nativePassWord)) != 0) {
  	  curl_easy_cleanup(curl);
  	  return 2;
  }
  if((res = get_adsense_reports()) != 0) {
  	  curl_easy_cleanup(curl);
  	  return 3;
  }	  
  process_adsense_reports();
  curl_easy_cleanup(curl);
  return 0;
}

jint
Java_in_humbug_adsenseclient_AccountSetupCheckSettings_delAccountDir( JNIEnv* env, jobject thiz, jstring path) {
	int status;
	char *command;
	const char *nativePath = (*env)->GetStringUTFChars(env, path, 0);
	command = (char *)malloc((strlen(nativePath)+10)*sizeof(char));
	sprintf(command,"rm -r %s",nativePath);
	D("Deleting Directory - command %s", command);
	system(command);
	free(command);
	return 0;
}
