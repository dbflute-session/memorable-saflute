<c:import url="${viewPrefix}/common/header.jsp">
	<c:param name="title" value="ログイン"/>
</c:import>
<div class="contents">
	<h2>ログイン</h2>
	<html:errors/>
	<s:form>
		<bean:message key="labels.emailOrAccount"/>
		<html:text property="email" placeholder="messages.input.note.emailOrAccount"/>
		<bean:message key="labels.password"/>
		<html:password property="password" placeholder="messages.input.note.password"/>
		<html:checkbox property="autoLogin"/>Auto Login
		<s:submit property="doLogin" value="ログイン"/>
	</s:form>
</div>
<c:import url="${viewPrefix}/common/footer.jsp"/>