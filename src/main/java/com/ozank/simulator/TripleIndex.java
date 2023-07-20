package com.ozank.simulator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TripleIndex {
    private final int a;
    private final int b;
    private final int c;
    private int hashCode;

    TripleIndex(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.hashCode = Objects.hash(a, b, c);
    }

    public Integer getA() {
        return a;
    }

    public Integer getB() {
        return b;
    }

    public Integer getC() {
        return c;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TripleIndex)) {
            return false;
        }
        TripleIndex p = (TripleIndex) obj;

        return p.getA() == a && p.getB() == b && p.getC() == c;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
    @Override
    public String toString(){
        return "(" + a + "," + b + "," +  c + ")";
    }

    public String toString(List m){
        return "(" + a + "," + b + "," +  m.get(c) + ")";
    }
}

