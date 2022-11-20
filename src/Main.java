
public class Main {

    public static void main(String[] args) {

        // Validation the number of parameters
        if (args.length != 4) {
            System.out.println("Usage: java Main <file.csp> <algorithm> <VarOrder> <ValOrder>");
            return;
        }

        String fileName = args[0];
        String algorithm = args[1];
        String varOrder = args[2];
        String valOrder = args[3];
        System.out.println("fileName=" + fileName);
        System.out.println("algorithm=" + algorithm);
        System.out.println("varOrder=" + varOrder);
        System.out.println("valOrder=" + valOrder);

        // Read the CSP from the CSP files
        BinaryCSP bcsp = Solver.getCSP(fileName);

        // Instantiate a Solver Class
        Solver csp_solver = new Solver(bcsp);

        // Solve the CSP using the specified algorithm
        csp_solver.solve(algorithm);

        // Get the results of the solution
        csp_solver.get_solutions();

        System.out.println("end of program");
    }
}
