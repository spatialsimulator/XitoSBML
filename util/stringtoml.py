from sympy import *
from sympy.printing.mathml import print_mathml
import sys
x,y,z=symbols('x y z')
j,k,l=symbols('j k l')

print "Variables x,y,z" 
print "Parameters j,k,l\n"

print "Enter equation"
input = sys.stdin.readline()
#print_mathml(sqrt(1/x))
print_mathml(input)
