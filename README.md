# Project

Constraint Solver.

## Description

Constraint solver for binary constraints.

Employing 2-way branching with both the Forward Checking and the Maintaining Arc Consistency algorithms.

## Getting Started

### Dependencies

* Java Version: openjdk 19.0.1

### Executing program

* Please run the following command
```
cd ConstraintSolver/src
javac Solver.java BinaryCSPReader.java
jar -cvfm outputs/ConstraintSolver.jar outputs/ConstraintSolver.mf *.class 
```
