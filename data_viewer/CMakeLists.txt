include(FetchContent)

cmake_minimum_required(VERSION 3.16)

project(data_viewer VERSION 0.1 LANGUAGES CXX)

set(CMAKE_AUTOMOC ON)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_LIST_DIR}/CMake)
include(Common)

find_package(Qt6 6.2 COMPONENTS Quick REQUIRED)

#set(ENV{QML_XHR_ALLOW_FILE_READ} 1)
#message("QML_XHR_ALLOW_FILE_READ = " $ENV{QML_XHR_ALLOW_FILE_READ})

#FetchContent_Declare(
#  libqmltreeview
#  GIT_REPOSITORY https://github.com/Daguerreo/QMLTreeView.git
#  GIT_TAG main
#)
#FetchContent_MakeAvailable(libqmltreeview)
#include_directories(SYSTEM "${libqmltreeview_SOURCE_DIR}/single_include")
#list(APPEND CMAKE_MODULE_PATH "${libqmltreeview_SOURCE_DIR}/contrib")
#include(Catch)

#add_custom_command(
#    OUTPUT ${CMAKE_CURRENT_SOURCE_DIR}/resources.qrc
#    COMMAND ./qml_qrc.sh
#    WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
#    COMMENT "Generating resources.qrc"
#)

# Library files
find_sources_recursive("src" "SOURCE_FILES")
set(SOURCES ${SOURCE_FILES})

qt_add_resources(SOURCES resources.qrc)

qt_add_executable(appdata_viewer ${SOURCES})

qt_add_qml_module(appdata_viewer
    URI data_viewer
    VERSION 1.0
#    QML_FILES ${QML_FILELIST}
)

set_target_properties(appdata_viewer PROPERTIES
    MACOSX_BUNDLE_GUI_IDENTIFIER my.example.com
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
)

target_link_libraries(appdata_viewer
    PRIVATE Qt6::Quick)

install(TARGETS appdata_viewer
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR})
