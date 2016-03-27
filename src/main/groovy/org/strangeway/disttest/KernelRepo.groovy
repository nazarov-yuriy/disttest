package org.strangeway.disttest

class KernelRepo {
    private static final gitUrl = "git@server.home:/home/git/linux-stable.git"

    static String getRepoPath() {
        File repoDir = new File("kernelRepo/linux-stable")
        if (repoDir.isDirectory()) {
            return repoDir.getPath()
        }
        Process process = new ProcessBuilder("git", "clone", "--no-checkout", gitUrl).directory(new File("kernelRepo")).start();
        process.consumeProcessOutput()
        process.waitFor()
        assert 0 == process.exitValue()
        return repoDir.getPath()
    }

    static String[] getVersionBetweenTags(String from, String to){
        Process process = new ProcessBuilder("git", "log", "--pretty=format:%H", "$from^..$to").directory(new File(getRepoPath())).start();
        StringBuilder sout = new StringBuilder()
        StringBuilder serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        return sout.tokenize("\n").reverse()
    }

    static def byLastNumber = {a,b ->
        def ma = a =~ /(\d+)$/
        def mb = b =~ /(\d+)$/
        ma[0][1] as int <=> mb[0][1] as int
    }

    static String[] getSubLevels(String patchLevel){
        Process process = new ProcessBuilder("git", "tag").directory(new File(getRepoPath())).start();
        StringBuilder sout = new StringBuilder()
        StringBuilder serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        return [
                sout.tokenize("\n").findAll({it =~ /^$patchLevel.\d+$/}).sort(byLastNumber)
        ].flatten()
    }

    static String[] getPatchLevels(){
        Process process = new ProcessBuilder("git", "tag").directory(new File(getRepoPath())).start();
        StringBuilder sout = new StringBuilder()
        StringBuilder serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        return [
                sout.tokenize("\n").findAll({it =~ /^v2.6.3[2-9]+$/}).sort(byLastNumber),
                sout.tokenize("\n").findAll({it =~ /^v3.\d+$/}).sort(byLastNumber),
                sout.tokenize("\n").findAll({it =~ /^v4.\d+$/}).sort(byLastNumber)
                ].flatten()
    }
}
