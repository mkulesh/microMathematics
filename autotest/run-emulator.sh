#!/bin/csh
# C-shell script used to run microMathematics Plus in a given emulator

setenv EMU_NAME ${1}

setenv APK_PACK com.mkulesh.micromath.plus
setenv APK_ACT MainActivityPlus
setenv APK_FILE ${2}

setenv APK_DATA ${3}

setenv APK_FLAG ${4}
setenv EMU_FLAG ${5}

setenv TOOLS_PATH ${ANDROID_HOME}/tools
setenv EMU_PORT 5558
setenv DEVICE_NAME emulator-${EMU_PORT}
setenv ADB_CMD "adb -s ${DEVICE_NAME}"

echo ================================================================================
echo Starting ${EMU_NAME} on port ${EMU_PORT} with flag ${EMU_FLAG}...
echo ================================================================================
adb kill-server
adb start-server
${ANDROID_HOME}/emulator/emulator ${EMU_FLAG} -no-snapshot -avd ${EMU_NAME} -no-boot-anim -port ${EMU_PORT} &

echo Waiting until boot is completed...
while ("`${ADB_CMD} wait-for-device shell getprop sys.boot_completed | tr -d '\r' `" != "1")
  sleep 1
end
sleep 10

# actual device
${ADB_CMD} root

echo Uninstalling ${APK_PACK}...
${ADB_CMD} uninstall ${APK_PACK}
${ADB_CMD} shell pm list packages | grep ${APK_PACK}

echo Installing ${APK_FILE} with flag ${APK_FLAG}...
${ADB_CMD} install -r ${APK_FLAG} ${APK_FILE}
${ADB_CMD} shell pm list packages | grep ${APK_PACK}

echo Starting app...
${ADB_CMD} shell am start -S -W -n ${APK_PACK}/.${APK_ACT} -a com.mkulesh.micromath.plus.AUTOTEST

echo Waiting until app is finished...
while ("`${ADB_CMD} shell dumpsys activity | grep top-activity | grep -c ${APK_PACK} | tr -d '\r' `" != "0")
  sleep 1
end
sleep 1

echo Collect results
${ADB_CMD} pull ${APK_DATA}/${APK_PACK}/files/autotest.html ${1}.html

echo Stopping emulator...
${ADB_CMD} -e emu kill
sleep 5
killall qemu-system-i386
adb kill-server

exit 1
