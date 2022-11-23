cmake_minimum_required(VERSION 3.16)

project(qml_app VERSION 0.1 LANGUAGES CXX)

set(CMAKE_AUTOMOC ON)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Qt6 6.2 COMPONENTS Quick REQUIRED)

set(SOURCES main.cpp)

qt_add_resources(SOURCES resources.qrc)

qt_add_executable(appqml_app ${SOURCES})

qt_add_qml_module(appqml_app
    URI qml_app
    VERSION 1.0
#    QML_FILES ${QML_FILELIST}
)

set_target_properties(appqml_app PROPERTIES
    MACOSX_BUNDLE_GUI_IDENTIFIER my.example.com
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
)

target_link_libraries(appqml_app
    PRIVATE Qt6::Quick)

install(TARGETS appqml_app
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR})
