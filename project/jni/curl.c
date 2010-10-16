#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <regex.h>

#include "adsensejni.h"
#include "urlcode.h"

static void *myrealloc(void *ptr, size_t size) {
  if (ptr)
	return realloc(ptr, size);
  else
	return calloc(1, size);
}


static size_t write_data(void *ptr, size_t size, size_t nmemb, FILE *stream) {
	size_t written;
    written = fwrite(ptr, size, nmemb, stream);
    return written;
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

int curl_local_init(void) {
  curl = curl_easy_init();

  if (! curl)
	return 1;

  curl_easy_setopt(curl, CURLOPT_COOKIEJAR, COOKIEFILE);
  curl_easy_setopt(curl, CURLOPT_COOKIEFILE, COOKIEFILE);
  curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1);
  curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0);
  curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0);

  return 0;
}

int adsense_login(const char *username, const char*password) {
  //const char *username = "pratikmsinha@gmail.com";
  //const char *password = "abbccdda";
  CURLcode status;
  int ret;
  const char *src;
  static struct write_result response;
  char dest[10];
  static regex_t gmail_re;
  regmatch_t gmail_match;
  char errbuf[256];
  int errcode;
  int n, m;
  char *params, *encoded_params;
  
  D("Username %s\n",username);
  D("Password %s\n",password);
  
  /* STAGE 1 Adsense Login */
  response.memory = NULL;
  response.size = 0;
  curl_easy_setopt(curl, CURLOPT_URL, ADSENSE_LOGIN_URL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_response);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to ADSENSE_LOGIN_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  
  src = (const char *)response.memory;
  if(errcode = regcomp (&gmail_re, GmailRe, REG_ICASE | REG_EXTENDED)) {
	  regerror (errcode, &gmail_re, errbuf, sizeof(errbuf));
	  D("Unable to compile GmailRe %s",errbuf);
	  ret = 1;
	  goto cleanup;
  }
  if (regexec (&gmail_re, src, 1, &gmail_match, 0) != 0) {
	  D("Didnot Match, Can't find GALX");
	  /* May be we are already logged in*/
	  if (strcasestr(response.memory, ADSENSE_VERIFY_LOGIN_STRING) == NULL) {
			D("We are already logged in\n"); /* 
			D("May be we are already logged in\n"); 
			response.memory = NULL;
			response.size = 0;
			curl_easy_setopt(curl, CURLOPT_URL, ADSENSE_LOGIN_URL_STAGE4);
			curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_response);
			curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
			status = curl_easy_perform(curl);
			if(status != 0) {
				D("curl error: unable to send data to ADSENSE_LOGIN_URL_STAGE4\n");
				D("%s\n", curl_easy_strerror(status));
				ret = status;
				goto cleanup;
			}
			if (strcasestr(response.memory, ADSENSE_VERIFY_LOGIN_STRING) == NULL) {
				D("Error: Login Unsuccesful\n");
				D("%s",response.memory);
				ret = 1; 
				goto cleanup;
			} else {
				D("Login Successful\n");
				ret = 0;
			}*/
	  } else {
	  	  D("Where are we????");
	  	  D("%s",response.memory);
	  	  ret = 1;
	  }
	  goto cleanup;
  } else {
	  D("Matches %d %d\n",gmail_match.rm_so, gmail_match.rm_eo);
  }
  n = gmail_match.rm_so + 39;
  m = gmail_match.rm_eo - 1;
  strncpy(dest, src + n, m - n + 1);
  dest[m - n] = 0;
  D("Extract %s\n",dest);
  
  /* STAGE 2 Adsense Login */
  params = (char *) malloc(1000*sizeof(char));
  sprintf(params, "%s&GALX=%s&Email=%s&Passwd=%s", ADSENSE_LOGIN_URL_STAGE2_PARAMS, dest, username, password);
  D("before encoding %s\n", params);
  encoded_params = url_encode(params);
  D("after encoding %s\n", encoded_params);
  
  response.memory = NULL;
  response.size = 0;
  curl_easy_setopt(curl, CURLOPT_URL, ADSENSE_LOGIN_URL_STAGE2);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_response);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
  curl_easy_setopt(curl, CURLOPT_POST, 1);
  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, encoded_params);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to ADSENSE_LOGIN_URL_STAGE2\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		free(encoded_params);
		free(params);
		goto cleanup;
  }
  if (strcasestr(response.memory, ADSENSE_USERNAME_PASSWORD_INCORRECT) != NULL) {
		D("Error: %s\n", ADSENSE_USERNAME_PASSWORD_INCORRECT);
		ret = 1; /* Reuse an uncommon curl error */
		free(encoded_params);
		free(params);
		goto cleanup;
  } else if(strcasestr(response.memory, "Can&#39;t access your account?") != NULL) {
		D("Error: Can't access your account?\n");
		free(encoded_params);
		free(params);
		ret = 1;
		goto cleanup;
  } else {
  	D("Username Password Accepted\n");
  }
  
  curl_easy_setopt(curl, CURLOPT_POST, 0);
  free(encoded_params);
  free(params);
  
  /* STAGE 3 Adsense Login */
  response.memory = NULL;
  response.size = 0;
  curl_easy_setopt(curl, CURLOPT_URL, ADSENSE_LOGIN_URL_STAGE3);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_response);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to ADSENSE_LOGIN_URL_STAGE3\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  
  /* STAGE 4 Adsense Login */
  response.memory = NULL;
  response.size = 0;
  curl_easy_setopt(curl, CURLOPT_URL, ADSENSE_LOGIN_URL_STAGE4);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_response);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to ADSENSE_LOGIN_URL_STAGE4\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  if (strcasestr(response.memory, ADSENSE_VERIFY_LOGIN_STRING) == NULL) {
		D("Error: Login Unsuccesful\n");
		D("%s",response.memory);
		ret = 1; /* Reuse an uncommon curl error */
		goto cleanup;
  } else {
  	D("Login Successful\n");
  }
  ret = 0;
  cleanup:
  free(response.memory);
  return ret;
}

