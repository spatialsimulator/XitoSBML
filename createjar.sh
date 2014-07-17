/bin/cp ./plugins.config ./src/*.java ./bin/*.class ./zip && cd zip && LC_ALL="C" jar cvf Spatial_SBML.jar plugins.config *.class *.java
