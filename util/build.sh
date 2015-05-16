#!/bin/sh

/bin/rm ./zip/* 
/users/ii/thesis/SBMLPlugin/createjar.sh
/bin/cp ./zip/Spatial_SBML.jar ./plugins
/bin/mv ./zip/Spatial_SBML.jar /Applications/Fiji.app/plugins
