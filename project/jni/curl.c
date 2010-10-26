#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>

#include "adsensejni.h"
#include "urlcode.h"
#include "ConvertUTF.h"

#define BSIZ 32768


int convert_to_utf8(FILE *stream1, FILE *stream2) {
	FILE *fp, *fp1;
	unsigned int bytes;
	UTF16 *source_start, *source_end, *source_backup;
	UTF8 *target_start, *target_end, *target_backup;
	char *sourceout;
	size_t num, i, bad;
	fp = stream1;
	fp1 = stream2;
	source_start = (UTF16 *) malloc(BSIZ*sizeof(UTF16));
	source_backup = source_start;
	while (0 < (num = fread(source_start,sizeof(UTF16),BSIZ,fp))) {
		ConversionResult ret;
		source_end = source_start + num + 1;
		ret = ConvertUTF16toUTF8((const UTF16 **)&source_start, source_end, NULL, NULL, strictConversion, &bytes);
		if (ret != conversionOK) {
			D("first call to ConvertUTF16toUTF8 failed (%d)", ret);
			if (ret == sourceExhausted) {
				D("Partial character in input");
			} else if (ret == targetExhausted) {
				D("target buffer exhausted");
			} else if (ret == sourceIllegal) {
				D("malformed/illegal source sequence");
			} else {
				D("unknown ConvertUTF16toUTF8 error");
			}
			return 1;
		}
		target_start = (UTF8 *) malloc((bytes + 1)*sizeof(UTF8));
		target_backup = target_start;
		target_end = target_start + bytes;
		source_start = source_backup;
		source_end = source_start + num;
		ret = ConvertUTF16toUTF8((const UTF16 **)&source_start, source_end, &target_start, target_end, strictConversion, &bytes);
		if (ret != conversionOK) {
			D("second call to ConvertUTF16toUTF8 failed (%d)", ret);
			if (ret == sourceExhausted) {
				D("Partial character in input");
			} else if (ret == targetExhausted) {
				D("target buffer exhausted");
			} else if (ret == sourceIllegal) {
				D("malformed/illegal source sequence");
			} else {
				D("unknown ConvertUTF16toUTF8 error");
			}
			return 1;
		}
		target_start = target_backup;
		source_start = source_backup;
		bad=fwrite(target_start, sizeof(UTF8),bytes,fp1);
		free(target_backup);
	}
	D("Bytes Written %d", bad);
	free(source_backup);
	return 0;
}


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
	D("Curl Init");
	if (! curl)
	return 1;
	D("Curl Init Done");
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
	char *token, *GALX;
	CURLcode status;
	int ret;
	char *src;
	static struct write_result response;
	char *params, *encoded_params;
	
	D("Stage 1 Login");
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
	
	GALX = NULL;
	src = response.memory;
	strtok(src,"\"");
	while((token=strtok(NULL,"\"")) != NULL) {
		if ((strstr(token, "GALX")) != NULL) {
			D("Matches GALX %s", token);
			strtok(NULL,"\"");
			GALX = strtok(NULL,"\"");
			break;
		} else
			GALX = NULL;
	}
	if (GALX == NULL) {
		D("Didnot Match, Can't find GALX");
		/* May be we are already logged in*/
		if (strcasestr(response.memory, ADSENSE_VERIFY_LOGIN_STRING) == NULL) {
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
				ret = 1000; 
				goto cleanup;
			} else {
				D("Login Successful\n");
				ret = 0;
			}
		} else {
				D("Where are we????");
				D("%s",response.memory);
				ret = 1000;
		}
		goto cleanup;
	} 
	D("Stage 2 Login");
	D("Extract %s\n",GALX);
	
	/* STAGE 2 Adsense Login */
	params = (char *) malloc(1000*sizeof(char));
	sprintf(params, "%s&GALX=%s&Email=%s&Passwd=%s", ADSENSE_LOGIN_URL_STAGE2_PARAMS, GALX, username, password);
	encoded_params = url_encode(params);
	
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
	FILE *fp, *fpreal;
	D("Gonna Fetch the Reports Now\n");
	response.memory = NULL;
	response.size = 0;
	fp = fopen(AFC_TEMP, "w+");
	fpreal = fopen(AFC_TODAYCSV,"w");
	curl_easy_setopt(curl, CURLOPT_URL, AFC_TODAY_REPORT_URL);
	curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
	curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
	status = curl_easy_perform(curl); //Pseudo statement to avoid getting the main adsense page
	fclose(fp);
	fp = fopen(AFC_TEMP,"w+");
	status = curl_easy_perform(curl);
	if(status != 0) {
		D("curl error: unable to send data to AFC_TODAY_REPORT_URL\n");
		D("%s\n", curl_easy_strerror(status));
		ret = status;
		goto cleanup;
	}
	fclose(fp);
	fp = fopen(AFC_TEMP, "r");
	status = convert_to_utf8(fp, fpreal);
	if(status != 0) {
		D("Conversion to UTF8 Failed");
		ret = 1;
	} else {
		ret = 0;
	}
	fclose(fpreal);
	fclose(fp);
	

	response.memory = NULL;
	response.size = 0;
	fp = fopen(AFC_TEMP, "w+");
	fpreal = fopen(AFC_YESTERDAYCSV,"w+");
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
	fp = fopen(AFC_TEMP, "r");
	status = convert_to_utf8(fp, fpreal);
	if(status != 0) {
		D("Conversion to UTF8 Failed");
		ret = 1;
	} else {
		ret = 0;
	}
	fclose(fpreal);
	fclose(fp);

	response.memory = NULL;
	response.size = 0;
	fp = fopen(AFC_TEMP, "w+");
	fpreal = fopen(AFC_LAST7DAYSCSV,"w+");
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
	fp = fopen(AFC_TEMP, "r");
	status = convert_to_utf8(fp, fpreal);
	if(status != 0) {
		D("Conversion to UTF8 Failed");
		ret = 1;
	} else {
		ret = 0;
	}
	fclose(fpreal);
	fclose(fp);

	response.memory = NULL;
	response.size = 0;
	fp = fopen(AFC_TEMP, "w+");
	fpreal = fopen(AFC_THISMONTHCSV,"w+");
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
	fp = fopen(AFC_TEMP, "r");
	status = convert_to_utf8(fp, fpreal);
	if(status != 0) {
		D("Conversion to UTF8 Failed");
		ret = 1;
	} else {
		ret = 0;
	}
	fclose(fpreal);
	fclose(fp);

	response.memory = NULL;
	response.size = 0;
	fp = fopen(AFC_TEMP, "w+");
	fpreal = fopen(AFC_LASTMONTHCSV,"w+");
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
	fp = fopen(AFC_TEMP, "r");
	status = convert_to_utf8(fp, fpreal);
	if(status != 0) {
		D("Conversion to UTF8 Failed");
		ret = 1;
	} else {
		ret = 0;
	}
	fclose(fpreal);
	fclose(fp);

	response.memory = NULL;
	response.size = 0;
	fp = fopen(AFC_TEMP, "w+");
	fpreal = fopen(AFC_ALLTIMECSV,"w+");
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
	fp = fopen(AFC_TEMP, "r");
	status = convert_to_utf8(fp, fpreal);
	if(status != 0) {
		D("Conversion to UTF8 Failed");
		ret = 1;
	} else {
		ret = 0;
	}
	fclose(fpreal);
	fclose(fp);

	ret = 0;
	cleanup:
	free(response.memory);
	return ret;
}
