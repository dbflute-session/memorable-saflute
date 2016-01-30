package org.dbflute.saflute.web.action.exception;

import org.dbflute.saflute.web.servlet.filter.RequestLoggingFilter.Request404NotFoundException;

/**
 * @author jflute
 */
public class ForcedRequest404NotFoundException extends Request404NotFoundException {

    private static final long serialVersionUID = 1L;

    public ForcedRequest404NotFoundException(String msg) {
        super(msg);
    }

    public ForcedRequest404NotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
