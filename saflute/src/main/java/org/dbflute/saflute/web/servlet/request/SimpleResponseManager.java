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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.web.servlet.OptionalServletDirection;
import org.dbflute.util.Srl;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.OutputStreamUtil;
import org.seasar.struts.util.ResponseUtil;

/**
 * @author jflute
 */
public class SimpleResponseManager implements ResponseManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(SimpleResponseManager.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The map of content type for extensions. (NullAllowed) */
    protected Map<String, String> downloadExtensionContentTypeMap;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public void initialize() {
        final OptionalServletDirection direction = assistOptionalServletDirection();
        final ResponseHandlingProvider provider = direction.assistResponseHandlingProvider();
        if (provider != null) {
            downloadExtensionContentTypeMap = provider.provideDownloadExtensionContentTypeMap();
        }
        showBootLogging();
    }

    protected OptionalServletDirection assistOptionalServletDirection() {
        return assistantDirector.assistOptionalServletDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Response Manager]");
            LOG.info(" downloadExtensionContentTypeMap: " + downloadExtensionContentTypeMap);
        }
    }

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    public HttpServletResponse getResponse() {
        return ResponseUtil.getResponse();
    }

    // ===================================================================================
    //                                                                      Write Response
    //                                                                      ==============
    public void write(String text) {
        ResponseUtil.write(text);
    }

    public void write(String text, String contentType) {
        ResponseUtil.write(text, contentType);
    }

    public void write(String text, String contentType, String encoding) {
        ResponseUtil.write(text, contentType, encoding);
    }

    public void writeAsJson(String json) {
        final String contentType = "application/json";
        if (LOG.isDebugEnabled()) {
            LOG.debug("#flow ...Writing response as " + contentType + ": \n" + json);
        }
        write(json, contentType);
    }

    public void writeAsJavaScript(String script) {
        final String contentType = "application/javascript";
        if (LOG.isDebugEnabled()) {
            LOG.debug("#flow ...Writing response as " + contentType + ": \n" + script);
        }
        write(script, contentType);
    }

    public void writeAsXml(String xmlStr, String encoding) {
        final String contentType = "text/xml";
        if (LOG.isDebugEnabled()) {
            LOG.debug("#flow ...Writing response as " + contentType + ": \n" + xmlStr);
        }
        write(xmlStr, contentType, encoding);
    }

    // ===================================================================================
    //                                                                   Download Response
    //                                                                   =================
    public void download(String fileName, byte[] data) {
        final ResponseDownloadResource resource = createResponseDownloadResource(fileName);
        resource.data(data);
        doDownload(resource);
    }

    public void download(String fileName, InputStream stream) {
        final ResponseDownloadResource resource = createResponseDownloadResource(fileName);
        resource.stream(stream);
        doDownload(resource);
    }

    public void download(String fileName, InputStream stream, int contentLength) {
        final ResponseDownloadResource resource = createResponseDownloadResource(fileName);
        resource.stream(stream, contentLength);
        doDownload(resource);
    }

    protected ResponseDownloadResource createResponseDownloadResource(String fileName) {
        final ResponseDownloadResource resource = new ResponseDownloadResource(fileName);
        if (resource.getContentType() != null) {
            return resource;
        }
        if (downloadExtensionContentTypeMap != null && fileName.contains(".")) {
            final String extension = Srl.substringLastRear(fileName, ".");
            final String contentType = downloadExtensionContentTypeMap.get(extension);
            if (contentType != null) {
                resource.contentType(contentType);
            }
        }
        return resource; // as default
    }

    public void download(ResponseDownloadResource resource) {
        doDownload(resource);
    }

    protected void doDownload(ResponseDownloadResource resource) {
        final HttpServletResponse response = getResponse();
        prepareDownloadResponse(resource, response);
        final byte[] byteData = resource.getByteData();
        if (byteData != null) {
            doDownloadByteData(resource, response, byteData);
        } else {
            doDownloadInputStream(resource, response);
        }
    }

    protected void prepareDownloadResponse(ResponseDownloadResource resource, HttpServletResponse response) {
        if (resource.getContentType() == null) {
            resource.contentTypeOctetStream(); // as default
            resource.headerContentDispositionAttachment(); // with header for the type
        }
        final String contentType = resource.getContentType();
        response.setContentType(contentType);
        final Map<String, String> headerMap = resource.getHeaderMap();
        for (Entry<String, String> entry : headerMap.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();
            response.setHeader(name, value);
        }
    }

    protected void doDownloadByteData(ResponseDownloadResource resource, HttpServletResponse response, byte[] byteData) {
        try {
            final OutputStream out = response.getOutputStream();
            try {
                out.write(byteData);
            } finally {
                OutputStreamUtil.close(out);
            }
        } catch (RuntimeException e) {
            String msg = "Failed to download the byte data: " + resource;
            throw new IllegalStateException(msg, e);
        } catch (IOException e) {
            String msg = "Failed to download the byte data: " + resource;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void doDownloadInputStream(ResponseDownloadResource resource, HttpServletResponse response) {
        final InputStream ins = resource.getInputStream();
        if (ins == null) {
            String msg = "Either byte data or input stream is required: " + resource;
            throw new IllegalArgumentException(msg);
        }
        try {
            final Integer contentLength = resource.getContentLength();
            if (contentLength != null) {
                response.setContentLength(contentLength);
            }
            final OutputStream out = response.getOutputStream();
            try {
                InputStreamUtil.copy(ins, out);
                OutputStreamUtil.flush(out);
            } finally {
                OutputStreamUtil.close(out);
            }
        } catch (RuntimeException e) {
            String msg = "Failed to download the input stream: " + resource;
            throw new IllegalStateException(msg, e);
        } catch (IOException e) {
            String msg = "Failed to download the input stream: " + resource;
            throw new IllegalStateException(msg, e);
        } finally {
            InputStreamUtil.close(ins);
        }
    }

    // ===================================================================================
    //                                                                     Header Handling
    //                                                                     ===============
    public void addNoCache() {
        getResponse().addHeader("Pragma", "no-cache");
        getResponse().addHeader("Cache-Control", "no-cache, no-store");
        getResponse().addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
    }

    public void redirect301(String url) {
        getResponse().setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        getResponse().setHeader("Location", url);
    }

    public void sendErrorStatus(int sc) {
        getResponse().setStatus(sc);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void assertObjectNotNull(String variableName, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}
