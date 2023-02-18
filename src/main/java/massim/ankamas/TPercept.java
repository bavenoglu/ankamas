package massim.ankamas;

import eis.iilang.Percept;

public class TPercept {
    public int prcptid;
    public long tmstmp;
    public Percept prcpt;
    public TPercept(){}; //Default constructor

    @Override
    public String toString() {
        return prcptid +
                ", " + tmstmp +
                ", " + prcpt;
    }
}
