package proofer;


/**
 *
 * @author Ryan Kenney
 */
public enum BaseFact implements Statable {

    P('p'), Q('q'), R('r'), S('s'), T('t');
    private char crep; // primitive value representation

    private BaseFact(char crep) {
        this.crep = crep;
    }
    
    public boolean equals(char crep) {
        return this.crep == crep;
    }

    @Override
    public Operator getOperator() {
        return null;
    }

    @Override
    public Statable[] getOperands() {
        return new Statable[] {this};
    }
    
    @Override
    public String toString() {
        return String.valueOf(crep);
    }
}
