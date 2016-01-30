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
package org.dbflute.saflute.web.action.callback;

import org.dbflute.saflute.web.action.processor.ActionRequestProcessor;
import org.dbflute.saflute.web.action.processor.GodHandableActionWrapper;

/**
 * The callback for action, which is called from {@link ActionRequestProcessor}. <br>
 * Methods that start with 'godHand' and 'callback' exist. <br >
 * You can creatively use like this:
 * <ul>
 *     <li>The 'godHand' methods are basically for super class by architect.</li>
 *     <li>The 'callback' methods are basically for concrete class by (many) developers.</li>
 * </ul>
 * The methods calling order is like this: <br>
 * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
 * <pre>
 * {@link #godHandActionPrologue()}
 * {@link #godHandBefore()}
 * {@link #callbackBefore()}
 * (exception in before is also handled by monologue)
 * try {
 *     (action execution, also validation, here)
 *     {@link #godHandSuccessMonologue()}
 * } catch (...) {
 *     {@link #godHandExceptionMonologue()}
 * } finally {
 *     {@link #callbackFinally()}
 *     {@link #godHandFinally()}
 *     {@link #godHandActionEpilogue()}
 * }
 * </pre>
 * @author jflute
 */
public interface ActionCallback {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The dummy forward path that means resolved response. */
    String RESPONSE_RESOLVED_DUMMY_FORWARD = "response_resolved_dummy_forward";

    // /= = = = = = = = = = = = = = = = =
    // definition-order is calling-order
    // = = = = = = = = = =/
    // ===================================================================================
    //                                                                              Before
    //                                                                              ======
    /**
     * Callback process as God hand (means Framework process) for action prologue (preparing action). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()} // *here
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @return The path to forward. (NullAllowed: if not null, no action execution, no more callback process)
     */
    String godHandActionPrologue(ActionExecuteMeta executeMeta);

    /**
     * Callback process as God hand (means Framework process) before action execution and validation. <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()} // *here
     * {@link #callbackBefore()}
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @return The path to forward. (NullAllowed: if not null, no action execution, no more callback process)
     */
    String godHandBefore(ActionExecuteMeta executeMeta);

    /**
     * Callback process as sub-class before action execution and validation. <br>
     * You can implement or override this at concrete class (Super class should not use this).
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()} // *here
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @return The path to forward. (NullAllowed: if not null, no action execution, no more callback process)
     */
    String callbackBefore(ActionExecuteMeta executeMeta);

    // ===================================================================================
    //                                                                          on Success
    //                                                                          ==========
    /**
     * Callback process as God hand (means Framework process) for success monologue (after action execution). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * It does not contain validation error, which is called on-success process. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()} // *here
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @return The path to forward. (NullAllowed: if not null, ignores action's return)
     */
    String godHandSuccessMonologue(ActionExecuteMeta executeMeta);

    // ===================================================================================
    //                                                                          on Failure
    //                                                                          ==========
    /**
     * Callback process as God hand (means Framework process) for exception monologue (exception handling). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * It does not contain validation error, which is called on-success process. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()} // *here
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @return The path to forward. (NullAllowed: if not null, ignores action's return)
     */
    String godHandExceptionMonologue(ActionExecuteMeta executeMeta);

    // ===================================================================================
    //                                                                             Finally
    //                                                                             =======
    /**
     * Callback process as sub-class after action execution (success or not: finally).
     * You can implement or override this at concrete class (Super class should not use this). <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()} // *here
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     */
    void callbackFinally(ActionExecuteMeta executeMeta);

    /**
     * Callback process as God hand (means Framework process) after action execution (success or not: finally). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()} // *here
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     */
    void godHandFinally(ActionExecuteMeta executeMeta);

    /**
     * Callback process as God hand (means Framework process) for action epilogue (closing action). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link GodHandableActionWrapper})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * (exception in before is also handled by monologue)
     * try {
     *     (action execution, also validation, here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()} // *here
     * }
     * </pre>
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     */
    void godHandActionEpilogue(ActionExecuteMeta executeMeta);

    // ===================================================================================
    //                                                                          Small Hook
    //                                                                          ==========
    /**
     * Hook the process when validation success before action execution. <br>
     * (if no validation, executed)
     * @return The path to forward. (NullAllowed: if not null, no action execution)
     */
    default String hookValidationSuccessActually() {
        return null;
    }

    /**
     * Hook the process when between validation action execution. <br>
     * (if validation error, not called)
     * @return The path to forward. (NullAllowed: if not null, no action execution)
     */
    default String hookBetweenValidationAndAction() { // for miyasama
        return null;
    }
}
