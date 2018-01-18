#!/bin/bash

ASSETSPATH="/home/webkey/harbor/assets/versions"
SRCPATH="/home/webkey/webkey_client"
cd $SRCPATH
git pull
online_tags=$(git tag -l | sed 's/rel_//')
exist_tags=$(ls $ASSETSPATH)

is_exist(){
    for i in $exist_tags
    do
        if [ "$1" = "$i" ]
        then
            echo -n "1";
            return;
        fi
    done
    echo -n "0";
}

for tag in $online_tags
do
    if [ "$(is_exist $tag)" == 1 ]
    then 
	echo "Already exist: $i"
    else
	echo "New tag. Will copy... $tag"
        git checkout tags/rel_$tag
	    mkdir $ASSETSPATH/$tag
	    cp -a $SRCPATH/app/src/main/assets/webkit/* $ASSETSPATH/$tag/
    fi
done
