#!/bin/sh

wcommand=`which wget`
if [$? -gt 0]; then
  sudo port install wget
fi

version="libSBML-5.11.4-Source/"
core="libSBML-5.11.4-core-plus-packages-src.zip"
spatial="spatial-5.11.4-beta-1.zip" 
req="req-5.11.4-beta-1.zip"
$wcommand http://sourceforge.net/projects/sbml/files/libsbml/5.11.4/stable/${core}
$wcommand http://sourceforge.net/projects/sbml/files/libsbml/5.11.4/experimental/source/${spatial}
$wcommand http://sourceforge.net/projects/sbml/files/libsbml/5.11.4/experimental/source/${req}
/usr/bin/unzip -o ${core} 
/bin/mv ${req} ${spatial} ${version} 
cd ${version}
/usr/bin/unzip -o ${req} 
/usr/bin/unzip -o ${spatial}
/bin/mkdir build
cd build
which swig 
if [$? -gt 0]; then
  sudo port install swig
fi

cmake=`which cmake`
if [$? -gt 0]; then
  sudo port install swig
fi

${cmake} ..
