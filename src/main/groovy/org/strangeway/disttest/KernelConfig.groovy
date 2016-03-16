package org.strangeway.disttest

class KernelConfig {
    static private final String basePath = "kernelConfigs"
    String version
    String name

    KernelConfig(String _version, String _name){
        version = _version
        name = _name
    }

    String getPath(){ //ToDo: implement actual search for config
        String path = "$basePath/$version/$name"
        assert new File(path).exists()
        return path
    }

    String getHash(){
        return Utils.calcHash(new File(getPath()).bytes)
    }
}
