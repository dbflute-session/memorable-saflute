<c:import url="${viewPrefix}/common/header.jsp">
	<c:param name="title" value="会員購入一覧"/>
</c:import>
<div class="contents">
	<h2>会員購入一覧</h2>
	<html:errors/>
	<div class="condition">
		<table>
			<tr>
				<td>会員ID</td>
				<td>${f:h(headerBean.memberId)}</td>
			</tr>
			<tr>
				<td>会員名</td>
				<td>${f:h(headerBean.memberName)}</td>
			</tr>
		</table>
	</div>
	<table>
		<thead>
			<tr>
				<th>購入日時</th>
				<th>商品名</th>
				<th>商品価格</th>
				<th>商品数量</th>
				<th>支払状態</th>
				<th>&nbsp;</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="bean" varStatus="s" items="${beanList}">
				<c:if test="${s.index % 2 == 0}"><tr id="row" style="background-color: #AACCEE;"></c:if>
				<c:if test="${s.index % 2 != 0}"><tr id="row" style="background-color: #EECCAA;"></c:if>
					<td><fmt:formatDate value="${bean.purchaseDatetime}" pattern="yyyy/MM/dd HH:mm:ss"/></td>
					<td>${f:h(bean.productName)}</td>
					<td><fmt:formatNumber value="${f:h(bean.purchasePrice)}" pattern="#,###"/></td>
					<td>${f:h(bean.purchaseCount)}</td>
					<td>
						<c:if test="${bean.paymentComplete}">支払済</c:if>
						<c:if test="!${bean.paymentComplete}">未払い</c:if>
					</td>
					<td>
						<s:form>
							<html:hidden property="memberId" value="${headerBean.memberId}"/>
							<html:hidden property="purchaseId" value="${bean.purchaseId}"/>
							<s:submit property="doDelete" value="削除"/>
						</s:form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<c:import url="${viewPrefix}/common/paging_navi.jsp"/>
	<div class="listback">
		<s:link href="/member/list/">会員一覧へ</s:link>
	</div>
</div>
<c:import url="${viewPrefix}/common/footer.jsp"/>