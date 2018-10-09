package org.monarchinitiative.lr2pg.poisson;

public class NumberIsTooLargeException extends Exception {
    public NumberIsTooLargeException(double mc, double threshold) {
        super(String.format("%f exceeded %f",mc,threshold));
    }
}