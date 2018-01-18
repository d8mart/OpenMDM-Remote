#!/bin/bash
set -e

PLATFORM_SRC_PATH=$1
WEBKEY_SRC_PATH=$2

for i in `ls "$PLATFORM_SRC_PATH" | grep android`
do
    cd "${PLATFORM_SRC_PATH}/${i}"
    pwd
    repo init -u https://android.googlesource.com/platform/manifest -b $i  --depth=1 --groups=all,-notdefault,-device,-darwin,-mips,-exynos5,-mako,-qcom,-qcom_wlan,-eclipse,-omap4
    repo sync --force-sync
    ln -s "${WEBKEY_SRC_PATH}/backend/webkeynative" external/
done
