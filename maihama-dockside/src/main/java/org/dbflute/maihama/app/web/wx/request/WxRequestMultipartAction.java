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
package org.dbflute.maihama.app.web.wx.request;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.maihama.app.web.base.DocksideBaseAction;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

/**
 * @author jflute
 */
public class WxRequestMultipartAction extends DocksideBaseAction {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(WxRequestMultipartAction.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @ActionForm
    @Resource
    protected WxRequestMultipartForm requestMultipartForm;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // http://localhost:8088/dockside/wx/request/multipart
    @Execute(validator = false)
    public String index() {
        // #thinking jflute making now, needs to resolve the following unexpected exception (2023/10/31)
        // Caused by: org.seasar.framework.container.IllegalAutoBindingPropertyRuntimeException:
        // [ESSR0080]クラス(org.dbflute.maihama.app.web.wx.request.WxRequestMultipartAction$$EnhancedByS2AOP$$6c8e03d8)のプロパティ(requestMultipartForm)の自動設定に失敗しました
        return path_WxRequestMultipart_WxRequestMultipartJsp;
    }

    @Execute(validator = false)
    public String doUpload() {
        LOG.debug("requestMultipartForm: " + requestMultipartForm);
        return path_WxRequestMultipart_WxRequestMultipartJsp;
    }
}
