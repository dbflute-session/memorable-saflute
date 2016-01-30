package org.dbflute.saflute.web.action.exception;

/**
 * @author jflute
 */
public class ForcedIllegalTransitionApplicationException extends MessageKeyApplicationException {

    private static final long serialVersionUID = 1L;

    public ForcedIllegalTransitionApplicationException(String transitionKey) {
        super(transitionKey);
    }

    public ForcedIllegalTransitionApplicationException(String transitionKey, Throwable cause) {
        super(transitionKey, cause);
    }
}
