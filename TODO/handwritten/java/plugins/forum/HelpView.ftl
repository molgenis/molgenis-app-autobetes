<#include "Header.ftl">
<DIV CLASS="row">
<DIV CLASS="span8">
	<DIV CLASS="page-header">
		<H1>Help <SMALL>veel gestelde vragen</SMALL></H1>
	</DIV>
	
	<@gray><P ALIGN="justify">
	Mocht je vraag hier niet tussen staan, <A HREF="#" ONCLICK="$('#contact').modal('show')">laat het ons dan weten</A>.
	</P></@>

	<DIV CLASS="accordion" ID="acc">
		<#list model.getHelpItems() as item>
			<@helpAccordionGroup "${item.getKey()}" "${item.getQuestion()}" "${model.getScreenHelpTab()}" = "${item.getKey()}" >${item.getAnswer()}</@>
		</#list>
      	<#-->@helpAccordionGroup "collapse3" "titel">uitleg</@-->
	</DIV>
	<A HREF="molgenis.do?__target=ForumPlugin&__action=toPreviousScreen">terug</A><BR>  

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