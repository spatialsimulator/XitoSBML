#include <stdio.h>
#include "Sbml/SBMLTypes.h"

/*
  adds species to the exported xml file  
*/

int main(int argc, char **argv)
{
  SBMLDocument_t *d = readSBML(argv[1]);
  Model_t *m = SBMLDocument_getModel(d);
  ListOf_t *lspecies = Model_getListOfSpecies(m);
  ListOf_t *reactant;
  ListOf_t *product;
  ListOf_t *lreaction;
  Reaction_t *reaction;
  
  
  
  

  return 0;
}
