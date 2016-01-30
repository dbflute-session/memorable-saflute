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
package org.dbflute.maihama.app.web.base;

import javax.annotation.Resource;

import org.dbflute.maihama.app.base.MaihamaBaseAction;
import org.dbflute.maihama.app.logic.MemberLoginLogic;
import org.dbflute.maihama.domainfw.action.DocksideJspPath;
import org.dbflute.maihama.domainfw.action.DocksideMessages;
import org.dbflute.maihama.domainfw.action.DocksideUserBean;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;

/**
 * @author jflute
 */
public abstract class DocksideBaseAction extends MaihamaBaseAction implements DocksideJspPath {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @Resource
    protected MemberLoginLogic memberLoginLogic;

    // -----------------------------------------------------
    //                                          Display Data
    //                                          ------------
    public DocksideUserWebBean userWebBean;

    // ===================================================================================
    //                                                                               Login
    //                                                                               =====
    /**
     * {@inheritDoc}
     */
    @Override
    protected DocksideUserBean getUserBean() {
        final DocksideUserBean userBean = sessionManager.getAttribute(DocksideUserBean.class);
        return userBean != null ? userBean : new DocksideUserBean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DocksideUserBean getUserBeanChecked() {
        return (DocksideUserBean) super.getUserBeanChecked();
    }

    /**
     * Logout the current session.
     */
    protected void logoutProc() {
        memberLoginLogic.logoutProc();
    }

    // ===================================================================================
    //                                                                             Message
    //                                                                             =======
    /**
     * {@inheritDoc}
     */
    @Override
    protected DocksideMessages createActionMessages() {
        return new DocksideMessages();
    }

    // ===================================================================================
    //                                                                            Callback
    //                                                                            ========
    // -----------------------------------------------------
    //                                      God Hand Finally
    //                                      ----------------
    // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = [TIPS by jflute]
    // どのリクエストが来ても、バリデーションエラーになっても例外発生しても最後に必ず実行される。
    // 例えば、画面の描画処理などで利用する。(中でJSPへのフォワードであることを判定できる)
    // godHandなんとか() は、Actionのスーパークラス用メソッド。(superは必ず呼ぶこと)
    // = = = = = = = = = =/
    @Override
    public void godHandFinally(ActionExecuteMeta executeMeta) {
        if (executeMeta.isForwardToJsp()) {
            if (userWebBean == null) { // basically true, however just in case
                final DocksideUserBean userBean = getUserBean();
                userWebBean = new DocksideUserWebBean();
                if (userBean.isLogin()) {
                    userWebBean.memberId = userBean.getMemberId();
                    userWebBean.memberName = userBean.getMemberName();
                }
                userWebBean.isLogin = userBean.isLogin();
            }
        }
        super.godHandFinally(executeMeta);
    }

    // ===================================================================================
    //                                                               Application Exception
    //                                                               =====================
    @Override
    protected String getErrorMessageJsp() {
        return path_Error_ErrorMessageJsp;
    }
}
