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
import java.util.List;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.maihama.dbflute.exentity.Member;
import org.dbflute.maihama.domainfw.action.DocksideJspPath;
import org.dbflute.maihama.unit.UnitDocksideContainerTestCase;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class MemberListActionTest extends UnitDocksideContainerTestCase {

    public void test_doSearch_memberName_and_status() throws Exception {
        // ## Arrange ##
        MemberListAction searchAction = new MemberListAction();
        inject(searchAction);

        searchAction.memberSearchForm.memberName = "S";
        searchAction.memberSearchForm.memberStatus = "FML";

        // ## Act ##
        String moveTo = searchAction.doSearch();

        // ## Assert ##
        List<MemberSearchRowBean> memberList = searchAction.beanList;
        assertHasAnyElement(memberList);
        for (MemberSearchRowBean member : memberList) {
            log(member.memberName + ", " + member.memberStatusName);
            assertTrue(Srl.containsIgnoreCase(member.memberName, "S"));
            assertFalse(member.withdrawalMember);
        }

        assertEquals(DocksideJspPath.path_Member_MemberListJsp, moveTo);
    }

    public void test_doSearch_formalizedDatetime_DateFromTo() throws Exception {
        // ## Arrange ##
        MemberListAction searchAction = new MemberListAction();
        inject(searchAction);
        String from = "2005/01/01";
        String to = "2006/12/31";
        searchAction.memberSearchForm.formalizedDateFrom = from;
        searchAction.memberSearchForm.formalizedDateTo = to;

        // ## Act ##
        searchAction.doSearch();

        // ## Assert ##
        List<MemberSearchRowBean> memberList = searchAction.beanList;
        assertHasAnyElement(memberList);
        for (MemberSearchRowBean member : memberList) {
            LocalDateTime formalizedDatetime = toLocalDateTime(member.formalizedDate);
            log(member.memberName + ", " + formalizedDatetime);
            LocalDateTime fromTime = toLocalDateTime(from);
            LocalDateTime toTime = toLocalDateTime(to);
            LocalDateTime addedToTime = toTime.plusDays(1L);
            assertTrue(formalizedDatetime.isAfter(fromTime) || formalizedDatetime.equals(fromTime));
            assertTrue(formalizedDatetime.isBefore(addedToTime));
        }
    }

    public void test_doSearch_MockExample() throws Exception {
        // ## Arrange ##
        final PagingResultBean<Member> rb = new PagingResultBean<Member>();
        MemberListAction searchAction = new MemberListAction() {
            @Override
            protected PagingResultBean<Member> selectMemberPage() {
                return rb;
            }
        };
        inject(searchAction);

        // ## Act ##
        searchAction.doSearch();

        // ## Assert ##
        assertEquals(rb.getSelectedList(), searchAction.beanList);
    }
}
