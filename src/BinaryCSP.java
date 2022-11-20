import java.util.*;

public final class BinaryCSP {
  private int[][] domainBounds;
  private ArrayList<BinaryConstraint> constraints;
  private ArrayList<Variable> varList;
  private ConstraintList constraintList;

  public BinaryCSP(int[][] db, ArrayList<BinaryConstraint> c, ArrayList<Variable> v, ConstraintList cl) {
    domainBounds = db;
    constraints = c;
    varList = v;
    constraintList = cl;
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("CSP:\n");
    for (int i = 0; i < domainBounds.length; i++)
      result.append("Var " + i + ": " + domainBounds[i][0] + " .. " + domainBounds[i][1] + "\n");
    for (BinaryConstraint bc : constraints)
      result.append(bc + "\n");
    return result.toString();
  }

  public int getNoVariables() {
    return domainBounds.length;
  }

  public int getLB(int varIndex) {
    return domainBounds[varIndex][0];
  }

  public int getUB(int varIndex) {
    return domainBounds[varIndex][1];
  }

  public ArrayList<BinaryConstraint> getConstraints() {
    return constraints;
  }

  public ArrayList<Variable> getVarList() {
    return varList;
  }

  public ConstraintList getConstraintList() {
    return constraintList;
  }
}
