import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary
import org.strangeway.disttest.Utils

import java.util.concurrent.ConcurrentHashMap

class Starter {
    public static void main(String[] args) {
        Initramfs initramfs = new Initramfs("hello.sh")
        initramfs.getArtifact() //Hack to prevent races in other threads
        Map<String, Distro> distros = new HashMap<>()
        List<Thread> threads = new ArrayList<>()
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>()
        for(version in ["linux-3.18.28","linux-2.6.32.71"]){
            KernelBinary kernelBinary = new KernelBinary(version, "acpi")
            Distro distro = new Distro(kernelBinary, initramfs)
            distros[version] = distro
            String ver = version
            threads.add(Thread.start {
                results[ver] = distro.run()
            })
        }
        while(true){
            boolean needBreak = true
            threads.each {t -> if(t.alive){needBreak = false}}
            Thread.sleep(100)
            distros.each { k, v ->
                println Utils.renderProgress(v)
            }

            if(needBreak) {
                break
            }
        }

        results.each {k,v->
            print k + " " + v
        }
    }
}

