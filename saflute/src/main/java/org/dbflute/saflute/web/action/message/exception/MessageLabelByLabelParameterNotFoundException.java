package org.dbflute.saflute.web.action.message.exception;

import org.dbflute.saflute.core.exception.MySystemException;

/**
 * @author jflute
 */
public class MessageLabelByLabelParameterNotFoundException extends MySystemException {

    private static final long serialVersionUID = 1L;

    public MessageLabelByLabelParameterNotFoundException(String transitionKey) {
        super(transitionKey);
    }

    public MessageLabelByLabelParameterNotFoundException(String transitionKey, Throwable cause) {
        super(transitionKey, cause);
    }
}
