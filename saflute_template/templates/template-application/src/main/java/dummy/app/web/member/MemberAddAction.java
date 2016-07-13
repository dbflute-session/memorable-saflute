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
package ${packageName}.app.web.member;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import ${packageName}.app.web.base.${AppName}BaseAction;
import ${packageName}.dbflute.allcommon.CDef;
import ${packageName}.dbflute.exbhv.MemberBhv;
import ${packageName}.dbflute.exbhv.MemberStatusBhv;
import ${packageName}.dbflute.exentity.Member;
import ${packageName}.dbflute.exentity.MemberStatus;
import ${packageName}.domainfw.action.${AppName}LoginRequired;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

/**
 * 会員追加アクション。
 * @author saflute_template
 */
@${AppName}LoginRequired
public class MemberAddAction extends ${AppName}BaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @ActionForm
    @Resource
    protected MemberForm memberForm;

    @Resource
    protected MemberBhv memberBhv;

    @Resource
    protected MemberStatusBhv memberStatusBhv;

    // -----------------------------------------------------
    //                                          Display Data
    //                                          ------------
    public Map<String, String> memberStatusMap;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute(validator = false)
    public String index() {
        return path_Member_MemberAddJsp;
    }

    @Execute(validator = true, input = path_Member_MemberAddJsp)
    public String doAdd() {
        Member member = new Member();
        member.setMemberId(memberForm.memberId);
        member.setMemberName(memberForm.memberName);
        member.setBirthdate(toLocalDate(memberForm.birthdate));
        member.setMemberStatusCodeAsMemberStatus(CDef.MemberStatus.codeOf(memberForm.memberStatusCode));
        member.setMemberAccount(memberForm.memberAccount);
        if (member.isMemberStatusCode正式会員()) { // 区分値の判定は Entity の isなんとか() メソッドで by jflute
            member.setFormalizedDatetime(timeManager.getCurrentLocalDateTime()); // 現在日時はTimeManagerから by jflute
        }
        member.setVersionNo(memberForm.versionNo);
        memberBhv.insert(member);
        return redirect(MemberListAction.class);
    }

    // ===================================================================================
    //                                                                            Callback
    //                                                                            ========
    @Override
    public void callbackFinally(ActionExecuteMeta executeMeta) {
        if (executeMeta.isForwardToJsp()) {
            prepareListBox(); // 会員ステータスなどリストボックスの構築
        }
    }

    // ===================================================================================
    //                                                                               Logic
    //                                                                               =====
    protected void prepareListBox() { // ここはアプリによって色々かと by jflute
        Map<String, String> statusMap = new LinkedHashMap<String, String>();
        ListResultBean<MemberStatus> statusList = memberStatusBhv.selectList(cb -> {
            cb.query().addOrderBy_DisplayOrder_Asc();
        });
        statusList.forEach(status -> {
            statusMap.put(status.getMemberStatusCode(), status.getMemberStatusName());
        });
        memberStatusMap = statusMap;
    }
}
