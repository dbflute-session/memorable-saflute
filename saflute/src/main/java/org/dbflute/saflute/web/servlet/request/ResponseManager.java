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
package org.dbflute.saflute.web.servlet.request;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * The manager of response. (response facade)
 * @author jflute
 */
public interface ResponseManager {

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    HttpServletResponse getResponse();

    // ===================================================================================
    //                                                                      Write Response
    //                                                                      ==============
    void write(String text);

    void write(String text, String contentType);

    void write(String text, String contentType, String encoding);

    void writeAsJson(String json);

    void writeAsJavaScript(String script);

    void writeAsXml(String xmlStr, String encoding);

    // ===================================================================================
    //                                                                   Download Response
    //                                                                   =================
    void download(String fileName, byte[] data);

    void download(String fileName, InputStream in);

    void download(String fileName, InputStream in, int length);

    void download(ResponseDownloadResource resource);

    // ===================================================================================
    //                                                                     Header Handling
    //                                                                     ===============
    /**
     * Add no-cache to response by e.g. Pragma, Cache-Control, Expires.
     */
    void addNoCache();

    /**
     * Set status as MOVED_PERMANENTLY and add the URL to location.
     * @param url The redirect URL for location of header. (NotNull)
     */
    void redirect301(String url);

    /**
     * Set custom status. <br>
     * if status set once, you can't other status.
     * @param sc HTTP response code. use HttpServletResponse.SC_＊
     */
    void sendErrorStatus(int sc);
}
