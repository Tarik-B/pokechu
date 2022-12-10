/*
 * Copyright (c) 2012 Romain Pokrzywka (KDAB) (romain@kdab.com)
 * Licensed under the MIT licence (http://opensource.org/licenses/mit-license.php)
 */

import QtQuick
import QtQml
import QtQml.XmlListModel
import QtQuick.Layouts

Window {
    id: self
//    width: 640
//    height: 480
    visibility: Window.Maximized
    visible: true
    title: qsTr("Pokechu Data Viewer")

    property var pokemon_names: ({})

    Component.onCompleted: {
        parseXML("../resources/strings-fr.xml");
    }

    function printObject(obj) {
//        console.log(JSON.stringify(obj, null, 4));

        Object.keys(obj).forEach((prop)=> console.log(prop + " = " + obj[prop]));

//        Object.keys(yourObj)

        // [ 'propA', 'propB' ]
        console.log(Object.keys(obj));

        // [ 'propA', 'propA2', 'propB', 'propB2' ]
        console.log(Object.getOwnPropertyNames(obj));

        // [ 'constructor', 'showMeToo' ]
        let protoB = Object.getPrototypeOf(obj);
        console.log(Object.getOwnPropertyNames(protoB));

        // [ 'constructor', 'showMe' ]
        let protoA = Object.getPrototypeOf(protoB);
        console.log(Object.getOwnPropertyNames(protoA));

        // shows properties on generic Object prototype
        let p1 = Object.getPrototypeOf(protoA);
        console.log(Object.getOwnPropertyNames(p1));

        // shows null because it's the end of the prototype chain
        let p2 = Object.getPrototypeOf(p1);
        console.log(p2);
    }

    JSONListModel {
        id: jsonModel1
        source: "qrc:///resources/pokemon_list.json"
        query: "$"
    }

    QmlTreeView {
        id: jsonView

        width: parent.width/2
        height: parent.height
        anchors.left: parent.left

        anchors.margins: 1

        model: jsonModel

        selectionEnabled: true
        hoverEnabled: true

        color: "steelblue"
        handleColor: "steelblue"
        hoverColor: "skyblue"
        selectedColor: "cornflowerblue"
        selectedItemColor: "white"
        //                  handleStyle: TreeView.Handle.Chevron
        //                  rowHeight: 40
        rowPadding: 30
        rowSpacing: 12
        font.pixelSize: 20

        contentItem: RowLayout {
            Text {
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignLeft
                text: currentRow.currentData.key
            }

            Text {
                Layout.fillWidth: true
                Layout.rightMargin: 10

                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignRight
                text: currentRow.currentData.value ? currentRow.currentData.value : ""
            }
        }

        // node is a QmlTreeViewItem
        function expand(node) {



//            console.log("jsonView.tree.parentIndex = " + jsonView.tree.parentIndex);
//            console.log("jsonView.tree.childCount = " + jsonView.tree.childCount);
//            console.log("node._prop.currentIndex = " + node._prop.currentIndex);
//            console.log("node._prop.itemChildCount = " + node._prop.itemChildCount);

//            const parent = node.model.index(0, 0, node.parentIndex)

//            console.log("parent = " + parent);

//            printObject(jsonView.tree);


//            for (var i=0; i < node.childCount; ++i) {

//            }
        }

        Component.onCompleted: {
            console.log("tree.childCount = " + jsonView.tree.childCount);

//            expand(jsonView.tree);

//            tree.childCount = root.model ? root.model.rowCount(tree.parentIndex) : 0

//            var recentTracks = root.childNodes[1];
//            for (var i=0; i < recentTracks.childNodes.length; ++i)
//            {
//                var child = recentTracks.childNodes[i];
//                for (var j=0; j < child.childNodes.length; ++j)
//                {
//                    if (child.childNodes[j].nodeName == "string")
//                    {
//                        console.log(child.childNodes[j].childNodes[0].nodeValue); // [!]
//                    }
//                }
//            }
        }
    }

    ListView {
        id: list1
        width: parent.width/2
        height: parent.height
        anchors.right: parent.right

        model: jsonModel1.model

        section.delegate: sectionDelegate
        section.property: "title"
        section.criteria: ViewSection.FirstCharacter

        delegate: Component {
            Text {
                width: parent.width
                horizontalAlignment: Text.AlignLeft
                font.pixelSize: 14
                color: "black"
                text: index + "/ Pokemon #" + model.ids.unique

                Text {
                    anchors.fill: parent
                    anchors.rightMargin: 5
                    horizontalAlignment: Text.AlignRight
                    font.pixelSize: 12
                    color: "gray"
                    text: self.pokemon_names[model.ids.unique]
                }
            }
        }
    }

    Component {
        id: sectionDelegate
        Rectangle {
            color: "gray"
            width: parent.width
            height: sectionLabel.height
            Text {
                id: sectionLabel
                anchors.horizontalCenter: parent.horizontalCenter
                font.bold: true
                font.pixelSize: 16
                color: "white"
                style: Text.Raised
                text: section
            }
        }
    }

    function parseXML(url) {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState == XMLHttpRequest.HEADERS_RECEIVED) {
            } else if (xhr.readyState == XMLHttpRequest.DONE) {

                var root = xhr.responseXML.documentElement;
                // Go through recenttracks children
                var recentTracks = root.childNodes[1];
                for (var i=0; i < recentTracks.childNodes.length; ++i)
                {
                    var child = recentTracks.childNodes[i];
                    for (var j=0; j < child.childNodes.length; ++j)
                    {
                        if (child.childNodes[j].nodeName == "string")
                        {
                            console.log(child.childNodes[j].childNodes[0].nodeValue); // [!]
                        }
                    }
                }

                var doc = xhr.responseXML.documentElement;
                console.log("xhr length: " + doc.childNodes.length );

                for (var i = 0; i < doc.childNodes.length; ++i) {
                    var child = doc.childNodes[i];

                    if ( child.nodeName ===  "string") {

                        var key = child.attributes[0].nodeValue;
                        var id = key.substring(key.lastIndexOf("_")+1);
//                        var id = /​.​*​_​(​.​*​)​/​.​exec(key)​[​1]​;
//                        var id = key.split("_").pop();

                        for (var j = 0; j < child.childNodes.length; ++j) {

                            var subchild = child.childNodes[j];
                            var value = subchild.nodeValue;

//                            console.log(id + " = " + value);
                            self.pokemon_names[id] = value;
                        }
                    }
                }
            }
        }

        xhr.open("GET", url);
        xhr.send();
    }
}
