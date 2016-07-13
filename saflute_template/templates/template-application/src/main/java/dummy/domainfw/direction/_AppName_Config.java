package ${packageName}.domainfw.direction;

import ${packageName}.projectfw.core.direction.${ProjectName}Config;

/**
 * @author FreeGen
 */
public interface ${AppName}Config extends ${ProjectName}Config {

    /** The key of the configuration. e.g. ${AppName} */
    String DOMAIN_TITLE = "domain.title";

    /** The key of the configuration. e.g. DCK */
    String COOKIE_AUTO_LOGIN_${APPNAME}_KEY = "cookie.auto.login.${appname}.key";

    /** The key of the configuration. e.g. 3 */
    String PAGING_PAGE_RANGE_SIZE = "paging.page.range.size";

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
     * Get the value for the key 'domain.title'. <br />
     * The value is, e.g. ${AppName} <br />
     * comment: @Override The title of domain the application for logging
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getDomainTitle();

    /**
     * Get the value for the key 'cookie.auto.login.${appname}.key'. <br />
     * The value is, e.g. DCK <br />
     * comment: The cookie key of auto-login for ${AppName}
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getCookieAutoLogin${AppName}Key();

    /**
     * Get the value for the key 'paging.page.range.size'. <br />
     * The value is, e.g. 3 <br />
     * comment: @Override The size of page range for paging
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    String getPagingPageRangeSize();

    /**
     * Get the value for the key 'paging.page.range.size' as {@link Integer}. <br />
     * The value is, e.g. 3 <br />
     * comment: @Override The size of page range for paging
     * @return The value of found property. (NullAllowed: if null, not found)
     * @throws NumberFormatException When the property is not integer.
     */
    Integer getPagingPageRangeSizeAsInteger();

    /**
     * The simple implementation for configuration.
     * @author FreeGen
     */
    public static class SimpleImpl extends ${ProjectName}Config.SimpleImpl implements ${AppName}Config {

        /** The serial version UID for object serialization. (Default) */
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        public String getDomainTitle() {
            return get(${AppName}Config.DOMAIN_TITLE);
        }

        /** {@inheritDoc} */
        public String getCookieAutoLogin${AppName}Key() {
            return get(${AppName}Config.COOKIE_AUTO_LOGIN_${APPNAME}_KEY);
        }

        /** {@inheritDoc} */
        @Override
        public String getPagingPageRangeSize() {
            return get(${AppName}Config.PAGING_PAGE_RANGE_SIZE);
        }

        /** {@inheritDoc} */
        @Override
        public Integer getPagingPageRangeSizeAsInteger() {
            return getAsInteger(${AppName}Config.PAGING_PAGE_RANGE_SIZE);
        }
    }
}
