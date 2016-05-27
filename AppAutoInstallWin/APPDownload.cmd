@echo off
echo open 192.168.1.107 > ftpcommand.txt
echo user MVP song19911020>>ftpcommand.txt

echo lcd E:\Users\MVP\Desktop\E2Mmsi >>ftpcommand.txt

echo get %1  %1 >>ftpcommand.txt

echo bye >>ftpcommand.txt

ftp -n -s:ftpcommand.txt

del ftpcommand.txt
