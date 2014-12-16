<#include "Header.ftl">
<DIV CLASS="row">
<DIV CLASS="span8">
	<DIV CLASS="row">
	<DIV CLASS="span8">
		<DIV CLASS="page-header">
  			<H1>Forum <SMALL>over voeding en diabetes</SMALL></H1>
		</DIV>
		<BUTTON TYPE="submit" CLASS="btn btn-large btn-info" <#if model.isLoggedIn()>ONCLICK="__action.value='toAddTopicView';return true;" <#else> ONCLICK="return false;" <@blockingPopup/>  </#if>><B>Begin een nieuw onderwerp</B></BUTTON><BR>
		<BR>
		<TABLE CLASS="table fixedColumnSize table-hover table-striped">
			<THEAD>
			<TR CLASS="myBlack">
				<TH STYLE="width: 55%">ONDERWERP</TH>
				<TH STYLE="width: 20%">GESTART DOOR</TH>
				<TH STYLE="width: 25%">LAATSTE REACTIE</TH>
			</TR>
			</THEAD>
			<TBODY data-provides="rowlink">
				<#list model.getTopicList() as topic>
				<TR data-toggle="popover" title="Laatste reactie" data-content="${model.getLastMessage(topic.getId())}">
					<TD><A HREF="molgenis.do?__target=ForumPlugin&__action=toMessageView&topicId=${topic.getId()}" >${topic.getTitle()} <SPAN CLASS="badge badge-warning">${topic.getNPosts()} reacties</SPAN></A></TD>
					<TD><SPAN CLASS="label label-info"><I CLASS="icon-user icon-white"></I> ${topic.getLmdUser_ForumName()}</SPAN></TD>
					<TD><@graySmall>${model.getLastReplyAuthorDate(topic.getId())}</@></TD>
				</TR>
				</#list>
			</TBODY>
		</TABLE>
	</DIV>
	</DIV>
</DIV>

<#include "RightColumn.ftl">
</DIV><#-- Close row -->

<#include "Footer.ftl">
<SCRIPT>  
$(function ()
	{ 	$('a').popover();
		$('button').popover({trigger:"hover"});
		$('tr').popover({placement:"right", trigger:"hover"});
	});
</SCRIPT>