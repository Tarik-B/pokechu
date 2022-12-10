# Finds cpp header/source files and stores their path in parent scope
# variable named ${OUT_VARIABLE_NAME}
function( find_sources_recursive SOURCES_PATH OUT_VARIABLE_NAME )
    file(GLOB_RECURSE SOURCE_FILES ${SOURCES_PATH}/*.cpp)
    file(GLOB_RECURSE HEADER_FILES ${SOURCES_PATH}/*.h)

    # Output
    set(${OUT_VARIABLE_NAME} ${HEADER_FILES} ${SOURCE_FILES} PARENT_SCOPE)
endfunction()
