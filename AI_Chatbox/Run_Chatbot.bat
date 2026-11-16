@echo off
cd /d "%~dp0"
echo Starting Java AI Chatbot Website...
java --add-modules jdk.httpserver main.java
pause
