<c:import url="${viewPrefix}/common/header.jsp">
	<c:param name="title" value="会員一覧"/>
</c:import>
<div class="contents">
	<h2>会員一覧</h2>
	<div class="condition">
		<s:form>
			<html:errors/>
			<table>
				<tr>
					<td><label>会員名</label></td>
					<td><html:text property="memberName"/></td>
				</tr>
				<tr>
					<td><label>購入商品名</label></td>
					<td><html:text property="purchaseProductName"/></td>
				</tr>
				<tr>
					<td><label>会員ステータス</label></td>
					<td>
						<html:select property="memberStatus">
							<c:forEach var="status" items="${memberStatusMap}">
								<html:option value="${f:h(status.key)}">${f:h(status.value)}</html:option>
							</c:forEach>
						</html:select>
					</td>
				</tr>
				<tr>
					<td><label>未払いあり</label></td>
					<td><html:checkbox property="unpaid"/></td>
				</tr>
				<tr>
					<td><label>正式会員日</label></td>
					<td><html:text property="formalizedDateFrom" size="14"/>～
					<html:text property="formalizedDateTo" size="14"/></td>
				</tr>
			</table>
			<input type="submit" name="doSearch" value="検索"></input>
		</s:form>
	</div>
	<hr />
	<s:link href="/member/add/">新規登録</s:link>
	<table>
		<thead>
			<tr>
				<th>会員ID</th>
				<th>会員名</th>
				<th>会員ステータス</th>
				<th>正式会員日</th>
				<th>会員更新日時</th>
				<th>編集</th>
				<th>購入履歴</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="bean" items="${beanList}">
			<tr>
				<td>${f:h(bean.memberId)}</td>
				<td>${f:h(bean.memberName)}</td>
				<td>${f:h(bean.memberStatusName)}</td>
				<td>${f:h(bean.formalizedDate)}</td>
				<td>${f:h(bean.updateDatetime)}</td>
				<td>
					<c:if test="${!bean.withdrawalMember}">
						<s:link href="/member/edit/${f:h(bean.memberId)}/">編集</s:link>
					</c:if>
					<c:if test="${bean.withdrawalMember}">編集不可</c:if>
				</td>
				<td>
					<c:if test="${bean.purchaseCount > 0}">
						<s:link href="/member/purchase/list/${f:h(bean.memberId)}/1/">購入履歴</s:link>
					</c:if>
				</td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
	<c:import url="${viewPrefix}/common/paging_navi.jsp"/>
</div>
<c:import url="${viewPrefix}/common/footer.jsp"/>
