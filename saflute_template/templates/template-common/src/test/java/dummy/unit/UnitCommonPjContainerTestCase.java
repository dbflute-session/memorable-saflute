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
package ${packageName}.unit;

import java.util.Date;

import org.dbflute.saflute.core.magic.ThreadCacheContext;
import org.dbflute.saflute.core.magic.TransactionTimeContext;
import org.dbflute.saflute.core.time.TimeManager;
import org.dbflute.utflute.seasar.ContainerTestCase;

/**
 * Use like this:
 * <pre>
 * YourTest extends {@link UnitCommonPjContainerTestCase} {
 * 
 *     public void test_yourMethod() {
 *         <span style="color: #3F7E5E">// ${_DS_} Arrange ${_DS_}</span>
 *         YourAction action = new YourAction();
 *         <span style="color: #FD4747">inject</span>(action);
 * 
 *         <span style="color: #3F7E5E">// ${_DS_} Act ${_DS_}</span>
 *         action.submit();
 * 
 *         <span style="color: #3F7E5E">// ${_DS_} Assert ${_DS_}</span>
 *         assertTrue(action...);
 *     }
 * }
 * </pre>
 * @author saflute_template
 */
public abstract class UnitCommonPjContainerTestCase extends ContainerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initializeThreadCacheContext();
        initializeTransactionTime();
    }

    protected void initializeThreadCacheContext() {
        ThreadCacheContext.initialize();
    }

    protected void initializeTransactionTime() {
        // because of non-UserTransaction transaction in UTFlute
        final TimeManager timeManager = getComponent(TimeManager.class);
        final Date transactionTime = timeManager.getFlashDate();
        TransactionTimeContext.setTransactionTime(transactionTime);
    }

    @Override
    protected String prepareConfigFile() {
        return "test_app.dicon"; // because this project does not have app.dicon
    }

    @Override
    public void tearDown() throws Exception {
        TransactionTimeContext.clear();
        ThreadCacheContext.clear();
        super.tearDown();
    }
}
