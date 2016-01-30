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
package org.dbflute.saflute.web.task.callback;

import org.dbflute.saflute.web.task.interceptor.BatchTaskInterceptor;

/**
 * The callback for batch task.
 * Basically this is very similar to action's callback. <br>
 * Actually created by copy at first but no recycle for little dependency between action and batch.
 * @author jflute
 */
public interface TaskCallback {

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
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()} // *here
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     */
    void godHandActionPrologue(TaskExecuteMeta executeMeta);

    /**
     * Callback process as God hand (means Framework process) before task execution and validation. <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()} // *here
     * {@link #callbackBefore()}
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     */
    void godHandBefore(TaskExecuteMeta executeMeta);

    /**
     * Callback process as sub-class before task execution and validation. <br>
     * You can implement or override this at concrete class (Super class should not use this).
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()} // *here
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     */
    void callbackBefore(TaskExecuteMeta executeMeta);

    // ===================================================================================
    //                                                                          on Success
    //                                                                          ==========
    /**
     * Callback process as God hand (means Framework process) for success monologue (after action execution). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * It does not contain validation error, which is called on-success process. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()} // *here
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     * @return The path to forward. (NullAllowed: if not null, ignores action's return)
     */
    String godHandSuccessMonologue(TaskExecuteMeta executeMeta);

    // ===================================================================================
    //                                                                          on Failure
    //                                                                          ==========
    /**
     * Callback process as God hand (means Framework process) for exception monologue (exception handling). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * It does not contain validation error, which is called on-success process. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()} // *here
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     * @return Does it finish handling the exception?
     */
    boolean godHandExceptionMonologue(TaskExecuteMeta executeMeta);

    // ===================================================================================
    //                                                                             Finally
    //                                                                             =======
    /**
     * Callback process as sub-class after task execution (success or not: finally).
     * You can implement or override this at concrete class (Super class should not use this). <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()} // *here
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     */
    void callbackFinally(TaskExecuteMeta executeMeta);

    /**
     * Callback process as God hand (means Framework process) after task execution (success or not: finally). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()} // *here
     *     {@link #godHandActionEpilogue()}
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     */
    void godHandFinally(TaskExecuteMeta executeMeta);

    /**
     * Callback process as God hand (means Framework process) for action epilogue (closing action). <br>
     * You should not implement or override this at concrete class because this is for super class. <br>
     * This method calling order is like this: <br>
     * (And you can see the details of this callback process by reading {@link BatchTaskInterceptor})
     * <pre>
     * {@link #godHandActionPrologue()}
     * {@link #godHandBefore()}
     * {@link #callbackBefore()}
     * try {
     *     (task execution here)
     *     {@link #godHandSuccessMonologue()}
     * } catch (...) {
     *     {@link #godHandExceptionMonologue()}
     * } finally {
     *     {@link #callbackFinally()}
     *     {@link #godHandFinally()}
     *     {@link #godHandActionEpilogue()} // *here
     * }
     * </pre>
     * @param executeMeta The meta of task execution which you can get the calling method. (NotNull)
     */
    void godHandActionEpilogue(TaskExecuteMeta executeMeta);
}
