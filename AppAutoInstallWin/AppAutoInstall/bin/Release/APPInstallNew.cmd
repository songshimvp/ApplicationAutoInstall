echo off

echo Installing .\\%1...Please wait for a few minutes.

if exist ".\\%1" start /wait .\\%1

del .\%1