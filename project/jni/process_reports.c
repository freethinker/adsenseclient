#include "adsensejni.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <regex.h>

#define LINESIZE 1000


void create_report(FILE *fp, FILE *fpout) {
	char *buf;
	buf = (char *) malloc(LINESIZE*sizeof(char));
	while (fgets(buf, LINESIZE, fp) != NULL) {
		char *token;
		int written;
		if (strstr(buf, "Totals") != NULL) {
			strtok(buf,"\t");
			while((token = strtok( (char *) NULL, "\t") ) != NULL ) {
				char *tokenupdate;
				tokenupdate=(char *)malloc((sizeof(token)+1)*sizeof(char));
				if(strstr(token,"\n")) {
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
	int i;
	FILE *fp, *fpout;
	fpout = fopen(AFC_CSV,"w");
	for (i = 0; i < 6; i++) {
		D("Processing %s\n",afcfiles[i]);
		fp = fopen(afcfiles[i],"r");
		create_report(fp, fpout);
		fclose(fp);
	}
	fclose(fpout);
}

