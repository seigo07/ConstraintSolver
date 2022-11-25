import java.io.*;
import java.util.*;

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

    // Validate the number of parameters
    if (args.length != 4) {
      System.out.println("Usage: java Main <file.csp> <algorithm> <VarOrder> <ValOrder>");
      return;
    }

    // Validation algorithm
    if (!args[1].matches("fc|mac")) {
      System.out.println("Usage: fc or mac for args[1]");
      return;
    }

    // Validation varOrder
    if (!args[2].matches("asc|sdf")) {
      System.out.println("Usage: asc or sdf for args[2]");
      return;
    }

    // Validation valOrder
    if (!args[3].contains("asc")) {
      System.out.println("Usage: asc for args[3]");
      return;
    }

    String fileName = args[0];
    String algorithm = args[1];
    String varOrder = args[2];
    String valOrder = args[3];

    // Instantiate a BinaryCSP through BinaryCSPReader
    BinaryCSPReader bcspr = new BinaryCSPReader();
    BinaryCSP bcsp = bcspr.readBinaryCSP(fileName);

    // Instantiate a Solver to run solver
    Solver solver = new Solver(bcsp, varOrder, valOrder);
    solver.run(algorithm);
    solver.printSolutions();
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
      BinaryCSP csp = new BinaryCSP(domainBounds, constraints);
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
}
