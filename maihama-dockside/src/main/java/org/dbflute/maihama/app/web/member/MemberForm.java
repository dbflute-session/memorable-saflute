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

import org.dbflute.maihama.domainfw.action.DocksideMessages;
import org.seasar.struts.annotation.Arg;
import org.seasar.struts.annotation.DateType;
import org.seasar.struts.annotation.Msg;
import org.seasar.struts.annotation.Required;

/**
 * 会員に対する操作の汎用ActionForm。
 * @author jflute
 */
public class MemberForm {

    @Required(target = "doUpdate, doDelete")
    public Integer memberId;

    @Required(arg0 = @Arg(key = "会員名", resource = false))
    public String memberName;

    @Required(arg0 = @Arg(key = "会員アカウント", resource = false))
    public String memberAccount;

    @Required(arg0 = @Arg(key = "会員ステータス", resource = false))
    public String memberStatusCode;

    @DateType(datePatternStrict = "yyyy/MM/dd", msg = @Msg(key = DocksideMessages.ERRORS_DATE, resource = true), arg0 = @Arg(key = "生年月日", resource = false))
    public String birthdate;

    public String formalizedDate;

    public String latestLoginDatetime;

    public String updateDatetime;

    @Required(target = "doUpdate")
    public String previousStatusCode;

    @Required(target = "doUpdate, doDelete")
    public Long versionNo;
}