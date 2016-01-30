package org.dbflute.saflute.web.action.message.exception;

import org.dbflute.saflute.core.exception.MySystemException;

/**
 * @author jflute
 */
public class MessageLabelByLabelVariableInfinityLoopException extends MySystemException {

    private static final long serialVersionUID = 1L;

    public MessageLabelByLabelVariableInfinityLoopException(String transitionKey) {
        super(transitionKey);
    }

    public MessageLabelByLabelVariableInfinityLoopException(String transitionKey, Throwable cause) {
        super(transitionKey, cause);
    }
}
