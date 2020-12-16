# Copyright 2020 NXP
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Kernel tools for Linux"
DESCRIPTION = "Kernel tools for Linux"
LICENSE = "GPLv2"

inherit linux-kernel-base kernel-arch
inherit kernelsrc

S = "${WORKDIR}/${BP}"

KERNEL_PCITEST_SRC ?= " \
             include \
             tools/arch \
             tools/build \
             tools/include \
             tools/lib \
             tools/Makefile \
             tools/pci \
             tools/spi \
             tools/virtio \
             tools/scripts \
"
do_configure[depends] += "virtual/kernel:do_shared_workdir"

do_configure[prefuncs] += "copy_pci_source_from_kernel"
python copy_pci_source_from_kernel() {
    sources = (d.getVar("KERNEL_PCITEST_SRC") or "").split()
    src_dir = d.getVar("STAGING_KERNEL_DIR")
    dest_dir = d.getVar("S")
    bb.utils.mkdirhier(dest_dir)
    for s in sources:
        src = oe.path.join(src_dir, s)
        dest = oe.path.join(dest_dir, s)
        if not os.path.exists(src):
            bb.fatal("Path does not exist: %s. Maybe PERF_SRC does not match the kernel version." % src)
        if os.path.isdir(src):
            oe.path.copyhardlinktree(src, dest)
        else:
            bb.utils.copyfile(src, dest)
}

EXTRA_OEMAKE = '\
    CROSS_COMPILE=${TARGET_PREFIX} \
    ARCH=${ARCH} \
    CC="${CC}" \
    AR="${AR}" \
    LD="${LD}" \
    DESTDIR="${D}" \
'

do_compile() {
    unset CFLAGS
    oe_runmake -C tools/pci
    oe_runmake -C tools/spi
}

do_install() {
    unset CFLAGS
    oe_runmake -C tools/pci install
    oe_runmake -C tools/spi install
}

PACKAGES =+ "${PN}-pci ${PN}-spi "

FILES_${PN}-pci = "${bindir}/pci*"
FILES_${PN}-spi = "${bindir}/spi*"

PACKAGE_ARCH = "${MACHINE_ARCH}"
