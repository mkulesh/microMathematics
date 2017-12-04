#!/bin/sh

TOOLS_PATH=/work/android/sdk/tools
AVDS_PATH=/work/android/avds

createAVD()
{
echo ""
echo "----------------------------------------------------------------------------------"
echo "Creating $1($2)"
echo 'no' | ${TOOLS_PATH}/bin/avdmanager create avd --name $1 --path ${AVDS_PATH}/$1 --sdcard 200M --force --package $2
# after creation, manually add in each .ini file the following parameters:
cat device.cfg >> ${AVDS_PATH}/$1/config.ini
}

createAVD android_2.3.3 'system-images;android-10;google_apis;x86'
createAVD android_4.0.4 'system-images;android-15;google_apis;x86'
createAVD android_4.1.2 'system-images;android-16;google_apis;x86'
createAVD android_4.2.2 'system-images;android-17;google_apis;x86'
createAVD android_4.3.1 'system-images;android-18;google_apis;x86'
createAVD android_4.4.2 'system-images;android-19;google_apis;x86'
createAVD android_5.0.1 'system-images;android-21;default;x86_64'
createAVD android_5.1.1 'system-images;android-22;default;x86_64'
createAVD android_6.0.0 'system-images;android-23;default;x86_64'
createAVD android_7.0.0 'system-images;android-24;default;x86_64'
createAVD android_7.1.1 'system-images;android-25;google_apis;x86'
createAVD android_8.0.0 'system-images;android-26;google_apis;x86'

echo ""
echo "----------------------------------------------------------------------------------"
${TOOLS_PATH}/emulator -list-avds

