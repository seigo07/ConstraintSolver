import java.util.*;
import java.util.stream.IntStream;

public class Solver {

    private final ArrayList<Integer> solution = new ArrayList<Integer>();
    private ArrayList<Variable> varList;
    private ArrayList<Constraint> constraintList;
    private final LinkedList<Variable> prunedList = new LinkedList<>();
    private LinkedList<Integer> idSequences = new LinkedList<>();
    private LinkedList<Arc> arcList = new LinkedList<>();
    private int searchNodes = 0;
    private int arcRevisions = 0;

    public enum Branch {
        LEFT, RIGHT
    }

    Branch branch;

    // Parameters
    String varOrder;
    String valOrder;

    /**
     * Constructor
     */
    public Solver(BinaryCSP csp, String varOrder, String valOrder) {
        this.varOrder = varOrder;
        this.valOrder = valOrder;
        this.varList = generateVarList(csp.getDomainBounds());
        this.constraintList = generateConstraintList(csp.getConstraints(), varList);
        this.branch = Branch.LEFT;
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
        }
    }

    /**
     * Generate variables from domainBounds
     */
    public ArrayList<Variable> generateVarList(int[][] domainBounds) {

        ArrayList<Variable> varList = new ArrayList<Variable>();
        for (int i = 0; i < domainBounds.length; i++) {
            int[] d = IntStream.range(0, domainBounds[i][1] + 1).toArray();
            Variable v = new Variable(i, d);
            varList.add(v);
        }
        return varList;
    }

    /**
     * Generate constraintList from constraints and variables
     */
    public ArrayList<Constraint> generateConstraintList(ArrayList<BinaryConstraint> constraints,
            ArrayList<Variable> variables) {

        ArrayList<Constraint> cl = new ArrayList<Constraint>();

        for (BinaryConstraint bc : constraints) {
            Variable var1 = new Variable();
            Variable var2 = new Variable();

            HashMap<Variable, int[]> v1 = new HashMap<>();
            HashMap<Variable, int[]> v2 = new HashMap<>();

            for (Variable var : variables) {
                if (var.getId() == bc.getFirstVar()) {
                    var1 = var;
                }

                if (var.getId() == bc.getSecondVar()) {
                    var2 = var.getCopy();
                    var2 = var;
                }
            }

            int[] ValuesInVar1 = new int[bc.getTuples().size()];
            int[] ValuesInVar2 = new int[bc.getTuples().size()];

            for (int counter = 0; counter < bc.getTuples().size(); counter++) {
                int i = -1;
                String[] ret = null;
                ret = bc.getTuples().get(counter).toString2().split(",");
                i = Integer.parseInt(ret[0]);
                ValuesInVar1[counter] = i;
                ValuesInVar2[counter] = (Integer.parseInt(ret[1]));
            }

            v1.put(var1, ValuesInVar1);
            v2.put(var2, ValuesInVar2);

            Constraint c = new Constraint(v1, v2);

            cl.add(c);
        }

        return cl;
    }

    /**
     * Generate solutions from varList
     */
    public void printSolutions() {
        solution.add(searchNodes);
        solution.add(arcRevisions);
        for (Variable v : varList) {
            solution.add(v.getValue());
            System.out.println("var " + v.getId() + " val: " + v.getValue());
        }
        System.out.println("#### Output solution ####");
        for (int s : solution) {
            System.out.println(s);
        }
    }

    /**
     * Check if all variables are assigned with values
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
     * FC 2-way version
     */
    public void forwardChecking() {
        // Check if all variables are assigned
        if (completeAssignment()) {
            // Output solutions and finish
            printSolutions();
            return;
        }
        // Get var based on varOrder
        Variable var = selectVar();
        // Get var based on valOrder
        // int val = selectVal(var);
        int val = var.getSmallestDomain();
        System.out.println("x" + var.getId() + " = " + val);
        // Branching
        branchFCLeft(var, val);
        branchFCRight(var, val);
    }

    /**
     * Select an assignment variable from a varList
     */
    private Variable selectVar() {

        Variable selectedVar = null;

        switch (varOrder) {
            // Ascending or first unassigned variable in the list
            case "asc":
                for (Variable v : varList) {
                    if (!v.isAssigned()) {
                        selectedVar = v;
                        break;
                    }
                }
                break;
            // Smallest domain first
            case "sdf":
                int smallestDomain = 1000;
                for (Variable v : varList) {
                    if (!v.isAssigned()) {
                        if (smallestDomain > v.getDomain().length) {
                            smallestDomain = v.getDomain().length;
                            selectedVar = v;
                        }
                    }
                }
                break;
            // Default asc
            default:
                for (Variable v : varList) {
                    if (!v.isAssigned()) {
                        selectedVar = v;
                        break;
                    }
                }
                break;
        }
        return selectedVar;
    }

    /**
     * Assigned variables are assigned by branchFCLeft
     */
    private void branchFCLeft(Variable var, int val) {

        this.branch = Branch.LEFT;
        searchNodes++;

        // Add val to var
        var.assign(val);

        // Pruning possible future domains
        if (reviseFutureArcs(var)) {
            // Forward checking for the rest of the unassigned variables
            idSequences.clear();
            forwardChecking();
        }

        // Reverse the changes made by reviseFutureArcs
        undoPruning();
        // Remove val from var
        var.unassign();

        System.out.println("End of branch left");
    }

    /**
     * BranchFCRight
     */
    private void branchFCRight(Variable var, int val) {

        this.branch = Branch.RIGHT;
        searchNodes++;

        // Delete value from domain
        var.deleteValue(val);
        var.savePrune();

        if (!var.isDomainEmpty()) {
            // Pruning possible future domains
            if (reviseFutureArcs(var)) {
                forwardChecking();
            } else {
                undoPruning();
            }
        } else {
            var.assign(val);
        }
        System.out.println("End of branch right");
    }

    /**
     * Get a list of all constraints where the variable v is connected
     */
    private ArrayList<Variable> getFutureVars(Variable root) {

        // Get all future variables
        ArrayList<Variable> futureVars = new ArrayList<Variable>();

        for (Constraint c : constraintList) {
            if (c.getFirstVar().containsKey(root)) {
                futureVars.add(c.getSecondVariable());
            }
        }
        return futureVars;
    }

    /**
     * Procedure for pruning possible future domains
     */
    private boolean reviseFutureArcs(Variable var) {

        // boolean consistent = true;

        // for (Variable futureVar : varList) {
        // if (futureVar != var) {
        // Arc arc = new Arc(futureVar, var);
        // consistent = revise3(arc);
        // if (!consistent) {
        // return false;
        // }
        // }
        // }

        // return consistent;

        // Get future variables
        ArrayList<Variable> futureVars = getFutureVars(var);
        return ac3(futureVars, var);
    }

    /**
     * revise3
     */
    private boolean revise3(Arc arc) {
        boolean changed = false;

        Variable xi = arc.getFirstVar();
        Variable xj = arc.getSecondVar();

        System.out.println("x" + xj.getId() + " = " + xj.getValue());
        System.out.println("x" + xi.getId() + " = " + xi.getValue());

        for (int di : xi.getDomain()) {
            for (int dj : xj.getDomain()) {
                if ((xi.getValue() == di && xj.getValue() == dj)) {
                    return changed;
                }
                xi.deleteValue(di);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Maintaining arc consistency
     */
    public void mac() {

        // Get a value from varList and a value from domain
        Variable var = selectVar();
        int val = var.getSmallestDomain();

        // Add val to var
        var.assign(val);

        if (completeAssignment()) {
            printSolutions();
            return;
        } else if (ac3()) {
            // Having assigned a value to a variable, reestablish AC
            mac();
        }

        // Reverse the changes made by reviseFutureArcs
        undoPruning();
        // Remove val from var
        var.unassign();
        // Removing val from domain
        var.prune(val);
        var.savePrune();

        if (!var.isDomainEmpty()) {
            if (ac3()) {
                mac();
            }
            // Reverse the changes made by reviseFutureArcs
            undoPruning();
        }
        // Replace the most recently removed value
        var.replaceVal();
    }

    /**
     * Check consistent through ac3 revises arcs algorithm
     */
    public boolean ac3(ArrayList<Variable> futureVars, Variable v) {
        boolean changed = false;
        arcList = makeArcs(v, futureVars, "from");
        Arc currentArc;

        // Revise domains unless arcList is not empty
        while (!arcList.isEmpty()) {
            currentArc = arcList.pop();
            idSequences.addLast(currentArc.getSecondVar().getId());

            if (currentArc.getFirstVar().getInDomainAndUnmarked() == null) {
                return false;
            }

            // Revise by branch direction
            switch (this.branch) {
                case LEFT:
                    changed = reviseForBranchLeft(currentArc);
                    break;
                case RIGHT:
                    changed = reviseForBranchRight(currentArc);
                    break;
            }

            currentArc.getSecondVar().saveMark();

            // Add all arcs to the queue if there is a change
            if (changed) {

                // Add the changed variable to pruned var list
                prunedList.push(currentArc.getFirstVar());

                // Check if a wipeout was occurred
                if (currentArc.getSecondVar().isNeedCancel()) {
                    return false;
                }

                // Get all variables incident on the first variable in the arc
                ArrayList<Variable> connectedVars = getVariablesIncidentOn(currentArc.getFirstVar());

                // create arcs from those variables to xi and add them to the queue
                LinkedList<Arc> newArcList = makeArcs(currentArc.getFirstVar(), connectedVars, "to");
                newArcList.remove(currentArc);

                // ToDo: Ensure this function only adds unique arcs to the list
                // add the new arcs to the queue
                mergeArcList(newArcList);
            }
        }

        return true;
    }

    /**
     * Add another arcList to this arcList
     */
    public void mergeArcList(LinkedList<Arc> al) {
        for (Arc arc : al) {
            if (arc != null) {
                arcList.add(arc);
            }
        }
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

    /**
     * Reverses the changes made by reviseFutureArcs
     */
    private void undoPruning() {
        Variable v;
        for (int i = prunedList.size() - 1; i >= 0; i--) {
            v = prunedList.remove(i);
            v.replaceVal();
        }

        for (int i = 0; i < idSequences.size(); i++) {
            for (Variable vv : varList) {
                if (idSequences.getLast() == vv.getId()) {
                    vv.undoMarking();
                }
            }
        }

        idSequences.clear();
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
    private LinkedList<Arc> makeArcs(Variable fv, ArrayList<Variable> variableList, String direction) {

        LinkedList<Arc> arcList = new LinkedList<Arc>();
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
            System.out.println("getFirstVariable " + c.getFirstVariable());
            if (c.getSecondVariable().equals(v)) {
                results.add(c.getFirstVariable());
            }
        }
        return results;
    }

    /**
     * Add secondtVarValues to supportList if the val supports values in the domain
     * of the secondVar
     */
    public ArrayList<Integer> getSupportList(int val, Arc arc) {

        ArrayList<Integer> supportList = new ArrayList<>();

        for (Constraint c : constraintList) {
            if (c.getFirstVariable().equals(arc.getFirstVar()) && c.getSecondVariable().equals(arc.getSecondVar())) {

                int[] firstVarValues = c.getFirstVar().get(arc.getFirstVar());
                int[] secondtVarValues = c.getSecondVar().get(arc.getSecondVar());

                for (int i = 0; i < firstVarValues.length; i++) {
                    if (firstVarValues[i] == val) {
                        supportList.add(secondtVarValues[i]);
                    }
                }
            }
        }

        return supportList;
    }

    /**
     * Prune future domains if there is given variables don't satisfy any
     * constraints
     */
    private boolean reviseForBranchLeft(Arc arc) {

        this.arcRevisions++;
        boolean changed = false;
        boolean supported;
        ArrayList<Integer> supportedValuesInSv;

        // Exit AC3 early if domain is empty
        if (arc.getSecondVar().isDomainEmpty()) {
            return changed;
        }

        // Check if the value in the domain supports in the second var
        supportedValuesInSv = getSupportList(arc.getFirstVar().getValue(), arc);

        // Check if the domain of sv at least one value
        supported = arc.getSecondVar().hasSupport(supportedValuesInSv);

        // Pruning the value from the domain of fv if there is no support
        if (supported == false) {

            arc.getFirstVar().prune(arc.getFirstVar().getValue());
            changed = true;
            arc.getSecondVar().markUnsupportedValues(supportedValuesInSv);
        }

        // Remove the values from the domain of sv if they are not supported by the
        // current value of fv
        arc.getSecondVar().markUnsupportedValues(supportedValuesInSv);

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
    private boolean reviseForBranchRight(Arc arc) {
        this.arcRevisions++;
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
        if (arc.getSecondVar().isDomainEmpty()) {
            return changed;
        } else {
            // go through every value in the domain of the first variable fv_i and check if
            // each value has support in the
            // domain of the second variable
            int[] InDomainAndUnmarked = arc.getFirstVar().getInDomainAndUnmarked();
            if (InDomainAndUnmarked == null) {
                return changed;
            }

            for (int i : arc.getFirstVar().getInDomainAndUnmarked()) {
                // check if the value in the domain has support in the second variable
                // supported_values_in_sv =
                // csp.getConstraintList().getConstraintsOn(arc.getFv().getValue(), arc);
                supported_values_in_sv = getSupportList(i, arc);

                // the arc is consistent iff AT LEAST ONE value in "supported_values_in_sv"
                // can be found in the domain of SV
                supported = arc.getSecondVar().hasSupport(supported_values_in_sv);

                if (supported == false) {

                    // if there is no support, prune the value fv_i from the domain of the first
                    // variable
                    // arc.getFv().prune(arc.getFv().getValue()); //remove from the domain
                    arc.getFirstVar().prune(arc.getFirstVar().getValue());
                    changed = true;

                    arc.getSecondVar().markUnsupportedValues(supported_values_in_sv);
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
