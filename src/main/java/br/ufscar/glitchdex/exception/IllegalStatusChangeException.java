package br.ufscar.glitchdex.exception;

public class IllegalStatusChangeException extends RuntimeException {
    public IllegalStatusChangeException(String s) {
        super(s);
    }
}