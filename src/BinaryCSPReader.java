import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

/**
 * A reader tailored for binary extensional CSPs.
 * It is created from a FileReader and a StreamTokenizer
 */
public final class BinaryCSPReader {
  private FileReader inFR;
  private StreamTokenizer in;

  /**
   * Main (for testing)
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java BinaryCSPReader <file.csp>");
      return;
    }
    BinaryCSPReader reader = new BinaryCSPReader();
    System.out.println(reader.readBinaryCSP(args[0]));
  }

  /**
   * File format:
   * <no. vars>
   * NB vars indexed from 0
   * We assume that the domain of all vars is specified in terms of bounds
   * <lb>, <ub> (one per var)
   * Then the list of constraints
   * c(<varno>, <varno>)
   * binary tuples
   * <domain val>, <domain val>
   */
  public BinaryCSP readBinaryCSP(String fn) {
    try {
      inFR = new FileReader(fn);
      in = new StreamTokenizer(inFR);
      in.ordinaryChar('(');
      in.ordinaryChar(')');
      in.nextToken(); // n
      int n = (int) in.nval;
      int[][] domainBounds = new int[n][2];
      for (int i = 0; i < n; i++) {
        in.nextToken(); // ith ub
        domainBounds[i][0] = (int) in.nval;
        in.nextToken(); // ','
        in.nextToken();
        domainBounds[i][1] = (int) in.nval;
      }
      ArrayList<BinaryConstraint> constraints = readBinaryConstraints();
      ArrayList<Variable> variables = generateVariables(domainBounds);
      ConstraintList constraintList = generateConstraintList(constraints, variables);
      BinaryCSP csp = new BinaryCSP(domainBounds, constraints, variables, constraintList);
      // TESTING:
      // System.out.println(csp) ;
      inFR.close();
      return csp;
    } catch (FileNotFoundException e) {
      System.out.println(e);
    } catch (IOException e) {
      System.out.println(e);
    }
    return null;
  }

  /**
   *
   */
  private ArrayList<BinaryConstraint> readBinaryConstraints() {
    ArrayList<BinaryConstraint> constraints = new ArrayList<BinaryConstraint>();

    try {
      in.nextToken(); // 'c' or EOF
      while (in.ttype != in.TT_EOF) {
        // scope
        in.nextToken(); // '('
        in.nextToken(); // var
        int var1 = (int) in.nval;
        in.nextToken(); // ','
        in.nextToken(); // var
        int var2 = (int) in.nval;
        in.nextToken(); // ')'

        // tuples
        ArrayList<BinaryTuple> tuples = new ArrayList<BinaryTuple>();
        in.nextToken(); // 1st allowed val of 1st tuple
        while (!"c".equals(in.sval) && (in.ttype != in.TT_EOF)) {
          int val1 = (int) in.nval;
          in.nextToken(); // ','
          in.nextToken(); // 2nd val
          int val2 = (int) in.nval;
          tuples.add(new BinaryTuple(val1, val2));
          in.nextToken(); // 1stallowed val of next tuple/c/EOF
        }
        BinaryConstraint c = new BinaryConstraint(var1, var2, tuples);
        constraints.add(c);
      }

      return constraints;
    } catch (IOException e) {
      System.out.println(e);
    }
    return null;
  }

  /**
   * Generate variables from domainBounds
   */
  public ArrayList<Variable> generateVariables(int[][] domainBounds) {

    ArrayList<Variable> variables = new ArrayList<Variable>();
    for (int i = 0; i < domainBounds.length; i++) {
      int[] d = IntStream.range(0, domainBounds[i][1] + 1).toArray();
      Variable v = new Variable(i, d);
      variables.add(v);
    }

    return variables;
  }

  /**
   * Generate constraintList from constraints and variables
   */
  public ConstraintList generateConstraintList(ArrayList<BinaryConstraint> constraints, ArrayList<Variable> variables) {
    ConstraintList cl = new ConstraintList();

    for (BinaryConstraint b : constraints) {
      Variable var1 = new Variable();
      Variable var2 = new Variable();

      HashMap<Variable, int[]> v1 = new HashMap<>();
      HashMap<Variable, int[]> v2 = new HashMap<>();

      for (Variable v11 : variables) {
        if (v11.getId() == b.getFirstVar()) {
          var1 = v11;
        }

        if (v11.getId() == b.getSecondVar()) {
          var2 = v11.getCopy();
          var2 = v11;
        }
      }

      int[] var1Values = new int[b.getTuples().size()];
      int[] var2Values = new int[b.getTuples().size()];

      /*
       * ToDo: Make sure that when adding the domain values, each value is a unique
       * objects
       */
      for (int counter = 0; counter < b.getTuples().size(); counter++) {
        int i = -1;
        String[] ret = null;
        ret = b.getTuples().get(counter).toString2().split(",");
        i = Integer.parseInt(ret[0]);
        var1Values[counter] = i;
        var2Values[counter] = (Integer.parseInt(ret[1]));
      }

      v1.put(var1, var1Values);
      v2.put(var2, var2Values);

      Constraint c = new Constraint(v1, v2);

      cl.add(c);
    }

    return cl;
  }
}
