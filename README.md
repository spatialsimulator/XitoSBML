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
5. XitoSBML will ask whether you want to add SBML objects (Species, Reactions and Parameters) to your model. If you just want to conver your image files to Spatial SBML, then just click [No]
6. Save converted SBML model
7. XitoSBML will display converted SBML document and Domain Hierarcy of your model to let you confirm the converted result
8. Exported SBML and merged image will be stored (where you specified in step 6)

Here is a screencast of above procedure.
![example1](./screenshots/example1.gif "Convert segmented images to Spatial SBML")

### Convert segmented images to Spatial SBML (as a mathematical model)
Next example will create Spatial SBML model and add SBML objects to the model and make it as a mathematical model. The following figure represents the diagram of this example model. Note that species `A` and `B` diffuses inside nucleus and cytosol with the given diffusion coefficient (`Dnuc` and `Dcyt`) respectively.

![diagram of example2](./screenshots/example2.png "Diagram of example model 2")

1. Launch ImageJ (Fiji)
2. Load original images (not mandatory) and its segmented images
3. Click [Plugins] -> [XitoSBML] -> [run Spatial Image SBML Plugin]
4. On [DomainType Namer] dialog, assign an opened image to each organelle (ex. nucleus, cytosol)
5. XitoSBML will ask whether you want to add SBML objects (Species, Reactions and Parameters) to your model. If you just want to conver your image files to Spatial SBML, then just click [Yes]
6. In [Species] tab, add species `A` and `B`. Note that species `A` will be added to Nucleus and `B` will be added to Cytosol
7. In [Diffusion] tab, add `Diffusion coefficient` for species `A (Dnuc)` and `B (Dcyt)`
8. In [Reaction] tab, add a reaction (`A -> B`) and its kinetic law. Please do not forget to assign reactants and products fot the reaction
9. Save converted SBML model

Here is a screencast of above procedure.
![example2](./screenshots/example2.gif "Convert segmented images to Spatial SBML")

The mathematical (spatial) model created by this example can be executed by spatial simulator which supports Spatial SBML. For example, you can use our [SpatialSimulator](https://github.com/spatialsimulator) for executing your model. We have provided [Docker image](https://github.com/funasoul/docker-spatialsim) for SpatialSimulator, so that you can easily install and run SpatialSimulator on your environment.

```sh
git clone https://github.com/funasoul/docker-spatialsim.git
cd ./docker-spatialsim/
# Copy example2.xml to this directory
./docker-spatialsim.sh -t 0.1 -d 0.0001 -o 100 example2.xml
```

SpatialSimulator will generate numbers and images of simulation result.

Species A             |  Species B
:-------------------------:|:-------------------------:
![example2 species A](./screenshots/example2_a.gif "Simulation result of example2.xml (species A)") | ![example2 species B](./screenshots/example2_b.gif "Simulation result of example2.xml (species B)")

### Licensing
------------------
XitoSBML is licensed under the Apache License, Version 2.0. See [LICENSE](https://github.com/spatialsimulator/XitoSBML/blob/master/LICENSE-2.0.txt) for the full license text.
