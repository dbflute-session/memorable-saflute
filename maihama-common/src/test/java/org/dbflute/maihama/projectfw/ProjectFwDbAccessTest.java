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
package org.dbflute.maihama.projectfw;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.maihama.dbflute.exbhv.MemberBhv;
import org.dbflute.maihama.dbflute.exentity.Member;
import org.dbflute.maihama.unit.UnitCommonPjContainerTestCase;

/**
 * @author jflute
 */
public class ProjectFwDbAccessTest extends UnitCommonPjContainerTestCase {

    protected MemberBhv memberBhv;

    public void test_dbaccess_Tx() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            log(member);
        }
    }
}
