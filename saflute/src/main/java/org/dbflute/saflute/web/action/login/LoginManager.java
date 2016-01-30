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
package org.dbflute.saflute.web.action.login;

import org.dbflute.Entity;
import org.dbflute.saflute.web.action.login.exception.LoginFailureException;

/**
 * @author jflute
 */
public interface LoginManager {

    // ===================================================================================
    //                                                                         Basic Login
    //                                                                         ===========
    /**
     * Do login for the user. (no auto-login fixedly)
     * @param email The email address for the login user. (NotNull)
     * @param password The plain password for the login user, which is encrypted in this method. (NotNull)
     * @throws LoginFailureException When it fails to login by the user info.
     */
    void login(String email, String password) throws LoginFailureException;

    /**
     * Do login for the user. (auto-login or not)
     * @param email The email address for the login user. (NotNull)
     * @param password The plain password for the login user, which is encrypted in this method. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    void login(String email, String password, boolean useAutoLogin) throws LoginFailureException;

    /**
     * Do login for the user. (auto-login and silent-login by the bean)
     * @param loginAuthBean The bean of login authentication info. (NotNull)
     * @throws LoginFailureException When it fails to login by the user info.
     */
    void login(LoginAuthBean loginAuthBean) throws LoginFailureException;

    // ===================================================================================
    //                                                                      Optional Login
    //                                                                      ==============
    /**
     * Do login with given user entity, e.g. used for partner authentication. (auto-login or not) <br>
     * No authentication here so the email and password is basically for auto-login key. 
     * @param givenEntity The given entity for user. (NullAllowed: if null, find user by email and password)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    void givenLogin(Entity givenEntity, boolean useAutoLogin) throws LoginFailureException;

    /**
     * Do login for the user by user ID (means identity login). (for auto-login or partner login)
     * @param userId for the login user. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to login by the user info.
     */
    void identityLogin(Long userId, boolean useAutoLogin) throws LoginFailureException;

    /**
     * Do login for the user by user key (means identity login). (for auto-login or partner login) <br>
     * The user key depends on implementation for your domain. e.g. key is same as ID <br>
     * You can change user key's structure by override. #change_user_key <br>
     * And the user key is also specified from auto-login process so it needs to match.
     * @param userKey The string key for the login user, same as auto-login key. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to login by the user info.
     */
    void identityLogin(String userKey, boolean useAutoLogin) throws LoginFailureException;

    /**
     * Do login silently (means no history) for the user. (auto-login or not) <br>
     * Basically you shouldn't call this except special needs.
     * @param email The email address for the login user. (NotNull)
     * @param password The plain password for the login user, which is encrypted in this method. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    void silentLogin(String email, String password, boolean useAutoLogin) throws LoginFailureException;

    /**
     * Do login silently (means no history) for the user by user ID. <br>
     * Basically you shouldn't call this except special needs.
     * @param userId The ID for the login user. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    void silentIdentityLogin(Long userId, boolean useAutoLogin) throws LoginFailureException;

    /**
     * Do login silently for the user by user key (means identity login). (for auto-login or partner login) <br>
     * The user key depends on implementation for your domain. e.g. key is same as ID <br>
     * You can change user key's structure by override. #change_user_key <br>
     * And the user key is also specified from auto-login process so it needs to match.
     * @param userKey The string key for the login user, same as auto-login key. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    void silentIdentityLogin(String userKey, boolean useAutoLogin) throws LoginFailureException;

    /**
     * Re-select user bean of session if exists. <br>
     * (synchronize user bean with database)
     * @throws LoginFailureException When it fails to find the user.
     */
    void reselectSessionUserBeanIfExists() throws LoginFailureException;

    // ===================================================================================
    //                                                                          Auto Login
    //                                                                          ==========
    /**
     * Do auto-login for the user.
     * @param updateToken Does it update access token of auto-login? (true: e.g. increase expire days)
     * @return Is the auto-login success?
     */
    boolean autoLogin(boolean updateToken);

    /**
     * Do auto-login silently for the user.
     * @param updateToken Does it update access token of auto-login? (true: e.g. increase expire days)
     * @return Is the auto-login success?
     */
    boolean silentAutoLogin(boolean updateToken);

    // ===================================================================================
    //                                                                              Logout
    //                                                                              ======
    /**
     * Logout for the user. (remove session and cookie info) <br>
     * The traditional name remains as memento.
     */
    void logoutProc();

    // ===================================================================================
    //                                                                         Login Check
    //                                                                         ===========
    /**
     * Check login required for the requested action. (with auto-login, preparing login-redirect)
     * @param resource The resource of login handling to determine required or not. (NotNull)
     * @return The forward path, basically for login redirect. (NullAllowed)
     */
    String checkLoginRequired(LoginHandlingResource resource);

    /**
     * Get the user bean in session. (you can determine login or not)
     * @return The user bean in session. (NullAllowed: if null, means not-login)
     */
    UserBean getSessionUserBean();

    /**
     * Is the action login-required?
     * @param resource The resource of login handling to determine required or not. (NotNull)
     * @return The determination, true or false.
     */
    boolean isLoginRequiredAction(LoginHandlingResource resource);

    /**
     * Is the action for login? (login action or not)
     * @param resource The resource of login handling to determine login action or not. (NotNull)
     * @return The determination, true or false.
     */
    boolean isLoginAction(LoginHandlingResource resource);

    /**
     * Is the action API-action?
     * @param resource The resource of login handling to determine required or not. (NotNull)
     * @return The determination, true or false.
     */
    boolean isApiAction(LoginHandlingResource resource);

    // ===================================================================================
    //                                                                      Login Redirect
    //                                                                      ==============
    /**
     * Save requested info to session for login-redirect.
     */
    void saveRequestedLoginRedirectInfo();

    /**
     * Redirect to login action as login-redirect.
     * @return The redirect path for login action. (NotNull)
     */
    String redirectToLoginAction();

    /**
     * Get the bean of login redirect saved in session.
     * @return The bean of login redirect. (NullAllowed: when no bean in session)
     */
    LoginRedirectBean getLoginRedirectBean();

    /**
     * Clear login redirect bean from session.
     */
    void clearLoginRedirectBean();

    /**
     * Redirect to the requested action before perform-login if it needs (perform-login and redirect info exists).
     * @param resource The resource of login handling to determine perform-login or not. (NotNull)
     * @return The forward path, basically for login redirect. (NullAllowed)
     */
    String redirectToRequestedActionIfNeeds(LoginHandlingResource resource);

    /**
     * Is the action request perform-login?
     * @param resource The resource of login handling to determine perform-login or not. (NotNull)
     * @return The determination, true or false.
     */
    boolean isPerformLoginAction(LoginHandlingResource resource);
}
