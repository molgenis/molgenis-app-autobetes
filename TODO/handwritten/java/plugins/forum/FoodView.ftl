<#include "Header.ftl">
<input type="hidden" name="foodType" value="consumption">
<DIV CLASS="row">
<DIV CLASS="span8">

	<DIV CLASS="page-header">
		<H1>Je voeding <SMALL>kan je gezond slank maken</SMALL></H1>
	</DIV>

	<UL CLASS="nav nav-pills">
	  <LI CLASS="active"><A HREF="#">Mijn voeding</A></LI>
	  <LI><A HREF="molgenis.do?__target=ForumPlugin&__action=toDishView&tab=overview">Mijn gerechten</A></LI>
	</UL>
	
	<#macro step number href bool>
		<A HREF="${href}"<#if bool> CLASS="current"</#if>>
			<SPAN CLASS="badge<#if bool> badge-warning</#if>">${number}</SPAN> <B><#nested></B>
		</A>
	</#macro>
	
	<DIV CLASS="wizard" STYLE="text-align:center">
	    <@step 1 "molgenis.do?__target=ForumPlugin&__action=toFoodTabFood" model.isScreenTabFoodFood()>Dagboek</@>
	    <@step 2 "molgenis.do?__target=ForumPlugin&__action=toFoodTabVitamin" model.isScreenTabFoodVitamin()>Overzicht</@>
	    <@step 3 "molgenis.do?__target=ForumPlugin&__action=toFoodTabAdvice" model.isScreenTabFoodAdvice()>Advies</@>
	</DIV>
	<#if model.isScreenTabFoodFood()>
		<#include "FoodViewTabFood.ftl">
	<#elseif model.isScreenTabFoodVitamin()>
		<#include "FoodViewTabVitamins.ftl">
	<#elseif model.isScreenTabFoodAdvice()>
		<#include "FoodViewTabAdvice.ftl">
	</#if>

</DIV>
<#--SCRIPT>
$('#tab1 a').click(function (e) {
  $(this).tab('show');
})
$('#tab2 a').click(function (e) {
	$(this).tab('show');
})

<#if model.isScreenTabFoodFood()>$('#myTab a[href="#tab1"]').tab('show');</#if>
<#if model.isScreenTabFoodVitamin()>$('#myTab a[href="#tab2"]').tab('show');</#if>
</SCRIPT-->	

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
