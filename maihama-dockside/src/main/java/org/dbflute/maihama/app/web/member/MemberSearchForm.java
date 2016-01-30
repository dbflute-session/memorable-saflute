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
package org.dbflute.maihama.app.web.member;

import java.io.Serializable;

import org.seasar.framework.container.annotation.tiger.Component;
import org.seasar.framework.container.annotation.tiger.InstanceType;
import org.seasar.struts.annotation.Arg;
import org.seasar.struts.annotation.DateType;
import org.seasar.struts.annotation.Msg;

/**
 * @author jflute
 */
@Component(instance = InstanceType.SESSION)
public class MemberSearchForm implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public Integer pageNumber = 0;

    // -----------------------------------------------------
    //                                             Condition
    //                                             ---------
    public String memberName;
    public String memberStatus;
    public String purchaseProductName;
    public boolean unpaid;

    @DateType(datePatternStrict = "yyyy/MM/dd", arg0 = @Arg(key = "正式会員日From", resource = false), msg = @Msg(key = "errors.date.strict", resource = true))
    public String formalizedDateFrom;

    @DateType(datePatternStrict = "yyyy/MM/dd", arg0 = @Arg(key = "正式会員日To", resource = false), msg = @Msg(key = "errors.date.strict", resource = true))
    public String formalizedDateTo;

    // ===================================================================================
    //                                                                               Reset
    //                                                                               =====
    public void reset() {
        unpaid = false;
    }
}
