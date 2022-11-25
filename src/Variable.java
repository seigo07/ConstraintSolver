import java.util.*;

public class Variable {

    private int id = -1;
    private int value = -1;
    private int depth;
    private int[] domain = null;
    private int[] assigned;

    private LinkedList<Integer> prunedList = new LinkedList<>();
    private LinkedList<ArrayList<Integer>> markedList = new LinkedList<ArrayList<Integer>>();
    private ArrayList<Integer> mostRecentlyPrunedList = new ArrayList<Integer>();
    private ArrayList<Integer> mostRecentlyMarkedList = new ArrayList<Integer>();

    /**
     * Constructers
     */

    public Variable() {
    }

    public Variable(int id, int[] d) {
        this.id = id;
        this.depth = 0;
        this.domain = d;
    }

    /**
     * Getters
     */

    public int getId() {
        return id;
    }

    public int getValue() {
        return this.value;
    }

    public int[] getDomain() {
        return this.domain;
    }

    public int getDepth() {
        return depth;
    }

    public List<Integer> getPrunedList() {
        return prunedList;
    }

    public int[] getAssigned() {
        return assigned;
    }

    public ArrayList<Integer> getMostRecentlyPruned() {
        return this.mostRecentlyPrunedList;
    }

    /**
     * Setters
     */

    public void setDomain(int[] domain) {
        this.domain = domain;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setPruned(LinkedList<Integer> pruned) {
        this.prunedList = pruned;
    }

    /**
     * Set val to var
     */
    public void assign(int value) {
        this.value = value;
        assigned = Utils.add(assigned, value);
    }

    /**
     * Remove val from var
     */
    public void unassign() {
        this.value = -1;
    }

    /**
     * Delete a value from the domain
     */
    public void deleteValue(int value) {
        this.domain = Utils.removeElement(this.domain, value);
    }

    /**
     * Restore a pruned value to the domain
     */
    public void restoreValue(int val) {
        this.domain = Utils.add(this.domain, val);
        Arrays.sort(domain);
    }

    /**
     * Removes a value from the domain and add it to the prune list
     */
    public void prune(int d) {
        this.domain = Utils.removeElement(this.domain, d);
        this.prunedList.add(d);
    }

    /**
     * Add mostRecentlyPrunedList to prunedList and init mostRecentlyPrunedList
     */
    public void savePrune() {
        if (mostRecentlyPrunedList.size() > 0) {
            prunedList.add(mostRecentlyPrunedList.get(0));
            mostRecentlyPrunedList = new ArrayList<Integer>();
        }

    }

    /**
     * Add mostRecentlyMarkedList to markedList and init mostRecentlyMarkedList
     */
    public void saveMark() {
        if (mostRecentlyMarkedList.size() > 0) {
            markedList.add(mostRecentlyMarkedList);
            mostRecentlyMarkedList = new ArrayList<Integer>();
        }

    }

    public Variable getCopy() {
        Variable new_var = new Variable(this.id, this.domain);
        new_var.setDomain(this.getDomain());
        return new_var;
    }

    /**
     * Reverses the changes made by reviseFutureArcs
     */
    public void replaceVal() {
        int val = this.prunedList.remove(prunedList.size() - 1);
        this.domain = Utils.add(this.domain, val);
    }

    /**
     * Remove last element from markedList if it is not empty
     */
    public void undoMarking() {
        if (!markedList.isEmpty()) {
            markedList.removeLast();
        }
    }

    /**
     * This happens if the domain is empty,
     */
    public boolean isNeedCancel() {
        for (int i : domain) {
            if (!prunedList.contains(i) && !isMarked(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get smallest domain
     */
    public int getSmallestDomain() {
        int smallestDomain = 1000;
        for (int d : domain) {
            if (smallestDomain > d && !Utils.contains(this.assigned, d) && !this.isMarked(d)) {
                smallestDomain = d;
            }
        }
        return smallestDomain;
    }

    /**
     * Checks if the domain is empty
     */
    public boolean isDomainEmpty() {
        return this.domain.length == 0;
    }

    /**
     * Assign check
     */
    public boolean isAssigned() {
        return value >= 0;
    }

    /**
     * Check if the value is marked
     */
    public boolean isMarked(int a) {
        for (ArrayList<Integer> l : markedList) {
            if (l.contains(a)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if vars have the same id
     */
    public boolean equals(Variable v) {
        return this.id == v.getId();
    }

    /**
     * Check if the domain include the value
     */
    public boolean hasSupport(int value) {
        return Utils.contains(this.domain, value);
    }

    /**
     * Check if the domain include the values in supports
     */
    public boolean hasSupport(ArrayList<Integer> supports) {
        int intersection_count = 0;

        for (int s : supports) {
            if (Utils.contains(this.domain, s) && !isMarked(s)) {
                intersection_count += 1;
            }
        }

        return intersection_count >= 1;
    }

    /**
     * Remove the unsupported values from the domain
     */
    public void markUnsupportedValues(ArrayList<Integer> supported_values) {
        for (int k = 0; k < this.domain.length; k++) {
            if (!supported_values.contains(this.domain[k]) && !isMarked(this.domain[k])) {
                mostRecentlyMarkedList.add(this.domain[k]);
            }
        }
    }

    /**
     * Get the integer list which includes unmarked variables and in domain
     */
    public int[] getInDomainAndUnmarked() {
        int[] inDomainUnmarked = null;

        for (int d : domain) {
            if (!isMarked(d)) {
                inDomainUnmarked = Utils.add(inDomainUnmarked, d);
            }
        }

        return inDomainUnmarked;

    }
}
