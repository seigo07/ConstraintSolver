import java.util.*;
import java.util.stream.IntStream;

public final class BinaryCSP {
  private int[][] domainBounds;
  private ArrayList<BinaryConstraint> constraints;
  // NEWS
  private ArrayList<Variable> variables;
  private ConstraintList constraintList;

  public BinaryCSP(int[][] db, ArrayList<BinaryConstraint> c, ArrayList<Variable> v) {
    domainBounds = db;
    constraints = c;
    variables = v;
  }

  // NEWS
  public static BinaryCSP getConstraints(BinaryCSP csp) {
    ArrayList<Variable> variables = csp.getVariables();
    ConstraintList cl = new ConstraintList();

    for (BinaryConstraint b : csp.constraints) {
      Variable var1 = new Variable();
      Variable var2 = new Variable();

      HashMap<Variable, int[]> v1 = new HashMap<>();
      HashMap<Variable, int[]> v2 = new HashMap<>();

      for (Variable v11 : variables) {
        if (v11.getId() == b.getFirstVar()) {
          // var1 = v11.getCopy();
          var1 = v11;
        }

        if (v11.getId() == b.getSecondVar()) {
          var2 = v11.getCopy();
          var2 = v11;
        }
      }

      int[] var1_values = new int[b.getTuples().size()];
      int[] var2_values = new int[b.getTuples().size()];

      /*
       * ToDo: Make sure that when adding the domain values, each value is a unique
       * objects
       */
      /**
       * for (csp.BinaryTuple bt : b.getTuples()) { Integer i = null; i = -1; String[]
       * ret = null; ret =
       * bt.toString2().split(","); i=Integer.parseInt(ret[0]); var1_values.add(i);
       *
       * <p>
       * var2_values.add(Integer.parseInt(ret[1])); }
       */
      for (int counter = 0; counter < b.getTuples().size(); counter++) {
        int i = -1;
        String[] ret = null;
        ret = b.getTuples().get(counter).toString2().split(",");
        i = Integer.parseInt(ret[0]);
        var1_values[counter] = i;
        var2_values[counter] = (Integer.parseInt(ret[1]));
      }

      v1.put(var1, var1_values);
      v2.put(var2, var2_values);

      Constraint new_constraint = new Constraint(v1, v2);

      cl.add(new_constraint);
    }

    csp.addConstrainList(cl);

    return csp;
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

  // NEWS
  public ArrayList<Variable> getVariables() {
    return variables;
  }

  public ConstraintList getConstraintList() {
    return constraintList;
  }

  private void addConstrainList(ConstraintList c) {
    constraintList = c;
  }
}
