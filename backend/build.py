#!/usr/bin/python
import getopt, sys
import os
import re
import time
import shutil
import commands
import subprocess
import logging
import zipfile
import tarfile
import errno
import build_config as config
import build_settings as settings

ARCHES = {
        "arm": {
            "compiledir": "/out/target/product/generic",
            "out_path": os.path.join(settings.OUT_PATH,"arm")
            },
        "x86": {
            "compiledir": "/out/target/product/generic_x86",
            "out_path": os.path.join(settings.OUT_PATH,"x86")
            }
        }


def prepare_output_dirs():
    try:
        os.mkdir(settings.OUT_PATH)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise

    try:
        for a in ARCHES:
            os.mkdir(ARCHES[a]["out_path"])
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise

def get_branch_by_build(build):
    for i in settings.PLATFORM_VERSIONS:
        if settings.PLATFORM_VERSIONS[i]["build"] == build:
            return settings.PLATFORM_VERSIONS[i]["branch"]

    return None

def copy_artifact(platform_version, arch):
    arch = ARCHES[arch]
    platform = settings.PLATFORM_VERSIONS[platform_version]

    src_path = settings.BACKENDROOT+"/"+platform["branch"]+arch["compiledir"]+"/system/bin/webkeynative"
    src_symbol_path = settings.BACKENDROOT+"/"+platform["branch"]+arch["compiledir"]+"/symbols/system/bin/webkeynative"

    shutil.copyfile(src_path, arch["out_path"]+"/webkeynative_"+platform["api"]+platform["wtag"])
    shutil.copyfile(src_symbol_path, arch["out_path"]+"/webkeynative_symbol_"+platform["api"]+platform["wtag"])

    logging.info("Copy ARM artifact: "+arch["out_path"]+"/webkeynative_"+platform["api"]+platform["wtag"])

def build_arch(platform_version, arch):
    p = settings.PLATFORM_VERSIONS[platform_version]

    logging.info("Start building: API: %s, JDK: %s, tag: %s, %s", p["api"], p["java"], p["branch"], arch)

    cmd = "cd "+settings.BACKENDROOT+"/"+p["branch"]+"; . build/envsetup.sh; lunch "+p["arch"][arch]+" && PATH="+p["java"]+"/bin:"+p["java"]+"/jre/bin:$PATH; pwd; make -j2 webkeynative"
    #cmd = "cd "+settings.BACKENDROOT+"/"+p["branch"]+"; PATH="+p["java"]+"/bin:"+p["java"]+"/jre/bin:$PATH; pwd; make -j4 webkeynative"
    logging.info(cmd)
    process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,  executable='/bin/bash')

    # Poll process for new output until finished
    while True:
        nextline = process.stdout.readline()
        if nextline == '' and process.poll() != None:
            break
        sys.stdout.write(nextline)
        sys.stdout.flush()

    output = process.communicate()[0]
    exitCode = process.returncode
    
    return exitCode

def untar(platform):
    branch = platform['branch']
    mytarfile = os.path.join(settings.BACKENDROOT, branch)+".tar.gz"
    if os.path.exists(mytarfile):
        logging.info("Untar android repo: %s", mytarfile)
        tar = tarfile.open(mytarfile)
        tar.extractall(settings.BACKENDROOT)
        tar.close()

def create_symlink(platform):
    branch = platform['branch']
    webkey_native_src_path = os.path.join(os.getcwd(), 'webkeynative')
    symlink_path = os.path.join(settings.BACKENDROOT, branch)+'/external/webkeynative'
    logging.info("Create new symlink: %s", symlink_path)

    if os.path.exists(symlink_path):
        os.remove(symlink_path)

    os.symlink(webkey_native_src_path, symlink_path)


def delete_android_repo(platform):
    branch = platform['branch']
    target = os.path.join(settings.BACKENDROOT, branch)
    mytar = target+".tar.gz"
    if os.path.exists(mytar):
        shutil.rmtree(target)

