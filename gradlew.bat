@ECHO OFF
SETLOCAL
SET APP_HOME=%~dp0
java "%APP_HOME%gradle\wrapper-bootstrap\GradleBootstrap.java" %*
EXIT /B %ERRORLEVEL%
