
public class Main {

    public static void main(String[] args) {

        // Validation the number of parameters
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

        // System.out.println("fileName=" + fileName);
        // System.out.println("algorithm=" + algorithm);
        // System.out.println("varOrder=" + varOrder);
        // System.out.println("valOrder=" + valOrder);

        // Instantiate a BinaryCSP through BinaryCSPReader
        BinaryCSPReader bcspr = new BinaryCSPReader();
        BinaryCSP bcsp = bcspr.readBinaryCSP(fileName);

        // Instantiate a Solver to run solver
        Solver solver = new Solver(bcsp);
        solver.run(algorithm);
        solver.printSolutions();
    }
}
