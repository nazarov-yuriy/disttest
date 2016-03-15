package org.strangeway.disttest

class Distro {
    KernelBinary kernelBinary
    Initramfs initramfs

    Distro(KernelBinary _kernelBinary, Initramfs _initramfs){
        kernelBinary = _kernelBinary
        initramfs = _initramfs
    }

    String run() {
        Process process = new ProcessBuilder("qemu-system-x86_64",
                "-M", "pc-1.0",
                "-m", "64",
                "-kernel", kernelBinary.getArtifact().path,
                "-initrd", initramfs.getArtifact().path,
                "-append", "'console=tty0 console=ttyS0,115200n8'",
                "-chardev", "stdio,id=charserial0",
                "-device", "isa-serial,chardev=charserial0,id=serial0",
                "-nographic", "-nodefconfig", "-nodefaults"
        ).start()
        process.waitFor()
        assert 0 == process.exitValue()
        return process.getText();
    }
}
