@echo off
chcp 936 >nul
setlocal enabledelayedexpansion
title 来财酒店 PMS - 一键启动

REM ============================================================
REM  【配置区】一般不用改，自动找不到时才填
REM
REM    Redis：Redis 文件夹路径（不带 redis-server.exe）
REM           例如  set "REDIS_DIR=D:\Redis"
REM    Java ：java.exe 的完整路径
REM           例如  set "JAVA_EXE=C:\Program Files\Java\jdk-21\bin\java.exe"
REM ============================================================
set "REDIS_DIR="
set "JAVA_EXE="
REM ============================================================

set "APP_DIR=%~dp0"
if "%APP_DIR:~-1%"=="\" set "APP_DIR=%APP_DIR:~0,-1%"

echo ============================================
echo        来财酒店 PMS   一键启动
echo ============================================
echo   项目目录：%APP_DIR%
echo.

REM ==================== 1. Java ====================
if not defined JAVA_EXE (
  where java >nul 2>nul
  if not errorlevel 1 set "JAVA_EXE=java"
)
if not defined JAVA_EXE (
  for %%R in ("C:\Program Files\Java" "C:\Program Files\Eclipse Adoptium" "C:\Program Files\Microsoft" "C:\Program Files\Zulu" "C:\Program Files\Amazon Corretto" "C:\Program Files\BellSoft" "D:\Java" "D:\Program Files\Java") do (
    if not defined JAVA_EXE if exist "%%~R" (
      for /d %%D in ("%%~R\*") do (
        if not defined JAVA_EXE if exist "%%D\bin\java.exe" set "JAVA_EXE=%%D\bin\java.exe"
      )
    )
  )
)
if not defined JAVA_EXE (
  echo [1/6] Java ............. 失败
  echo.
  echo        本机没有找到 Java。请先安装 JDK 17 或 21：
  echo          https://adoptium.net/  （选 Temurin 21，Windows x64 的 msi）
  echo        安装时勾选 "Set JAVA_HOME variable"，装完重新双击本文件。
  echo.
  pause & exit /b 1
)
echo [1/6] Java ............. OK

REM ==================== 2. MySQL ====================
set "P=0"
for /f "tokens=*" %%A in ('netstat -ano ^| findstr ":3306" ^| findstr "LISTENING"') do set "P=1"
if "!P!"=="1" (
  echo [2/6] MySQL ............ 已在运行
) else (
  echo [2/6] MySQL ............ 未运行，尝试启动服务...
  for %%S in (mysql mysql80 mysql8 mysql57 mysql5 mariadb MariaDB MySQL80 MySQL) do (
    if "!P!"=="0" (
      net start %%S >nul 2>nul
      ping -n 3 127.0.0.1 >nul
      for /f "tokens=*" %%A in ('netstat -ano ^| findstr ":3306" ^| findstr "LISTENING"') do set "P=1"
    )
  )
)
if "!P!"=="0" (
  echo.
  echo        没能自动启动 MySQL。请手动启动：
  echo          按 Win+R，输入 services.msc 回车，
  echo          在列表里找到 MySQL 服务，右键 - 启动
  echo.
  echo        启动好了按任意键继续...
  pause >nul
  for /f "tokens=*" %%A in ('netstat -ano ^| findstr ":3306" ^| findstr "LISTENING"') do set "P=1"
)
if "!P!"=="1" (
  echo [2/6] MySQL ............ 就绪
) else (
  echo [2/6] MySQL ............ 失败：3306 端口仍然没通
  echo        请确认 MySQL 已安装并启动后重试
  pause & exit /b 1
)

