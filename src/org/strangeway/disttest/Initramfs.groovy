package org.strangeway.disttest

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream
import org.apache.commons.compress.archivers.cpio.CpioConstants
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

class Initramfs {
    ToolsBinary toolsBinary

    Initramfs(){
        toolsBinary = new ToolsBinary("busybox-1.24.1")
    }

    private static void addDir(CpioArchiveOutputStream archive, String path) {
        CpioArchiveEntry entry = new CpioArchiveEntry(path)
        entry.setMode(CpioConstants.C_ISDIR | CpioConstants.C_IXUSR | CpioConstants.C_IRUSR)
        archive.putArchiveEntry(entry)
        archive.closeArchiveEntry()
    }

    private static void addSymlink(CpioArchiveOutputStream archive, String path, String target) {
        CpioArchiveEntry entry = new CpioArchiveEntry(path)
        entry.setSize(target.length());
        entry.setMode(CpioConstants.C_ISLNK | CpioConstants.C_IXUSR | CpioConstants.C_IRUSR)
        archive.putArchiveEntry(entry)
        archive.write(target.bytes)
        archive.closeArchiveEntry()
    }

    private static void addFile(CpioArchiveOutputStream archive, String path, byte[] contents){
        CpioArchiveEntry entry = new CpioArchiveEntry(path)
        entry.setSize(contents.length);
        entry.setMode(CpioConstants.C_ISREG | CpioConstants.C_IXUSR | CpioConstants.C_IRUSR);
        archive.putArchiveEntry(entry);
        archive.write(contents);
        archive.closeArchiveEntry();
    }

    private static void addCharDev(CpioArchiveOutputStream archive, String path, long major, long minor) {
        CpioArchiveEntry entry = new CpioArchiveEntry(path)
        entry.setMode(CpioConstants.C_ISCHR | CpioConstants.C_IXUSR | CpioConstants.C_IRUSR)
        entry.setRemoteDeviceMaj(major)
        entry.setRemoteDeviceMin(minor)
        archive.putArchiveEntry(entry)
        archive.closeArchiveEntry()
    }

    String getInitramfs(){
        GzipCompressorOutputStream gz = new GzipCompressorOutputStream( new FileOutputStream(new File("../initramfs.cpio.gz")) )
        CpioArchiveOutputStream cpio = new CpioArchiveOutputStream(gz);
        for( dir in ['bin', 'dev', 'etc', 'proc', 'sys']) {
            addDir(cpio, dir);
        }
        addCharDev(cpio, 'dev/console', 5, 1)
        addCharDev(cpio, 'dev/null', 1, 3)
        addCharDev(cpio, 'dev/ttyS0', 4, 64)
        addCharDev(cpio, 'dev/zero', 1, 5)
        addFile(cpio, 'bin/busybox', new File(toolsBinary.getBinary()).bytes)
        addSymlink(cpio, 'bin/sh', 'busybox')
        addSymlink(cpio, 'init', 'bin/busybox')
        addFile(cpio, 'etc/rcS', "#!/bin/sh\npoweroff".getBytes())
        addFile(cpio, 'etc/inittab', ("::sysinit:/etc/rcS\n" + "ttyS0::respawn:/bin/busybox getty -nl /bin/sh 38400 ttyS0\n").getBytes());
        addFile(cpio, 'etc/fstab', "proc            /proc        proc    defaults          0       0\n".getBytes());
        cpio.close()
        return "../initramfs.cpio.gz"
    }
}
