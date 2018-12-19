#!/bin/csh
# C-shell script used to run microMathematics Plus in a given emulator

setenv EMU_NAME ${1}
setenv APK_DATA /sdcard/Android/data

setenv APK_PACK com.mkulesh.micromath
setenv APK_ACT MainActivity
setenv APK_FILE ${2}

setenv TOOLS_PATH ${ANDROID_HOME}/tools
setenv EMU_PORT 5558
setenv DEVICE_NAME emulator-${EMU_PORT}
setenv ADB_CMD "adb -s ${DEVICE_NAME}"

echo ================================================================================
echo Starting ${EMU_NAME} on port ${EMU_PORT}...
echo ================================================================================
adb kill-server
adb start-server
${ANDROID_HOME}/emulator/emulator -avd ${EMU_NAME} -no-boot-anim -port ${EMU_PORT} &

# waiting until boot_completed
while ("`${ADB_CMD} wait-for-device shell getprop sys.boot_completed | tr -d '\r' `" != "1")
  sleep 1
end
sleep 1

# actual device
adb devices
adb root

echo Uninstalling ${APK_PACK}...
${ADB_CMD} uninstall ${APK_PACK}
${ADB_CMD} shell pm list packages | grep ${APK_PACK}

echo Installing ${APK_FILE}...
${ADB_CMD} install -r ${3} ${APK_FILE}
${ADB_CMD} shell pm list packages | grep ${APK_PACK}

echo Starting app...
${ADB_CMD} push ./autotest.cfg ${APK_DATA}/${APK_PACK}/files/autotest.cfg
${ADB_CMD} shell am start -S -W -n ${APK_PACK}/.${APK_ACT}

echo Waiting until app is finished...
while ("`${ADB_CMD} shell dumpsys activity | grep top-activity | grep -c ${APK_PACK} | tr -d '\r' `" != "0")
  sleep 1
end
sleep 1

echo Collect results
${ADB_CMD} pull ${APK_DATA}/${APK_PACK}/files/autotest.html ${1}.html
${ADB_CMD} uninstall ${APK_PACK}

echo Stopping emulator...
${ADB_CMD} -e emu kill
sleep 5
killall qemu-system-i386
adb kill-server

exit 1
