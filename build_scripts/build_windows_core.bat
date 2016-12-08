rem     Copyright (c) Open Connectivity Foundation (OCF) and AllJoyn Open
rem     Source Project (AJOSP) Contributors and others.
rem
rem     SPDX-License-Identifier: Apache-2.0
rem
rem     All rights reserved. This program and the accompanying materials are
rem     made available under the terms of the Apache License, Version 2.0
rem     which accompanies this distribution, and is available at
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem     Copyright (c) Open Connectivity Foundation and Contributors to AllSeen
rem     Alliance. All rights reserved.
rem
rem     Permission to use, copy, modify, and/or distribute this software for
rem     any purpose with or without fee is hereby granted, provided that the
rem     above copyright notice and this permission notice appear in all
rem     copies.
rem
rem      THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
rem      WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
rem      WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
rem      AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
rem      DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
rem      PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
rem      TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
rem      PERFORMANCE OF THIS SOFTWARE.
rem
pushd %ALLJOYN_CORE_DIR%
set MSVC_VERSION=%MSVC_VER%
set OS=%OS%
set BINDINGS=%BINDINGS%
set BR=off
set WS=off
set CPU=%CPU%
set VARIANT=%VARIANT%

cmd /c %SCONS% "--jobs=%JOBS%" "V=1" "OS=%OS%" "BINDINGS=%BINDINGS%" "VARIANT=%VARIANT%" "CPU=%CPU%" "MSVC_VERSION=%MSVC_VER%"
echo Build completed
IF %ERRORLEVEL% GTR 0 exit %ERRORLEVEL%
popd
setlocal enableextensions
rem TODO rename ALLJOYN_DIST_DIR (batch script argument) such that it isn't confused with ALLJOYN_DISTDIR (SCons argument)
rd /s /q %ALLJOYN_DIST_DIR%
md %ALLJOYN_DIST_DIR%
xcopy /e %ALLJOYN_SRC_DIST_DIR% %ALLJOYN_DIST_DIR%\
echo Core cached!!!
endlocal 
exit 0