#include "adsensejni.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <regex.h>

#define LINESIZE 1000

#define DELIM "\t"

void create_report(FILE *fp, FILE *fpout, char *timeRange) {
	char *buf;
	int timeRangeWritten = 0;
	buf = (char *) malloc(LINESIZE*sizeof(char));
	while (fgets(buf, LINESIZE, fp) != NULL) {
		char *token;
		int written;
		
		int isDate = (strstr(buf, "Date") == NULL) && (strstr(buf, "Totals") == NULL) && (strstr(buf, "Averages") == NULL);
		
		if ( timeRangeWritten == 0  && isDate == 1) {
			char *timetoken;
			timetoken=(char *)malloc((strlen(timeRange)+10)*sizeof(char));
			sprintf(timetoken,"%s,",timeRange);
			written=fwrite(timetoken, sizeof(char), strlen(timetoken), fpout);
			free(timetoken);
			timeRangeWritten = 1;
		}
		if (strstr(buf, "Totals") != NULL) {
			strtok(buf,"\t");
			while((token = strtok( (char *) NULL, DELIM) ) != NULL ) {
				char *tokenupdate;
				tokenupdate=(char *)malloc((strlen(token)+10)*sizeof(char));
				if(strstr(token,"\n")) {
					written=fwrite(token, sizeof(char), strlen(token)-1, fpout);
				} else {
					sprintf(tokenupdate,"%s,",token);
					written=fwrite(tokenupdate, sizeof(char), strlen(tokenupdate), fpout);
				}
				free(tokenupdate);
			}
			fwrite(",",sizeof(char),1,fpout);
		}
		if (strstr(buf, "Averages") != NULL) {
			int i = 0;
			strtok(buf,"\t");
			while((token = strtok( (char *) NULL, DELIM) ) != NULL ) {
				i++;
				if ( i != 3 && i != 4 && i != 5 ) {
					continue;
				}
				char *tokenupdate;
				tokenupdate=(char *)malloc((strlen(token)+10)*sizeof(char));
				if( i == 3) {
					sprintf(tokenupdate,"%s,",token);
					written=fwrite(tokenupdate, sizeof(char), strlen(tokenupdate), fpout);
				} else if( i == 5) {
					written=fwrite(token, sizeof(char), strlen(token)-1, fpout);	
				} else {
					sprintf(tokenupdate,"%s,",token);
					written=fwrite(tokenupdate, sizeof(char), strlen(tokenupdate), fpout);
				}
				free(tokenupdate);
			}
			fwrite("\n",sizeof(char),1,fpout);
		}
	}
	free(buf);
}

void process_adsense_reports(void) {
	char afcfiles[6][1000]={AFC_TODAYCSV,AFC_YESTERDAYCSV,AFC_LAST7DAYSCSV,AFC_THISMONTHCSV,AFC_LASTMONTHCSV,AFC_ALLTIMECSV};
	char afctime[6][50]={"Today","Yesterday","Last 7 Days","This Month","Last Month","All Time"};
	int i;
	FILE *fp, *fpout;
	fpout = fopen(AFC_CSV,"w");
	for (i = 0; i < 6; i++) {
		D("Processing %s\n",afcfiles[i]);
		fp = fopen(afcfiles[i],"r");
		create_report(fp, fpout, afctime[i]);
		fclose(fp);
	}
	fclose(fpout);
}

