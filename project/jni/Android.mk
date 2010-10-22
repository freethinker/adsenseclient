LOCAL_PATH:= $(call my-dir)
OPENSSL_INCLUDE_DIR := /home/pratik/work/android/droid/external/openssl/include

common_CFLAGS := -Wpointer-arith -Wwrite-strings -Wunused -Winline -Wnested-externs -Wmissing-declarations -Wmissing-prototypes -Wno-long-long -Wfloat-equal -Wno-multichar -Wsign-compare -Wno-format-nonliteral -Wendif-labels -Wstrict-prototypes -Wdeclaration-after-statement -Wno-system-headers -DHAVE_CONFIG_H

#########################
# Build the mxml library
include $(CLEAR_VARS)
MXML_HEADERS := \
	config.h \
	mxml.h \
	mxml-private.h

MXML_SOURCES := \
	mxml-attr.c \
	mxmldoc.c \
	mxml-entity.c \
	mxml-file.c \
	mxml-index.c \
	mxml-node.c \
	mxml-private.c \
	mxml-search.c \
	mxml-set.c \
	mxml-string.c \
	testmxml.c 

	


LOCAL_SRC_FILES := $(addprefix mxml/,$(MXML_SOURCES))
LOCAL_MODULE:= libmxml
include $(BUILD_STATIC_LIBRARY)

#########################
# Build the libcurl library

include $(CLEAR_VARS)
include $(LOCAL_PATH)/lib/Makefile.inc
CURL_HEADERS := \
	curlbuild.h \
	curl.h \
	curlrules.h \
	curlver.h \
	easy.h \
	mprintf.h \
	multi.h \
	stdcheaders.h \
	typecheck-gcc.h \
	types.h

LOCAL_SRC_FILES := $(addprefix lib/,$(CSOURCES))
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/ $(OPENSSL_INCLUDE_DIR)
LOCAL_CFLAGS += $(common_CFLAGS)

LOCAL_COPY_HEADERS_TO := libcurl/curl
LOCAL_COPY_HEADERS := $(addprefix include/curl/,$(CURL_HEADERS))

LOCAL_MODULE:= libcurl

# Copy the licence to a place where Android will find it.
# Actually, this doesn't quite work because the build system searches
# for NOTICE files before it gets to this point, so it will only be seen
# on subsequent builds.
ALL_PREBUILT += $(LOCAL_PATH)/NOTICE
$(LOCAL_PATH)/NOTICE: $(LOCAL_PATH)/COPYING | $(ACP)
	$(copy-file-to-target)

include $(BUILD_STATIC_LIBRARY)

#adsensejni
###########

include $(CLEAR_VARS)

LOCAL_MODULE := adsensejni
LOCAL_SRC_FILES := adsensejni.c curl.c urlcode.c ConvertUTF.c process_reports.c
LOCAL_STATIC_LIBRARIES := libcurl libmxml
#LOCAL_C_INCLUDES += adsensejni.h urlcode.h $(LOCAL_PATH)/curl/include $(OPENSSL_INCLUDE_DIR)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/curl/include $(OPENSSL_INCLUDE_DIR)
LOCAL_LDLIBS := -lz -lcrypto -lssl -llog
#LOCAL_LDLIBS := -lz 
include $(BUILD_SHARED_LIBRARY) 
