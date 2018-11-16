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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.maihama.app.web.base.DocksideBaseAction;
import org.dbflute.maihama.dbflute.allcommon.CDef;
import org.dbflute.maihama.dbflute.exbhv.MemberBhv;
import org.dbflute.maihama.dbflute.exbhv.MemberStatusBhv;
import org.dbflute.maihama.dbflute.exentity.Member;
import org.dbflute.maihama.dbflute.exentity.MemberStatus;
import org.dbflute.maihama.domainfw.action.DocksideLoginRequired;
import org.dbflute.maihama.projectfw.web.paging.PagingNavi;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

/**
 * 会員一覧検索。
 * @author jflute
 */
@DocksideLoginRequired
public class MemberListAction extends DocksideBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @ActionForm
    @Resource
    protected MemberSearchForm memberSearchForm;

    @Resource
    protected MemberBhv memberBhv;

    @Resource
    protected MemberStatusBhv memberStatusBhv;

    // -----------------------------------------------------
    //                                          Display Data
    //                                          ------------
    public Map<String, String> memberStatusMap;
    public List<MemberSearchRowBean> beanList;
    public PagingNavi pagingNavi = createPagingNavi();

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute(validator = false, urlPattern = "{pageNumber}")
    public String index() {
        doPaging();
        return path_Member_MemberListJsp;
    }

    @Execute(validator = true, input = path_Member_MemberListJsp)
    public String doSearch() {
        memberSearchForm.pageNumber = 1;
        doPaging();
        return path_Member_MemberListJsp;
    }

    protected void doPaging() {
        if (memberSearchForm.pageNumber != null && memberSearchForm.pageNumber > 0) { // 検索対象ページ番号が指定されていれば
            // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = [TIPS by jflute]
            // Beansなんとかなど、リフレクションによる詰め替えは「絶対に利用しない」こと
            // http://dbflute.seasar.org/ja/tutorial/architect.html#entityset
            // = = = = = = = = = =/
            PagingResultBean<Member> memberPage = selectMemberPage(); // ここで検索しまっさ
            beanList = memberPage.stream().map(member -> {
                MemberSearchRowBean bean = new MemberSearchRowBean();
                bean.memberId = member.getMemberId();
                bean.memberName = member.getMemberName();
                member.getMemberStatus().alwaysPresent(status -> {
                    bean.memberStatusName = status.getMemberStatusName();
                });
                bean.formalizedDate = toStringDate(member.getFormalizedDatetime());
                bean.updateDatetime = toStringDateTime(member.getUpdateDatetime());
                bean.withdrawalMember = member.isMemberStatusCodeWithdrawal();
                bean.purchaseCount = member.getPurchaseCount();
                return bean;
            }).collect(toList());

            // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = [TIPS by jflute]
            // ページングナビゲーションの表示処理はinclude機能で再利用します。
            // それにより、以下のメリットがあります。
            //   o ページングナビゲーション部分のレイアウトを再利用できる (他の検索一覧画面が再利用)
            //   o ページングナビゲーション部分の表示処理を再利用できる (同上)
            //   o ページングナビゲーション処理を局所化してバグの発生を抑える (自動テストも一箇所で済む)
            //   o PagingResultBeanの利用を開発者に隠蔽する (誰か一人が最初に作れば良い)
            // = = = = = = = = = =/
            preparePagingNavi(pagingNavi, memberPage);
        }
    }

    protected PagingResultBean<Member> selectMemberPage() { // ここはまさしくDBFlute by jflute
        return memberBhv.selectPage(cb -> {
            cb.ignoreNullOrEmptyQuery();
            cb.setupSelect_MemberStatus();
            cb.specify().derivedPurchase().count(purchaseCB -> {
                purchaseCB.specify().columnPurchaseId();
            }, Member.ALIAS_purchaseCount);

            cb.query().setMemberName_LikeSearch(memberSearchForm.memberName, op -> op.likeContain());
            final String purchaseProductName = memberSearchForm.purchaseProductName;
            final boolean unpaid = memberSearchForm.unpaid;
            if ((purchaseProductName != null && purchaseProductName.trim().length() > 0) || unpaid) {
                cb.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().queryProduct().setProductName_LikeSearch(purchaseProductName, op -> op.likeContain());
                    if (unpaid) {
                        purchaseCB.query().setPaymentCompleteFlg_Equal_False();
                    }
                });
            }
            cb.query().setMemberStatusCode_Equal_AsMemberStatus(CDef.MemberStatus.codeOf(memberSearchForm.memberStatus));
            LocalDateTime formalizedDateFrom = toLocalDateTime(memberSearchForm.formalizedDateFrom);
            LocalDateTime formalizedDateTo = toLocalDateTime(memberSearchForm.formalizedDateTo);
            cb.query().setFormalizedDatetime_FromTo(formalizedDateFrom, formalizedDateTo, op -> op.compareAsDate());

            cb.query().addOrderBy_UpdateDatetime_Desc();
            cb.query().addOrderBy_MemberId_Asc();

            int pageSize = getPagingPageSize();
            cb.paging(pageSize, memberSearchForm.pageNumber);
        });
    }

    // ===================================================================================
    //                                                                            Callback
    //                                                                            ========
    // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = [TIPS by jflute]
    // どのリクエストが来ても、バリデーションエラーになっても例外発生しても最後に必ず実行される。
    // 例えば、画面の描画処理などで利用する。(中でJSPへのフォワードであることを判定できる)
    // callbackなんとか() は、末端のActionクラス用メソッド。(superは呼ぶ必要なし)
    // = = = = = = = = = =/
    @Override
    public void callbackFinally(ActionExecuteMeta executeMeta) {
        if (executeMeta.isForwardToJsp()) {
            prepareListBox(); // 会員ステータスなどリストボックスの構築
        }
    }

    protected void prepareListBox() { // ここはアプリによって色々かと by jflute
        ListResultBean<MemberStatus> statusList = memberStatusBhv.selectList(cb -> {
            cb.query().addOrderBy_DisplayOrder_Asc();
        });
        Map<String, String> statusMap = new LinkedHashMap<String, String>();
        statusMap.put("", "選択してください");
        statusList.forEach(status -> statusMap.put(status.getMemberStatusCode(), status.getMemberStatusName()));
        memberStatusMap = statusMap;
    }
}
