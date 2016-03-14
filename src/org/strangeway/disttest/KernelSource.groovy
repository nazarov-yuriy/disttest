package org.strangeway.disttest

class KernelSource {
    private static final basePath = "../kernelSources"
    String path

    static KernelSource findByVersion(String version){
        KernelSource kernelSource = new KernelSource()
        kernelSource.path = "$basePath/$version"
        assert new File(kernelSource.path).isDirectory()
        return kernelSource
    }
}
