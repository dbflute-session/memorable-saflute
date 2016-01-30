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
package org.dbflute.saflute.web.action.processor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.dbflute.util.DfTypeUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.exception.IllegalUrlPatternRuntimeException;

/**
 * @author jflute
 */
public class ActionExecuteConfig extends S2ExecuteConfig {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    protected static final String ELEMENT_PATTERN = "([^/]+)";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Set<String> urlParamRequiredSet = new LinkedHashSet<String>(4);

    // ===================================================================================
    //                                                                          UrlPattern
    //                                                                          ==========
    @Override
    public void setUrlPattern(String urlPattern) {
        // copied from super's and extended
        if (StringUtil.isEmpty(urlPattern)) {
            return;
        }
        this.urlPattern = urlPattern;
        final String pattern = analyzeUrlPattern(urlPattern);
        urlPatternAllSelected = pattern.equals(ELEMENT_PATTERN);
        urlPatternRegexp = Pattern.compile("^" + pattern + "$");
    }

    protected String analyzeUrlPattern(String urlPattern) {
        final StringBuilder sb = new StringBuilder(50);
        final char[] chars = urlPattern.toCharArray();
        final int length = chars.length;
        Character previousChar = null;
        boolean requiredElement = false;
        int index = -1;
        for (int i = 0; i < length; i++) {
            final char currentChar = chars[i];
            if (currentChar == '{') {
                index = i;
            } else if (previousChar != null && previousChar == '{' && currentChar == '*') {
                // e.g. {*id}/{*name} means required parameter, 404 not found if no value
                index = i; // to skip required mark
                requiredElement = true;
            } else if (currentChar == '}') {
                if (index >= 0) {
                    sb.append(ELEMENT_PATTERN);
                    final String elementName = urlPattern.substring(index + 1, i);
                    urlParamNames.add(elementName);
                    if (requiredElement) {
                        urlParamRequiredSet.add(elementName);
                    }
                    requiredElement = false;
                    index = -1;
                } else {
                    throw new IllegalUrlPatternRuntimeException(urlPattern);
                }
            } else if (index < 0) {
                sb.append(currentChar);
                requiredElement = false;
            }
            previousChar = currentChar;
        }
        if (index >= 0) {
            throw new IllegalUrlPatternRuntimeException(urlPattern);
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        final StringBuilder urlParamSb = new StringBuilder();
        for (String name : urlParamNames) {
            if (urlParamSb.length() > 0) {
                urlParamSb.append(", ");
            }
            if (urlParamRequiredSet.contains(name)) {
                urlParamSb.append("*");
            }
            urlParamSb.append(name);
        }
        final String urlParamExp = urlParamSb.toString();
        return title + ":{validator=" + isValidator() + ", urlParamNames=[" + urlParamExp + "]}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<String> getUrlParamNames() {
        return urlParamNames;
    }

    public Pattern getUrlPatternRegexp() {
        return urlPatternRegexp;
    }

    public Set<String> getUrlParamRequiredSet() {
        return urlParamRequiredSet;
    }
}
