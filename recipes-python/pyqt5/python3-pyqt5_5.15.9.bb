SUMMARY = "Python qt5 Bindings"
HOMEPAGE = "https://www.riverbankcomputing.com/software/pyqt"
SECTION = "devel/python"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d32239bcb673463ab874e80d47fae504"

inherit pypi python3-dir python3native qmake5 qmake5_paths

SRC_URI[sha256sum] = "dc41e8401a90dc3e2b692b411bd5492ab559ae27a27424eed4bd3915564ec4c0"
PYPI_PACKAGE = "PyQt5"

S = "${WORKDIR}/PyQt5-${PV}"

DEPENDS = " \
    python3 \
    python3-ply-native \
    python3-pyqt-builder-native \
    python3-toml-native \
    qtbase \
    qtdeclarative \
    qtquickcontrols2 \
    sip \
    sip-native \
"

DISABLED_FEATURES = " \
    PyQt_Desktop_OpenGL \
    PyQt_Accessibility \
    PyQt_SessionManager \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', '', 'PyQt_OpenGL', d)} \
"

PYQT_MODULES = " \
    QtCore \
    QtGui \
    QtNetwork \
    QtXml \
    QtNetwork \
    QtQml \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'QtQuick QtWidgets QtQuickWidgets', '', d)} \
"

do_configure:prepend() {
    local i
    local extra_args

    cd ${S}

    for i in ${DISABLED_FEATURES}; do
        extra_args+=" --disabled-feature=${i}"
    done

    for i in ${PYQT_MODULES}; do
        extra_args+=" --enable=${i}"
    done

    sip-build \
        --verbose \
        --confirm-license \
        --scripts-dir="${bindir}" \
        --build-dir="${B}" \
        --target-dir="${PYTHON_SITEPACKAGES_DIR}" \
        --qmake=${OE_QMAKE_QMAKE} \
        --no-make \
        --enable=pylupdate \
        --enable=pyrcc \
        --enable=Qt \
        --enable=QtCore \
        --no-dbus-python \
        ${extra_args}

    QMAKE_PROFILES=${B}/PyQt5.pro
}

do_compile:append() {
    sed -i "s,${STAGING_DIR_TARGET},," ${B}/inventory.txt
}

do_install:append() {
    sed -i "s,exec .*nativepython3,exec ${bindir}/python3," ${D}/${bindir}/*
}

pyqt_fix_sources() {
    find ${PKGD}/usr/src/debug/${PN} -type f -exec sed -i "s,\(${B}\|${S}\),/usr/src/debug/${PN}/${PV},g" {} \;
}
PACKAGESPLITFUNCS += "pyqt_fix_sources"

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR} ${OE_QMAKE_PATH_PLUGINS}"
RDEPENDS:${PN} = " \
    python3-core \
    python3-pyqt5-sip \
    qtbase \
    qtdeclarative  \
    qtquickcontrols2 \
    qtquickcontrols2-mkspecs \
    sip \
"
