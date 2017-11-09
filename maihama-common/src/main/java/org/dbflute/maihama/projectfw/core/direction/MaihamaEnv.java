package org.dbflute.maihama.projectfw.core.direction;

import org.dbflute.saflute.core.direction.ObjectiveConfig;

/**
 * @author FreeGen
 */
public interface MaihamaEnv {

    /** The key of the configuration. e.g. true */
    String DEVELOPMENT_HERE = "development.here";

    /** The key of the configuration. e.g. Local Development */
    String ENVIRONMENT_TITLE = "environment.title";

    /** The key of the configuration. e.g. false */
    String FRAMEWORK_DEBUG = "framework.debug";

    /** The key of the configuration. e.g. 0 */
    String TIME_ADJUST_TIME_MILLIS = "time.adjust.time.millis";

    /** The key of the configuration. e.g. true */
    String MAIL_SEND_MOCK = "mail.send.mock";

    /** The key of the configuration. e.g. localhost:25 */
    String MAIL_SMTP_SERVER_DEFAULT_HOST_AND_PORT = "mail.smtp.server.default.host.and.port";

    /** The key of the configuration. e.g. jdbc:mysql://localhost:3306/maihamadb */
    String JDBC_URL = "jdbc.url";

    /** The key of the configuration. e.g. maihamauser */
    String JDBC_USER = "jdbc.user";

    /** The key of the configuration. e.g. maihamaword */
    String JDBC_PASSWORD = "jdbc.password";

    /** The key of the configuration. e.g. 10 */
    String JDBC_CONNECTION_POOLING_SIZE = "jdbc.connection.pooling.size";

    /** The key of the configuration. e.g. true */
    String SWAGGER_ENABLED = "swagger.enabled";

    /**
     * Get the value of property as {@link String}.
     * @param propertyKey The key of the property. (NotNull)
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String get(String propertyKey);

    /**
     * Is the property true?
     * @param propertyKey The key of the property which is boolean type. (NotNull)
     * @return The determination, true or false. (if the property can be true, returns true)
     */
    boolean is(String propertyKey);

    /**
     * Get the value for the key 'development.here'. <br />
     * The value is, e.g. true <br />
     * comment: Is development environment here? (used for various purpose, you should set false if unknown)
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getDevelopmentHere();

    /**
     * Is the property for the key 'development.here' true? <br />
     * The value is, e.g. true <br />
     * comment: Is development environment here? (used for various purpose, you should set false if unknown)
     * @return The determination, true or false. (if the property can be true, returns true)
     */
    boolean isDevelopmentHere();

    /**
     * Get the value for the key 'environment.title'. <br />
     * The value is, e.g. Local Development <br />
     * comment: The title of environment (e.g. local or integartion or production)
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getEnvironmentTitle();

    /**
     * Get the value for the key 'framework.debug'. <br />
     * The value is, e.g. false <br />
     * comment: Does it enable the Framework internal debug? (true only when emergency)
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getFrameworkDebug();

    /**
     * Is the property for the key 'framework.debug' true? <br />
     * The value is, e.g. false <br />
     * comment: Does it enable the Framework internal debug? (true only when emergency)
     * @return The determination, true or false. (if the property can be true, returns true)
     */
    boolean isFrameworkDebug();

    /**
     * Get the value for the key 'time.adjust.time.millis'. <br />
     * The value is, e.g. 0 <br />
     * comment: The milliseconds for (relative or absolute) adjust time (set only when test) @LongType *dynamic in development
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getTimeAdjustTimeMillis();

    /**
     * Get the value for the key 'time.adjust.time.millis' as {@link Long}. <br />
     * The value is, e.g. 0 <br />
     * comment: The milliseconds for (relative or absolute) adjust time (set only when test) @LongType *dynamic in development
     * @return The value of found property. (NullAllowed: if null, not found)
     * @throws NumberFormatException When the property is not long.
     */
    Long getTimeAdjustTimeMillisAsLong();

    /**
     * Get the value for the key 'mail.send.mock'. <br />
     * The value is, e.g. true <br />
     * comment: Does it send mock mail? (true: no send actually, logging only)
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getMailSendMock();

    /**
     * Is the property for the key 'mail.send.mock' true? <br />
     * The value is, e.g. true <br />
     * comment: Does it send mock mail? (true: no send actually, logging only)
     * @return The determination, true or false. (if the property can be true, returns true)
     */
    boolean isMailSendMock();

