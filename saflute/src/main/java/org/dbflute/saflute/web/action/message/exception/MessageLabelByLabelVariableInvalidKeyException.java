package org.dbflute.saflute.web.action.message.exception;

import org.dbflute.saflute.core.exception.MySystemException;

/**
 * @author jflute
 */
public class MessageLabelByLabelVariableInvalidKeyException extends MySystemException {

    private static final long serialVersionUID = 1L;

    public MessageLabelByLabelVariableInvalidKeyException(String transitionKey) {
        super(transitionKey);
    }

    public MessageLabelByLabelVariableInvalidKeyException(String transitionKey, Throwable cause) {
        super(transitionKey, cause);
    }
}
