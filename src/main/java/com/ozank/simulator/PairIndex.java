package com.ozank.simulator;

import java.util.Objects;

public class PairIndex {
    private final int a;
    private final int b;
    private int hashCode;

    PairIndex(int a, int b) {
        this.a = a;
        this.b = b;
        this.hashCode = Objects.hash(a, b);
    }

    public Integer getA() {
        return a;
    }

    public Integer getB() {
        return b;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PairIndex)) {
            return false;
        }
        PairIndex p = (PairIndex) obj;

        return p.getA() == a && p.getB() == b;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString(){
        return "(" + a + "," + b + ")";
    }
}
