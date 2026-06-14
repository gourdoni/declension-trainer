package io.github.gourdoni.declension.domain;

/**
 * Failed repository operation.
 * Keeps checked persistence layer exceptions (e.g. `SQLException`) isolated to that layer.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String content, Throwable cause) {
        super(content, cause);
    }
}
