#!/bin/bash

adb help &> /dev/null
if [ $? -ne 0 ]
then
    echo "adb command is missing"
    exit 1
fi

if [ -z "$1" ]
then
    echo "No session argument given"
    exit 1
fi

adb shell <<EOF
export LD_LIBRARY_PATH=/data/data/com.webkey/files
cp /data/data/com.webkey/files/webkeynative_update /data/local/tmp/webkeynative
chmod 700 /data/local/tmp/webkeynative
nohup /data/local/tmp/webkeynative -l /data/local/tmp/webkeynative.lock -s $1 -u /data/data/com.webkey/files/webkeynative_update > /dev/null 2>&1& 
exit
EOF
