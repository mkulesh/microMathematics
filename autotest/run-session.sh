#!/bin/csh
# C-shell script used to run microMathematics Plus in all configured emulators

echo Configured emulators:
${ANDROID_HOME}/tools/emulator -list-avds

echo APK to be tested: ${1}

./run-emulator.sh android_4.0.3 ${1}
./run-emulator.sh android_4.1.2 ${1}
#./run-emulator.sh android_4.2.2 ${1} - Skipped: SD does not more work in the new emulator
./run-emulator.sh android_4.3.1 ${1}
./run-emulator.sh android_4.4.2 ${1}
./run-emulator.sh android_5.0.1 ${1}
./run-emulator.sh android_5.1.1 ${1}
./run-emulator.sh android_6.0.0 ${1} -g
./run-emulator.sh android_7.0.0 ${1} -g
./run-emulator.sh android_7.1.1 ${1} -g
./run-emulator.sh android_8.0.0 ${1} -g
./run-emulator.sh android_8.1.0 ${1} -g

firefox *.html

exit 1
