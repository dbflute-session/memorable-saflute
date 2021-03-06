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
package org.dbflute.maihama.projectfw.core.direction.sponsor;

import java.util.Date;

import org.dbflute.maihama.projectfw.core.direction.MaihamaConfig;
import org.dbflute.saflute.core.time.BusinessTimeHandler;
import org.dbflute.saflute.core.time.RelativeDateScript;
import org.dbflute.saflute.core.time.TimeManager;
import org.dbflute.saflute.core.time.TimeResourceProvider;
import org.dbflute.saflute.core.time.TypicalBusinessTimeHandler;

/**
 * @author jflute
 */
public class MaihamaTimeResourceProvider implements TimeResourceProvider {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final MaihamaConfig maihamaConfig;
    protected final RelativeDateScript script = new RelativeDateScript();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MaihamaTimeResourceProvider(MaihamaConfig maihamaConfig) {
        this.maihamaConfig = maihamaConfig;
    }

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    public BusinessTimeHandler provideBusinessTimeHandler(TimeManager timeManager) {
        return new TypicalBusinessTimeHandler(() -> {
            return timeManager.getCurrentMillis();
        }, () -> {
            return MaihamaUserTimeZoneProcessProvider.centralTimeZone;
        });
    }

    public boolean isCurrentIgnoreTransaction() {
        // this project uses transaction time for current date
        return false; // fixedly
    }

    // ===================================================================================
    //                                                                     Time Adjustment
    //                                                                     ===============
    public boolean isAdjustAbsoluteMode() { // *1
        final String exp = maihamaConfig.getTimeAdjustTimeMillis();
        return exp.startsWith("$"); // means absolute e.g. $(2014/07/10)
    }

    public long provideAdjustTimeMillis() { // *1
        final String exp = maihamaConfig.getTimeAdjustTimeMillis();
        try {
            return doProvideAdjustTimeMillis(exp);
        } catch (RuntimeException e) {
            String msg = "Illegal property for time.adjust.time.millis: " + exp;
            throw new IllegalStateException(msg);
        }
    }

    protected long doProvideAdjustTimeMillis(final String exp) {
        if (exp.startsWith("$")) { // absolute e.g. $(2014/07/10)
            return script.resolveHardCodingDate(exp).getTime();
        } else if (exp.contains("(")) { // relative e.g. addDay(3)
            final long current = System.currentTimeMillis();
            final Date resolved = script.resolveRelativeDate(exp, new Date(current));
            return resolved.getTime() - current;
        } else { // should be millisecond as relative
            return maihamaConfig.getTimeAdjustTimeMillisAsLong();
        }
    }
    // *1: called per called for dynamic change in development
}
