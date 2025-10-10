#!/usr/bin/env sh

##############################################################################
##
##  Gradle wrapper script for UNIX
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"

# Need this for relative symlinks to work.
while [ -h "$PRG" ] ; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="$(dirname "$PRG")/$link"
  fi
done

APP_HOME="$(dirname "$PRG")"

# Universal way to get the real path to the script, even if it's symlinked
APP_HOME="$(cd "$APP_HOME" ; pwd -P)"

# OS specific support (must be 'true' or 'false').
cygwin=false
darwin=false
mingw=false
case "`uname`" in
  CYGWIN*)
    cygwin=true
    ;;
  Darwin*)
    darwin=true
    ;;
  MINGW*)
    mingw=true
    ;;
esac

# Determine if we're running on Windows (either Cygwin or MINGW)
windows=false
if $cygwin || $mingw; then
  windows=true
fi

# Set Java home if it's not already set
if [ -z "$JAVA_HOME" ]; then
  if $darwin; then
    JAVA_HOME=$(/usr/libexec/java_home)
  else
    # Fallback to searching for Java in common locations
    if [ -x /usr/bin/java ]; then
      JAVA_HOME=$(dirname $(dirname $(readlink -f /usr/bin/java)))
    elif [ -x /usr/local/bin/java ]; then
      JAVA_HOME=$(dirname $(dirname $(readlink -f /usr/local/bin/java)))
    fi
  fi
fi

# If JAVA_HOME is still not set, try to find it via 'java' command
if [ -z "$JAVA_HOME" ]; then
  if command -v java > /dev/null 2>&1; then
    JAVA_CMD=$(command -v java)
    if [ -x "$JAVA_CMD" ]; then
      # Attempt to determine JAVA_HOME from the java command path
      JAVA_HOME=$(dirname $(dirname "$JAVA_CMD"))
    fi
  fi
fi

# If JAVA_HOME is still not set, and we are on Windows, try registry
if [ -z "$JAVA_HOME" ] && $windows; then
  # Try to find Java home using 'where java' (for Windows)
  _java_exe=$(command -v java)
  if [ -n "$_java_exe" ]; then
    _java_exe_path=$(dirname "$_java_exe")
    JAVA_HOME=$(cd "$_java_exe_path/.." && pwd -W) # Get parent directory path
  fi
fi

# If JAVA_HOME is set, add it to the PATH
if [ -n "$JAVA_HOME" ] ; then
  PATH="$JAVA_HOME/bin:$PATH"
fi

# For Cygwin, ensure paths are in UNIX format before passing to Java
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  APP_HOME="$(cygpath --unix "$APP_HOME")"
fi

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    # IBM's JDK on AIX uses this path
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
else
  JAVACMD="java"
fi

# Check if Java is available
if ! command -v "$JAVACMD" > /dev/null 2>&1; then
  echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
  echo ""
  echo "Please set the JAVA_HOME environment variable to the root directory of your Java installation (e.g. /usr/lib/jvm/java-11-openjdk-amd64) or add 'java' to your PATH."
  exit 1
fi

# Main class for the Gradle wrapper
CLASS='org.gradle.wrapper.GradleWrapperMain'

# Find the wrapper JAR
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec "$JAVACMD" "-Dorg.gradle.appname=$APP_NAME" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$CLASS" "$@"
else
  echo "ERROR: Cannot find $APP_HOME/gradle/wrapper/gradle-wrapper.jar"
  echo "This might indicate a corrupted or incomplete Gradle wrapper setup."
  echo "Please ensure the 'gradle/wrapper' directory and its contents are present."
  exit 1
fi