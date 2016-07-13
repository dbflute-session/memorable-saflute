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
package ${packageName}.domainfw.action;

import ${packageName}.dbflute.exentity.Member;
import ${packageName}.projectfw.web.login.${ProjectName}UserBaseBean;

/**
 * @author saflute_template
 */
public class ${AppName}UserBean extends ${ProjectName}UserBaseBean {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    /** The user type for Member, e.g. used by access context. */
    public static final String USER_TYPE = "M";

    /** The application type for ${AppName}, e.g. used by access context. */
    public static final String DOMAIN_TYPE = "MYT";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Long memberId;
    protected final String memberName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ${AppName}UserBean() {
        memberId = null;
        memberName = null;
    }

    public ${AppName}UserBean(Member member) {
        memberId = Long.valueOf(member.getMemberId());
        memberName = member.getMemberName();
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    public Long getUserId() {
        return memberId;
    }

    @Override
    public String getUserType() {
        return USER_TYPE;
    }

    @Override
    public String getDomainType() {
        return DOMAIN_TYPE;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + memberId + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Long getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }
}