    /**
     * Get the value for the key 'mail.smtp.server.default.host.and.port'. <br />
     * The value is, e.g. localhost:25 <br />
     * comment: SMTP server settings for default: host:port
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getMailSmtpServerDefaultHostAndPort();

    /**
     * Get the value for the key 'jdbc.url'. <br />
     * The value is, e.g. jdbc:mysql://localhost:3306/maihamadb <br />
     * comment: The URL of database connection for JDBC
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getJdbcUrl();

    /**
     * Get the value for the key 'jdbc.user'. <br />
     * The value is, e.g. maihamauser <br />
     * comment: The user of database connection for JDBC
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getJdbcUser();

    /**
     * Get the value for the key 'jdbc.password'. <br />
     * The value is, e.g. maihamaword <br />
     * comment: @Secure The password of database connection for JDBC
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getJdbcPassword();

    /**
     * Get the value for the key 'jdbc.connection.pooling.size'. <br />
     * The value is, e.g. 10 <br />
     * comment: The (max) pooling size of Seasar's connection pool
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getJdbcConnectionPoolingSize();

    /**
     * Get the value for the key 'jdbc.connection.pooling.size' as {@link Integer}. <br />
     * The value is, e.g. 10 <br />
     * comment: The (max) pooling size of Seasar's connection pool
     * @return The value of found property. (NullAllowed: if null, not found)
     * @throws NumberFormatException When the property is not integer.
     */
    Integer getJdbcConnectionPoolingSizeAsInteger();

    /**
     * Get the value for the key 'swagger.enabled'. <br />
     * The value is, e.g. true <br />
     * comment: is it use swagger in this environment?
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getSwaggerEnabled();

    /**
     * Is the property for the key 'swagger.enabled' true? <br />
     * The value is, e.g. true <br />
     * comment: is it use swagger in this environment?
     * @return The determination, true or false. (if the property can be true, returns true)
     */
    boolean isSwaggerEnabled();

    /**
     * The simple implementation for configuration.
     * @author FreeGen
     */
    public static class SimpleImpl extends ObjectiveConfig implements MaihamaEnv {

        /** The serial version UID for object serialization. (Default) */
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        public String getDevelopmentHere() {
            return get(MaihamaEnv.DEVELOPMENT_HERE);
        }

        /** {@inheritDoc} */
        public boolean isDevelopmentHere() {
            return is(MaihamaEnv.DEVELOPMENT_HERE);
        }

        /** {@inheritDoc} */
        public String getEnvironmentTitle() {
            return get(MaihamaEnv.ENVIRONMENT_TITLE);
        }

        /** {@inheritDoc} */
        public String getFrameworkDebug() {
            return get(MaihamaEnv.FRAMEWORK_DEBUG);
        }

        /** {@inheritDoc} */
        public boolean isFrameworkDebug() {
            return is(MaihamaEnv.FRAMEWORK_DEBUG);
        }

        /** {@inheritDoc} */
        public String getTimeAdjustTimeMillis() {
            return get(MaihamaEnv.TIME_ADJUST_TIME_MILLIS);
        }

        /** {@inheritDoc} */
        public Long getTimeAdjustTimeMillisAsLong() {
            return getAsLong(MaihamaEnv.TIME_ADJUST_TIME_MILLIS);
        }

        /** {@inheritDoc} */
        public String getMailSendMock() {
            return get(MaihamaEnv.MAIL_SEND_MOCK);
        }

        /** {@inheritDoc} */
        public boolean isMailSendMock() {
            return is(MaihamaEnv.MAIL_SEND_MOCK);
        }

        /** {@inheritDoc} */
        public String getMailSmtpServerDefaultHostAndPort() {
            return get(MaihamaEnv.MAIL_SMTP_SERVER_DEFAULT_HOST_AND_PORT);
        }

        /** {@inheritDoc} */
        public String getJdbcUrl() {
            return get(MaihamaEnv.JDBC_URL);
        }

        /** {@inheritDoc} */
        public String getJdbcUser() {
            return get(MaihamaEnv.JDBC_USER);
        }

        /** {@inheritDoc} */
        public String getJdbcPassword() {
            return get(MaihamaEnv.JDBC_PASSWORD);
        }

        /** {@inheritDoc} */
        public String getJdbcConnectionPoolingSize() {
            return get(MaihamaEnv.JDBC_CONNECTION_POOLING_SIZE);
        }

        /** {@inheritDoc} */
        public Integer getJdbcConnectionPoolingSizeAsInteger() {
            return getAsInteger(MaihamaEnv.JDBC_CONNECTION_POOLING_SIZE);
        }

        /** {@inheritDoc} */
        public String getSwaggerEnabled() {
            return get(MaihamaEnv.SWAGGER_ENABLED);
        }

        /** {@inheritDoc} */
        public boolean isSwaggerEnabled() {
            return is(MaihamaEnv.SWAGGER_ENABLED);
        }
    }
}
