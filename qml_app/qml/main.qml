import QtQuick

Window {
    width: 640
    height: 480
    visible: true
    title: qsTr("Hello World")

    LineEdit {
        id: inputField
        anchors.centerIn: parent
        width: parent.width / 2
        height: parent.height / 10

        borderColor: "white"
    }

}
