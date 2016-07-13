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
import org.dbflute.util.DfTypeUtil;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

/**
 * 会員編集アクション。
 * @author saflute_template
 */
@${AppName}LoginRequired
public class MemberEditAction extends ${AppName}BaseAction {

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
    @Execute(validator = false, urlPattern = "{memberId}")
    public String index() {
        Integer memberId = memberForm.memberId;
        assertGetParameterExists(memberId);

        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = [TIPS by jflute]
        // Beansなんとかなど、リフレクションによる詰め替えは「絶対に利用しない」こと
        // http://dbflute.seasar.org/ja/tutorial/architect.html#entityset
        // = = = = = = = = = =/
        Member member = selectMember(memberId);
        memberForm.memberId = member.getMemberId();
        memberForm.memberName = member.getMemberName();
        memberForm.memberAccount = member.getMemberAccount();
        memberForm.memberStatusCode = member.getMemberStatusCode();
        // 日付フォーマットのやり方はアプリによって色々かと by jflute
        String ymd = "yyyy/MM/dd";
        memberForm.birthdate = DfTypeUtil.toString(member.getBirthdate(), ymd);
        memberForm.formalizedDate = DfTypeUtil.toString(member.getFormalizedDatetime(), ymd);
        String ymdhms = "yyyy/MM/dd HH:mm:ss";
        memberForm.latestLoginDatetime = DfTypeUtil.toString(member.getLatestLoginDatetime(), ymdhms);
        memberForm.updateDatetime = DfTypeUtil.toString(member.getUpdateDatetime(), ymdhms);
        memberForm.previousStatusCode = member.getMemberStatusCode(); // to determine new formalized member
        memberForm.versionNo = member.getVersionNo();

        return path_Member_MemberEditJsp;
    }

    protected Member selectMember(Integer memberId) {
        return memberBhv.selectEntityWithDeletedCheck(cb -> {
            cb.specify().derivedMemberLogin().max(loginCB -> {
                loginCB.specify().columnLoginDatetime();
            }, Member.ALIAS_latestLoginDatetime);
            cb.query().setMemberId_Equal(memberId);
            cb.query().setMemberStatusCode_InScope_ServiceAvailable();
        });
    }

    @Execute(validator = true, input = path_Member_MemberEditJsp)
    public String doUpdate() {
        Member member = new Member();
        member.setMemberId(memberForm.memberId);
        member.setMemberName(memberForm.memberName);
        member.setBirthdate(toLocalDate(memberForm.birthdate));
        member.setMemberStatusCodeAsMemberStatus(CDef.MemberStatus.codeOf(memberForm.memberStatusCode));
        member.setMemberAccount(memberForm.memberAccount);
        CDef.MemberStatus previousStatus = CDef.MemberStatus.codeOf(memberForm.previousStatusCode);
        if (member.isMemberStatusCode正式会員()) {
            if (previousStatus != null && previousStatus.isShortOfFormalized()) {
                member.setFormalizedDatetime(timeManager.getCurrentLocalDateTime());
            }
        } else if (member.isMemberStatusCode_ShortOfFormalized()) {
            member.setFormalizedDatetime(null);
        }
        member.setVersionNo(memberForm.versionNo);
        memberBhv.update(member);
        return redirectById(MemberEditAction.class, member.getMemberId());
    }

    @Execute(validator = true, input = path_Member_MemberEditJsp)
    public String doDelete() {
        Member member = new Member();
        member.setMemberId(memberForm.memberId);
        member.setMemberStatusCode_退会会員();
        member.setVersionNo(memberForm.versionNo);
        memberBhv.update(member);
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
