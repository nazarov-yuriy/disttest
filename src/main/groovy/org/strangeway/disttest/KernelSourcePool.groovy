package org.strangeway.disttest

class KernelSourcePool {
    volatile boolean[] slotsAvailability = [true, true]

    synchronized KernelSource getKernelSource(String version, String commit) {
        while (!(slotsAvailability[0] || slotsAvailability[1])) {
            try {
                wait();
            } catch (ignored) {
            }
        }
        if (slotsAvailability[0]) {
            slotsAvailability[0] = false
            return new KernelSource(version, commit, 0)
        } else {
            slotsAvailability[1] = false
            return new KernelSource(version, commit, 1)
        }
    }

    synchronized void putKernelSource(KernelSource kernelSource) {
        kernelSource.close()
        slotsAvailability[kernelSource.getSlot()] = true
        notifyAll();
    }
}
