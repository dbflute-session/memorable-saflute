package org.dbflute.saflute.web.action.message.exception;

import org.dbflute.saflute.core.exception.MySystemException;

/**
 * @author jflute
 */
public class MessageLabelByLabelVariableNotFoundException extends MySystemException {

    private static final long serialVersionUID = 1L;

    public MessageLabelByLabelVariableNotFoundException(String transitionKey) {
        super(transitionKey);
    }

    public MessageLabelByLabelVariableNotFoundException(String transitionKey, Throwable cause) {
        super(transitionKey, cause);
    }
}
