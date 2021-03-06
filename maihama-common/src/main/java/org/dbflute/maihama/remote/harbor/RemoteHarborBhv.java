/*
 * Copyright 2015-2017 the original author or authors.
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
package org.dbflute.maihama.remote.harbor;

import java.util.List;

import org.dbflute.maihama.remote.harbor.base.RemoteHbPagingReturn;
import org.dbflute.maihama.remote.harbor.base.RemoteHbUnifiedFailureResult;
import org.dbflute.maihama.remote.harbor.mypage.RemoteHbMypageProductReturn;
import org.dbflute.maihama.remote.harbor.product.RemoteHbProductRowReturn;
import org.dbflute.maihama.remote.harbor.product.RemoteHbProductSearchParam;
import org.dbflute.maihama.remote.harbor.signin.RemoteHbSigninParam;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.saflute.core.remoteapi.LastaRemoteBehavior;
import org.dbflute.saflute.core.remoteapi.mapping.LaVacantMappingPolicy;
import org.dbflute.saflute.core.remoteapi.receiver.LaJsonReceiver;
import org.dbflute.saflute.core.remoteapi.sender.body.LaJsonSender;
import org.dbflute.saflute.core.remoteapi.sender.query.LaQuerySender;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.di.helper.misc.ParameterizedRef;

/**
 * @author jflute
 */
public class RemoteHarborBhv extends LastaRemoteBehavior {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public RemoteHarborBhv(RequestManager requestManager) {
        super(requestManager);
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    @Override
    protected void yourDefaultRule(FlutyRemoteApiRule rule) {
        rule.sendQueryBy(new LaQuerySender(new LaVacantMappingPolicy()));

        JsonMappingOption jsonMappingOption = new JsonMappingOption();
        rule.sendBodyBy(new LaJsonSender(requestManager, jsonMappingOption));
        rule.receiveBodyBy(new LaJsonReceiver(requestManager, jsonMappingOption));

        rule.handleFailureResponseAs(RemoteHbUnifiedFailureResult.class); // server-managed message way
        // asHtmlValidationError() is unsupported at SAFlute 
        //rule.translateClientError(resource -> {
        //    RemoteApiHttpClientErrorException clientError = resource.getClientError();
        //    if (clientError.getHttpStatus() == 400) { // controlled client error
        //        RemoteHbUnifiedFailureResult result = (RemoteHbUnifiedFailureResult) clientError.getFailureResponse().get();
        //        if (RemoteUnifiedFailureType.VALIDATION_ERROR.equals(result.cause)) {
        //            UserMessages messages = new UserMessages();
        //            result.errors.forEach(error -> {
        //                error.messages.forEach(message -> {
        //                    messages.add(error.field, UserMessage.asDirectMessage(message));
        //                });
        //            });
        //            return resource.asHtmlValidationError(messages);
        //        }
        //    }
        //    return null; // no translation
        //});
    }

    @Override
    protected String getUrlBase() {
        return "http://localhost:8090/harbor";
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void requestSignin(RemoteHbSigninParam param) {
        doRequestPost(void.class, "/lido/auth/signin", noMoreUrl(), param, rule -> {});
    }

    public List<RemoteHbMypageProductReturn> requestMypage() {
        return doRequestGet(new ParameterizedRef<List<RemoteHbMypageProductReturn>>() {
        }.getType(), "/lido/mypage", noMoreUrl(), noQuery(), rule -> {});
    }

    public RemoteHbPagingReturn<RemoteHbProductRowReturn> requestProductList(RemoteHbProductSearchParam param) {
        return doRequestPost(new ParameterizedRef<RemoteHbPagingReturn<RemoteHbProductRowReturn>>() {
        }.getType(), "/lido/product/list", moreUrl(1), param, rule -> {});
    }
}
