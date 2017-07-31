#!/bin/csh

setenv TOOLS_PATH /work/adt-eclipse/sdk/tools
setenv AVDS_PATH /work/android/avds
setenv EMU_PORT 5558

${TOOLS_PATH}/android create avd -t 1  -n android_2.3.3 -p ${AVDS_PATH}/android_2.3.3 -d 8 -c 200M -b x86 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 3  -n android_4.0.3 -p ${AVDS_PATH}/android_4.0.3 -d 8 -c 200M -b x86 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 4  -n android_4.1.2 -p ${AVDS_PATH}/android_4.1.2 -d 8 -c 200M -b x86 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 5  -n android_4.2.2 -p ${AVDS_PATH}/android_4.2.2 -d 8 -c 200M -b x86 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 6  -n android_4.3.1 -p ${AVDS_PATH}/android_4.3.1 -d 8 -c 200M -b x86 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 7  -n android_4.4.2 -p ${AVDS_PATH}/android_4.4.2 -d 8 -c 200M -b x86 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 8  -n android_5.0.1 -p ${AVDS_PATH}/android_5.0.1 -d 8 -c 200M -b x86_64 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 9  -n android_5.1.1 -p ${AVDS_PATH}/android_5.1.1 -d 8 -c 200M -b x86_64 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 10 -n android_6.0.0 -p ${AVDS_PATH}/android_6.0.0 -d 8 -c 200M -b x86_64 -s 1080x1920 -f
${TOOLS_PATH}/android create avd -t 11 -n android_7.0.0 -p ${AVDS_PATH}/android_7.0.0 -d 8 -c 200M -b x86_64 -s 1080x1920 -f
${TOOLS_PATH}/emulator -list-avds


#after creation, manually add in each .ini file the following parameters:
#hw.ramSize=512
#skin.dynamic=yes
#vm.heapSize=48
