package org.strangeway.disttest

class KernelConfig {
    static private final String basePath = "../kernelConfigs"
    public String path
    static KernelConfig findByname(String name){
        KernelConfig kernelConfig = new KernelConfig()
        kernelConfig.path = "$basePath/$name"
        assert new File(kernelConfig.path).isFile()
        return kernelConfig
    }
}
