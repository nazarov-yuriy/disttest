package org.strangeway.disttest

import groovy.transform.CompileStatic

@CompileStatic
class Distro {
    KernelBinary kernelBinary
    Initramfs initramfs

    Distro(KernelBinary _kernelBinary, Initramfs _initramfs) {
        kernelBinary = _kernelBinary
        initramfs = _initramfs
    }

    String run() {
        Process process = new ProcessBuilder("qemu-system-x86_64",
                "-machine", "accel=kvm",
                "-m", "512",
                "-kernel", kernelBinary.getArtifact().path,
                "-initrd", initramfs.getArtifact().path,
                "-chardev", "stdio,id=charserial0",
                "-device", "isa-serial,chardev=charserial0,id=serial0",
                "-nographic", "-nodefconfig", "-nodefaults"
        ).start()
        process.waitForOrKill(5000) //ToDo: make value at least configurable
        assert 0 == process.exitValue()
        try {
            return process.getText().replace("\n", "").replace("\r", "");
        } catch (ignored) {
            return "<KILLED>"
        }
    }

    void close() {
        kernelBinary.close()
    }
}
