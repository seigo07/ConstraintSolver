import java.util.*;
import java.util.stream.IntStream;

public class Solver {
    private final BinaryCSP csp;
    private final ArrayList<Integer> solution = new ArrayList<Integer>();
    private LinkedList<Integer> id_sequences = new LinkedList<>();
    private int mode = 1;

    // maintain a global list of variables that are modified during the arc revision
    // method
    private final LinkedList<Variable> pruned = new LinkedList<>();
    private final LinkedList<Variable> marked = new LinkedList<>();

    private ArrayList<Variable> varList;
    private ConstraintList constraintList;

    // Parameters
    String varOrder;
    String valOrder;

    /**
     * Constructor
     */
    public Solver(BinaryCSP csp, String varOrder, String valOrder) {
        this.csp = csp;
        this.varOrder = varOrder;
        this.valOrder = valOrder;
        varList = generateVarList(csp.getDomainBounds());
        constraintList = generateConstraintList(csp.getConstraints(), varList);
    }

    /**
     * Run solver
     */
    public void run(String algorithm) {

        switch (algorithm) {
            case "fc":
                forwardChecking();
                break;
            case "mac":
                mac();
                break;
            default:
                forwardChecking();
                break;
        }
    }

    /**
     * Generate variables from domainBounds
     */
    public ArrayList<Variable> generateVarList(int[][] domainBounds) {

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
    public ConstraintList generateConstraintList(ArrayList<BinaryConstraint> constraints,
            ArrayList<Variable> variables) {
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

    /**
     * Generate solutions from variables
     */
    public void printSolutions() {
        for (Variable v : varList) {
            System.out.println("Variable " + v.getId() + " is assigned: " + v.getValue());
        }
        System.out.println("The number of solution:" + this.solution.size());
    }

    /**
     * FC: 2-way version
     */
    public void forwardChecking() {
        if (completeAssignment()) {
            // Output solutions and finish
            printSolutions();
        } else {
            // Pick var and value
            Variable var = selectVar();
            int value = var.getMaxValInDomain();
            // Branching
            branchFCLeft(var, value);
            branchFCRight(var, value);
        }
    }

    /**
     * Check if all variables have been assigned with values
     */
    private boolean completeAssignment() {

        for (Variable v : varList) {
            if (v.isAssigned() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Solves a CSP using the maintaining arc consistency(MAC) algorithm
     */
    public void mac() {

        // get a value from the list of variables and a value from its domain
        Variable var = selectVar();
        int value = var.getMaxValInDomain();
        // assign the value to the variable
        var.assign(value);

        if (completeAssignment()) {
            printSolutions();
        }
        // if problem is arc consistent, recursively call MAC again
        // I don't provide a list of variables as an argument because the selectVariable
        // method does that at the
        // start of the MAC method
        else if (ac3()) {
            mac();
        }

        // If we're in this branch, then the problem is not arc consistent

        // undo pruning
        undoPruning();
        var.unassign();
        var.prune(value); // this has the same function of removing the value from the domain of the
                          // variable
        var.savePrune(); // remember to call this function after all pruning is done

        if (!var.isDomainEmpty()) {
            if (ac3() == true) {
                mac();
            }

            undoPruning();

        }

        var.undoPruning(); // replaces the value which was most recently removed from the domain of var

    }

    /**
     * select an assignment variable from a varList
     */

    private Variable selectVar() {

        Variable selectedVar = null;
        int smallest_domain = 1000;

        switch (varOrder) {
            // Ascending or first unassigned variable in the list
            case "asc":
                for (int i = 0; i < varList.size(); i++) {
                    if (!varList.get(i).isAssigned()) {
                        selectedVar = varList.get(i);
                    }
                }
                break;
            // Strategy d: Smallest domain first
            case "sdf":
                selectedVar = null;
                smallest_domain = 1000;
                // check the size of the domain for each unassigned variable
                for (Variable v : varList) {
                    if (!v.isAssigned()) {
                        if (v.getDomain().length < smallest_domain) {
                            smallest_domain = v.getDomain().length;
                            selectedVar = v;
                        }
                    }
                }
                break;
            // Default asc
            default:
                for (int i = 0; i < varList.size(); i++) {
                    if (!varList.get(i).isAssigned()) {
                        selectedVar = varList.get(i);
                    }
                }
                break;
        }
        return selectedVar;
    }

    /**
     * Select a value from the domain
     */
    private void selectVal(Variable var) {
    }

    private void branchFCLeft(Variable var, int value) {
        // From lecture 7_1: An assigned variable has been assigned by a
        // left branch
        this.mode = 1;
        boolean consistent = false;

        // Assign the value to the variable
        var.assign(value);
        System.out.println("Variable " + var.getId() + " is assigned " + value);

        // get the list of future variables
        ArrayList<Variable> futureVars = getFutureVars(csp, var);

        // revise future arcs
        consistent = reviseFutureArcs(futureVars, var);

        if (consistent == true) {

            // Do forward checking on the rest of the UNASSIGNED variables
            id_sequences.clear();
            forwardChecking();
        }

        undoPruning();
        var.unassign();
        System.out.println("End of branch left");
    }

    private void branchFCRight(Variable var, int value) {
        this.mode = 2;
        // From lecture 7_1: An assigned variable can only be assigned by branchLeft
        var.delete(value);
        var.savePrune();

        ArrayList<Variable> futureVars = getFutureVars(csp, var);

        // for(Variable v: futureVars){
        // v.undoMarking();
        // }

        if (!var.isDomainEmpty()) {
            if (reviseFutureArcs(futureVars, var) == true) { // reviseFutureArcs(ArrayList<Variable> futureVars,
                                                             // Variable v)
                forwardChecking();
            } else {
                undoPruning();
            }
        } else {
            var.assign(value);
        }
    } // end branchRight

    /**
     * Get a list of all constraints where the variable v is connected
     *
     * @param csp
     * @param root
     */
    private ArrayList<Variable> getFutureVars(BinaryCSP csp, Variable root) {

        // get the list of all future variables
        ArrayList<Variable> futureVars = new ArrayList<Variable>();

        for (Constraint c : constraintList) {

            if (c.getFv().containsKey(root)) {
                futureVars.add(c.getSecondVariable());
            }
        }
        return futureVars;
    }

    /**
     * Get a list of all constraints where the variable v is connected
     *
     * @param csp
     * @param root
     */
    private ArrayList<Variable> getFutureVars_old(BinaryCSP csp, Variable root) {

        // get the list of all future variables
        ArrayList<Variable> futureVars = new ArrayList<Variable>();
        ArrayList<Variable> agenda = new ArrayList<Variable>();
        agenda.add(root);

        while (!agenda.isEmpty()) {
            doGetFutureVars(agenda, futureVars, csp, root);
        }
        return futureVars;
    }

    // [DONE] ToDo: Revise this algorithm to get only nodes connected to the current
    // variable
    private void doGetFutureVars(
            ArrayList<Variable> agenda, ArrayList<Variable> futureVars, BinaryCSP csp, Variable root) {
        Variable v;

        for (int i = 0; i < agenda.size(); i++) {
            v = agenda.get(0);
            agenda.remove(0);

            for (Constraint c : constraintList) {

                if (c.getFv().containsKey(v) && !futureVars.contains(c.getSecondVariable())) {
                    futureVars.add(c.getSecondVariable());
                    agenda.add(c.getSecondVariable());
                }
            }
        }
    }

    /**
     * Make the problem arc consistent for all future arcs This is done by
     * recursively removing
     * conflicting values on *all* connected variables Implement the AC3 algorithm
     * here Arc
     * consistency(definition): for every value in the domain of a variable x, there
     * exists a value in
     * the domain of a connected variable y that satisfies all the constraints
     * X -> Y, Z, A
     */
    private boolean reviseFutureArcs(ArrayList<Variable> futureVars, Variable v) {
        // execute the AC3 algorithm
        boolean consistent;
        consistent = ac3(futureVars, v);

        // if the domain does NOT contain the currently assigned variable, reset the
        // assignment
        // if(!ArrayUtils.contains(v.getDomain(), v.getValue()) && consistent){
        // v.unassign();
        // }
        return consistent;
    }

    /**
     * Make the problem arc consistent for all arcs connected to the curernt
     * variable we've just
     * assigned. This is done by recursively removing conflicting values on *all*
     * connected variables.
     * Implement the AC3 algorithm here Arc consistency(definition): for every value
     * in the domain of
     * a variable x, there exists a value in the domain of a connected variable y
     * that satisfies all
     * the constraints
     * x -> y, y -> x
     */
    public boolean ac3(ArrayList<Variable> futureVars, Variable v) {
        boolean changed = false;
        ArcList arcList = makeArcs(v, futureVars, "from");
        Arc currentArc;

        // get an arc off the queue and revise its domains
        while (!arcList.isEmpty()) {
            currentArc = arcList.pop();
            id_sequences.addLast(currentArc.getSv().getId());

            if (currentArc.getFv().getInDomainAndUnmarked() == null) {
                return false;
            }

            // KEY LINE OF THIS METHOD
            if (this.mode == 1) {
                changed = revise(currentArc);
            } else {
                changed = revise_2(currentArc);
            }

            currentArc.getSv().saveMark();
            // if there is a change, add all arcs incident of the first variable of the arc
            // to the queue
            // of arcs
            if (changed == true) {
                // if there has been a change

                // a. add the changed variable to the list of variables pruned. This will help
                // us know which
                // variables to undo the pruning for
                pruned.push(currentArc.getFv());

                // b. check if a wipeout has occurred and if it has
                // exit IMMEDIATELY and return the value of consistent to the calling function
                // if (currentArc.getSv().isWipeout()) {
                if (currentArc.getSv().isWipeout()) {
                    return false;
                }

                // If we're here, the domain of the first variable in the current arc has
                // changed
                // Add to the queue(arcList) all arcs(xh, xi) (h != j)
                // find all variables incident on the first variable in the arc.i.e the left
                // variable
                ArrayList<Variable> connectedVars = getVariablesIncidentOn(currentArc.getFv());

                // create arcs from those variables to xi and add them to the queue
                ArcList new_arcs = makeArcs(currentArc.getFv(), connectedVars, "to");

                // be sure to not add the current arc that has been popped
                new_arcs.remove(currentArc);

                // ToDo: Ensure this function only adds unique arcs to the list
                // add the new arcs to the queue
                arcList.add(new_arcs);
            }
        } // end while

        return true;
    }

    /**
     * AC3 algorithm when no arguments are provided. In this case the goal is to
     * enforce global arc consistency.
     * Used for the MAC algorithm.
     * 
     * @return
     */
    public boolean ac3() {
        return false;
    }

    private boolean checkConsistent(ArrayList<Variable> vars) {
        // checks to see if any variable's domain has been emptied by revise
        return false;
    }

    /**
     * Goes through the list of variables that were modified during the arc revision
     * process and
     * undoes their pruning
     */
    private void undoPruning() {
        Variable v;
        for (int i = pruned.size() - 1; i >= 0; i--) {
            v = pruned.remove(i);
            v.undoPruning();
        }

        for (int i = 0; i < id_sequences.size(); i++) {
            for (Variable vv : varList) {
                if (id_sequences.getLast() == vv.getId()) {
                    vv.undoMarking();
                }
            }
        }

        // ToDo: clear the id_sequnces arrayList
        id_sequences.clear();

        // this.pruned.clear(); // clear the contents of the list of pruned variables
    }

    /**
     * A utility function to create a list of arcs given the first variable and a
     * list of variables
     * it's connected to. It additionally also includes the reverse of the arcs For
     * instance, after
     * adding arc(x, y), it also adds arc(y,x)
     *
     * @param fv
     * @param variableList
     * @return a list of arcs
     */
    private ArcList makeArcs(Variable fv, ArrayList<Variable> variableList, String direction) {
        ArcList arcList = new ArcList();
        Arc arc;

        if (direction.equals("to")) {
            // create the arcs
            for (Variable v : variableList) {
                arc = new Arc(v, fv);
                arcList.add(arc); // adding the arc from the the assigned variable to the connected variable
            }
        } else if (direction.equals("from")) {
            // create the arcs
            for (Variable v : variableList) {
                arc = new Arc(fv, v);
                arcList.add(arc); // adding the arc from the the assigned variable to the connected variable
            }
        }

        // Add the reverse of each arc. For instance if we have arc(x1, x2), this method
        // adds
        // arc(x2, x1)
        // ArcList reverse = new ArcList();
        // for (Arc a : arcList) {
        // reverse.add(a.reverse());
        // }

        // arcList.add(reverse); // adds the reversed arcs to the arcList

        return arcList;
    }

    /**
     * Gets a list of all variables that are incident on a given variable v
     *
     * @param v
     * @return an arrayList of all variables incident on v
     */
    private ArrayList<Variable> getVariablesIncidentOn(Variable v) {
        ArrayList<Variable> results = new ArrayList<>();

        for (Constraint c : constraintList) {
            if (c.getSecondVariable().equals(v)) {
                results.add(c.getFirstVariable());
            }
        }
        return results;
    }

    /**
     * Revise returns true if the value of the first variable in the arc(i.e. the
     * left variable) has
     * been changed Assuming also that empty domains are caught
     *
     * @param arc
     * @return True if a change was made to the domain of the left variable(or first
     *         variable) in the
     *         arc, else false.
     */
    private boolean old_revise(Arc arc) {
        boolean changed = false;
        boolean supported = false;
        ArrayList<Integer> prunedList = new ArrayList<>();
        ArrayList<Integer> supported_values_in_sv;

        // if domain is empty exit early
        if (arc.getSv().isDomainEmpty()) {
            return changed;
        } else {
            for (int v : arc.getSv().getDomain()) {

                // for each integer in the domain of FV, find the list of valid values which it
                // supports in
                // the second variable, sv,
                // based on the data in the list of constraints
                supported_values_in_sv = constraintList.getConstraintsOn(v, arc);

                // the arc is consistent iff AT LEAST ONE value in "supported_values_in_sv"
                // can be found in the domain of SV
                supported = arc.getSv().hasSupport(supported_values_in_sv);

                if (supported == false) {

                    // if there is no support, prune the current integer value from the domain of
                    // the first
                    // variable
                    arc.getFv().prune(v); // remove from the domain
                    changed = true;
                }
            } // endFor

            // IMPORTANT!: Before exiting this method, remember to call .savePrune to store
            // the pruned
            // variables
            // in the list of pruned variables. This step will enable us do backtracking
            // properly
            arc.getFv().savePrune();
        } // end else

        return changed;
    }

    /**
     * Revise returns true if the value of the first variable in the arc(i.e. the
     * left variable) has
     * been changed Assuming also that empty domains are caught.
     * This function assumes that at the point of calling revise, the a value
     * has been assigned to the left variable
     *
     * @param arc
     * @return True if a change was made to the domain of the left variable(or first
     *         variable) in the
     *         arc, else false.
     */
    private boolean revise(Arc arc) {
        boolean changed = false;
        boolean supported;
        ArrayList<Integer> supported_values_in_sv;
        /**
         * Source: https://ktiml.mff.cuni.cz/~bartak/constraints/consistent.html
         * procedure REVISE(Vi,Vj)
         * DELETE <- false;
         * for each X in Di do
         * if there is no such Y in Dj such that (X,Y) is consistent,
         * then
         * delete X from Di;
         * DELETE <- true;
         * endif
         * endfor
         * return DELETE;
         * end REVISE
         */

        // if domain is empty exit early
        if (arc.getSv().isDomainEmpty()) {
            return changed;
        } else {

            // go through every value in the domain of the first variable fv_i and check if
            // each value has support in the
            // domain of the second variable

            // check if the value in the domain has support in the second variable
            // supported_values_in_sv =
            // csp.getConstraintList().getConstraintsOn(arc.getFv().getValue(), arc);
            supported_values_in_sv = constraintList.getConstraintsOn(arc.getFv().getValue(), arc);

            // the arc is consistent iff AT LEAST ONE value in "supported_values_in_sv"
            // can be found in the domain of SV
            supported = arc.getSv().hasSupport(supported_values_in_sv);

            if (supported == false) {

                // if there is no support, prune the value fv_i from the domain of the first
                // variable
                // arc.getFv().prune(arc.getFv().getValue()); //remove from the domain
                arc.getFv().prune(arc.getFv().getValue());
                changed = true;

                arc.getSv().markUnsupportedValues(supported_values_in_sv);
            }

            else {
                // removes the values which are not supported by the current value of fv from
                // the domain of SV
                arc.getSv().markUnsupportedValues(supported_values_in_sv);
                // arc.getFv().saveMark();
            }

        }

        return changed;
    }

    /**
     * Revise returns true if the value of the first variable in the arc(i.e. the
     * left variable) has
     * been changed Assuming also that empty domains are caught
     *
     * @param arc
     * @return True if a change was made to the domain of the left variable(or first
     *         variable) in the
     *         arc, else false.
     */
    private boolean revise_2(Arc arc) {
        boolean changed = false;
        boolean supported;
        ArrayList<Integer> supported_values_in_sv;
        /**
         * Source: https://ktiml.mff.cuni.cz/~bartak/constraints/consistent.html
         * procedure REVISE(Vi,Vj)
         * DELETE <- false;
         * for each X in Di do
         * if there is no such Y in Dj such that (X,Y) is consistent,
         * then
         * delete X from Di;
         * DELETE <- true;
         * endif
         * endfor
         * return DELETE;
         * end REVISE
         */

        // if domain is empty exit early
        if (arc.getSv().isDomainEmpty()) {
            return changed;
        } else {
            // go through every value in the domain of the first variable fv_i and check if
            // each value has support in the
            // domain of the second variable
            int[] InDomainAndUnmarked = arc.getFv().getInDomainAndUnmarked();
            if (InDomainAndUnmarked == null) {
                return changed;
            }

            for (int i : arc.getFv().getInDomainAndUnmarked()) {
                // check if the value in the domain has support in the second variable
                // supported_values_in_sv =
                // csp.getConstraintList().getConstraintsOn(arc.getFv().getValue(), arc);
                supported_values_in_sv = constraintList.getConstraintsOn(i, arc);

                // the arc is consistent iff AT LEAST ONE value in "supported_values_in_sv"
                // can be found in the domain of SV
                supported = arc.getSv().hasSupport(supported_values_in_sv);

                if (supported == false) {

                    // if there is no support, prune the value fv_i from the domain of the first
                    // variable
                    // arc.getFv().prune(arc.getFv().getValue()); //remove from the domain
                    arc.getFv().prune(arc.getFv().getValue());
                    changed = true;

                    arc.getSv().markUnsupportedValues(supported_values_in_sv);
                }

                else {
                    // removes the values which are not supported by the current value of fv from
                    // the domain of SV
                    // arc.getSv().markUnsupportedValues(supported_values_in_sv);
                    // arc.getFv().saveMark();
                }

            }

        }

        return changed;
    }
}
