<#include "Header.ftl">
<input type="hidden" name="foodType" value="dish">
<DIV CLASS="row">
<DIV CLASS="span8">

	<DIV CLASS="page-header">
		<H1>Je voeding <SMALL>kan je gezond slank maken</SMALL></H1>
	</DIV>

	<UL CLASS="nav nav-pills">
	  <LI><A HREF="molgenis.do?__target=ForumPlugin&__action=toFoodView">Mijn voeding</A></LI>
	  <LI CLASS="active"><A HREF="molgenis.do?__target=ForumPlugin&__action=toDishView&tab=overview">Mijn gerechten</A></LI>
	</UL>

	<#if model.isScreenTabDishOverview()>
		<#include "DishViewTabOverview.ftl">
	<#elseif model.isScreenTabDishEdit()>
		<#include model.getScreenDishTabEdit()>
	</#if>


</DIV>
<#include "RightColumn.ftl">
</DIV><#-- Close row -->

<#include "Footer.ftl">
