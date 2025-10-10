@rem
@rem Copyright 2017 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle wrapper script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows 2000 or greater
@setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS environment variables.
@set DEFAULT_JVM_OPTS="-Xmx64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

@set JAVA_EXE=java.exe
@for %%i in ("%PATH:;"=" "%") do if exist "%%i\%JAVA_EXE%" set JAVA_EXE="%%i\%JAVA_EXE%" & goto runJava

:findJavaFromJavaHome
@set JAVA_EXE="%JAVA_HOME%\bin\java.exe"

:runJava
@if not exist %JAVA_EXE% goto noJavaFound

@rem Set to where gradle-wrapper.jar is located
@set APP_HOME=%~dp0

@rem Resolve APP_HOME to an absolute path, without symlinks
for %%i in ("%APP_HOME%") do @set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS environment variables.
@set DEFAULT_JVM_OPTS="-Xmx64m"

@rem Use the project's Gradle distribution, if present
@set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

@if not exist "%CLASSPATH%" goto noWrapperFound

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
@if %ERRORLEVEL% NEQ 0 goto fail
@goto end

:noJavaFound
@echo.
@echo ERROR: JAVA_HOME is not set and no 'java.exe' command could be found in your PATH.
@echo.
@echo Please set the JAVA_HOME environment variable to the root directory of your Java installation (e.g. C:\Program Files\Java\jdk11) or add 'java.exe' to your PATH.
@echo.
@goto fail

:noWrapperFound
@echo.
@echo ERROR: Cannot find %CLASSPATH%
@echo This might indicate a corrupted or incomplete Gradle wrapper setup.
@echo Please ensure the 'gradle\wrapper' directory and its contents are present.
@echo.
@goto fail

:fail
@set ERROR_CODE=1

:end
@endlocal & set ERRORLEVEL=%ERROR_CODE%
@exit /b %ERRORLEVEL%