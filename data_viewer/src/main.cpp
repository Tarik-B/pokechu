#include <QGuiApplication>
#include <QQmlApplicationEngine>

#include <QLocale>
#include <QTranslator>

#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>

#include <QFile>
#include <QJsonArray>
#include <QJsonDocument>
#include <QJsonObject>

#include "tree_model.h"
#include "json_entry.h"

void loadValue(const QJsonValue& value, TreeItem* parent, TreeModel* model)
{
   if(value.isObject()) {
      auto object = value.toObject();
      for(auto it=object.begin(); it!=object.end(); ++it){
         JsonEntry entry;
         entry.setKey(it.key());
         entry.setType(QJsonValue::Object);
         if(it.value().isArray() || it.value().isObject()){
            auto child = new TreeItem(QVariant::fromValue(entry));
            loadValue(it.value(), child, model);
            model->addItem(parent, child);
         }
         else {
            entry.setValue(it.value().toVariant());
            auto child = new TreeItem(QVariant::fromValue(entry));
            model->addItem(parent, child);
         }
      }
   }
   else if(value.isArray()){
      int index = 0;
      auto array = value.toArray();
      for(auto&& element: array){
         JsonEntry entry;
         entry.setKey("[" + QString::number(index) + "]");
         entry.setType(QJsonValue::Array);
         auto child = new TreeItem(QVariant::fromValue(entry));
         model->addItem(parent, child);
         loadValue(element, child, model);
         ++index;
      }
   }
   else {
      JsonEntry entry;
      entry.setValue(value.toVariant());
      entry.setType(value.type());
      auto child = new TreeItem(QVariant::fromValue(entry));
      model->addItem(parent, child);
   }
}

void populateModel(TreeModel& model)
{
   QFile jsonFile{":/resources/pokemon_list.json"};
   if(!jsonFile.open(QIODevice::ReadOnly | QIODevice::Text)){
      qCritical() << "error: json file cannot be open";
      return;
   }

   QJsonParseError error;
   auto doc = QJsonDocument::fromJson(jsonFile.readAll(), &error);
   qDebug() << "loading json file:" << error.errorString();

   auto root = doc.isArray() ? QJsonValue(doc.array()) : QJsonValue(doc.object());
   loadValue(root, model.rootItem(), &model);
}


int main(int argc, char *argv[])
{
    QGuiApplication app(argc, argv);

    qputenv("QML_XHR_ALLOW_FILE_READ", QByteArray("1"));

    QQmlApplicationEngine engine;
    const QUrl url(u"qrc:/qml/main.qml"_qs);

    auto jsonModel = new TreeModel(&engine);
    populateModel(*jsonModel);

    engine.rootContext()->setContextProperty("jsonModel", jsonModel);

    QObject::connect(&engine, &QQmlApplicationEngine::objectCreated,
                     &app, [url](QObject *obj, const QUrl &objUrl) {
        if (!obj && url == objUrl)
            QCoreApplication::exit(-1);
    }, Qt::QueuedConnection);
    engine.load(url);

    return app.exec();
}
