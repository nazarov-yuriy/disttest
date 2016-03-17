import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary

class Starter {
    public static void main(String[] args) {
        KernelBinary kernelBinary318 = new KernelBinary("linux-3.18.28", "acpi")
        KernelBinary kernelBinary44 = new KernelBinary("linux-4.4.4", "acpi")
        KernelBinary kernelBinary45 = new KernelBinary("linux-4.5", "acpi")

        Initramfs initramfs = new Initramfs("hello.sh")

        Distro distro318 = new Distro(kernelBinary318, initramfs)
        print distro318.run()
        Distro distro44 = new Distro(kernelBinary44, initramfs)
        print distro44.run()
        Distro distro45 = new Distro(kernelBinary45, initramfs)
        print distro45.run()
    }
}

