import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary

KernelBinary kernelBinary = new KernelBinary("linux-4.4.4", "acpi")
//println kernelBinary.getBinary()

Initramfs initramfs = new Initramfs()
//println initramfs.getInitramfs()

Distro distro = new Distro(kernelBinary, initramfs)
print distro.run()
