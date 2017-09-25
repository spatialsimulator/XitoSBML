XitoSBML: Spatial SBML Plugin for ImageJ
======================
XitoSBML is a plugin for [ImageJ](https://imagej.net/Welcome) which creates an Spatial SBML Model with segmented images

![XitoSBML](./screenshots/xitosbml.png "XitoSBML: Spatial SBML Plugin for ImageJ")

How to Compile
------------------
### Dependencies ###
XitoSBML requires the following third-party application.

+ [JSBML](http://sbml.org/Software/JSBML"JSBML")
+ [Fiji Is Just ImageJ](http://fiji.sc/Fiji "Fiji Is Just ImageJ")
+ [ImageJ 3D Viewer](http://3dviewer.neurofly.de/ "ImageJ 3D Viewer") version 1.5 or higher
+ [Maven](https://maven.apache.org/ "Maven")


#### Build Spatial SBML Plugin ####
    % git clone https://github.com/spatialsimulator/XitoSBML.github
    % mvn install -Dimagej.app.directory=/path/to/ImageJ

How to Use
-------------------
### Convert segmented images to Spatial SBML
1. Launch ImageJ (Fiji)
2. Load original images (not mandatory) and its segmented images
3. Click [Plugins] -> [XitoSBML] -> [run Spatial Image SBML Plugin]
4. On [DomainType Namer] dialog, assign an opened image to each organelle (ex. nucleus, cytosol)
5. XitoSBML will ask whether you want to add SBML objects (Species, Reactions and Parameters) to your model. If you just want to conver your image files to Spatial SBML, then just click [No].
6. Save converted SBML model
7. XitoSBML will display converted SBML document and Domain Hierarcy of your model to let you confirm the converted result
8. Exported SBML and merged image will be stored (where you specified in step 6)

Here is a screencast of above procedure.
![XitoSBML](./screenshots/example1.gif "Convert segmented images to Spatial SBML")

### Licensing
------------------
XitoSBML is licensed under the Apache License, Version 2.0. See [LICENSE](https://github.com/spatialsimulator/XitoSBML/blob/master/LICENSE-2.0.txt) for the full license text.
