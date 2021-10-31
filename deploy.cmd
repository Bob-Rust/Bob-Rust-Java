:: Temporary fix for creating release zip file
for /f "tokens=* usebackq" %%x in (`dir target /b ^| findstr [0-9]\.jar`) do set "filename=%%x"
set "releasefolder=%filename:~0,-4% Release"
mkdir "target/%releasefolder%"
copy "LICENSE" "target/%releasefolder%/LICENSE"
copy "README.md" "target/%releasefolder%/README.md"
echo java -jar %filename% > "target/%releasefolder%/run.cmd"
cd target
copy "%filename%" "%releasefolder%/%filename%"
cd ..
exit /b