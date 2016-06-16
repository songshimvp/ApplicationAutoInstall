@echo off
echo open 192.168.1.122 > ftpcommand.txt
echo user 123456 123456>>ftpcommand.txt

echo lcd . >>ftpcommand.txt

echo get %1  %1 >>ftpcommand.txt

echo bye >>ftpcommand.txt

ftp -n -s:ftpcommand.txt

del ftpcommand.txt
