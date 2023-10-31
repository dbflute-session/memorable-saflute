<c:import url="${viewPrefix}/common/header.jsp">
	<c:param name="title" value="ファイルアップロード画面"/>
</c:import>
<div class="contents">
	<h2><s:label key="labels.member.list"/></h2>
	<div class="condition">
		<s:form enctype="multipart/form-data">
			<html:errors/>
			<table>
				<tr>
					<td><label>sea項目</label></td>
					<td><html:text property="sea"/></td>
				</tr>
				<tr>
					<td><label>land項目</label></td>
					<td><html:text property="land"/></td>
				</tr>
				<tr>
					<td><label>file項目</label></td>
					<td><input type="file" name="uploadedFile" /></td>
				</tr>
			</table>
			<s:submit property="doUpload" value="アップロード"/>
		</s:form>
	</div>
</div>
<c:import url="${viewPrefix}/common/footer.jsp"/>
