#!/bin/sh

TOOLS_PATH=/work/adt-eclipse/sdk/tools
AVDS_PATH=/work/android/avds
EMU_PORT=5558

createAVD()
{
echo "Creating $2($3)..."
${TOOLS_PATH}/android create avd -t $1 -n $2 -p ${AVDS_PATH}/$2 -d 8 -c 200M -b $3 -s 1080x1920 -f
# after creation, manually add in each .ini file the following parameters:
# hw.ramSize=512
# skin.dynamic=yes
# vm.heapSize=48
echo "hw.ramSize=512" >> ${AVDS_PATH}/$2/config.ini
echo "skin.dynamic=yes" >> ${AVDS_PATH}/$2/config.ini
echo "vm.heapSize=48" >> ${AVDS_PATH}/$2/config.ini
}

createAVD 1  android_2.3.3 x86
createAVD 3  android_4.0.3 x86
createAVD 4  android_4.1.2 x86
createAVD 5  android_4.2.2 x86
createAVD 6  android_4.3.1 x86
createAVD 7  android_4.4.2 x86
createAVD 8  android_5.0.1 x86_64
createAVD 9  android_5.1.1 x86_64
createAVD 10 android_6.0.0 x86_64
createAVD 10 DocExport_6.0 x86_64
createAVD 11 android_7.0.0 x86_64
createAVD 12 android_7.1.1 google_apis/x86_64

${TOOLS_PATH}/emulator -list-avds

