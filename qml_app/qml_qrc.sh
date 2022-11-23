#!/bin/bash

{
    echo "<RCC>
	<qresource prefix=\"/\">" 

    find ./qml/* ./resources/ -type f -not -path "./desktop/*" -not -iname "*.qrc" -not -iname "*.sh" -not -iname ".DS_Store" | sort -fd | while read FILE; do
        echo "		<file>$FILE</file>"
    done

    echo "	</qresource>
</RCC>"
	
} > resources.qrc
