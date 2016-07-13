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
package ${packageName}.projectfw.core.direction;

import java.util.TimeZone;

import javax.annotation.Resource;

import ${packageName}.projectfw.core.direction.sponsor.${ProjectName}ActionAdjustmentProvider;
import ${packageName}.projectfw.core.direction.sponsor.${ProjectName}TimeResourceProvider;
import ${packageName}.projectfw.core.direction.sponsor.${ProjectName}UserLocaleProcessProvider;
import ${packageName}.projectfw.core.direction.sponsor.${ProjectName}UserTimeZoneProcessProvider;
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
import org.dbflute.saflute.web.task.OptionalTaskDirection;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.system.provider.DfFinalTimeZoneProvider;
import org.dbflute.util.DfTypeUtil;

/**
 * @author saflute_template
 */
public abstract class ${ProjectName}FwAssistantDirector extends CachedFwAssistantDirector {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ${PROJECTNAME}_CONFIG_FILE = "${projectname}_config.properties";
    public static final String ${PROJECTNAME}_ENV_FILE = "${projectname}_env.properties";
    public static final String ${PROJECTNAME}_MESSAGE_NAME = "${projectname}_message";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected ${ProjectName}Config ${projectname}Config;

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
        return new String[] { ${PROJECTNAME}_CONFIG_FILE, ${PROJECTNAME}_ENV_FILE };
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
        // this configuration is on ${projectname}_env.properties
        // because this is true only when development
        direction.directDevelopmentHere(${projectname}Config.isDevelopmentHere());

        // titles are from configurations
        direction.directLoggingTitle(${projectname}Config.getDomainTitle(), ${projectname}Config.getEnvironmentTitle());

        // this configuration is on sea_env.properties
        // because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(${projectname}Config.isFrameworkDebug()); // basically false

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
            protected final TimeZone provided = ${ProjectName}UserTimeZoneProcessProvider.centralTimeZone;

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
        final String key = "${projectname}"; // TODO you! should set key and determine algorithm
        final InvertibleCipher primaryInvertibleCipher = InvertibleCipher.createBlowfishCipher(key);
        direction.directSecurity(new SecurityResourceProvider() {
            public InvertibleCipher providePrimaryInvertibleCipher() {
                return primaryInvertibleCipher;
            }
        });
    }

    // -----------------------------------------------------
    //                                                  Time
    //                                                  ----
    protected void prepareTime(OptionalCoreDirection direction) {
        direction.directTime(createTimeResourceProvider());
    }

    protected ${ProjectName}TimeResourceProvider createTimeResourceProvider() {
        return new ${ProjectName}TimeResourceProvider(${projectname}Config);
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

    protected ${ProjectName}ActionAdjustmentProvider createActionAdjustmentProvider() {
        return new ${ProjectName}ActionAdjustmentProvider();
    }

    protected void prepareMessage(OptionalActionDirection direction) {
        direction.directMessage(getDomainMessageName(), getExtendsMessageNames());
    }

    protected abstract String getDomainMessageName();

    protected String[] getExtendsMessageNames() {
        return new String[] { ${PROJECTNAME}_MESSAGE_NAME };
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

    protected ${ProjectName}UserLocaleProcessProvider createUserLocaleProcessProvider() {
        return new ${ProjectName}UserLocaleProcessProvider();
    }

    protected ${ProjectName}UserTimeZoneProcessProvider createUserTimeZoneProcessProvider() {
        return new ${ProjectName}UserTimeZoneProcessProvider();
    }

    protected void prepareCookie(OptionalServletDirection direction) {
        final String key = "${appname}"; // TODO you! should set key and determine algorithm
        final String cookieDefaultPath = ${projectname}Config.getCookieDefaultPath();
        final Integer cookieExpireDefault = ${projectname}Config.getCookieDefaultExpireAsInteger();
        final InvertibleCipher cookieCipher = InvertibleCipher.createBlowfishCipher(key);
        direction.directCookie(new CookieResourceProvider() {
            public String provideDefaultPath() {
                return cookieDefaultPath;
            }

            public Integer provideDefaultExpire() {
                return cookieExpireDefault;
            }

            public InvertibleCipher provideCipher() {
                return cookieCipher;
            }
        });
    }

    // -----------------------------------------------------
    //                                                  Task
    //                                                  ----
    @Override
    protected OptionalTaskDirection prepareOptionalTaskDirection() {
        return new OptionalTaskDirection();
    }
}
