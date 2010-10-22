#ifndef _ADSENSECLIENT_CURL_H
#define _ADSENSECLIENT_CURL_H

#include "curl/curl.h"
#include "curl/easy.h"


#define DEBUG 1

#if DEBUG
#include <android/log.h>
#  define  D(x...)  __android_log_print(ANDROID_LOG_INFO,"adsenseclient",x)
#else
#  define  D(...)  do {} while (0)
#endif

#define COOKIEFILE "/sdcard/humbug_adsense_client/cookies.txt"

struct write_result {
  char *memory;
  size_t size;
};

/* Messages */

#define ADSENSE_USERNAME_PASSWORD_INCORRECT "The username or password you entered is incorrect."
#define ADSENSE_VERIFY_LOGIN_STRING "<a href=\"/adsense/signout\">"

/* File Names */

#define AFC_CSV "/sdcard/humbug_adsense_client/afc.csv"
#define AFC_TODAYCSV "/sdcard/humbug_adsense_client/afc_today.csv"
#define AFC_YESTERDAYCSV "/sdcard/humbug_adsense_client/afc_yesterday.csv"
#define AFC_LAST7DAYSCSV "/sdcard/humbug_adsense_client/afc_last7days.csv"
#define AFC_THISMONTHCSV "/sdcard/humbug_adsense_client/afc_thismonth.csv"
#define AFC_LASTMONTHCSV "/sdcard/humbug_adsense_client/afc_lastmonth.csv"
#define AFC_ALLTIMECSV "/sdcard/humbug_adsense_client/afc_alltime.csv"
#define AFC_TEMP "/sdcard/humbug_adsense_client/afc_temp.csv"

/* URLS */

#define ADSENSE_LOGIN_URL "https://www.google.com/accounts/ServiceLoginBox?service=adsense&ltmpl=login&ifr=true&rm=hide&fpui=3&nui=15&alwf=true&passive=true&continue=https%3A%2F%2Fwww.google.com%2Fadsense%2Flogin-box-gaiaauth&followup=https%3A%2F%2Fwww.google.com%2Fadsense%2Flogin-box-gaiaauth&hl=en_US"
#define ADSENSE_LOGIN_URL_STAGE2 "https://www.google.com/accounts/ServiceLoginBoxAuth"
#define ADSENSE_LOGIN_URL_STAGE3 "https://www.google.com/accounts/CheckCookie?continue=https%3A%2F%2Fwww.google.com%2Fadsense%2Flogin-box-gaiaauth&followup=https%3A%2F%2Fwww.google.com%2Fadsense%2Flogin-box-gaiaauth&hl=en_US&service=adsense&ltmpl=login&chtml=LoginDoneHtml"
#define ADSENSE_LOGIN_URL_STAGE4 "https://www.google.com/adsense"

#define ADSENSE_LOGIN_URL_STAGE2_PARAMS "continue=https://www.google.com/adsense/login-box-gaiaauth&followup=https://www.google.com/adsense/login-box-gaiaauth&service=adsense&nui=15&fpui=3&ifr=true&rm=hide&ltmpl=login&hl=en_US&alwf=true"

#define AFC_TODAY_REPORT_URL "https://www.google.com/adsense/report/aggregate?product=afc&dateRange.dateRangeType=simple&dateRange.simpleDate=today&reportType=property&groupByPref=date&outputFormat=TSV_EXCEL&unitPref=page"

#define AFC_YESTERDAY_REPORT_URL "https://www.google.com/adsense/report/aggregate?product=afc&dateRange.dateRangeType=simple&dateRange.simpleDate=yesterday&reportType=property&groupByPref=date&outputFormat=TSV_EXCEL&unitPref=page"

#define AFC_LAST7DAYS_REPORT_URL "https://www.google.com/adsense/report/aggregate?product=afc&dateRange.dateRangeType=simple&dateRange.simpleDate=last7days&reportType=property&groupByPref=date&outputFormat=TSV_EXCEL&unitPref=page"

#define AFC_THISMONTH_REPORT_URL "https://www.google.com/adsense/report/aggregate?product=afc&dateRange.dateRangeType=simple&dateRange.simpleDate=thismonth&reportType=property&groupByPref=date&outputFormat=TSV_EXCEL&unitPref=page"

#define AFC_LASTMONTH_REPORT_URL "https://www.google.com/adsense/report/aggregate?product=afc&dateRange.dateRangeType=simple&dateRange.simpleDate=lastmonth&reportType=property&groupByPref=date&outputFormat=TSV_EXCEL&unitPref=page"

#define AFC_ALLTIME_REPORT_URL "https://www.google.com/adsense/report/aggregate?product=afc&dateRange.dateRangeType=simple&dateRange.simpleDate=alltime&reportType=property&groupByPref=date&outputFormat=TSV_EXCEL&unitPref=page"


/* RE */
static char *GmailRe = "input type=\"hidden\" name=\"GALX\" value=\"[^\"]*\"";

int curl_local_init(void);
int adsense_login(const char *, const char *);
int get_adsense_reports(void);
void process_adsense_reports(void);

CURL *curl;
extern CURL *curl;

#endif /* _ADSENSECLIENT_CURL_H */
