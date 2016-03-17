package org.strangeway.disttest

class KernelConfig {
    static private final String basePath = "kernelConfigs"
    String version
    String name

    KernelConfig(String _version, String _name){
        version = _version
        name = _name
    }

    String getPath(){
        String fullVersion = version
        String path = "$basePath/$fullVersion/$name" //linux-4.4.6

        if(!new File(path).exists()){
            fullVersion = fullVersion.replaceFirst(/\.\d+$/, "")
            path = "$basePath/$fullVersion/$name" //linux-4.4
        }
        if(!new File(path).exists()){
            fullVersion = fullVersion.replaceFirst(/\.\d+$/, "")
            path = "$basePath/$fullVersion/$name" //linux-4
        }

        assert new File(path).exists()
        return path
    }

    String getHash(){
        return Utils.calcHash(new File(getPath()).bytes)
    }
}
