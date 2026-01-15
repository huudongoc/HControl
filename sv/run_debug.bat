@echo off
java ^
 -Xms2G ^
 -Xmx15G ^
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 ^
 -jar paper-1.20.4-499.jar nogui
pause
