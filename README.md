Spatial SBML Plugin
======================
This program is a plugin for ImageJ which creates an Spatial SBML Model with segmented images

How to Compile
------------------
### Dependencies ###
Spatial SBML Plugin requires the following third-party application.
Please install the following before using Spatial SBML Plugin.

+ [libsbml 5.11.4](http://sbml.org/Software/libSBML "libsbml")
+ [Fiji Is Just ImageJ](http://fiji.sc/Fiji "Fiji Is Just ImageJ")
+ [ImageJ 3D Viewer](http://3dviewer.neurofly.de/ "ImageJ 3D Viewer") version 1.5 or higher
+ [Apache Ant](http://ant.apache.org/ "Apache Ant")

####How to install apache-ant using macports####

    % sudo port install apache-ant

#### Build Spatial SBML Plugin ####
    % git clone https://funa@fun.bio.keio.ac.jp:8443/git/Spatial_SBML_Plugin.git
    % ant main

*When using the program for the first time execute below as well

    % ant patch
    % ant mv

It will modify info.plist and add library.

How to Use
-------------------

License
------------------
