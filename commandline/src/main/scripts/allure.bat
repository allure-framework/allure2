@echo off
setlocal enableextensions enabledelayedexpansion

if errorlevel 1 (
    echo "Unable to 'setlocal enableextensions enabledelayedexpansion'"
    goto error
)

@REM Decide how to startup depending on the version of windows
@REM -- Windows NT with Novell Login
if "%OS%"=="WINNT" goto WinNTNovell
@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

:WinNTNovell
@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTArgs
@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto start
@REM The 4NT Shell from jp software

:4NTArgs
set CMD_LINE_ARGS=%$
goto start

:Win9xArg
@REM Slurp the command line arguments. This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set CMD_LINE_ARGS=

:Win9xApp
if %1a==a goto start
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM exit with error code 1
:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1
exit /B 1

@REM start validation
:start

@REM find jre or jdk and test it
if defined JRE_HOME (
    set "JAVA=%JRE_HOME%\bin\java.exe"
    if not exist "!JAVA!" (
        echo Invalid JRE_HOME: %JRE_HOME% is not a valid jre directory >&2
        goto error
    )
) else if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\java.exe"
    if not exist "!JAVA!" (
        echo Invalid JAVA_HOME: %JAVA_HOME% is not a valid java directory >&2
        goto error
    )
) else (
    @REM find java in PATH
    for %%x in (java.exe) do (set JAVA=%%~$PATH:x)
)

"%JAVA%" -version >nul 2>&1

if ERRORLEVEL 1 (
    echo Could not find java implementation: try to set JAVA_HOME, JRE_HOME or add java to PATH >&2
    goto error
)

@REM find and validate allure home directory
if "%ALLURE_HOME%" == "" (
    set "ALLURE_HOME=%~dp0.."
)

if not exist "%ALLURE_HOME%\bin\allure.bat" (
    echo Invalid ALLURE_HOME: %ALLURE_HOME% is not a valid allure commandline directory >&2
    goto error
)

set "ALLURE_MAIN=org.allurefw.report.CommandLine"
set "ALLURE_CP=%ALLURE_HOME%\bin\commandline.jar;%ALLURE_HOME%\conf"
set "ALLURE_ARGS="

call :add_system_property_arg "allure.home" "%ALLURE_HOME%"
call :add_system_property_arg "allure.config" "%ALLURE_CONFIG%"
call :add_system_property_arg "allure.bundle.javaOpts" "%ALLURE_BUNDLE_JAVA_OPTS%"

"%JAVA%" %JAVA_ARGS% %ALLURE_ARGS% -cp "%ALLURE_CP%" %ALLURE_MAIN% %CMD_LINE_ARGS%

goto end

@REM extra functions

:add_system_property_arg
if not "%~2"=="" (
    set ARGUMENT="-D%~1=%~2"
    set "ALLURE_ARGS=!ALLURE_ARGS! !ARGUMENT!"
)
goto:eof

:end
endlocal