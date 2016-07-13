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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.helper.HandyDate;

import ${packageName}.dbflute.exentity.Member;
import ${packageName}.unit.Unit${AppName}ContainerTestCase;

/**
 * @author saflute_template
 */
public class MemberListActionTest extends Unit${AppName}ContainerTestCase {

    public void test_doSearch_memberName_and_status() throws Exception {
        // ${_DS_} Arrange ${_DS_}
        MemberListAction searchAction = new MemberListAction();
        inject(searchAction);

        searchAction.memberListForm.memberName = "S";
        searchAction.memberListForm.memberStatus = "FML";

        // ${_DS_} Act ${_DS_}
        String moveTo = searchAction.doSearch();

        // ${_DS_} Assert ${_DS_}
        Map<String, String> memberStatusMap = searchAction.memberStatusMap;
        assertHasAnyElement(memberStatusMap.keySet());

        List<MemberWebBean> memberList = searchAction.beanList;
        assertHasAnyElement(memberList);
        for (MemberWebBean member : memberList) {
            log(member.memberName + ", " + member.memberStatusName);
            assertTrue(member.memberName.startsWith("S"));
            assertTrue(member.withdrawalMember);
        }

        assertEquals("list.jsp", moveTo);
    }

    public void test_doSearch_formalizedDatetime_DateFromTo() throws Exception {
        // ${_DS_} Arrange ${_DS_}
        MemberListAction searchAction = new MemberListAction();
        inject(searchAction);
        String from = "2005/01/01";
        String to = "2006/12/31";
        searchAction.memberListForm.formalizedDateFrom = from;
        searchAction.memberListForm.formalizedDateTo = to;

        // ${_DS_} Act ${_DS_}
        searchAction.doSearch();

        // ${_DS_} Assert ${_DS_}
        List<MemberWebBean> memberList = searchAction.beanList;
        assertHasAnyElement(memberList);
        for (MemberWebBean member : memberList) {
            LocalDateTime formalizedDatetime = toLocalDateTime(member.formalizedDate);
            log(member.memberName + ", " + formalizedDatetime);
            LocalDateTime fromTime = toLocalDateTime(from);
            LocalDateTime toTime = toLocalDateTime(to);
            LocalDateTime addedToTime = new HandyDate(toTime).addDay(1).getLocalDateTime();
            assertTrue(formalizedDatetime.isAfter(fromTime) || formalizedDatetime.equals(fromTime));
            assertTrue(formalizedDatetime.isBefore(addedToTime));
        }
    }

    public void test_doSearch_MockExample() throws Exception {
        // ${_DS_} Arrange ${_DS_}
        final PagingResultBean<Member> rb = new PagingResultBean<Member>();
        MemberListAction searchAction = new MemberListAction() {
            @Override
            protected PagingResultBean<Member> selectMemberPage() {
                return rb;
            }
        };
        inject(searchAction);

        // ${_DS_} Act ${_DS_}
        searchAction.doSearch();

        // ${_DS_} Assert ${_DS_}
        assertEquals(rb, searchAction.beanList);
        Map<String, String> memberStatusMap = searchAction.memberStatusMap;
        assertHasAnyElement(memberStatusMap.keySet());
    }
}
