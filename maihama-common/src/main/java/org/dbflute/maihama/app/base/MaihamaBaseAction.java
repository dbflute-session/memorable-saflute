/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.maihama.app.base;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.Entity;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.helper.HandyDate;
import org.dbflute.hook.AccessContext;
import org.dbflute.maihama.projectfw.core.direction.MaihamaConfig;
import org.dbflute.maihama.projectfw.web.action.MaihamaMessages;
import org.dbflute.maihama.projectfw.web.paging.PagingNavi;
import org.dbflute.saflute.db.dbflute.accesscontext.AccessContextArranger;
import org.dbflute.saflute.db.dbflute.accesscontext.AccessContextResource;
import org.dbflute.saflute.web.action.TypicalBaseAction;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.dbflute.saflute.web.action.exception.ForcedRequest404NotFoundException;
import org.dbflute.saflute.web.action.login.UserBean;

/**
 * @author jflute
 */
public abstract class MaihamaBaseAction extends TypicalBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MaihamaConfig maihamaConfig;

    // ===================================================================================
    //                                                                              Paging
    //                                                                              ======
    /**
     * Create the paging navigation as empty.
     * @return The new-created instance of paging navigation as empty. (NotNull)
     */
    protected PagingNavi createPagingNavi() {
        return new PagingNavi();
    }

    /**
     * Prepare the paging navigation for page-range.
     * @param pagingNavi The paging navigation prepared for the paging data. (NotNull)
     * @param page The selected page as bean of paging result. (NotNull)
     * @param linkPaths The varying array of link paths. (NotNull, EmptyAllowed)
     */
    protected void preparePagingNavi(PagingNavi pagingNavi, PagingResultBean<? extends Entity> page, Object... linkPaths) {
        final Integer rangeSize = maihamaConfig.getPagingPageRangeSizeAsInteger();
        final boolean fillLimit = maihamaConfig.isPagingPageRangeFillLimit();
        pagingNavi.prepare(page, op -> {
            op.rangeSize(rangeSize);
            if (fillLimit) {
                op.fillLimit();
            }
        }, linkPaths);
    }

    /**
     * Get page size (record count of one page) for paging.
     * @return The integer as page size. (NotZero, NotMinus)
     */
    protected int getPagingPageSize() {
        return maihamaConfig.getPagingPageSizeAsInteger();
    }

    // ===================================================================================
    //                                                                            Callback
    //                                                                            ========
    // to suppress unexpected override by sub-class
    // you should remove the 'final' if you want to override this
    @Override
    public final String godHandActionPrologue(ActionExecuteMeta executeMeta) {
        return super.godHandActionPrologue(executeMeta);
    }

    @Override
    public final String godHandExceptionMonologue(ActionExecuteMeta executeMeta) {
        return super.godHandExceptionMonologue(executeMeta);
    }

    @Override
    public final void godHandActionEpilogue(ActionExecuteMeta executeMeta) {
        super.godHandActionEpilogue(executeMeta);
    }

    // ===================================================================================
    //                                                                      Access Context
    //                                                                      ==============
    @Override
    protected AccessContextArranger createAccessContextArranger() {
        return new AccessContextArranger() {
            private static final int TRACE_COLUMN_SIZE = 200;

            public AccessContext arrangePreparedAccessContext(final AccessContextResource resource) {
                final AccessContext context = new AccessContext();
                // uses provider to synchronize it with transaction time
                context.setAccessLocalDateTimeProvider(() -> {
                    return timeManager.getCurrentLocalDateTime();
                });
                // uses provider to synchronize it with login status in session
                context.setAccessUserProvider(() -> {
                    return buildAccessUserTrace(resource);
                });
                return context;
            }

            private String buildAccessUserTrace(AccessContextResource resource) {
                final UserBean userBean = getUserBean();
                final Long userId = userBean.getUserId();
                final String userType = userBean.getUserType();
                final String domainType = userBean.getDomainType();
                final String moduleName = resource.getModuleName();
                final StringBuilder sb = new StringBuilder();
                sb.append(userType).append(":").append(userId != null ? userId : "-1");
                sb.append(",").append(domainType).append(",").append(moduleName);
                final String trace = sb.toString();
                if (trace.length() > TRACE_COLUMN_SIZE) {
                    return trace.substring(0, TRACE_COLUMN_SIZE);
                }
                return trace;
            }
        };
    }

    // ===================================================================================
    //                                                                     Verify Anything
    //                                                                     ===============
    /**
     * Check the condition is true or it throws client error (e.g. 400 bad request) forcedly. <br>
     * You can use this in your action process against invalid path parameters.
     * <pre>
     * verifyOrClientError("The pageNumber should be positive number: " + pageNumber, pageNumber &gt; 0);
     * </pre>
     * @param debugMsg The debug message for developer. (NotNull)
     * @param expectedBool The expected determination for your business, true or false. (false: e.g. 404 not found)
     */
    protected void verifyOrClientError(String debugMsg, boolean expectedBool) { // application may call
        assertArgumentNotNull("debugMsg", debugMsg);
        if (!expectedBool) {
            throwVerifiedClientError(debugMsg);
        }
    }

    protected void throwVerifiedClientError(String debugMsg) {
        throw new ForcedRequest404NotFoundException(debugMsg);
    }

    // ===================================================================================
    //                                                               Application Exception
    //                                                               =====================
    @Override
    protected String getErrorsAppAlreadyDeletedKey() {
        return MaihamaMessages.ERRORS_APP_ALREADY_DELETED;
    }

    @Override
    protected String getErrorsAppAlreadyUpdatedKey() {
        return MaihamaMessages.ERRORS_APP_ALREADY_UPDATED;
    }

    @Override
    protected String getErrorsAppAlreadyExistsKey() {
        return MaihamaMessages.ERRORS_APP_ALREADY_EXISTS;
    }

    @Override
    protected String getErrorsNotLoginKey() {
        return MaihamaMessages.ERRORS_NOT_LOGIN;
    }

    @Override
    protected String getErrorsAppIllegalTransitionKey() {
        return MaihamaMessages.ERRORS_APP_ILLEGAL_TRANSITION;
    }

    // ===================================================================================
    //                                                                   Conversion Helper
    //                                                                   =================
    // -----------------------------------------------------
    //                                            Collectors
    //                                            ----------
    protected <T> Collector<T, ?, List<T>> toList() {
        return Collectors.toList();
    }

    protected <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper);
    }

    // -----------------------------------------------------
    //                                           String Date
    //                                           -----------
    protected String toStringDate(LocalDate localDate) {
        return localDate != null ? doConvertToDisp(localDate) : null;
    }

    protected String toStringDate(LocalDateTime localDateTime) {
        return localDateTime != null ? doConvertToStringDate(localDateTime) : null;
    }

    private String doConvertToDisp(LocalDate localDate) {
        return new HandyDate(localDate, getConversionTimeZone()).toDisp(getStringDatePattern());
    }

    private String doConvertToStringDate(LocalDateTime localDateTime) {
        return new HandyDate(localDateTime, getConversionTimeZone()).toDisp(getStringDatePattern());
    }

    protected String toStringDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? doConvertToStringDateTime(localDateTime) : null;
    }

    private String doConvertToStringDateTime(LocalDateTime localDateTime) {
        return new HandyDate(localDateTime, getConversionTimeZone()).toDisp(getStringDateTimePattern());
    }

    // -----------------------------------------------------
    //                                            Local Date
    //                                            ----------
    protected LocalDate toLocalDate(String dateExp) {
        if (dateExp == null || dateExp.isEmpty()) {
            return null;
        }
        TimeZone userTimeZone = getConversionTimeZone();
        return new HandyDate(dateExp, userTimeZone).getLocalDate();
    }

    protected LocalDateTime toLocalDateTime(String dateTimeExp) {
        if (dateTimeExp == null || dateTimeExp.isEmpty()) {
            return null;
        }
        TimeZone userTimeZone = getConversionTimeZone();
        return new HandyDate(dateTimeExp, userTimeZone).getLocalDateTime();
    }

    // -----------------------------------------------------
    //                                   Conversion Resource
    //                                   -------------------
    protected String getStringDatePattern() {
        return "yyyy/MM/dd";
    }

    protected String getStringDateTimePattern() {
        return "yyyy/MM/dd HH:mm:ss";
    }

    protected TimeZone getConversionTimeZone() {
        return requestManager.getUserTimeZone();
    }
}
