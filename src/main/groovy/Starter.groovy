import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary
import org.strangeway.disttest.KernelRepo
import org.strangeway.disttest.KernelSourcePool
import org.strangeway.disttest.Utils

import java.util.concurrent.ConcurrentHashMap

class Starter {
    public static void threadedDemo(){
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        Initramfs initramfs = new Initramfs("hello.sh")
        initramfs.getArtifact() //Hack to prevent races in other threads
        Map<String, Distro> distros = new HashMap<>()
        List<Thread> threads = new ArrayList<>()
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>()
        for (version in ["linux-2.6.32.71", "linux-3.18.28"]) {
            KernelBinary kernelBinary = new KernelBinary(version, version.replace(/linux-/, "v"), "acpi", kernelSourcePool)
            Distro distro = new Distro(kernelBinary, initramfs)
            distros[version] = distro
            String ver = version
            threads.add(Thread.start {
                results[ver] = distro.run()
                distro.close()
            })
        }
        while (true) {
            boolean needBreak = true
            threads.each { t ->
                if (t.alive) {
                    needBreak = false
                }
            }
            Thread.sleep(100)
            distros.each { k, v ->
                if (results.containsKey(k)) {
                    println String.format("%-20s %-100s", k, results[k])
                    //ToDo: use more reliable method to clear terminal
                } else {
                    println String.format("%-20s %-100s", k, Utils.renderProgress(v))
                }
            }
            print String.format("%c[%dA", 0x1B, distros.size());

            if (needBreak) {
                break
            }
        }

        results.each { k, v ->
            println String.format("%-20s %-100s", k, v)
        }
    }

    public static void bisectDemo(){
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        Initramfs initramfs = new Initramfs("hello.sh")
        initramfs.getArtifact()

        String[] commits = KernelRepo.getVersionBetweenTags("v3.18", "v3.18.28")
        int broken = 0
        int working = commits.length-1
        while(broken+1 != working){
            int testIndex = (broken+working)/2
            KernelBinary kernelBinary = new KernelBinary("linux-3.18", commits[testIndex], "acpi", kernelSourcePool)
            println "Testing commit #$testIndex($broken..$working)"+commits[testIndex]
            Distro distro = new Distro(kernelBinary, initramfs)
            String res = distro.run()
            distro.close()
            println "Res: $res\n"
            if(res == "<KILLED>"){
                broken = testIndex
            }else{
                working = testIndex
            }
        }
    }

    public static void bisectSemtexDemo(){
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        Initramfs initramfs = new Initramfs("cve-2013-2094.sh")
        initramfs.getArtifact()

        String[] commits = KernelRepo.getVersionBetweenTags("v3.0", "v3.0.101")
        int broken = 0
        int working = commits.length-1
        while(broken+1 != working){
            int testIndex = (broken+working)/2
            KernelBinary kernelBinary = new KernelBinary("linux-3.0", commits[testIndex], "acpi_perf_sec", kernelSourcePool)
            println "Testing commit #$testIndex($broken..$working)"+commits[testIndex]
            Distro distro = new Distro(kernelBinary, initramfs)
            String res = distro.run()
            distro.close()
            println "Res: $res\n"
            if(res == "uid=0(root) gid=0(root) groups=1(user)"){
                broken = testIndex
            }else{
                working = testIndex
            }
        }
    }

    public static void main(String[] args) {
        bisectSemtexDemo()
    }
}

