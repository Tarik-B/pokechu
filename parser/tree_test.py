from anytree import Node, RenderTree
from anytree.importer import JsonImporter
from anytree.exporter import JsonExporter


if __name__ == "__main__":
    root = Node("root")
    marc = Node("Marc", parent=root)
    lian = Node("Lian", parent=marc)
    dan = Node("Dan", parent=root)
    jet = Node("Jet", parent=dan)
    jan = Node("Jan", parent=dan)
    joe = Node("Joe", parent=dan)

    print(root)
    Node('/root')
    print(joe)
    Node('/root/Dan/Joe')

    for pre, fill, node in RenderTree(root):
        print("%s%s" % (pre, node.name))

    print(dan.children)

    # Export
    exporter = JsonExporter(indent=2, sort_keys=True)
    root_exported = exporter.export(root)
    print(root_exported)

    # Import
    importer = JsonImporter()
    root_new = importer.import_(root_exported)
    print(RenderTree(root_new))

    data = '''
        {
          "a": "root",
          "children": [
            {
              "a": "sub0",
              "children": [
                {
                  "a": "sub0A",
                  "b": "foo"
                },
                {
                  "a": "sub0B"
                }
              ]
            },
            {
              "a": "sub1"
            }
          ]
        }'''
    root = importer.import_(data)
    print(RenderTree(root))