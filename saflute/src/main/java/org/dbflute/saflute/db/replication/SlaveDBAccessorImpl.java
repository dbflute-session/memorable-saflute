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
package org.dbflute.saflute.db.replication;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.bhv.core.BehaviorCommandHook;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.hook.CallbackContext;
import org.seasar.extension.datasource.DataSourceFactory;

/**
 * @author jflute
 */
public class SlaveDBAccessorImpl implements SlaveDBAccessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance for sub class. */
    private static final Log _log = LogFactory.getLog(SlaveDBAccessorImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected DataSourceFactory dataSourceFactory;

    // ===================================================================================
    //                                                                      SlaveDB Access
    //                                                                      ==============
    // -----------------------------------------------------
    //                                               Fixedly
    //                                               -------
    /** {@inheritDoc} */
    public <RESULT> RESULT accessFixedly(SlaveDBCallback<RESULT> noArgLambda) {
        assertCallbackNotNull(noArgLambda);
        return doAccessFixedly(noArgLambda);
    }

    protected <RESULT> RESULT doAccessFixedly(SlaveDBCallback<RESULT> callback) {
        assertCallbackNotNull(callback);
        final String currentName = dataSourceFactory.getSelectableDataSourceName();
        try {
            final String slaveDB = SLAVE_DB;
            if (_log.isDebugEnabled()) {
                _log.debug(buildSlaveDBAccessDebugMessage(slaveDB));
            }
            setupForcedMasterCallback();
            dataSourceFactory.setSelectableDataSourceName(slaveDB);
            return callback.callback();
        } finally {
            dataSourceFactory.setSelectableDataSourceName(currentName);
            clearForcedMasterCallback();
        }
    }

    protected String buildSlaveDBAccessDebugMessage(String slaveDB) {
        return "...Accessing to SlaveDB for " + mySchemaDisp() + ": " + slaveDB;
    }

    // -----------------------------------------------------
    //                                               IfNeeds
    //                                               -------
    /** {@inheritDoc} */
    public <RESULT> RESULT accessIfNeeds(SlaveDBCallback<RESULT> noArgLambda, boolean toSlave) {
        assertCallbackNotNull(noArgLambda);
        if (toSlave) {
            return doAccessFixedly(noArgLambda);
        } else {
            return noArgLambda.callback();
        }
    }

    // -----------------------------------------------------
    //                                         Random Access
    //                                         -------------
    /** {@inheritDoc} */
    public <RESULT> RESULT accessRandomFifty(SlaveDBCallback<RESULT> noArgLambda, long determinationNumber) {
        assertCallbackNotNull(noArgLambda);
        if (isRandomFiftyHit(determinationNumber)) {
            return doAccessFixedly(noArgLambda);
        } else {
            return noArgLambda.callback();
        }
    }

    protected boolean isRandomFiftyHit(long determinationNumber) {
        return (determinationNumber % 2) == 0;
    }

    // ===================================================================================
    //                                                                        Fixed Master
    //                                                                        ============
    // you can use in public methods in your project component that inherits this
    // (it might be available in your UnitTest, so this method was prepared)
    protected <RESULT> RESULT doMasterAccessFixedly(SlaveDBCallback<RESULT> noArgLambda) {
        assertCallbackNotNull(noArgLambda);
        final String currentName = dataSourceFactory.getSelectableDataSourceName();
        final String masterDB = MASTER_DB;
        if (_log.isDebugEnabled()) {
            _log.debug(buildMasterAccessFixedlyDebugMessage(masterDB));
        }
        dataSourceFactory.setSelectableDataSourceName(masterDB);
        try {
            return noArgLambda.callback();
        } finally {
            dataSourceFactory.setSelectableDataSourceName(currentName);
        }
    }

    protected String buildMasterAccessFixedlyDebugMessage(String masterDB) {
        return "...Accessing to MasterDB for " + mySchemaDisp() + " fixedly: " + masterDB;
    }

    // ===================================================================================
    //                                                                       Forced Master
    //                                                                       =============
    protected void setupForcedMasterCallback() {
        CallbackContext.setBehaviorCommandHookOnThread(createForcedMasterHook());
    }

    protected BehaviorCommandHook createForcedMasterHook() {
        return new BehaviorCommandHook() {

            protected String currentName;
            protected boolean forcedSet;

            public void hookBefore(BehaviorCommandMeta meta) {
                if (needsForcedMasterCommand(meta)) {
                    final String masterDB = MASTER_DB;
                    currentName = dataSourceFactory.getSelectableDataSourceName();
                    if (!masterDB.equals(currentName)) {
                        if (_log.isDebugEnabled()) {
                            _log.debug(buildForcedMasterHookDebugMessage(masterDB));
                        }
                        dataSourceFactory.setSelectableDataSourceName(masterDB);
                        forcedSet = true;
                    }
                }
            }

            public void hookFinally(BehaviorCommandMeta meta, RuntimeException cause) {
                if (forcedSet) {
                    dataSourceFactory.setSelectableDataSourceName(currentName);
                }
            }

            @Override
            public boolean inheritsExistingHook() {
                return isExistingHookInherited(); // not override existing hook
            }
        };
    }

    protected boolean needsForcedMasterCommand(BehaviorCommandMeta meta) {
        return !meta.isSelect();
    }

    protected String buildForcedMasterHookDebugMessage(String masterDB) {
        return "...Accessing to MasterDB for " + mySchemaDisp() + " forcedly: " + masterDB;
    }

    protected boolean isExistingHookInherited() {
        return true;
    }

    protected void clearForcedMasterCallback() {
        CallbackContext.clearBehaviorCommandHookOnThread();
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected <RESULT> void assertCallbackNotNull(SlaveDBCallback<RESULT> callback) {
        if (callback == null) {
            String msg = "The argument 'noArgLambda' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                         Schema Info
    //                                                                         ===========
    protected String mySchemaDisp() {
        return "main schema";
    }
}
