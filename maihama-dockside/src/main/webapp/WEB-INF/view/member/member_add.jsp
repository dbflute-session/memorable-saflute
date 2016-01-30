<c:import url="${viewPrefix}/common/header.jsp">
	<c:param name="title" value="会員追加"/>
</c:import>
<div class="contents">
	<h2>会員追加</h2>
	<s:form>
		<html:errors/>
		<table>
			<tr>
				<td>会員名</td>
				<td><html:text property="memberName" /></td>
			</tr>
			<tr>
				<td>誕生日</td>
				<td><html:text property="birthdate" /></td>
			</tr>
			<tr>
				<td>会員ステータス</td>
				<td>
					<html:select property="memberStatusCode">
						<c:forEach var="status" items="${memberStatusMap}">
							<html:option value="${f:h(status.key)}">${f:h(status.value)}</html:option>
						</c:forEach>
					</html:select>
				</td>
			</tr>
			<tr>
				<td>会員アカウント</td>
				<td><html:text property="memberAccount" /></td>
			</tr>
		</table>
		<s:submit property="doAdd" value="登録" />
	</s:form>
	<div class="listback">
		<s:link href="/member/list/">会員一覧へ</s:link>
	</div>
</div>
<c:import url="${viewPrefix}/common/footer.jsp"/>
