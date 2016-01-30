<%@page pageEncoding="UTF-8"%>

<%-- JSTL tag libraries --%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Struts tag libraries --%>
<%@taglib prefix="html" uri="http://dbflute.seasar.org/saflute-htmltag"%>  
<%@taglib prefix="bean" uri="http://struts.apache.org/tags-bean"%>  
<%@taglib prefix="tiles" uri="http://jakarta.apache.org/struts/tags-tiles"%>

<%-- SAStruts tag libraries --%>
<%@taglib prefix="s" uri="http://dbflute.seasar.org/saflute-stag" %>
<%@taglib prefix="f" uri="http://sastruts.seasar.org/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="viewPrefix" value="/WEB-INF/view" />
