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
package org.dbflute.saflute.core.direction;

import org.dbflute.saflute.db.dbflute.OptionalDBFluteDirection;
import org.dbflute.saflute.web.action.OptionalActionDirection;
import org.dbflute.saflute.web.servlet.OptionalServletDirection;

/**
 * @author jflute
 */
public abstract class CachedFwAssistantDirector implements FwAssistantDirector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected OptionalAssistDirection optionalAssistDirection;
    protected OptionalCoreDirection optionalCoreDirection;
    protected OptionalDBFluteDirection optionalDBFluteDirection;
    protected OptionalActionDirection optionalActionDirection;
    protected OptionalServletDirection optionalServletDirection;

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    public OptionalAssistDirection assistOptionalAssistDirection() {
        if (optionalAssistDirection != null) {
            return optionalAssistDirection;
        }
        synchronized (this) {
            if (optionalAssistDirection != null) {
                return optionalAssistDirection;
            }
            optionalAssistDirection = prepareOptionalAssistDirection();
        }
        if (optionalAssistDirection == null) {
            String msg = "Not found optional core direction.";
            throw new IllegalStateException(msg);
        }
        return optionalAssistDirection;
    }

    /**
     * Prepare the optional direction to assist. <br>
     * You cannot get configurations in this method
     * because the configuration component does not be injected to this yet.
     * @return The new-created instance of direction. (NotNull)
     */
    protected abstract OptionalAssistDirection prepareOptionalAssistDirection();

    // ===================================================================================
    //                                                                                Core
    //                                                                                ====
    public OptionalCoreDirection assistOptionalCoreDirection() {
        if (optionalCoreDirection != null) {
            return optionalCoreDirection;
        }
        synchronized (this) {
            if (optionalCoreDirection != null) {
                return optionalCoreDirection;
            }
            optionalCoreDirection = prepareOptionalCoreDirection();
        }
        if (optionalCoreDirection == null) {
            String msg = "Not found optional core direction.";
            throw new IllegalStateException(msg);
        }
        return optionalCoreDirection;
    }

    protected abstract OptionalCoreDirection prepareOptionalCoreDirection();

    // ===================================================================================
    //                                                                                  DB
    //                                                                                  ==
    public OptionalDBFluteDirection assistOptionalDBFluteDirection() {
        if (optionalDBFluteDirection != null) {
            return optionalDBFluteDirection;
        }
        synchronized (this) {
            if (optionalDBFluteDirection != null) {
                return optionalDBFluteDirection;
            }
            optionalDBFluteDirection = prepareOptionalDBFluteDirection();
        }
        if (optionalDBFluteDirection == null) {
            String msg = "Not found optional dbflute direction.";
            throw new IllegalStateException(msg);
        }
        return optionalDBFluteDirection;
    }

    protected abstract OptionalDBFluteDirection prepareOptionalDBFluteDirection();

    // ===================================================================================
    //                                                                                 Web
    //                                                                                 ===
    // -----------------------------------------------------
    //                                                Action
    //                                                ------
    public OptionalActionDirection assistOptionalActionDirection() {
        if (optionalActionDirection != null) {
            return optionalActionDirection;
        }
        synchronized (this) {
            if (optionalActionDirection != null) {
                return optionalActionDirection;
            }
            optionalActionDirection = prepareOptionalActionDirection();
        }
        if (optionalActionDirection == null) {
            String msg = "Not found optional action direction.";
            throw new IllegalStateException(msg);
        }
        return optionalActionDirection;
    }

    protected abstract OptionalActionDirection prepareOptionalActionDirection();

    // -----------------------------------------------------
    //                                               Servlet
    //                                               -------
    public OptionalServletDirection assistOptionalServletDirection() {
        if (optionalServletDirection != null) {
            return optionalServletDirection;
        }
        synchronized (this) {
            if (optionalServletDirection != null) {
                return optionalServletDirection;
            }
            optionalServletDirection = prepareOptionalServletDirection();
        }
        if (optionalServletDirection == null) {
            String msg = "Not found optional servlet direction.";
            throw new IllegalStateException(msg);
        }
        return optionalServletDirection;
    }

    protected abstract OptionalServletDirection prepareOptionalServletDirection();
}
