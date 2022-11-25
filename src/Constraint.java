import java.util.*;
import java.util.stream.Collectors;

public class Constraint {

    private HashMap<Variable, int[]> firstVar;
    private HashMap<Variable, int[]> secondVar;

    /**
     * Constructers
     */
    public Constraint(HashMap<Variable, int[]> firstVar, HashMap<Variable, int[]> secondVar) {
        this.firstVar = new HashMap<>();
        this.firstVar = firstVar;
        this.secondVar = new HashMap<>();
        this.secondVar = secondVar;
    }

    /**
     * Getters
     */

    public Map<Variable, int[]> getFirstVar() {
        return firstVar;
    }

    public Map<Variable, int[]> getSecondVar() {
        return secondVar;
    }

    public Variable getFirstVariable() {
        return firstVar.keySet().stream().collect(Collectors.toList()).get(0);
    }

    public Variable getSecondVariable() {
        return secondVar.keySet().stream().collect(Collectors.toList()).get(0);
    }
}
