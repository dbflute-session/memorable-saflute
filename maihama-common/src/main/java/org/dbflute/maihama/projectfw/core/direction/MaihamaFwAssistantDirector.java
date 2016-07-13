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
package org.dbflute.maihama.projectfw.core.direction;

import java.util.TimeZone;

import javax.annotation.Resource;

import org.dbflute.maihama.projectfw.core.direction.sponsor.MaihamaActionAdjustmentProvider;
import org.dbflute.maihama.projectfw.core.direction.sponsor.MaihamaTimeResourceProvider;
import org.dbflute.maihama.projectfw.core.direction.sponsor.MaihamaUserLocaleProcessProvider;
import org.dbflute.maihama.projectfw.core.direction.sponsor.MaihamaUserTimeZoneProcessProvider;
import org.dbflute.saflute.core.direction.BootProcessCallback;
import org.dbflute.saflute.core.direction.CachedFwAssistantDirector;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.OptionalAssistDirection;
import org.dbflute.saflute.core.direction.OptionalCoreDirection;
import org.dbflute.saflute.core.security.InvertibleCipher;
import org.dbflute.saflute.core.security.SecurityResourceProvider;
import org.dbflute.saflute.db.dbflute.OptionalDBFluteDirection;
import org.dbflute.saflute.web.action.OptionalActionDirection;
import org.dbflute.saflute.web.servlet.OptionalServletDirection;
import org.dbflute.saflute.web.servlet.cookie.CookieResourceProvider;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.system.provider.DfFinalTimeZoneProvider;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public abstract class MaihamaFwAssistantDirector extends CachedFwAssistantDirector {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String MAIHAMA_CONFIG_FILE = "maihama_config.properties";
    public static final String MAIHAMA_ENV_FILE = "maihama_env.properties";
    public static final String MAIHAMA_MESSAGE_NAME = "maihama_message";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MaihamaConfig maihamaConfig;

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    @Override
    protected OptionalAssistDirection prepareOptionalAssistDirection() {
        final OptionalAssistDirection direction = new OptionalAssistDirection();
        prepareConfiguration(direction);
        return direction;
    }

    protected void prepareConfiguration(OptionalAssistDirection direction) {
        direction.directConfiguration(getDomainConfigFile(), getExtendsConfigFiles());
    }

    protected abstract String getDomainConfigFile();

    protected String[] getExtendsConfigFiles() {
        return new String[] { MAIHAMA_CONFIG_FILE, MAIHAMA_ENV_FILE };
    }

    // ===================================================================================
    //                                                                                Core
    //                                                                                ====
    @Override
    protected OptionalCoreDirection prepareOptionalCoreDirection() {
        final OptionalCoreDirection direction = new OptionalCoreDirection();
        prepareFramework(direction);
        prepareSecurity(direction);
        prepareTime(direction);
        return direction;
    }

    // -----------------------------------------------------
    //                                             Framework
    //                                             ---------
    protected void prepareFramework(OptionalCoreDirection direction) {
        // this configuration is on maihama_env.properties
        // because this is true only when development
        direction.directDevelopmentHere(maihamaConfig.isDevelopmentHere());

        // titles are from configurations
        direction.directLoggingTitle(maihamaConfig.getDomainTitle(), maihamaConfig.getEnvironmentTitle());

        // this configuration is on sea_env.properties
        // because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(maihamaConfig.isFrameworkDebug()); // basically false

        // you can add your own process when your application is booting
        direction.directBootProcessCallback(new BootProcessCallback() {
            public void callback(FwAssistantDirector assistantDirector) {
                processDBFluteSystem();
            }
        });
    }

    protected void processDBFluteSystem() {
        DBFluteSystem.unlock();
        DBFluteSystem.setFinalTimeZoneProvider(new DfFinalTimeZoneProvider() {
            protected final TimeZone provided = MaihamaUserTimeZoneProcessProvider.centralTimeZone;

            public TimeZone provide() {
                return provided;
            }

            @Override
            public String toString() {
                return DfTypeUtil.toClassTitle(this) + ":{" + provided.getID() + "}";
            }
        });
        DBFluteSystem.lock();
    }

    // -----------------------------------------------------
    //                                              Security
    //                                              --------
    protected void prepareSecurity(OptionalCoreDirection direction) {
        final String key = getPrimarySecurityWord();
        final InvertibleCipher primaryInvertibleCipher = InvertibleCipher.createAesCipher(key); // AES for now
        direction.directSecurity(new SecurityResourceProvider() {
            public InvertibleCipher providePrimaryInvertibleCipher() {
                return primaryInvertibleCipher;
            }
        });
    }

    protected String getPrimarySecurityWord() {
        return "maihama:dockside"; // hard coding for now
    }

    // -----------------------------------------------------
    //                                                  Time
    //                                                  ----
    protected void prepareTime(OptionalCoreDirection direction) {
        direction.directTime(createTimeResourceProvider());
    }

    protected MaihamaTimeResourceProvider createTimeResourceProvider() {
        return new MaihamaTimeResourceProvider(maihamaConfig);
    }

    // ===================================================================================
    //                                                                                  DB
    //                                                                                  ==
    @Override
    protected OptionalDBFluteDirection prepareOptionalDBFluteDirection() {
        final OptionalDBFluteDirection direction = new OptionalDBFluteDirection();
        return direction;
    }

    // ===================================================================================
    //                                                                                 Web
    //                                                                                 ===
    // -----------------------------------------------------
    //                                                Action
    //                                                ------
    @Override
    protected OptionalActionDirection prepareOptionalActionDirection() {
        final OptionalActionDirection direction = new OptionalActionDirection();
        prepareAdjustment(direction);
        prepareMessage(direction);
        return direction;
    }

    protected void prepareAdjustment(OptionalActionDirection direction) {
        direction.directAdjustment(createActionAdjustmentProvider());
    }

    protected MaihamaActionAdjustmentProvider createActionAdjustmentProvider() {
        return new MaihamaActionAdjustmentProvider();
    }

    protected void prepareMessage(OptionalActionDirection direction) {
        direction.directMessage(getDomainMessageName(), getExtendsMessageNames());
    }

    protected abstract String getDomainMessageName();

    protected String[] getExtendsMessageNames() {
        return new String[] { MAIHAMA_MESSAGE_NAME };
    }

    // -----------------------------------------------------
    //                                               Servlet
    //                                               -------
    @Override
    protected OptionalServletDirection prepareOptionalServletDirection() {
        final OptionalServletDirection direction = new OptionalServletDirection();
        prepareRequest(direction);
        prepareCookie(direction);
        return direction;
    }

    protected OptionalServletDirection prepareRequest(OptionalServletDirection direction) {
        direction.directRequest(createUserLocaleProcessProvider(), createUserTimeZoneProcessProvider());
        return direction;
    }

    protected MaihamaUserLocaleProcessProvider createUserLocaleProcessProvider() {
        return new MaihamaUserLocaleProcessProvider();
    }

    protected MaihamaUserTimeZoneProcessProvider createUserTimeZoneProcessProvider() {
        return new MaihamaUserTimeZoneProcessProvider();
    }

    protected void prepareCookie(OptionalServletDirection direction) {
        final String key = getCookieSecurityWord();
        final String cookieDefaultPath = maihamaConfig.getCookieDefaultPath();
        final Integer cookieDefaultExpire = maihamaConfig.getCookieDefaultExpireAsInteger();
        final InvertibleCipher cookieCipher = InvertibleCipher.createAesCipher(key); // AES for now
        direction.directCookie(new CookieResourceProvider() {
            public String provideDefaultPath() {
                return cookieDefaultPath;
            }

            public Integer provideDefaultExpire() {
                return cookieDefaultExpire;
            }

            public InvertibleCipher provideCipher() {
                return cookieCipher;
            }

            @Override
            public String toString() {
                return "{" + cookieDefaultPath + ", " + cookieDefaultExpire + "}";
            }
        });
    }

    protected String getCookieSecurityWord() {
        return "dockside:maihama"; // hard coding for now
    }
}
