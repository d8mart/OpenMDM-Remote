#protc version: 2.6
#protoc  --cpp_out=. --java_out=/home/pzoli/webkey/webkey_client/app/src/main/java wipc.proto
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
$(info PLATFOMR_VERSION is $(PLATFORM_VERSION))

LOCAL_SRC_FILES:= \
	webkey_utile.cc \
	webkey_auth.cc \
	updater.cc \
	wipc.pb.cc \
	screencap.cc \
	suinput.cc \
	touchinject.cc \
	keyinject.cc \
	buttoninject.cc \
	webkeynative.cc 

#http://developer.android.com/guide/topics/manifest/uses-sdk-element.html
ifeq ($(PLATFORM_VERSION), 4.2.2)
	LOCAL_SRC_FILES += flinger_function_17.cc
else ifeq ($(PLATFORM_VERSION), 4.3.1)
	LOCAL_SRC_FILES += flinger_function_18.cc
else ifeq ($(PLATFORM_VERSION), 4.4.2)
	LOCAL_SRC_FILES += flinger_function_19.cc
else ifeq ($(PLATFORM_VERSION), 4.4.3)
	LOCAL_SRC_FILES += flinger_function_19.cc
else ifeq ($(PLATFORM_VERSION), 4.4.4)
	LOCAL_SRC_FILES += flinger_function_19.cc
else ifeq ($(PLATFORM_VERSION), 5.0.2)
	LOCAL_SRC_FILES += flinger_function_21.cc
else ifeq ($(PLATFORM_VERSION), 5.1.1)
	LOCAL_SRC_FILES += flinger_function_22.cc
else ifeq ($(PLATFORM_VERSION), 6.0)
	LOCAL_SRC_FILES += flinger_function_23.cc
else ifeq ($(PLATFORM_VERSION), 7.0)
	LOCAL_SRC_FILES += flinger_function_24.cc
endif


LOCAL_MODULE:= webkeynative

LOCAL_MODULE_TAGS := optional

LOCAL_LDLIBS := -llog

LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libbinder \
    libskia \
    libui \
    libgui


LOCAL_C_INCLUDES += \
	external/skia/include/core \
	external/skia/include/effects \
	external/skia/include/images \
	external/skia/src/ports \
	external/skia/include/utils \
	external/stlport/stlport \
	bionic 

ifeq ($(PLATFORM_VERSION), 6.0)
	#6.0 felett nem kellene sajat protobuf lib-et hasznalunk
	LOCAL_SHARED_LIBRARIES += libprotobuf-cpp-2.6.0-full
	LOCAL_C_INCLUDES += external/webkeynative/protobuf_static/src
else ifeq ($(PLATFORM_VERSION), 7.0)
	LOCAL_SHARED_LIBRARIES += libprotobuf-cpp-2.6.0-full
	LOCAL_C_INCLUDES += external/webkeynative/protobuf_static/src
else
	LOCAL_STATIC_LIBRARIES := libprotobuf-cpp-2.6.0-full
	LOCAL_C_INCLUDES += external/webkeynative/protobuf_static/src

	LOCAL_SHARED_LIBRARIES += libstlport
endif

LOCAL_CPP_EXTENSION := .cc
#LOCAL_CFLAGS := -Wall -frtti -Wunreachable-code
LOCAL_CFLAGS += -O2 -DNDEBUG -DGOOGLE_PROTOBUF_NO_RTTI
APP_OPTIM := release

ifeq ($(PLATFORM_VERSION), 5.1)
    LOCAL_CFLAGS   += -DNEWSKIA
else ifeq ($(PLATFORM_VERSION), 5.0)
    LOCAL_CFLAGS   += -DNEWSKIA
else ifeq ($(PLATFORM_VERSION), 5.1.1)
    LOCAL_CFLAGS   += -DNEWSKIA
else ifeq ($(PLATFORM_VERSION), 5.0.2)
    LOCAL_CFLAGS   += -DNEWSKIA
else ifeq ($(PLATFORM_VERSION), 6.0)
    LOCAL_CFLAGS   += -DNEWSKIA
else ifeq ($(PLATFORM_VERSION), 7.0)
    LOCAL_CFLAGS   += -DNEWSKIA
endif


include $(BUILD_EXECUTABLE)
include $(call all-makefiles-under,$(LOCAL_PATH))
