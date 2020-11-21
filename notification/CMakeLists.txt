cmake_minimum_required(VERSION 3.9)

project(Alljoyn LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(alljoyn-notification-sources
    cpp/src/NotificationAsyncTaskEvents.cc
    cpp/src/Notification.cc
    cpp/src/NotificationConstants.cc
    cpp/src/NotificationDismisser.cc
    cpp/src/NotificationDismisserReceiver.cc
    cpp/src/NotificationDismisserSender.cc
    cpp/src/NotificationEnums.cc
    cpp/src/NotificationProducer.cc
    cpp/src/NotificationProducerListener.cc
    cpp/src/NotificationProducerReceiver.cc
    cpp/src/NotificationProducerSender.cc
    cpp/src/NotificationReceiver.cc
    cpp/src/NotificationSender.cc
    cpp/src/NotificationService.cc
    cpp/src/NotificationText.cc
    cpp/src/NotificationTransport.cc
    cpp/src/NotificationTransportConsumer.cc
    cpp/src/NotificationTransportProducer.cc
    cpp/src/PayloadAdapter.cc
    cpp/src/RichAudioUrl.cc
    cpp/src/SConscript
    cpp/src/Transport.cc
)

list(APPEND alljoyn-notification-sources
    ${alljoyn-services-common-sources}
    )

include_directories("cpp/inc"
    "../services_common/cpp/inc"
    "${CMAKE_CURRENT_SOURCE_DIR}/../../core-alljoyn/alljoyn_core/inc"
    "${CMAKE_CURRENT_SOURCE_DIR}/../../core-alljoyn/common/inc"
    "${CMAKE_CURRENT_SOURCE_DIR}/../../core-alljoyn/services/about/cpp/inc"
    "${CMAKE_CURRENT_BINARY_DIR}/../../core-alljoyn/alljoyn_core/inc"
    "${CMAKE_CURRENT_BINARY_DIR}/../../core-alljoyn/inc"
    "${CMAKE_CURRENT_BINARY_DIR}/cpp/inc"
    "${CMAKE_CURRENT_BINARY_DIR}/cpp/inc/alljoyn"

    )

#add_custom_command(OUTPUT "${CMAKE_CURRENT_BINARY_DIR}/inc/alljoyn/Status.h"
#    "${CMAKE_CURRENT_BINARY_DIR}/alljoyn_core/Status.cc"
#    COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/../build_core/tools/bin/make_status.py --header
#        "${CMAKE_CURRENT_BINARY_DIR}/inc/alljoyn/Status.h" --code
#        "${CMAKE_CURRENT_BINARY_DIR}/alljoyn_core/Status.cc" --base
#        "${CMAKE_CURRENT_SOURCE_DIR}"
#        "${CMAKE_CURRENT_SOURCE_DIR}/src/Status.xml"
#)

# also adds generated files to sources
add_library(alljoyn_notification
    ${alljoyn-notification-sources}
    )

add_dependencies(alljoyn_notification status_header_cpp)


