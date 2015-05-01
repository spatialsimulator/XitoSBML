#! /bin/sh

/bin/mv ./Spatial_SBML.jar ./libsbmlj.jar /Applications/Fiji.app/plugins
/bin/mv ./libsbml.5.11.0.dylib ./libsbmlj.jnilib /Applications/Fiji.app
/usr/bin/patch /Applications/Fiji.app/Contents/Info.plist < info.patch
