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

import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.upload.MultipartRequestHandler;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.web.servlet.filter.RequestLoggingFilter;
import org.seasar.struts.upload.S2MultipartRequestHandler;

/**
 * @author jflute
 */
public class ActionMultipartRequestHandler extends S2MultipartRequestHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(ActionMultipartRequestHandler.class);

    // ===================================================================================
    //                                                                      Handle Request
    //                                                                      ==============
    @Override
    public void handleRequest(HttpServletRequest request) throws ServletException {
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // copied from super's method and extends it
        // basically for JVN#14876762
        // thought not all problems are resolved however the main case is safety
        // - - - - - - - - - -/
        final ModuleConfig ac = getModuleConfig(request);
        final ServletFileUpload upload = createServletFileUpload(request, ac);
        prepareElementsHash();
        try {
            final List<?> items = parseRequest(request, upload);
            mappingParameter(request, items);
        } catch (SizeLimitExceededException e) {
            handleSizeLimitExceededException(request, e);
        } catch (FileUploadException e) {
            handleFileUploadException(e);
        }
    }

    protected ModuleConfig getModuleConfig(HttpServletRequest request) {
        return (ModuleConfig) request.getAttribute(Globals.MODULE_KEY);
    }

    // ===================================================================================
    //                                                            Create ServletFileUpload
    //                                                            ========================
    protected ServletFileUpload createServletFileUpload(HttpServletRequest request, ModuleConfig ac) {
        final DiskFileItemFactory fileItemFactory = createDiskFileItemFactory(ac);
        final ServletFileUpload upload = newServletFileUpload(fileItemFactory);
        upload.setHeaderEncoding(request.getCharacterEncoding());
        upload.setSizeMax(getSizeMax(ac));
        return upload;
    }

    protected ServletFileUpload newServletFileUpload(DiskFileItemFactory fileItemFactory) {
        return new ServletFileUpload(fileItemFactory) {
            @Override
            protected byte[] getBoundary(String contentType) { // for security
                final byte[] boundary = super.getBoundary(contentType);
                checkBoundarySize(contentType, boundary);
                return boundary;
            }
        };
    }

    protected void checkBoundarySize(String contentType, byte[] boundary) {
        final int boundarySize = boundary.length;
        final int limitSize = getBoundaryLimitSize();
        if (boundarySize > getBoundaryLimitSize()) {
            throwTooLongBoundarySizeException(contentType, boundarySize, limitSize);
        }
    }

    protected int getBoundaryLimitSize() {
        // one HTTP proxy tool already limits the size (e.g. 3450 bytes)
        // so specify this size for test
        return 2000; // you can override as you like it
    }

    protected void throwTooLongBoundarySizeException(String contentType, int boundarySize, int limitSize) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Too long boundary size so treats it as 404.");
        br.addItem("Advice");
        br.addElement("Against for JVN14876762.");
        br.addElement("Boundary size is limited by Framework.");
        br.addElement("Too long boundary is treated as 404 because it's thought of as attack.");
        br.addElement("");
        br.addElement("While, you can override the boundary limit size");
        br.addElement(" in " + ActionMultipartRequestHandler.class.getSimpleName() + ".");
        br.addItem("Content Type");
        br.addElement(contentType);
        br.addItem("Boundary Size");
        br.addElement(boundarySize);
        br.addItem("Limit Size");
        br.addElement(limitSize);
        final String msg = br.buildExceptionMessage();
        throw new RequestLoggingFilter.Request404NotFoundException(msg);
    }

    protected DiskFileItemFactory createDiskFileItemFactory(ModuleConfig ac) {
        final File repository = createRepositoryFile(ac);
        return new DiskFileItemFactory((int) getSizeThreshold(ac), repository);
    }

    protected File createRepositoryFile(ModuleConfig ac) {
        return new File(getRepositoryPath(ac));
    }

    // ===================================================================================
    //                                                                      Handling Parts
    //                                                                      ==============
    @SuppressWarnings("rawtypes")
    protected void prepareElementsHash() {
        elementsText = new Hashtable();
        elementsFile = new Hashtable();
        elementsAll = new Hashtable();
    }

    protected List<?> parseRequest(HttpServletRequest request, ServletFileUpload upload) throws FileUploadException {
        return upload.parseRequest(request);
    }

    protected void mappingParameter(HttpServletRequest request, List<?> items) {
        // Partition the items into form fields and files.
        showFieldLoggingTitle();
        final Iterator<?> iter = items.iterator();
        while (iter.hasNext()) {
            final FileItem item = (FileItem) iter.next();
            if (item.isFormField()) {
                showFormFieldParameter(item);
                addTextParameter(request, item);
            } else {
                showFileFieldParameter(item);
                addFileParameter(item);
            }
        }
    }

    protected void showFieldLoggingTitle() {
        // logging filter cannot show the parameters when multi-part so logging here
        if (LOG.isDebugEnabled()) {
            LOG.debug("[Multipart Request Parameter]");
        }
    }

    protected void showFormFieldParameter(FileItem item) {
        if (LOG.isDebugEnabled()) {
            final String fieldName = item.getFieldName();
            LOG.debug("[param] " + fieldName + "=" + item.getString());
        }
    }

    protected void showFileFieldParameter(FileItem item) {
        if (LOG.isDebugEnabled()) {
            final String fieldName = item.getFieldName();
            LOG.debug("[param] " + fieldName + ":{name=" + item.getName() + ", size=" + item.getSize() + "}");
        }
    }

    protected void handleSizeLimitExceededException(HttpServletRequest request, SizeLimitExceededException e) {
        request.setAttribute(MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED, Boolean.TRUE);
        request.setAttribute(SIZE_EXCEPTION_KEY, e);
        try {
            final InputStream is = request.getInputStream();
            try {
                final byte[] buf = new byte[1024];
                @SuppressWarnings("unused")
                int len = 0;
                while ((len = is.read(buf)) != -1) {
                }
            } catch (Exception ignore) {
            } finally {
                try {
                    is.close();
                } catch (Exception ignore) {
                }
            }
        } catch (Exception ignore) {
        }
    }

    protected void handleFileUploadException(FileUploadException e) throws ServletException {
        // suppress logging because it can be caught by logging filter 
        //log.error("Failed to parse multipart request", e);
        throw new ServletException(e);
    }
}
