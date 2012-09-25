<%--
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--%>
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.ui.ConflictStatisticsInfo" %>
<%@ page import="org.unitime.timetable.model.Solution" %>
<%@ page import="org.unitime.timetable.model.dao.SolutionDAO" %>
<%@ page import="org.unitime.timetable.form.CbsForm" %>
<%@ page import="org.unitime.commons.Debug" %>
<%@ page import="org.unitime.timetable.model.TimetableManager" %>
<%@ page import="org.unitime.commons.User" %>
<%@ page import="org.unitime.timetable.model.Session" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.model.UserData" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/cbs">
<%
try {
	ConflictStatisticsInfo.printHtmlHeader(out);
%>
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>Mode:</TD>
			<TD>
				<html:select property="type">
					<html:options name="cbsForm" property="types"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Limit:</TD>
			<TD>
				<html:text property="limit" size="5" maxlength="5"/> %
				&nbsp;<html:errors property="limit"/>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" value="Change"/>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
<%
	ConflictStatisticsInfo cbs = null;
	SolverProxy solver = WebSolver.getSolver(session);
	if (solver!=null) {
		//if (!solver.isWorking())
		cbs = solver.getCbsInfo();
	} else {
		String solutionIdsStr = (String)session.getAttribute("Solver.selectedSolutionId");
		if (solutionIdsStr!=null) {
			for (StringTokenizer s = new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
				Long solutionId = Long.valueOf(s.nextToken());
				Solution solution = (new SolutionDAO()).get(solutionId);
				if (solution==null) continue;
				ConflictStatisticsInfo x = (ConflictStatisticsInfo)solution.getInfo("CBSInfo");
				if (x!=null) {
					if (cbs==null) cbs = x;
					else cbs.merge(x);
				}
			}
		}
	}
	if (cbs!=null) {
		double limit = UserData.getPropertyDouble(session, "Cbs.limit", CbsForm.sDefaultLimit);
		int type = UserData.getPropertyInt(session, "Cbs.type", CbsForm.sDefaultType);
		User user = Web.getUser(request.getSession());
		TimetableManager manager = (user==null?null:TimetableManager.getManager(user)); 
		Session acadSession = (user==null?null:Session.getCurrentAcadSession(user));
		boolean clickable = manager.canSeeTimetable(acadSession, user);
		
%>
	<font size='2'>
<%
	cbs.printHtml(out, limit/100.0, type, clickable);
%>
	</font>
	<table border='0' width='90%'><tr><td>
		<tt:displayPrefLevelLegend/>
	</td></tr></table>
<%
	} else {
%>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">	
				<i>Conflict-based statistics is not available at the moment.</i>
			</TD>
		</TR>
	</TABLE>
<%
	}
} catch (Exception e) {
	Debug.error(e);
%>		
		<font color='red'><B>ERROR:<%=e.getMessage()%></B></font>
<%
}
%>
</html:form>