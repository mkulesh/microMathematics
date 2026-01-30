#!/bin/csh
# C-shell script used to run microMathematics Plus in all configured emulators

echo APK to be tested: ${1}

./run-emulator.sh android_4.0.4 ${1} /sdcard/Android/data
./run-emulator.sh android_4.1.2 ${1} /sdcard/Android/data
./run-emulator.sh android_4.2.2 ${1} /sdcard/Android/data
./run-emulator.sh android_4.3.1 ${1} /sdcard/Android/data
./run-emulator.sh android_4.4.2 ${1} /sdcard/Android/data
./run-emulator.sh android_5.0.2 ${1} /sdcard/Android/data
./run-emulator.sh android_5.1.1 ${1} /sdcard/Android/data
./run-emulator.sh android_6.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_7.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_7.1.1 ${1} /sdcard/Android/data -g
./run-emulator.sh android_8.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_8.1.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_9.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_10.0.0 ${1} /sdcard/Android/data -g
./run-emulator.sh android_11.0.0 ${1} /data/user/0 -g -delay-adb
./run-emulator.sh android_12.0.0 ${1} /data/user/0 -g -delay-adb
./run-emulator.sh android_12.1.0 ${1} /data/user/0 -g -delay-adb
./run-emulator.sh android_13.0.0 ${1} /data/user/0 -g -delay-adb
./run-emulator.sh android_14.0.0 ${1} /data/user/0 -g -delay-adb
./run-emulator.sh android_15.0.0 ${1} /data/user/0 -g -delay-adb
./run-emulator.sh android_16.0.0 ${1} /data/user/0 -g -delay-adb

# Check for failures and report status
set failures = `grep FAILED *.html`
if ("$failures" == "") then
    echo ">>>>> Test PASSED."
else
    echo ">>>>> Test FAILED:"
    echo "$failures"
endif

# Open report files in Firefox
if (`uname` == "Darwin") then
    # macOS
    open -a Firefox *.html
else
    # Assuming Linux
    firefox *.html
endif

exit 1
