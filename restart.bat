rem
rem           _           _   
rem     /\   | |         (_)      
rem    /  \  | | ___  ___ _  __ _ 
rem   / /\ \ | |/ _ \/ __| |/ _` |
rem  / ____ \| |  __/\__ \ | (_| |
rem /_/    \_\_|\___||___/_|\__,_|
rem 
rem bat file to restart the application


TASKKILL /IM javaw.exe
start /MIN Alesia.exe
EXIT 0