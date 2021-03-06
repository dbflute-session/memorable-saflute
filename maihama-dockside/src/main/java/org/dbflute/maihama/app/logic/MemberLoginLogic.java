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
package org.dbflute.maihama.app.logic;

import java.lang.annotation.Annotation;

import javax.annotation.Resource;

import org.dbflute.maihama.app.base.MaihamaLoginBaseLogic;
import org.dbflute.maihama.app.web.login.LoginIndexAction;
import org.dbflute.maihama.dbflute.exbhv.MemberBhv;
import org.dbflute.maihama.dbflute.exbhv.MemberLoginBhv;
import org.dbflute.maihama.dbflute.exentity.Member;
import org.dbflute.maihama.dbflute.exentity.MemberLogin;
import org.dbflute.maihama.domainfw.action.DocksideLoginRequired;
import org.dbflute.maihama.domainfw.action.DocksidePerformLogin;
import org.dbflute.maihama.domainfw.action.DocksideUserBean;
import org.dbflute.maihama.domainfw.direction.DocksideConfig;

/**
 * @author jflute
 */
public class MemberLoginLogic extends MaihamaLoginBaseLogic<DocksideUserBean, Member> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;

    @Resource
    protected MemberLoginBhv memberLoginBhv;

    @Resource
    protected DocksideConfig docksideConfig;

    // ===================================================================================
    //                                                                                Find
    //                                                                                ====
    @Override
    protected boolean doCheckUserLoginable(String email, String cipheredPassword) {
        return memberBhv.selectCount(cb -> {
            cb.query().arrangeLogin(email, cipheredPassword);
        }) > 0;
    }

    @Override
    protected Member doFindLoginUser(String email, String cipheredPassword) {
        return memberBhv.selectEntity(cb -> {
            cb.query().arrangeLogin(email, cipheredPassword);
        }).orElse(null);
    }

    @Override
    protected Member doFindLoginUser(Long userId) {
        return memberBhv.selectEntity(cb -> {
            cb.query().arrangeLoginByIdentity(userId.intValue());
        }).orElse(null);
    }

    // ===================================================================================
    //                                                                               Login
    //                                                                               =====
    @Override
    protected Class<? extends Annotation> getLoginRequiredAnnotationType() {
        return DocksideLoginRequired.class;
    }

    @Override
    protected Class<? extends Annotation> getPerformLoginAnnotationType() {
        return DocksidePerformLogin.class;
    }

    @Override
    protected DocksideUserBean createUserBean(Member userEntity) {
        return new DocksideUserBean(userEntity);
    }

    @Override
    protected String getCookieAutoLoginKey() {
        return docksideConfig.getCookieAutoLoginDocksideKey();
    }

    @Override
    protected void saveLoginHistory(Member member, DocksideUserBean userBean, boolean useAutoLogin) {
        MemberLogin login = new MemberLogin();
        login.setMemberId(member.getMemberId());
        login.setLoginMemberStatusCodeAsMemberStatus(member.getMemberStatusCodeAsMemberStatus());
        login.setLoginDatetime(timeManager.getCurrentLocalDateTime());
        login.setMobileLoginFlg_False(); // mobile unsupported for now
        memberLoginBhv.insert(login);
    }

    // ===================================================================================
    //                                                              Login Control Resource
    //                                                              ======================
    @Override
    protected Class<DocksideUserBean> getUserBeanType() {
        return DocksideUserBean.class;
    }

    @Override
    protected Class<?> getLoginActionType() {
        return LoginIndexAction.class;
    }
}
