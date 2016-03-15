package org.strangeway.disttest

import java.security.MessageDigest

class KernelSource {
    private static final basePath = "../kernelSources"
    String path

    static KernelSource findByVersion(String version){
        KernelSource kernelSource = new KernelSource()
        kernelSource.path = "$basePath/$version"
        assert new File(kernelSource.path).isDirectory()
        return kernelSource
    }

    String getHash(){
        return Utils.calcHash(path)
    }
}
