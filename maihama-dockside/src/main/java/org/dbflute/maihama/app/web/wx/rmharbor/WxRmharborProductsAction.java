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
package org.dbflute.maihama.app.web.wx.rmharbor;

import javax.annotation.Resource;

import org.dbflute.maihama.app.web.base.DocksideBaseAction;
import org.dbflute.maihama.remote.harbor.RemoteHarborBhv;
import org.dbflute.maihama.remote.harbor.base.RemoteHbPagingReturn;
import org.dbflute.maihama.remote.harbor.product.RemoteHbProductRowReturn;
import org.dbflute.maihama.remote.harbor.product.RemoteHbProductSearchParam;
import org.dbflute.saflute.web.action.response.JsonResponse;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

/**
 * @author jflute
 */
public class WxRmharborProductsAction extends DocksideBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @ActionForm
    @Resource
    protected WxRmharborProductSearchForm wxRmharborProductSearchForm;
    @Resource
    protected RemoteHarborBhv remoteHarborBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // http://localhost:8088/dockside/wx/rmharbor/products/?productName=S
    // http://localhost:8088/dockside/wx/rmharbor/products/?productName=SeaLandPiariBonvo
    @Execute(validator = false)
    public JsonResponse index() {
        RemoteHbPagingReturn<RemoteHbProductRowReturn> ret = requestProductList();
        return asJson(ret);
    }

    private RemoteHbPagingReturn<RemoteHbProductRowReturn> requestProductList() {
        RemoteHbProductSearchParam param = new RemoteHbProductSearchParam();
        param.productName = wxRmharborProductSearchForm.productName;
        return remoteHarborBhv.requestProductList(param);
    }
}
