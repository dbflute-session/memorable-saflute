<c:import url="${viewPrefix}/common/header.jsp">
	<c:param name="title" value="会員編集"/>
</c:import>
<div class="contents">
	<h2>会員編集</h2>
	<s:form>
		<html:errors/>
		<table>
			<tr>
				<td>会員名</td>
				<td><html:text property="memberName"/></td>
			</tr>
			<tr>
				<td>誕生日</td>
				<td><html:text property="birthdate"/></td>
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
				<td><html:text property="memberAccount"/></td>
			</tr>
			<tr>
				<td>正式会員日</td>
				<td>${f:h(formalizedDate)}</td>
			</tr>
			<tr>
				<td>最終ログイン日時</td>
				<td>${f:h(latestLoginDatetime)}</td>
			</tr>
			<tr>
				<td>更新日時</td>
				<td>${f:h(updateDatetime)}</td>
			</tr>
		</table>
		<html:hidden property="memberId"/>
		<html:hidden property="formalizedDate"/>
		<html:hidden property="latestLoginDatetime"/>
		<html:hidden property="updateDatetime"/>
		<html:hidden property="previousStatusCode"/>
		<html:hidden property="versionNo"/>
		<s:submit property="doUpdate" value="更新"/>
		<s:submit property="doDelete" value="強制退会"/>
	</s:form>
	<div class="listback">
		<s:link href="/member/list/">会員一覧へ</s:link>
	</div>
</div>
<c:import url="${viewPrefix}/common/footer.jsp"/>
