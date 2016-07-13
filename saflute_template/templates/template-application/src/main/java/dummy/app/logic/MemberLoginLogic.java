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
package ${packageName}.app.logic;

import java.lang.annotation.Annotation;

import javax.annotation.Resource;

import ${packageName}.app.web.login.LoginIndexAction;
import ${packageName}.dbflute.exbhv.MemberBhv;
import ${packageName}.dbflute.exbhv.MemberLoginBhv;
import ${packageName}.dbflute.exentity.Member;
import ${packageName}.dbflute.exentity.MemberLogin;
import ${packageName}.domainfw.action.${AppName}LoginRequired;
import ${packageName}.domainfw.action.${AppName}PerformLogin;
import ${packageName}.domainfw.action.${AppName}UserBean;
import ${packageName}.domainfw.direction.${AppName}Config;
import ${packageName}.projectfw.web.login.${ProjectName}LoginBaseLogic;

/**
 * @author saflute_template
 */
public class MemberLoginLogic extends ${ProjectName}LoginBaseLogic<${AppName}UserBean, Member> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MemberBhv memberBhv;

    @Resource
    protected MemberLoginBhv memberLoginBhv;

    @Resource
    protected ${AppName}Config ${appname}Config;

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
        return ${AppName}LoginRequired.class;
    }

    @Override
    protected Class<? extends Annotation> getPerformLoginAnnotationType() {
        return ${AppName}PerformLogin.class;
    }

    @Override
    protected ${AppName}UserBean createUserBean(Member userEntity) {
        return new ${AppName}UserBean(userEntity);
    }

    @Override
    protected String getCookieAutoLoginKey() {
        return ${appname}Config.getCookieAutoLogin${AppName}Key();
    }

    @Override
    protected void saveLoginHistory(Member member, ${AppName}UserBean userBean, boolean useAutoLogin) {
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
    protected Class<${AppName}UserBean> getUserBeanType() {
        return ${AppName}UserBean.class;
    }

    @Override
    protected Class<?> getLoginActionType() {
        return LoginIndexAction.class;
    }
}
