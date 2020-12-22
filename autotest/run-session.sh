#!/bin/csh
# C-shell script used to run microMathematics Plus in all configured emulators

echo Configured emulators:
${ANDROID_HOME}/tools/emulator -list-avds

echo APK to be tested: ${1}

./run-emulator.sh android_4.0.4 ${1} /sdcard/Android/data
./run-emulator.sh android_4.1.2 ${1} /sdcard/Android/data
./run-emulator.sh android_4.2.2 ${1} /sdcard/Android/data
./run-emulator.sh android_4.3.1 ${1} /sdcard/Android/data
./run-emulator.sh android_4.4.2 ${1} /sdcard/Android/data
./run-emulator.sh android_5.0.1 ${1} /sdcard/Android/data
./run-emulator.sh android_5.1.1 ${1} /sdcard/Android/data
./run-emulator.sh android_6.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_7.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_7.1.1 ${1} /sdcard/Android/data -g
./run-emulator.sh android_8.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_8.1.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_9.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_10.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_11.0.0 ${1} /data/user/0 -g

firefox *.html

exit 1
