#!/bin/sh

/bin/rm ./zip/* 
/users/ii/thesis/SBMLPlugin/createjar.sh
/bin/mv ./zip/Spatial_SBML.jar /Applications/Fiji.app/plugins
