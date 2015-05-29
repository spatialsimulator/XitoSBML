from sympy import *
from sympy.printing.mathml import print_mathml
import sys
x,y,z=symbols('x y z')

input = sys.stdin.readline()
#print_mathml(sqrt(1/x))
print_mathml(input)