def build_platform(platform_version):
    platform = settings.PLATFORM_VERSIONS[platform_version]
    build_result = 0

    untar(platform)
    create_symlink(platform)
    for i, arch in enumerate(ARCHES):
        logging.info("Starting to build: %s, %s", platform_version, arch)
        build_result = build_arch(platform_version, arch)
        if build_result == 0:
            copy_artifact(platform_version, arch)
            logging.info("Build done: %s, %s", platform_version, arch)
        else:
            logging.info("Failed to build: %s, %s", platform_version, arch)

    delete_android_repo(platform)
    return build_result

def clean_output_dirs():
    if os.path.exists(settings.OUT_PATH): 
        shutil.rmtree(settings.OUT_PATH)
        logging.info("clean the output folder: %s", settings.OUT_PATH)

    prepare_output_dirs()    

def compile_all():
    logging.info("start building to all platforms")
    res = 0

    for k in settings.PLATFORM_VERSIONS:
        res = build_platform(k)
        if res != 0:
            break
    return res

def repo_sync(platform):
    out_path = os.path.join(settings.BACKENDROOT,platform["branch"])

    for repo in platform["repos"]:
        cmd = "cd "+out_path+"; repo sync " + repo
        process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,  executable='/bin/bash')

        while True:
            nextline = process.stdout.readline()
            if nextline == '' and process.poll() != None:
                break
            sys.stdout.write(nextline)
            sys.stdout.flush()

        output = process.communicate()[0]

        exitCode = process.returncode
        if exitCode != 0:
            return exitCode
    return 0

def prepare_repo(platform):
    out_path = os.path.join(settings.BACKENDROOT,platform["branch"])

    try:
        os.mkdir(out_path)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise

    cmd = "cd "+out_path+"; repo init -u https://android.googlesource.com/platform/manifest -b" + platform["branch"] + "  --depth=1"
    process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,  executable='/bin/bash')

    while True:
        nextline = process.stdout.readline()
        if nextline == '' and process.poll() != None:
            break
        sys.stdout.write(nextline)
        sys.stdout.flush()

    output = process.communicate()[0]
    return process.returncode

def sync_all_repo():
    for k in settings.PLATFORM_VERSIONS:
	p = settings.PLATFORM_VERSIONS[k]
    	logging.info("Start repo sync: %s", p["branch"])

        res = prepare_repo(p)
        if res != 0:
            logging.info("Failed to init repo")
	    return res

	res = repo_sync(p)
        if res != 0:
            logging.info("Failed to run repo sync")
	    return res

def get_webkey_vcode():
    d = {}
    with open("../app/version.properties") as f:
        for line in f:
            try:
                key, val = line.split("=",1)
                d[key] = val 
            except:
                pass
    return d['VERSION_CODE']
    

def set_version():
    version = get_webkey_vcode()
    f = open('webkeynative/version.h', 'w')
    f.write('#define VERSION_CODE '+version)
    f.close()

    f = open(settings.VERSION_FILE, 'w')
    f.write(version)
    f.close()

def usage():
    print 'Usage: build.sh [-h] [-l] [-s] [-b api level]\n' \
        'Webkey backend\'s source code manager\n\n'\
        'where:\n'\
    '\t-h  Show this help text.\n'\
    '\t-a  Build for all platform.\n'\
    '\t-s  Sync all repos.\n'\
    '\t-b  Build the given version.\n'\
    '\t    Available versions:'
    
    for p in settings.PLATFORM_VERSIONS:
        print "\t\t"+p

def main():
    logging.basicConfig(format='%(asctime)s %(message)s', level=logging.DEBUG)
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hb:sa")
    except getopt.GetoptError as err:
        print str(err)
        sys.exit(2)

    prepare_output_dirs()

    for opt, arg in opts:
        if opt == "-h":
            usage()
            sys.exit(0)
        if opt == "-b":
            set_version()
            err = build_platform(arg)
            sys.exit(err)
	if opt == "-s":
	    sys.exit(sync_all_repo())
        elif opt in ("-a"):
            clean_output_dirs()
            set_version()
            err = compile_all()
            sys.exit(err)
        else:
            assert False, "unhandled option"
    usage()
    sys.exit(1)

if __name__ == "__main__":
    main()
