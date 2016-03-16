package org.strangeway.disttest

class ToolsSource {
    private static final basePath = "toolsSources"
    String path

    static ToolsSource findByVersion(String version){
        ToolsSource toolsSource = new ToolsSource()
        toolsSource.path = "$basePath/$version"
        assert new File(toolsSource.path).isDirectory()
        return toolsSource
    }

    String getHash(){
        return Utils.calcHash(path) //ToDo: use version instead
    }
}