REM ==================== 3. Redis ====================
set "P=0"
for /f "tokens=*" %%A in ('netstat -ano ^| findstr ":6379" ^| findstr "LISTENING"') do set "P=1"
if "!P!"=="1" (
  echo [3/6] Redis ............ 已在运行
) else (
  echo [3/6] Redis ............ 未运行，正在查找...
  if not defined REDIS_DIR if exist "%APP_DIR%\redis\redis-server.exe" set "REDIS_DIR=%APP_DIR%\redis"
  if not defined REDIS_DIR if exist "D:\Redis\redis-server.exe" set "REDIS_DIR=D:\Redis"
  if not defined REDIS_DIR if exist "D:\redis\redis-server.exe" set "REDIS_DIR=D:\redis"
  if not defined REDIS_DIR if exist "C:\Redis\redis-server.exe" set "REDIS_DIR=C:\Redis"
  if not defined REDIS_DIR if exist "C:\redis\redis-server.exe" set "REDIS_DIR=C:\redis"
  if not defined REDIS_DIR (
    for /d %%D in ("D:\*") do (
      if not defined REDIS_DIR if exist "%%D\redis-server.exe" set "REDIS_DIR=%%D"
    )
  )
  if not defined REDIS_DIR (
    for /d %%D in ("C:\*") do (
      if not defined REDIS_DIR if exist "%%D\redis-server.exe" set "REDIS_DIR=%%D"
    )
  )
  if not defined REDIS_DIR (
    for /d %%D in ("%USERPROFILE%\Desktop\*" "%USERPROFILE%\Downloads\*" "%USERPROFILE%\Documents\*") do (
      if not defined REDIS_DIR if exist "%%D\redis-server.exe" set "REDIS_DIR=%%D"
    )
  )
  if defined REDIS_DIR (
    echo       找到 Redis：!REDIS_DIR!
    if exist "!REDIS_DIR!\redis.windows.conf" (
      start "PMS-Redis" /min "!REDIS_DIR!\redis-server.exe" "!REDIS_DIR!\redis.windows.conf"
    ) else (
      start "PMS-Redis" /min "!REDIS_DIR!\redis-server.exe"
    )
    ping -n 6 127.0.0.1 >nul
    for /f "tokens=*" %%A in ('netstat -ano ^| findstr ":6379" ^| findstr "LISTENING"') do set "P=1"
  )
)
if "!P!"=="0" (
  echo.
  echo        没找到 Redis 程序。请手动启动：
  echo          打开 Redis 文件夹，双击 redis-server.exe
  echo          看到 "ready to accept connections" 后回到本窗口
  echo.
  echo        启动好了按任意键继续...
  pause >nul
  for /f "tokens=*" %%A in ('netstat -ano ^| findstr ":6379" ^| findstr "LISTENING"') do set "P=1"
)
if "!P!"=="1" (
  echo [3/6] Redis ............ 就绪
) else (
  echo [3/6] Redis ............ 失败：6379 端口仍然没通
  pause & exit /b 1
)

REM ==================== 4. 系统程序 ====================
set "JAR=%APP_DIR%\yudao-server.jar"
if not exist "%JAR%" set "JAR=%APP_DIR%\yudao-server\target\yudao-server.jar"

if exist "%JAR%" (
  echo [4/6] 系统程序 ......... 已找到
  goto run_app
)

echo [4/6] 系统程序 ......... 未找到，尝试自动编译
echo.
if not exist "%APP_DIR%\pom.xml" (
  echo        失败：本目录既没有 yudao-server.jar，也没有 pom.xml
  echo        请把编译好的 yudao-server.jar 放到：%APP_DIR%
  pause & exit /b 1
)
set "MVN="
where mvn >nul 2>nul
if not errorlevel 1 set "MVN=mvn"
if not defined MVN (
  for %%R in ("C:\Program Files\apache-maven*" "D:\apache-maven*" "D:\maven\apache-maven*" "C:\apache-maven*") do (
    if not defined MVN if exist "%%~R\bin\mvn.cmd" set "MVN=%%~R\bin\mvn.cmd"
  )
)
if not defined MVN (
  echo        失败：本机没有 Maven，无法自动编译
  echo.
  echo        两个办法：
  echo          1. 把编译好的 yudao-server.jar 放到本目录（推荐）
  echo          2. 安装 Maven 后重试
  echo.
  pause & exit /b 1
)
echo        正在编译，首次需要 5-15 分钟（要下载依赖），请耐心等待...
echo.
pushd "%APP_DIR%"
call "%MVN%" package -DskipTests -q
popd
if exist "%APP_DIR%\yudao-server\target\yudao-server.jar" (
  set "JAR=%APP_DIR%\yudao-server\target\yudao-server.jar"
  echo        编译成功
) else (
  echo        编译失败，请查看上方的错误信息
  pause & exit /b 1
)

:run_app
echo [5/6] 启动系统 ......... 新窗口 PMS-Server，请勿关闭
start "PMS-Server" "%JAVA_EXE%" -jar "%JAR%"
echo       等待系统就绪（约 60 秒）...
set "W=0"
:wait
ping -n 4 127.0.0.1 >nul
set /a W+=3
curl -s -o nul --max-time 2 http://localhost:48080/ >nul 2>nul
if not errorlevel 1 goto ready
if %W% LSS 180 goto wait
echo       [警告] 系统 180 秒未就绪，请查看 PMS-Server 窗口的报错
goto open

:ready
echo       系统已就绪（约 %W% 秒）

:open
echo [6/6] 打开浏览器
start "" http://localhost:48080

echo.
echo ============================================
echo   启动完成
echo.
echo   访问地址：http://localhost:48080
echo   租户：来财酒店
echo   账号：admin      密码：admin123
echo ============================================
echo.
echo   本窗口可以关闭。
echo   PMS-Server 窗口不能关（关了系统就停）。
echo   数据库和 Redis 窗口也请保持运行。
echo.
pause
