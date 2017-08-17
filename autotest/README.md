## Directory microMathematics/autotest

This directory contains some useful scripts used to automatically test the micro Mathematics Plus.

*create-avd.sh* - can be used in order to create necessary Android Virtual Devices (AVDs) 

*run-session.sh* - main script that runs a test session using all emulators previously created by create-avd.sh.

Usage:
```
# ./run-session.sh ../release/microMathematics_<version>.apk
```

**Note**
In order to run this script, the package *expect* shall be installed on the host machine:

```
# yum install expect
Package expect-5.45-22.fc24.x86_64 is already installed, skipping.
```

*run-emulator.sh*, *kill-emulator.exp* - helper scripts
