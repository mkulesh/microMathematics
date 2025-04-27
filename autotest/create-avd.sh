#!/bin/sh

TOOLS_PATH=/work/android/sdk/cmdline-tools/latest/bin
AVDS_PATH=/work/android/avds
DEVICE_NAME="pixel_xl"

createAVD()
{
echo ""
echo "----------------------------------------------------------------------------------"
echo "Creating $1($2)"
${TOOLS_PATH}/avdmanager create avd --name $1 --path ${AVDS_PATH}/$1 --sdcard 256M --force --package $2 --device ${DEVICE_NAME}
echo "hw.ramSize=2048" >> ${AVDS_PATH}/$1/config.ini
echo "skin.path=_no_skin" >> ${AVDS_PATH}/$1/config.ini
sed -i 's/showDeviceFrame=yes/showDeviceFrame=no/g' ${AVDS_PATH}/$1/config.ini
sed -i 's/hw.keyboard=no/hw.keyboard=yes/g' ${AVDS_PATH}/$1/config.ini
sed -i 's/hw.gpu.enabled=no/hw.gpu.enabled=yes/g' ${AVDS_PATH}/$1/config.ini
sed -i 's/hw.gpu.mode=auto/hw.gpu.mode=host/g' ${AVDS_PATH}/$1/config.ini
sed -i 's/fastboot.forceColdBoot=no/fastboot.forceColdBoot=yes/g' ${AVDS_PATH}/$1/config.ini
sed -i 's/fastboot.forceFastBoot=yes/fastboot.forceFastBoot=no/g' ${AVDS_PATH}/$1/config.ini
}

mkdir -p ${AVDS_PATH}
createAVD android_4.0.4  'system-images;android-15;google_apis;x86'
createAVD android_4.1.2  'system-images;android-16;google_apis;x86'
createAVD android_4.2.2  'system-images;android-17;google_apis;x86'
createAVD android_4.3.1  'system-images;android-18;google_apis;x86'
createAVD android_4.4.2  'system-images;android-19;google_apis;x86'
createAVD android_5.0.2  'system-images;android-21;google_apis;x86_64'
createAVD android_5.1.1  'system-images;android-22;google_apis;x86_64'
createAVD android_6.0.0  'system-images;android-23;google_apis;x86_64'
createAVD android_7.0.0  'system-images;android-24;google_apis;x86_64'
createAVD android_7.1.1  'system-images;android-25;google_apis;x86_64'
createAVD android_8.0.0  'system-images;android-26;google_apis;x86_64'
createAVD android_8.1.0  'system-images;android-27;google_apis;x86'
createAVD android_9.0.0  'system-images;android-28;default;x86_64'
createAVD android_10.0.0 'system-images;android-29;google_apis;x86_64'
createAVD android_11.0.0 'system-images;android-30;google_apis;x86_64'
createAVD android_12.0.0 'system-images;android-31;google_apis;x86_64'
createAVD android_12.1.0 'system-images;android-32;google_apis;x86_64'
createAVD android_13.0.0 'system-images;android-33;google_apis;x86_64'
createAVD android_14.0.0 'system-images;android-34;google_apis;x86_64'
createAVD android_15.0.0 'system-images;android-35;google_apis;x86_64'
createAVD android_16.0.0 'system-images;android-36;google_apis;x86_64'
