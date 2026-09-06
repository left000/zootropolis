package com.zootropolis.exception;

public class ErroreValidazioneException extends RuntimeException {

    public ErroreValidazioneException(String messaggio) {
        super(messaggio);
    }
}