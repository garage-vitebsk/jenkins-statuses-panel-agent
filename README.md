# jenkins-statuses-panel-agent
The PC agent to connect to Jenkins and transfer data into Jenkins Statuses Panel

## Prerequizites ##
- Install Maven
- Add plugin for Lombok into your IDE
- Configure your IDE to support Lombok

## Build steps ##
- Download https://github.com/zsmartsystems/com.zsmartsystems.bluetooth.bluegiga
- Build project 'com.zsmartsystems.bluetooth.bluegiga' by command `mvn install` 

## Run steps ##
TODO

## Resources 
- Main project https://github.com/vkorecky/bluetooth-client-hc06
- Examples: 
- Review:
    - https://github.com/sputnikdev/bluetooth-manager
    - Examples of using main library https://github.com/sputnikdev/bluetooth-cli 
    - Dependency project: https://github.com/zsmartsystems/com.zsmartsystems.bluetooth.bluegiga

## Possible issues with running
- **javax.bluetooth.BluetoothStateException: BluetoothStack not detected** - make sure you turn on bluetooth on the laptop
- **Exception in thread "main" javax.bluetooth.BluetoothStateException: BlueCove libraries not available** - add libs\bluecove-2.1.1,jar as dependencies to project via idea configuration (IDE configuration), reimport maven project