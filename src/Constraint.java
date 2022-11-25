import java.util.*;
import java.util.stream.Collectors;

public class Constraint {

    private HashMap<Variable, int[]> firstMap;
    private HashMap<Variable, int[]> secondMap;

    /**
     * Constructers
     */
    public Constraint(HashMap<Variable, int[]> firstMap, HashMap<Variable, int[]> secondMap) {
        this.firstMap = new HashMap<>();
        this.firstMap = firstMap;
        this.secondMap = new HashMap<>();
        this.secondMap = secondMap;
    }

    /**
     * Getters
     */

    public Map<Variable, int[]> getFirstMap() {
        return firstMap;
    }

    public Map<Variable, int[]> getSecondMap() {
        return secondMap;
    }

    public Variable getFirstVar() {
        return firstMap.keySet().stream().collect(Collectors.toList()).get(0);
    }

    public Variable getSecondVar() {
        return secondMap.keySet().stream().collect(Collectors.toList()).get(0);
    }
}
