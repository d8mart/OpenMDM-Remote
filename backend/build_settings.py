#!/usr/bin/python
import build_config as config
import os

BACKENDROOT = config.BACKENDROOT
OUT_PATH = config.OUT_PATH
VERSION_FILE = os.path.join(config.OUT_PATH, "version_code.txt")

COMMON_REPOS = [
    'bionic',
    'build',
    'external/clang',
    'external/compiler-rt',
    'external/expat',
    'external/freetype',
    'external/giflib',
    'external/liblzf',
    'external/libpng',
    'external/llvm',
    'external/protobuf',
    'external/skia',
    'external/webp',
    'external/zlib',
    'frameworks/native',
    'hardware/libhardware',
    'prebuilts/ndk',
    'prebuilts/sdk',
    'system/core'
]

PLATFORM_VERSIONS = {
    #http://source.android.com/source/build-numbers.html
    "4.2.2": {
	"build": "JDQ39E",
        "branch": "android-4.2.2_r1.2",
        "java": config.JAVA_6_PATH,
        "api": "17",
        "wtag": "", 
        "arch": {"arm": "full-eng", "x86": "full_x86-eng"},
        "repos": [
            'frameworks/opt/emoji',
            'external/harfbuzz',
            'external/safe-iop',
            'external/stlport',
            'frameworks/compile/libbcc',
            'frameworks/compile/mclinker',
            'prebuilts/gcc/linux-x86/host/x86_64-linux-glibc2.7-4.6',
            'prebuilts/gcc/linux-x86/host/i686-linux-glibc2.7-4.6',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.6',
            'prebuilts/gcc/linux-x86/x86/i686-linux-android-4.6',
            'prebuilts/misc',
            'prebuilts/tools',
            'external/gcc-demangle',
            'external/jpeg',
            'libcore'            
            ] + COMMON_REPOS
        },
    "4.3.1": {
        "build": "JLS36I",
        "branch": "android-4.3.1_r1",
        "java": config.JAVA_6_PATH,
        "api": "18",
        "wtag": "",
        "arch": {"arm": "aosp_arm-eng", "x86": "aosp_x86-eng"},
        "repos": [
            'external/harfbuzz',
            'external/harfbuzz_ng',
            'external/gcc-demangle',
            'external/stlport',
            'external/safe-iop',
            'external/jpeg',
            'frameworks/opt/emoji',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.7',
            'prebuilts/gcc/linux-x86/x86/i686-linux-android-4.7'
            ] + COMMON_REPOS
        },
    "4.4.2": {
        "build": "KOT49H",
        "branch": "android-4.4.2_r1",
        "java": config.JAVA_6_PATH,
        "api": "19",
        "wtag": "",
        "arch": {"arm": "aosp_arm-eng", "x86": "aosp_x86-eng"},
        "repos": [
            'abi/cpp',
            'external/gcc-demangle',
            'external/harfbuzz',
            'external/harfbuzz_ng',
            'external/icu4c',
            'external/sfntly',
            'external/stlport',
            'external/jpeg',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.7',
            'prebuilts/gcc/linux-x86/x86/i686-linux-android-4.7'
            ] + COMMON_REPOS
        },
    "4.4.3": {
        "build": "KTU84L",
        "branch": "android-4.4.3_r1",
        "java": config.JAVA_6_PATH,
        "api": "19",
        "wtag": "_4.4.3",
        "arch": {"arm": "aosp_arm-eng", "x86": "aosp_x86-eng"},
        "repos": [
            'abi/cpp',
            'external/gcc-demangle',
            'external/harfbuzz',
            'external/harfbuzz_ng',
            'external/icu4c',
            'external/marisa-trie',
            'external/sfntly',
            'external/srec',
            'external/stlport',
            'external/jpeg',
            'libcore',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.7',
            'prebuilts/gcc/linux-x86/host/x86_64-linux-glibc2.7-4.6',
            'prebuilts/gcc/linux-x86/x86/i686-linux-android-4.7'
            ] + COMMON_REPOS
        },
    "5.0.2": {
        "build": "LRX22L",
        "branch": "android-5.0.2_r3",
        "java": config.JAVA_7_PATH,
        "api": "21",
        "wtag": "",
        "arch": {"arm": "aosp_arm-eng", "x86": "aosp_x86-eng"},
        "repos": [
            'abi/cpp',
            'external/gcc-demangle',
            'external/harfbuzz_ng',
            'external/icu',
            'external/jemalloc',
            'external/libcxx',
            'external/libunwind',
            'external/sfntly',
            'external/stlport',
            'external/jpeg',
            'frameworks/compile/libbcc',
            'frameworks/compile/slang',
            'libcore',
            'libnativehelper',
            'prebuilts/clang/linux-x86/host/3.5',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.8',
            'prebuilts/gcc/linux-x86/x86/x86_64-linux-android-4.8',
            'prebuilts/misc'
            ] + COMMON_REPOS
        },
    "5.1.1": {
        "build": "LMY47Z",
        "branch": "android-5.1.1_r4",
        "java": config.JAVA_7_PATH,
        "api": "22",
        "wtag": "",
        "arch": {"arm": "aosp_arm-eng", "x86": "aosp_x86-eng"},
        "repos": [
            'abi/cpp',
            'external/gcc-demangle',
            'external/harfbuzz_ng',
            'external/icu',
            'external/jemalloc',
            'external/libcxx',
            'external/libunwind',
            'external/sfntly',
            'external/stlport',
            'external/jpeg',
            'frameworks/compile/libbcc',
            'frameworks/compile/slang',
            'libcore',
            'libnativehelper',
            'prebuilts/clang/linux-x86/host/3.5',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.8',
            'prebuilts/gcc/linux-x86/host/x86_64-linux-glibc2.11-4.8',
            'prebuilts/gcc/linux-x86/x86/x86_64-linux-android-4.8',
            'prebuilts/misc'
            ] + COMMON_REPOS
        },
    "6.0.0": {
        "build": "MRA58N",
        "branch": "android-6.0.0_r2",
        "java": config.JAVA_7_PATH,
        "api": "23",
        "wtag": "",
        "arch": {"arm": "aosp_arm-eng", "x86": "aosp_x86-eng"},
        "repos": [
            'external/icu',
            'external/jemalloc',
            'external/libcxx',
            'external/libcxxabi',
            'external/libunwind',
            'external/safe-iop',
            'external/sfntly',
            'external/jpeg',
            'frameworks/compile/slang',
            'libnativehelper',
            'prebuilts/clang/linux-x86/host/3.6',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.9',
            'prebuilts/gcc/linux-x86/host/x86_64-linux-glibc2.15-4.8',
            'prebuilts/gcc/linux-x86/x86/x86_64-linux-android-4.9',
            'prebuilts/misc'
            ] + COMMON_REPOS
        },
    "7.0.0": {
        "build": "NRD90M",
        "branch": "android-7.0.0_r1",
        "java": config.JAVA_8_PATH,
        "api": "24",
        "wtag": "",
        "arch": {"arm": "aosp_arm-eng", "x86": "aosp_x86-eng"},
        "repos": [
            'external/icu',
            'external/jemalloc',
            'external/libcxx',
            'external/libcxxabi',
            'external/libunwind',
            'external/safe-iop',
            'external/sfntly',
            'frameworks/compile/slang',
            'libnativehelper',
            'prebuilts/clang/linux-x86/host/3.6',
            'prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.9',
            'prebuilts/gcc/linux-x86/host/x86_64-linux-glibc2.15-4.8',
            'prebuilts/gcc/linux-x86/x86/x86_64-linux-android-4.9',
            'prebuilts/misc',
            'platform/build/kati',
            'platform/prebuilts/clang/host/linux-x86',
            'platform/libcore',
            'platform/external/junit',
            'platform/prebuilts/ninja/linux-x86',
            'frameworks/base',
            'platform/external/lzma', ''' it may need to screencap only '''
            'platform/external/libunwind_llvm', ''' it may need to screencap only '''
            'platform/external/libjpeg-turbo',
            'platform/external/dng_sdk'
            'platform/external/piex'
            ] + COMMON_REPOS
        }
}

