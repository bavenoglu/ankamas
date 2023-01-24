package massim.ankamas;

import eis.iilang.Percept;


public class TPercept {
    public int prcptid;
    public long tmstmp;
    public Percept prcpt;

    @Override
    public String toString() {
        return prcptid +
                ", " + tmstmp +
                ", " + prcpt;
    }
}
