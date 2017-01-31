@echo off

echo ------------------------------------
echo --------  ADD Git DEMOTRON  --------
echo ------------------------------------

for /F "usebackq tokens=1,2 delims==" %%i in (`wmic os get LocalDateTime /VALUE 2^>NUL`) do if '.%%i.'=='.LocalDateTime.' set ldt=%%j
set ldt=%ldt:~0,4%-%ldt:~4,2%-%ldt:~6,2% %ldt:~8,2%:%ldt:~10,2%


::cd c:\Users\rubro\Google Drive\Desarrollo\Wamp\www\demotron
git status
git add .
set /P palabra= Escriba un Mensaje de Commit: 
git commit -m "%palabra% -- [%ldt%]"
echo --- INICIANDO SUBIDA, ESPERE ---
git push origin master
pause
