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

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.Entity;
import org.dbflute.helper.HandyDate;
import org.dbflute.saflute.core.security.PrimaryCipher;
import org.dbflute.saflute.core.time.TimeManager;
import org.dbflute.saflute.web.action.ActionResolver;
import org.dbflute.saflute.web.action.api.ApiAction;
import org.dbflute.saflute.web.action.login.exception.LoginFailureException;
import org.dbflute.saflute.web.action.login.exception.LoginTransactionFailureException;
import org.dbflute.saflute.web.servlet.cookie.CookieManager;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.session.SessionManager;
import org.seasar.extension.tx.TransactionCallback;
import org.seasar.extension.tx.TransactionManagerAdapter;

/**
 * @param <USER_BEAN> The type of user bean.
 * @param <USER_ENTITY> The type of user entity or model.
 * @author jflute
 */
public abstract class TypicalLoginBaseLogic<USER_BEAN extends UserBean, USER_ENTITY> implements LoginManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(TypicalLoginBaseLogic.class);

    /** The delimiter of auto login value saved in cookie. */
    private static final String AUTO_LOGIN_DELIMITER = ":>:<:";

    /** The default expire days for auto login access token. */
    private static final int AUTO_LOGIN_ACCESS_TOKEN_DEFAULT_EXPIRE_DAYS = 7; // you can change by override

    /** The pattern of expire date for auto-login. */
    private static final String AUTO_LOGIN_ACCESS_TOKEN_EXPIRE_DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected PrimaryCipher primaryCipher;

    @Resource
    protected TimeManager timeManager;

    @Resource
    protected RequestManager requestManager;

    @Resource
    protected SessionManager sessionManager;

    @Resource
    protected CookieManager cookieManager;

    @Resource
    protected ActionResolver actionResolver;

    @Resource
    protected TransactionManagerAdapter transactionManagerAdapter;

    // ===================================================================================
    //                                                                                Find
    //                                                                                ====
    /**
     * Check the user is login-able. (basically for validation)
     * @param email The email address for the login user. (NotNull)
     * @param password The plain password for the login user, which is encrypted in this method. (NotNull)
     * @return true if the user is login-able.
     */
    public boolean checkUserLoginable(String email, String password) {
        return doCheckUserLoginable(email, encryptPassword(password));
    }

    /**
     * Check the user is login-able. (basically for validation)
     * @param email The email address for the login user. (NotNull)
     * @param cipheredPassword The ciphered password for the login user. (NotNull)
     * @return true if the user is login-able.
     */
    protected abstract boolean doCheckUserLoginable(String email, String cipheredPassword);

    /**
     * Check the user is login-able. (basically for validation)
     * @param loginAuthBean The bean of login authentication info. (NotNull)
     * @return true if the user is login-able.
     */
    public boolean checkUserLoginable(LoginAuthBean loginAuthBean) {
        return doCheckUserLoginable(loginAuthBean, encryptPassword(loginAuthBean.getPassword()));
    }

    /**
     * Check the user is login-able. (basically for validation)
     * @param loginAuthBean The bean of login authentication info. (NotNull)
     * @param cipheredPassword The ciphered password for the login user. (NotNull)
     * @return true if the user is login-able.
     */
    protected boolean doCheckUserLoginable(LoginAuthBean loginAuthBean, String cipheredPassword) {
        // unsupported as default for compatibility
        throw new IllegalStateException("you should override this method: " + loginAuthBean);
    }

    /**
     * Find the login user in the database.
     * @param email The email address for the login user. (NotNull)
     * @param password The plain password for the login user, which is encrypted in this method. (NotNull)
     * @return The entity of the found user. (NullAllowed: when the login user is not found)
     */
    public USER_ENTITY findLoginUser(String email, String password) {
        return doFindLoginUser(email, encryptPassword(password));
    }

    /**
     * Finding the login user in the database.
     * @param email The email address for the login user. (NotNull)
     * @param cipheredPassword The ciphered password for the login user. (NotNull)
     * @return The entity of the found user. (NullAllowed: when the login user is not found)
     */
    protected abstract USER_ENTITY doFindLoginUser(String email, String cipheredPassword);

    /**
     * Find the login user in the database.
     * @param loginAuthBean The bean of login authentication info. (NotNull)
     * @return The entity of the found user. (NullAllowed: when the login user is not found)
     */
    public USER_ENTITY findLoginUser(LoginAuthBean loginAuthBean) {
        return doFindLoginUser(loginAuthBean, encryptPassword(loginAuthBean.getPassword()));
    }

    /**
     * Finding the login user in the database.
     * @param loginAuthBean The bean of login authentication info. (NotNull)
     * @param cipheredPassword The ciphered password for the login user. (NotNull)
     * @return The entity of the found user. (NullAllowed: when the login user is not found)
     */
    protected USER_ENTITY doFindLoginUser(LoginAuthBean loginAuthBean, String cipheredPassword) {
        // unsupported as default for compatibility
        throw new IllegalStateException("you should override this method: " + loginAuthBean);
    }

    /**
     * Encrypt the password of the login user.
     * @param plainPassword The plain password for the login user, which is encrypted in this method. (NotNull)
     * @return The encrypted string of the password. (NotNull)
     */
    protected String encryptPassword(String plainPassword) {
        return primaryCipher.encrypt(plainPassword);
    }

    /**
     * Find the login user in the database.
     * @param userId for the login user. (NotNull)
     * @return The entity of the found user. (NullAllowed: when the login user is not found)
     */
    public USER_ENTITY findLoginUser(Long userId) {
        return doFindLoginUser(userId);
    }

    /**
     * Finding the login user in the database.
     * @param userId for the login user. (NotNull)
     * @return The entity of the found user. (NullAllowed: when the login user is not found)
     */
    protected abstract USER_ENTITY doFindLoginUser(Long userId);

    // ===================================================================================
    //                                                                         Basic Login
    //                                                                         ===========
    // -----------------------------------------------------
    //                                       Login Interface
    //                                       ---------------
    /**
     * {@inheritDoc}
     */
    public void login(String email, String password) throws LoginFailureException {
        login(email, password, false); // no auto-login
    }

    /**
     * {@inheritDoc}
     */
    public void login(String email, String password, boolean useAutoLogin) throws LoginFailureException {
        doLogin(email, password, useAutoLogin, false);
    }

    /**
     * {@inheritDoc}
     */
    public void login(LoginAuthBean loginAuthBean) throws LoginFailureException {
        doLogin(loginAuthBean);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void givenLogin(Entity givenEntity, boolean useAutoLogin) throws LoginFailureException {
        doLoginByGivenEntity((USER_ENTITY) givenEntity, useAutoLogin);
    }

    /**
     * {@inheritDoc}
     */
    public void identityLogin(Long userId, boolean useAutoLogin) throws LoginFailureException {
        doLoginByIdentity(userId, useAutoLogin);
    }

    /**
     * {@inheritDoc}
     */
    public void identityLogin(String userKey, boolean useAutoLogin) throws LoginFailureException {
        final Long userId = convertToUserId(userKey);
        doLoginByIdentity(userId, useAutoLogin); // as default (override if it needs)
    }

    /**
     * {@inheritDoc}
     */
    public void silentLogin(String email, String password, boolean useAutoLogin) throws LoginFailureException {
        doLogin(email, password, useAutoLogin, true);
    }

    /**
     * {@inheritDoc}
     */
    public void silentIdentityLogin(Long userId, boolean useAutoLogin) throws LoginFailureException {
        doLoginByIdentity(userId, useAutoLogin, true);
    }

    /**
     * {@inheritDoc}
     */
    public void silentIdentityLogin(String userKey, boolean useAutoLogin) throws LoginFailureException {
        final Long userId = convertToUserId(userKey);
        doLoginByIdentity(userId, useAutoLogin, true);
    }

    /**
     * Convert user key to user ID by your rule. #change_user_key <br>
     * The default rule is simple conversion to number, you can change it by overriding.
     * @param userKey The string key for the login user, same as auto-login key. (NotNull)
     * @return The ID for the login user. (NotNull)
     */
    protected Long convertToUserId(String userKey) {
        final Long userId;
        try {
            userId = Long.valueOf(userKey); // as default (override if it needs)
        } catch (NumberFormatException e) {
            throw new LoginFailureException("Invalid user key (not ID): " + userKey, e);
        }
        return userId;
    }

    // -----------------------------------------------------
    //                                    Actual Login Logic
    //                                    ------------------
    /**
     * Do actually login for the user by email and password.
     * @param email The email address for the login user. (NotNull)
     * @param password The plain password for the login user, which is encrypted in this method. (NullAllowed: only when given)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @param silently Is the login executed silently? (no saving history)
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    protected void doLogin(String email, String password, boolean useAutoLogin, boolean silently) throws LoginFailureException {
        assertLoginEMailRequired(email);
        assertLoginPasswordRequired(password);
        final USER_ENTITY userEntity = findLoginUser(email, password);
        if (userEntity == null) { // means login failure
            String msg = "Not found the user by the email and password: " + email;
            throw new LoginFailureException(msg);
        }
        handleLoginOption(userEntity, useAutoLogin, silently);
    }

    /**
     * Do actually login for the user by email and password.
     * @param loginAuthBean The bean of login authentication info. (NotNull)
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    protected void doLogin(LoginAuthBean loginAuthBean) throws LoginFailureException {
        assertLoginAuthBeanRequired(loginAuthBean);
        assertLoginPasswordRequired(loginAuthBean.getPassword());
        final USER_ENTITY userEntity = findLoginUser(loginAuthBean);
        if (userEntity == null) { // means login failure
            String msg = "Not found the user by the email and password: " + loginAuthBean;
            throw new LoginFailureException(msg);
        }
        handleLoginOption(userEntity, loginAuthBean.isUseAutoLogin(), loginAuthBean.isSilently());
    }

    /**
     * Do actually login for the user by given entity. (no silent)
     * @param givenEntity The given entity for user. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    protected void doLoginByGivenEntity(USER_ENTITY givenEntity, boolean useAutoLogin) throws LoginFailureException {
        assertGivenEntityRequired(givenEntity);
        handleLoginOption(givenEntity, useAutoLogin, false);
    }

    /**
     * Do actually login for the user by identity (user ID). (no silent)
     * @param userId for the login user. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    protected void doLoginByIdentity(Long userId, boolean useAutoLogin) throws LoginFailureException {
        doLoginByIdentity(userId, useAutoLogin, false);
    }

    /**
     * Do actually login for the user by identity (user ID). (no silent)
     * @param userId for the login user. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @param silently Is the login executed silently? (no saving history)
     * @throws LoginFailureException When it fails to do login by the user info.
     */
    protected void doLoginByIdentity(Long userId, boolean useAutoLogin, boolean silently) throws LoginFailureException {
        assertUserIdRequired(userId);
        final USER_ENTITY userEntity = findLoginUser(userId);
        if (userEntity == null) { // means login failure
            String msg = "Not found the user by the user ID: " + userId;
            throw new LoginFailureException(msg);
        }
        handleLoginOption(userEntity, useAutoLogin, silently);
    }

    /**
     * Handle login options for the found login user.
     * @param userEntity The found entity of the login user. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     * @param silently Is the login executed silently? (no saving history)
     */
    protected void handleLoginOption(USER_ENTITY userEntity, boolean useAutoLogin, boolean silently) {
        assertUserEntityRequired(userEntity);
        final USER_BEAN userBean = saveLoginInfoToSession(userEntity);
        if (userBean instanceof SyncCheckable) {
            ((SyncCheckable) userBean).setLastestSyncCheckDate(timeManager.getCurrentDate());
        }
        if (useAutoLogin) {
            saveAutoLoginKeyToCookie(userEntity, userBean);
        }
        if (!silently) { // mainly here
            transactionCallSaveLoginHistory(userEntity, userBean, useAutoLogin);
            processOnBrightLogin(userEntity, userBean, useAutoLogin);
        } else {
            processOnSilentLogin(userEntity, userBean, useAutoLogin);
        }
    }

    protected void assertLoginEMailRequired(String email) {
        if (email == null || email.length() == 0) {
            String msg = "The argument 'email' should not be null for login.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertLoginPasswordRequired(String password) {
        if (password == null || password.length() == 0) {
            String msg = "The argument 'password' should not be null for login.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertLoginAuthBeanRequired(LoginAuthBean loginAuthBean) {
        if (loginAuthBean == null) {
            String msg = "The argument 'loginAuthBean' should not be null for login.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertGivenEntityRequired(USER_ENTITY givenEntity) {
        if (givenEntity == null) {
            String msg = "The argument 'givenEntity' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertUserIdRequired(Long userId) {
        if (userId == null) {
            String msg = "The argument 'userId' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    protected void assertUserEntityRequired(USER_ENTITY userEntity) {
        if (userEntity == null) {
            String msg = "The argument 'userEntity' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    // -----------------------------------------------------
    //                                     UserBean Handling
    //                                     -----------------
    /**
     * Save login info as user bean to session.
     * @param userEntity The entity of the found user. (NotNull)
     * @return The user bean saved in session. (NotNull)
     */
    protected USER_BEAN saveLoginInfoToSession(USER_ENTITY userEntity) {
        regenerateSessionId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Saving login info to session");
        }
        final USER_BEAN userBean = createUserBean(userEntity);
        sessionManager.setAttribute(userBean);
        return userBean;
    }

    /**
     * Regenerate session ID for security. <br>
     * call invalidate() but it inherits existing session attributes.
     */
    protected void regenerateSessionId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Regenerating session ID for security");
        }
        sessionManager.regenerateSessionId();
    }

    /**
     * Create the user bean for the user.
     * @param userEntity The selected entity of login user. (NotNull)
     * @return The new-created instance of user bean to be saved in session. (NotNull)
     */
    protected abstract USER_BEAN createUserBean(USER_ENTITY userEntity);

    /**
     * {@inheritDoc}
     */
    public void reselectSessionUserBeanIfExists() throws LoginFailureException {
        final USER_BEAN oldBean = getSessionUserBean();
        if (oldBean == null) {
            return;
        }
        inheritUserBeanAdditionalInfo(oldBean);
        final Long userId = oldBean.getUserId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Re-selecting user bean in session: userId=" + userId);
        }
        final USER_ENTITY userEntity = findLoginUser(userId);
        if (userEntity == null) { // might be already left
            logoutProc(); // to clear old user info in session
            String msg = "Not found the user by the user ID: " + userId;
            throw new LoginFailureException(msg);
        }
        sessionManager.setAttribute(createUserBean(userEntity));
    }

    protected void inheritUserBeanAdditionalInfo(USER_BEAN oldBean) {
        // do nothing as default
    }

    // -----------------------------------------------------
    //                                    AutoLogin Handling
    //                                    ------------------
    /**
     * Save auto-login key to cookie.
     * @param userEntity The selected entity of login user. (NotNull)
     * @param userBean The user bean saved in session. (NotNull)
     */
    protected void saveAutoLoginKeyToCookie(USER_ENTITY userEntity, USER_BEAN userBean) {
        final int expireDays = getAutoLoginAccessTokenExpireDays();
        final String cookieKey = getCookieAutoLoginKey();
        doSaveAutoLoginCookie(userEntity, userBean, expireDays, cookieKey);
    }

    /**
     * Get the expire days of both access token and cookie value. <br>
     * You can change it by override.
     * @return The count of expire days. (NotMinus, NotZero)
     */
    protected int getAutoLoginAccessTokenExpireDays() {
        return AUTO_LOGIN_ACCESS_TOKEN_DEFAULT_EXPIRE_DAYS; // as default for compatibility
    }

    /**
     * Get the key of auto login saved in cookie.
     * @return The string key for cookie. (NotNull)
     */
    protected abstract String getCookieAutoLoginKey();

    /**
     * Do save auto-login key to cookie.
     * @param userEntity The selected entity of login user. (NotNull)
     * @param userBean The user bean saved in session. (NotNull)
     * @param expireDays The expire days of both access token and cookie value.
     * @param cookieKey The key of the cookie. (NotNull)
     */
    protected void doSaveAutoLoginCookie(USER_ENTITY userEntity, USER_BEAN userBean, int expireDays, String cookieKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Saving auto-login key to cookie: key=" + cookieKey);
        }
        final String value = buildAutoLoginCookieValue(userEntity, userBean, expireDays);
        final int expireSeconds = expireDays * 60 * 60 * 24; // cookie's expire, same as access token
        cookieManager.setCookieCiphered(cookieKey, value, expireSeconds);
    }

    /**
     * Build the value for auto login saved in cookie. <br>
     * You can change access token's structure by override. #change_access_token
     * @param userEntity The selected entity of login user. (NotNull)
     * @param userBean The user bean saved in session. (NotNull)
     * @param expireDays The count of expired days from current times. (NotNull)
     * @return The string value for auto login. (NotNull)
     */
    protected String buildAutoLoginCookieValue(USER_ENTITY userEntity, USER_BEAN userBean, int expireDays) {
        final String autoLoginKey = createAutoLoginKey(userEntity, userBean);
        final String delimiter = getAutoLoginDelimiter();
        final HandyDate currentHandyDate = timeManager.getCurrentHandyDate();
        final HandyDate expireDate = currentHandyDate.addDay(expireDays); // access token's expire
        return autoLoginKey + delimiter + formatForAutoLoginExpireDate(expireDate);
    }

    /**
     * Create auto-login key for the user. <br>
     * You can change user key's structure by override. #change_user_key
     * @param userEntity The selected entity of login user. (NotNull)
     * @param userBean The user bean saved in session. (NotNull)
     * @return The string expression for auto-login key. (NotNull)
     */
    protected String createAutoLoginKey(USER_ENTITY userEntity, USER_BEAN userBean) {
        return String.valueOf(userBean.getUserId()); // as default (override if it needs)
    }

    // -----------------------------------------------------
    //                                      History Handling
    //                                      ----------------
    /**
     * Call the process, saving login history, in new transaction for e.g. auto-login in callback. <br>
     * Update statement needs transaction (access-context) so needed. <br>
     * Meanwhile, the transaction inherits already-begun transaction for e.g. normal login process.
     * @param userEntity The entity of the found login user. (NotNull)
     * @param userBean The bean of the user saved in session. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     */
    protected void transactionCallSaveLoginHistory(final USER_ENTITY userEntity, final USER_BEAN userBean, final boolean useAutoLogin) {
        try {
            // inherit when e.g. called by action, begin new when e.g. auto-login
            transactionManagerAdapter.required(new TransactionCallback() {
                public Object execute(TransactionManagerAdapter adapter) throws Throwable {
                    try {
                        saveLoginHistory(userEntity, userBean, useAutoLogin);
                    } catch (RuntimeException e) {
                        adapter.setRollbackOnly(); // wants to set when really begun
                        throw e;
                    }
                    return null;
                }
            });
        } catch (Throwable e) {
            handleSavingLoginHistoryTransactionFailure(userBean, e);
        }
    }

    /**
     * Handle the exception of transaction failure for saving login history.
     * @param userBean The bean of the user saved in session. (NotNull)
     * @param cause The cause exception of transaction failure. (NotNull)
     */
    protected void handleSavingLoginHistoryTransactionFailure(USER_BEAN userBean, Throwable cause) {
        // thinking system error or ignore... system error for now
        String msg = "Failed to save login history: " + userBean.getUserId() + ", " + userBean.getUserType();
        throw new LoginTransactionFailureException(msg, cause);
    }

    /**
     * Save the history of the success login. (already saved in session at this point) <br>
     * For example, you can save the login user's info to database. <br>
     * This is NOT called when silent login.
     * @param userEntity The entity of the login user. (NotNull)
     * @param userBean The user bean of the login user, already saved in session. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     */
    protected abstract void saveLoginHistory(USER_ENTITY userEntity, USER_BEAN userBean, boolean useAutoLogin);

    // -----------------------------------------------------
    //                                    Process on Success
    //                                    ------------------
    /**
     * Process your favorite logic on bright login (except silent-login).
     * @param userEntity The entity of the login user. (NotNull)
     * @param userBean The user bean of the login user, already saved in session. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     */
    protected void processOnBrightLogin(USER_ENTITY userEntity, USER_BEAN userBean, boolean useAutoLogin) {
        // do nothing as default
    }

    /**
     * Process your favorite logic on silent login.
     * @param userEntity The entity of the login user. (NotNull)
     * @param userBean The user bean of the login user, already saved in session. (NotNull)
     * @param useAutoLogin Does it use auto-login for next time login?
     */
    protected void processOnSilentLogin(USER_ENTITY userEntity, USER_BEAN userBean, boolean useAutoLogin) {
        // do nothing as default
    }

    // ===================================================================================
    //                                                                          Auto Login
    //                                                                          ==========
    /**
     * {@inheritDoc}
     */
    public boolean autoLogin(boolean updateToken) {
        return delegateAutoLogin(updateToken, false);
    }

    /**
     * {@inheritDoc}
     */
    public boolean silentAutoLogin(boolean updateToken) {
        return delegateAutoLogin(updateToken, true);
    }

    protected boolean delegateAutoLogin(boolean updateToken, boolean silently) {
        final Cookie cookie = cookieManager.getCookieCiphered(getCookieAutoLoginKey());
        if (cookie == null) {
            return false;
        }
        final String cookieValue = cookie.getValue();
        if (cookieValue != null && cookieValue.trim().length() > 0) {
            final String[] valueAry = cookieValue.split(getAutoLoginDelimiter());
            final Boolean handled = handleAutoLoginCookie(updateToken, silently, valueAry);
            if (handled != null) {
                return handled;
            }
            if (handleAutoLoginInvalidCookie(cookieValue, valueAry)) { // you can also retry
                return true; // success by the handling
            }
        }
        return false;
    }

    /**
     * Handle auto-login cookie (and do auto-login). <br>
     * You can change access token's structure by override. #change_access_token
     * @param updateToken Does it update access token of auto-login? (true: e.g. increase expire days)
     * @param silently Is the login executed silently? (no saving history)
     * @param valueAry The array of cookie values. (NotNull)
     * @return The determination of auto-login, true or false or null. (NullAllowed: means invalid cookie) 
     */
    protected Boolean handleAutoLoginCookie(boolean updateToken, boolean silently, String[] valueAry) {
        if (valueAry.length != 2) { // invalid cookie
            return null;
        }
        final String userKey = valueAry[0]; // resolved by identity login
        final String expireDate = valueAry[1]; // AccessToken's expire
        if (isValidAutoLoginCookie(userKey, expireDate)) {
            return doAutoLogin(userKey, expireDate, updateToken, silently);
        }
        return null;
    }

    /**
     * Are the user ID and expire date extracted from cookie valid?
     * @param userKey The key of the login user. (NotNull)
     * @param expireDate The string expression for expire date of auto-login access token. (NotNull)
     * @return Is a validation for auto login OK?
     */
    protected boolean isValidAutoLoginCookie(String userKey, String expireDate) {
        final String currentDate = formatForAutoLoginExpireDate(timeManager.getCurrentHandyDate());
        if (currentDate.compareTo(expireDate) < 0) { // String v.s. String
            return true; // valid access token within time limit
        }
        // expired here
        if (LOG.isDebugEnabled()) {
            LOG.debug("The access token for auto-login expired: userKey=" + userKey + " expireDate=" + expireDate);
        }
        return false;
    }

    /**
     * Do actually auto-login for the user.
     * @param userKey The key of the login user, used by identity login. (NotNull)
     * @param expireDate The string expression for expire date of auto-login access token. (NotNull)
     * @param updateToken Does it update access token of auto-login? (true: e.g. increase expire days)
     * @param silently Is the login executed silently? (no saving history)
     * @return Is the auto-login success?
     */
    protected boolean doAutoLogin(String userKey, String expireDate, boolean updateToken, boolean silently) {
        if (LOG.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("...Doing auto-login: user=").append(userKey);
            sb.append(", expire=").append(expireDate);
            if (updateToken) {
                sb.append(", updateToken");
            }
            if (silently) {
                sb.append(", silently");
            }
            final String msg = sb.toString();
            LOG.debug(msg);
        }
        try {
            // no delete cookie in this implementation if false
            final boolean useAutoLogin = updateToken;
            if (silently) {
                silentIdentityLogin(userKey, useAutoLogin);
            } else {
                identityLogin(userKey, useAutoLogin);
            }
            return true;
        } catch (NumberFormatException invalidUserKey) { // just in case
            if (LOG.isDebugEnabled()) { // to know invalid user key or bug
                LOG.debug("*The user key might be invalid: " + userKey + ", " + invalidUserKey.getMessage());
            }
            return false;
        } catch (LoginFailureException autoLoginFailed) {
            return false;
        }
    }

    protected boolean handleAutoLoginInvalidCookie(String cookieValue, String[] valueAry) {
        // if invalid length, it might be hack so do nothing here as default
        return false;
    }

    protected String getAutoLoginDelimiter() {
        return AUTO_LOGIN_DELIMITER;
    }

    protected String formatForAutoLoginExpireDate(HandyDate expireDate) {
        return expireDate.toDisp(AUTO_LOGIN_ACCESS_TOKEN_EXPIRE_DATE_PATTERN);
    }

    // ===================================================================================
    //                                                                              Logout
    //                                                                              ======
    /**
     * {@inheritDoc}
     */
    public void logoutProc() {
        sessionManager.remove(getUserBeanType());
        cookieManager.removeCookie(getCookieAutoLoginKey());
    }

    /**
     * Get the type of user bean basically for session key.
     * @return The type of user bean. (NotNull)
     */
    protected abstract Class<USER_BEAN> getUserBeanType();

    // ===================================================================================
    //                                                                         Login Check
    //                                                                         ===========
    // -----------------------------------------------------
    //                                         LoginRequired
    //                                         -------------
    /**
     * {@inheritDoc}
     */
    public String checkLoginRequired(LoginHandlingResource resource) {
        final String redirectTo;
        if (isLoginRequiredAction(resource)) {
            redirectTo = processLoginRequired(resource);
        } else {
            redirectTo = processNotLoginRequired(resource);
        }
        return redirectTo;
    }

    /**
     * Process for the login-required action.
     * @param resource The resource of login handling to determine. (NotNull)
     * @return The forward path, basically for login redirect. (NullAllowed: if null, login check passed)
     */
    protected String processLoginRequired(LoginHandlingResource resource) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Checking login status for login required");
        }
        if (processAlreadyLogin(resource)) {
            return processAuthority(resource);
        }
        if (processAutoLogin(resource)) {
            return processAuthority(resource);
        }
        saveRequestedLoginRedirectInfo();
        final String loginAction = redirectToRequiredCheckedLoginAction();
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Redirecting to login action: " + loginAction);
        }
        return loginAction;
    }

    /**
     * Redirect to action when required checked (basically login action). <br>
     * You can customize the redirection when not login but login required.
     * @return The forward path, basically for login redirect. (NullAllowed: if null, login check passed)
     */
    protected String redirectToRequiredCheckedLoginAction() {
        return redirectToLoginAction(); // as default
    }

    // -----------------------------------------------------
    //                                         Already Login
    //                                         -------------
    protected boolean processAlreadyLogin(LoginHandlingResource resource) {
        final USER_BEAN userBean = getSessionUserBean();
        if (userBean != null) {
            if (!syncCheckLoginSessionIfNeeds(userBean)) {
                return false;
            }
            clearLoginRedirectBean();
            if (LOG.isDebugEnabled()) {
                LOG.debug("...Passing login check as already-login");
            }
            return true;
        }
        return false;
    }

    // -----------------------------------------------------
    //                                LoginSession SyncCheck
    //                                ----------------------
    protected boolean syncCheckLoginSessionIfNeeds(USER_BEAN userBean) {
        if (!(userBean instanceof SyncCheckable)) {
            return true; // means no check
        }
        final SyncCheckable checkable = (SyncCheckable) userBean;
        final Date checkDate = checkable.getLastestSyncCheckDate(); // might be null
        final Date currentDate = timeManager.getCurrentDate();
        if (!needsLoginSessionSyncCheck(userBean, checkDate, currentDate)) {
            return true; // means no check
        }
        if (LOG.isDebugEnabled()) {
            final Long userId = userBean.getUserId();
            final String checkDisp = checkDate != null ? new HandyDate(checkDate).toDisp("yyyy/MM/dd HH:mm:ss") : null;
            LOG.debug("...Sync-checking login session: userId=" + userId + ", checkDate=" + checkDisp);
        }
        checkable.setLastestSyncCheckDate(currentDate); // update latest check date
        final USER_ENTITY loginUser = findLoginSessionSyncCheckUser(userBean);
        if (loginUser != null) {
            handleLoginSessionSyncCheckSuccess(userBean, loginUser);
            return true; // means check OK
        }
        // the user might be assigned here
        if (LOG.isDebugEnabled()) {
            LOG.debug("*The user already cannot login: " + userBean);
        }
        logoutProc(); // remove user info from session
        return false; // means check NG
    }

    protected boolean needsLoginSessionSyncCheck(USER_BEAN userBean, Date checkDate, Date currentDate) {
        if (checkDate == null) {
            return true; // first check
        }
        final int checkInterval = getLoginSessionSyncCheckInterval();
        return new HandyDate(checkDate).addSecond(checkInterval).isLessEqual(currentDate);
    }

    protected int getLoginSessionSyncCheckInterval() {
        return 300; // as default (second)
    }

    protected USER_ENTITY findLoginSessionSyncCheckUser(USER_BEAN userBean) {
        return findLoginUser(userBean.getUserId());
    }

    protected void handleLoginSessionSyncCheckSuccess(USER_BEAN userBean, USER_ENTITY loginUser) {
        // do nothing as default (you can add original process by override)
    }

    // -----------------------------------------------------
    //                                            Auto Login
    //                                            ----------
    protected boolean processAutoLogin(LoginHandlingResource resource) {
        final boolean updateToken = isUpdateTokenWhenAutoLogin(resource);
        final boolean silently = isSilentlyWhenAutoLogin(resource);
        final boolean success = silently ? silentAutoLogin(updateToken) : autoLogin(updateToken);
        if (success) {
            final USER_BEAN userBean = getSessionUserBean();
            if (userBean != null) { // exclusive control, just in case
                clearLoginRedirectBean();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("...Passing login check as auto-login");
                }
                return true;
            }
        }
        return false;
    }

    protected boolean isUpdateTokenWhenAutoLogin(LoginHandlingResource resource) {
        return false; // as default
    }

    protected boolean isSilentlyWhenAutoLogin(LoginHandlingResource resource) {
        return false; // as default
    }

    // -----------------------------------------------------
    //                                      Session UserBean
    //                                      ----------------
    /**
     * {@inheritDoc}
     */
    public USER_BEAN getSessionUserBean() {
        return sessionManager.getAttribute(getUserBeanType());
    }

    // -----------------------------------------------------
    //                                             Authority
    //                                             ---------
    /**
     * Process for the authority of the login user. (called in login status)
     * @param resource The resource of login handling to determine. (NotNull)
     * @return The forward path, basically for authority redirect. (NullAllowed: if null, authority check passed)
     */
    protected String processAuthority(LoginHandlingResource resource) {
        return processAuthority(resource, getSessionUserBean());
    }

    /**
     * Process for the authority of the login user. (called in login status)
     * @param resource The resource of login handling to determine. (NotNull)
     * @param userBean The bean of the login user. (NotNull)
     * @return The forward path, basically for authority redirect. (NullAllowed: if null, authority check passed)
     */
    protected String processAuthority(LoginHandlingResource resource, USER_BEAN userBean) {
        return null; // no check as default, you can override
    }

    // -----------------------------------------------------
    //                                     Not LoginRequired
    //                                     -----------------
    /**
     * Process for the NOT login-required action.
     * @param resource The resource of login handling to determine. (NotNull)
     * @return The forward path, basically for login redirect. (NullAllowed)
     */
    protected String processNotLoginRequired(LoginHandlingResource resource) {
        if (isAutoLoginWhenNotLoginRequired(resource)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("...Checking login status for not-login required");
            }
            if (processAlreadyLogin(resource)) {
                return processAuthority(resource);
            }
            if (processAutoLogin(resource)) {
                return processAuthority(resource);
            }
        }
        if (isLoginRedirectBeanKeptAction(resource)) {
            // keep login-redirect path in session
            if (LOG.isDebugEnabled()) {
                LOG.debug("...Passing login check as login action (or redirect-kept action)");
            }
        } else {
            clearLoginRedirectBean();
            if (LOG.isDebugEnabled()) {
                LOG.debug("...Passing login check as not required");
            }
        }
        return null; // no redirect
    }

    /**
     * Does the action keep login redirect bean in session?
     * @param resource The resource of login handling to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isLoginRedirectBeanKeptAction(LoginHandlingResource resource) {
        // normally both are same action, but redirect action might be changed
        return isLoginAction(resource) || isRedirectLoginAction(resource);
    }

    /**
     * Does it auto-login when not-login required? <br>
     * If not-login-required action also should accept auto-login, <br>
     * e.g. switch display by login or not, override this and return true.
     * @param resource The resource of login handling to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isAutoLoginWhenNotLoginRequired(LoginHandlingResource resource) {
        return false; // as default
    }

    // -----------------------------------------------------
    //                                           Action Type
    //                                           -----------
    /**
     * {@inheritDoc}
     */
    public boolean isLoginAction(LoginHandlingResource resource) {
        final Class<?> actionClass = resource.getActionClass();
        final Class<?> loginActionType = getLoginActionType();
        return loginActionType.isAssignableFrom(actionClass);
    }

    /**
     * Get the type of (pure) login action (not related to login-redirect). <br>
     * Action type for login-redirect is defined at {@link #getRedirectLoginActionType()}.
     * @return The type of (pure) login action. (NotNull)
     */
    protected abstract Class<?> getLoginActionType();

    /**
     * Is the action for login-redirect?
     * @param resource The resource of login handling to determine. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isRedirectLoginAction(LoginHandlingResource resource) {
        final Class<?> actionClass = resource.getActionClass();
        final Class<?> loginActionType = getRedirectLoginActionType();
        return loginActionType.isAssignableFrom(actionClass);
    }

    /**
     * Get the type of login action for login-redirect. <br>
     * It redirects to the action when login required.
     * @return The type of login action for login-redirect. (NotNull)
     */
    protected Class<?> getRedirectLoginActionType() {
        return getLoginActionType(); // same pure login type as default
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApiAction(LoginHandlingResource resource) {
        final Class<?> actionClass = resource.getActionClass();
        return ApiAction.class.isAssignableFrom(actionClass);
    }

    // ===================================================================================
    //                                                                      Login Redirect
    //                                                                      ==============
    /**
     * {@inheritDoc}
     */
    public void saveRequestedLoginRedirectInfo() {
        final String pathAndQuery = requestManager.getRoutingOriginRequestPathAndQuery();
        final String redirectPath = actionResolver.toRedirectPath(pathAndQuery);
        final LoginRedirectBean redirectBean = createLoginRedirectBean(redirectPath);
        // not use instance type because it might be extended
        // (basically not use it when the object might be extended)
        sessionManager.setAttribute(generateLoginRedirectBeanKey(), redirectBean);
    }

    protected LoginRedirectBean createLoginRedirectBean(String redirectPath) {
        return new LoginRedirectBean(redirectPath);
    }

    /**
     * {@inheritDoc}
     */
    public String redirectToLoginAction() {
        final Class<?> redirectLoginActionType = getRedirectLoginActionType();
        return actionResolver.toActionUrl(redirectLoginActionType, true, null);
    }

    /**
     * {@inheritDoc}
     */
    public LoginRedirectBean getLoginRedirectBean() {
        return sessionManager.getAttribute(generateLoginRedirectBeanKey());
    }

    /**
     * {@inheritDoc}
     */
    public void clearLoginRedirectBean() {
        sessionManager.remove(generateLoginRedirectBeanKey());
    }

    protected String generateLoginRedirectBeanKey() {
        return getLoginRedirectBeanType().getName();
    }

    protected Class<? extends LoginRedirectBean> getLoginRedirectBeanType() {
        return LoginRedirectBean.class;
    }

    /**
     * {@inheritDoc}
     */
    public String redirectToRequestedActionIfNeeds(LoginHandlingResource resource) {
        String redirectTo = null;
        if (isRedirectToRequestedActionAllowed(resource)) {
            final LoginRedirectBean redirectBean = sessionManager.getAttribute(getLoginRedirectBeanType());
            if (redirectBean != null && redirectBean.hasRedirectPath()) {
                clearLoginRedirectBean();
                redirectTo = redirectBean.getRedirectPath();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("...Switching redirection to: " + redirectTo);
                }
            }
        }
        return redirectTo;
    }

    protected boolean isRedirectToRequestedActionAllowed(LoginHandlingResource resource) {
        return isPerformLoginAction(resource) && !resource.hasValidationError(); // login success
    }
}
