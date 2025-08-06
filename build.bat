@echo off
setlocal enabledelayedexpansion

:: H2-ORM Build Script for Windows
:: Comprehensive build and release automation

set PROJECT_NAME=H2-ORM
set BUILD_DIR=target
set DIST_DIR=dist
set DOCS_DIR=docs

:: Colors (limited support on Windows)
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set BLUE=[94m
set NC=[0m

echo.
echo %BLUE%======================================================================%NC%
echo %BLUE%  Building %PROJECT_NAME%%NC%
echo %BLUE%======================================================================%NC%
echo.

:: Get version from Maven
for /f "delims=" %%i in ('mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2^>nul') do set VERSION=%%i

:: Parse command line arguments
set SKIP_TESTS=false
set SKIP_DOCS=false
set QUICK_BUILD=false

:parse_args
if "%~1"=="" goto start_build
if "%~1"=="--skip-tests" (
    set SKIP_TESTS=true
    shift
    goto parse_args
)
if "%~1"=="--skip-docs" (
    set SKIP_DOCS=true
    shift
    goto parse_args
)
if "%~1"=="--quick" (
    set QUICK_BUILD=true
    set SKIP_TESTS=true
    set SKIP_DOCS=true
    shift
    goto parse_args
)
if "%~1"=="--help" (
    echo Usage: %0 [OPTIONS]
    echo Options:
    echo   --skip-tests    Skip running tests
    echo   --skip-docs     Skip generating documentation
    echo   --quick         Quick build ^(skip tests and docs^)
    echo   --help          Show this help message
    goto end
)
echo Unknown option %~1
goto error

:start_build
echo %BLUE%Checking Prerequisites...%NC%

:: Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo %RED%Error: Java is not installed or not in PATH%NC%
    goto error
)
echo %GREEN%✓ Java found%NC%

:: Check Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo %RED%Error: Maven is not installed or not in PATH%NC%
    goto error
)
echo %GREEN%✓ Maven found%NC%

echo.
echo %BLUE%Cleaning Previous Build...%NC%
call mvn clean
if errorlevel 1 goto error

if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%DIST_DIR%"
echo %GREEN%✓ Build cleaned%NC%

echo.
echo %BLUE%Building Project...%NC%
call mvn compile
if errorlevel 1 goto error
echo %GREEN%✓ Project compiled%NC%

if "%SKIP_TESTS%"=="false" (
    echo.
    echo %BLUE%Running Tests...%NC%
    call mvn test
    if errorlevel 1 goto error

    call mvn verify
    if errorlevel 1 goto error
    echo %GREEN%✓ All tests passed%NC%
) else (
    echo %YELLOW%⚠ Skipping tests%NC%
)

echo.
echo %BLUE%Packaging Project...%NC%
call mvn package -DskipTests
if errorlevel 1 goto error
echo %GREEN%✓ Project packaged%NC%

if "%SKIP_DOCS%"=="false" (
    echo.
    echo %BLUE%Generating Documentation...%NC%
    call mvn javadoc:javadoc
    if errorlevel 1 echo %YELLOW%⚠ Javadoc generation had warnings%NC%

    call mvn jacoco:report
    if errorlevel 1 echo %YELLOW%⚠ Coverage report had warnings%NC%

    if exist "target\site\apidocs" (
        xcopy "target\site\apidocs" "%DIST_DIR%\javadocs\" /e /i /q
        echo %GREEN%✓ Javadocs copied to %DIST_DIR%\javadocs%NC%
    )

    if exist "target\site\jacoco" (
        xcopy "target\site\jacoco" "%DIST_DIR%\coverage\" /e /i /q
        echo %GREEN%✓ Coverage report copied to %DIST_DIR%\coverage%NC%
    )
) else (
    echo %YELLOW%⚠ Skipping documentation generation%NC%
)

if "%QUICK_BUILD%"=="false" (
    echo.
    echo %BLUE%Running Quality Checks...%NC%
    call mvn spotbugs:check
    if errorlevel 1 echo %YELLOW%⚠ SpotBugs found issues (non-blocking)%NC%
    echo %GREEN%✓ Quality checks completed%NC%
)

echo.
echo %BLUE%Creating Distribution...%NC%

:: Copy JAR files
copy "target\h2-orm-*.jar" "%DIST_DIR%\" >nul 2>&1

:: Create release notes
(
echo # %PROJECT_NAME% v%VERSION% Release Notes
echo.
echo ## 📦 Distribution Contents
echo.
echo - `h2-orm-%VERSION%.jar` - Main library JAR
echo - `h2-orm-%VERSION%-sources.jar` - Sources JAR
echo - `h2-orm-%VERSION%-javadoc.jar` - Javadoc JAR
echo - `h2-orm-%VERSION%-complete.jar` - Complete JAR with all dependencies
echo - `javadocs/` - API documentation
echo - `coverage/` - Code coverage reports
echo.
echo ## 🚀 Features
echo.
echo - Spring Boot-style repository pattern
echo - Support for H2, MySQL, PostgreSQL, SQLite
echo - Advanced backup and export functionality
echo - Sorting and pagination support
echo - Field-based query methods
echo - Table management utilities
echo - Comprehensive test coverage
echo.
echo ## 📖 Usage
echo.
echo Add to your Maven dependencies:
echo.
echo ```xml
echo ^<dependency^>
echo     ^<groupId^>com.h2.orm^</groupId^>
echo     ^<artifactId^>h2-orm^</artifactId^>
echo     ^<version^>%VERSION%^</version^>
echo ^</dependency^>
echo ```
echo.
echo ## 🔧 Quick Start
echo.
echo ```java
echo // Start H2-ORM
echo H2ORM.startInMemory^(^);
echo.
echo // Get repository
echo JpaRepository^<User, Long^> userRepo = H2ORM.repository^(User.class^);
echo.
echo // Use like Spring Data JPA
echo User user = new User^("john", "john@example.com", "John Doe"^);
echo userRepo.save^(user^);
echo.
echo List^<User^> users = userRepo.findByField^("username", "john"^);
echo Page^<User^> page = userRepo.findAll^(PageRequest.of^(0, 10, Sort.by^("username"^^^)^);
echo ```
echo.
echo Built on %date% %time%
) > "%DIST_DIR%\RELEASE_NOTES.md"

echo %GREEN%✓ Distribution created in %DIST_DIR%\%NC%

echo.
echo %BLUE%Creating Checksums...%NC%
cd "%DIST_DIR%"
for %%f in (*.jar) do (
    if exist "%%f" (
        certutil -hashfile "%%f" SHA256 > "%%f.sha256"
        certutil -hashfile "%%f" MD5 > "%%f.md5"
    )
)
cd ..
echo %GREEN%✓ Checksums created%NC%

echo.
echo %GREEN%======================================================================%NC%
echo %GREEN%  🎉 Build Completed Successfully!%NC%
echo %GREEN%======================================================================%NC%
echo.
echo Project: %PROJECT_NAME% v%VERSION%
echo Build Date: %date% %time%
echo Distribution: %DIST_DIR%\
echo.
echo Generated Files:
dir "%DIST_DIR%\*.jar" /b 2>nul
dir "%DIST_DIR%\*.md" /b 2>nul
echo.
echo %BLUE%ℹ Run 'release.bat' to create a release package%NC%
echo.
goto end

:error
echo.
echo %RED%❌ Build failed!%NC%
exit /b 1

:end
echo.
pause
