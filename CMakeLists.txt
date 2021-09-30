cmake_minimum_required(VERSION 3.9)

project(Alljoyn-services LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories("services_common/cpp/inc")

add_definitions("-DQCC_OS_GROUP_POSIX -DQCC_OS_LINUX")

set(alljoyn-services-common-sources
    "${CMAKE_CURRENT_SOURCE_DIR}/services_common/cpp/src/AsyncTaskQueue.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/services_common/cpp/src/BsSecurity.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/services_common/cpp/src/Common.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/services_common/cpp/src/GuidUtil.cc"
    )

add_subdirectory(notification)

