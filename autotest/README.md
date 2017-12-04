## Directory microMathematics/autotest

This directory contains some useful scripts used to automatically test the microMathematics Plus.

*create-avd.sh* - can be used in order to create necessary Android Virtual Devices (AVDs).
The AVDs will be created with respect to the configuration file *device.cfg* that contains
necessary AVD parameters.

*run-session.sh* - main script that runs a test session using all emulators previously created by create-avd.sh.

Usage:
```
# ./run-session.sh ../release/microMathematics_<version>.apk
```

*run-emulator.sh* - helper script
