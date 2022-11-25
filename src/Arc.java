
public class Arc {

    private final Variable firstVar;
    private final Variable secondVar;

    /**
     * Constructers
     */
    public Arc(Variable firstVar, Variable secondVar) {
        this.firstVar = firstVar;
        this.secondVar = secondVar;
    }

    /**
     * Getters
     */

    public Variable getFirstVar() {
        return this.firstVar;
    }

    public Variable getSecondVar() {
        return this.secondVar;
    }

    /**
     * Check if variables have the same id
     */
    public boolean equals(Arc a) {
        return this.firstVar.equals(a.getFirstVar()) && this.secondVar.equals(a.getSecondVar());
    }

    /**
     * Swapping first and second var
     */
    public Arc reverse() {
        return new Arc(this.secondVar, this.firstVar);
    }

}
