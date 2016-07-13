<!DOCTYPE html>
<html>
<head>
	<title>${param.title}</title>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/main.css"/>
</head>
<body>
<!-- header start -->
<header>
	<div>
		<hgroup>
			<h1><a href="${ctx}/">${ProjectName} (SAFlute Example)</a></h1>
		</hgroup>
		<c:choose>
			<c:when test="${userWebBean.isLogin}"><p class="nameHeader">こんにちは、${f:h(userWebBean.memberName)}さん</p></c:when>
			<c:otherwise><p class="nameHeader">こんにちは、ゲストさん</p></c:otherwise>
		</c:choose>
	</div>
</header>
<!-- header end -->
