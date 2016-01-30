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

/**
 * The interface for the bean of login authentication info.
 * @author jflute
 */
public interface LoginAuthBean {

    /**
     * Get the password to login.
     * @return The string for plain password. (NotNull)
     */
    String getPassword();

    /**
     * Does it use auto-login?
     * @return The determination, true or false.
     */
    boolean isUseAutoLogin();

    /**
     * Does it login silently? (without the login logging)
     * @return The determination, true or false.
     */
    boolean isSilently();
}
