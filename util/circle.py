from sympy import * 
from sympy.printing.mathml import print_mathml
import sys
r,p=symbols('r p')
pi=3.14159

def area(r):
  return r + "**2*pi"
  
def volume(r):
  return r + "**3*4/3*pi"


print "Enter radius"
r = sys.stdin.readline()
print"area"
print_mathml(area(r))

print "volume"
print_mathml(volume(r))
