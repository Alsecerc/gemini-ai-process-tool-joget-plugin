@echo off
echo Building plugin...
call mvn clean package

if not exist "target\gemini-ai-process-tool-1.0.0.jar" (
    echo ERROR: Build failed - JAR file not found
    pause
    exit /b 1
)

echo Build successful!

set TARGET_DIR=C:\Joget-DX8-Enterprise\wflow\app_plugins
set JAR_FILE=gemini-ai-process-tool-1.0.0.jar

echo Checking target directory...
if not exist "%TARGET_DIR%" (
    echo ERROR: Target directory does not exist: %TARGET_DIR%
    pause
    exit /b 1
)

echo Removing old plugin file...
if exist "%TARGET_DIR%\%JAR_FILE%" (
    del /f /q "%TARGET_DIR%\%JAR_FILE%"
    if exist "%TARGET_DIR%\%JAR_FILE%" (
        echo ERROR: Failed to delete old plugin file
        pause
        exit /b 1
    )
    echo Old plugin file removed successfully
) else (
    echo No old plugin file to remove
)

echo Copying new plugin file...
copy "target\%JAR_FILE%" "%TARGET_DIR%\"

if exist "%TARGET_DIR%\%JAR_FILE%" (
    echo SUCCESS: Plugin updated successfully!
    echo Plugin location: %TARGET_DIR%\%JAR_FILE%
) else (
    echo ERROR: Failed to copy plugin file
    pause
    exit /b 1
)

echo.
echo Plugin update complete. You may need to restart Joget for changes to take effect.
exit