int get_adsense_reports(void) {
  CURLcode status;
  int ret;
  static struct write_result response;
  FILE *fp;
  
  response.memory = NULL;
  response.size = 0;
  fp = fopen(AFC_TODAYCSV,"w+");
  curl_easy_setopt(curl, CURLOPT_URL, AFC_TODAY_REPORT_URL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
  status = curl_easy_perform(curl);	//Repeating the statement to avoid getting the main adsense page
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to AFC_TODAY_REPORT_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  fclose(fp);

  response.memory = NULL;
  response.size = 0;
  fp = fopen(AFC_YESTERDAYCSV,"w+");
  curl_easy_setopt(curl, CURLOPT_URL, AFC_YESTERDAY_REPORT_URL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to AFC_YESTERDAY_REPORT_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  fclose(fp);

  response.memory = NULL;
  response.size = 0;
  fp = fopen(AFC_LAST7DAYSCSV,"w+");
  curl_easy_setopt(curl, CURLOPT_URL, AFC_LAST7DAYS_REPORT_URL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to AFC_LAST7DAYS_REPORT_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  fclose(fp);

  response.memory = NULL;
  response.size = 0;
  fp = fopen(AFC_THISMONTHCSV,"w+");
  curl_easy_setopt(curl, CURLOPT_URL, AFC_THISMONTH_REPORT_URL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to AFC_THISMONTH_REPORT_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  fclose(fp);

  response.memory = NULL;
  response.size = 0;
  fp = fopen(AFC_LASTMONTHCSV,"w+");
  curl_easy_setopt(curl, CURLOPT_URL, AFC_LASTMONTH_REPORT_URL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to AFC_LASTMONTH_REPORT_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  fclose(fp);

  response.memory = NULL;
  response.size = 0;
  fp = fopen(AFC_ALLTIMECSV,"w+");
  curl_easy_setopt(curl, CURLOPT_URL, AFC_ALLTIME_REPORT_URL);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
  status = curl_easy_perform(curl);
  if(status != 0) {
		D("curl error: unable to send data to AFC_ALLTIME_REPORT_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
  }
  fclose(fp);

  ret = 0;
  cleanup:
  free(response.memory);
  return ret;
}
