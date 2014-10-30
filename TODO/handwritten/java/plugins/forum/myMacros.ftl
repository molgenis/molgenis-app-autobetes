<#macro gray>
	<FONT CLASS="myGray"><#nested></FONT>
</#macro>
<#macro graySmall>
	<SMALL><FONT CLASS="myGray"><#nested></FONT></SMALL>
</#macro>
<#macro orange>
	<FONT CLASS="myOrange"><#nested></FONT>
</#macro>
<#macro orangeSmall>
	<SMALL><FONT CLASS="myOrange"><#nested></FONT></SMALL>
</#macro>
<#macro blue>
	<FONT CLASS="myBlue"><#nested></FONT>
</#macro>

<#macro popup title>
	data-toggle="popover" title="${title}" data-content=<#nested>
</#macro>

<#macro blockingPopup>
	data-toggle="popover" title="Log eerst in!" data-content="Om iets te kunnen plaatsen moet je eerst aangemeld en ingelogd zijn."
</#macro>

<#macro box number title fgcolor bgcolorClass>
	<TABLE>
	<TR><TD CLASS="${bgcolorClass}" STYLE="text-align: center; vertical-align: middle; width: 300px; height: 150px;">
	<SPAN ID="boxTitle${number}" STYLE="color: ${fgcolor};">
	<H2>${title}</H2>
	</SPAN>
	</TD></TR>
	</TABLE>
	<SCRIPT>
		$("#box${number}").hover(function (){
		        $("#boxTitle${number}").attr("style", "color:white");
		    },function(){
		        $("#boxTitle${number}").attr("style", "color:lightgray");
		    }
		);
	</SCRIPT>
</#macro>

<#macro helpAccordionGroup href title show>
		<DIV CLASS="accordion-group">
			<DIV CLASS="accordion-heading">
				<A CLASS="accordion-toggle<#if !show> collapsed</#if>" DATA-TOGGLE="collapse" DATA-PARENT="#acc" HREF="#${href}">
        			${title}
      			</A>
      		</DIV>
			<DIV ID="${href}" CLASS="accordion-body<#if show> in</#if> collapse">
				<DIV CLASS="accordion-inner myGray">
					<P ALIGN="justify"><#nested></P>
				</DIV>
			</DIV>
      	</DIV>
</#macro>

<#macro toTabLink prevName prevHref nextName nextHref>
	<DIV>
		<P STYLE="float: left;">
			<#if 0 < prevHref?length><A HREF="${prevHref}">&larr; ${prevName}</A></#if>
		</P>
		<P STYLE="float: right">
			<#if 0 < nextHref?length><A HREF="${nextHref}">${nextName} &rarr;</A></#if>
		</P>
		<DIV STYLE="clear: both;"></DIV>
	</DIV>
</#macro>

<#macro datePicker id date todayHighlight todayBtn withScript>
	<A HREF="#" CLASS="btn small" ID="${id}DP" DATA-DATE-FORMAT="dd-mm-yyyy" <#if 0 < date?length>DATA-DATE="${date}"</#if> DATA-DATE-LANGUAGE="nl" DATA-TODAYHIGHLIGHT="${todayHighlight}" DATA-DATE-TODAY-BTN="${todayBtn}" DATA-DATE-AUTOCLOSE="true">
		<DIV ID="${id}" NAME="${id}"><SPAN CLASS="add-on"><I CLASS="icon-th"></I></SPAN></DIV>
	</A>

	<#if withScript> 
		<SCRIPT>
			$(function (){
				$('#${id}DP').datepicker();
				$('#${id}DP').datepicker().on('changeDate', function(e){
					$('#${id}').html(formatDate(e));
				});
			});
		</SCRIPT>
		<input type="hidden" name="${id}">
	</#if>
	<SCRIPT>
		<#if 0 < date?length>$('#${id}').html('${date}');</#if>
	</SCRIPT>
</#macro>

<#macro removeDate id>
	<A HREF="#" ID="${id}Remove"><I CLASS="icon-remove"></I></A>
	<SCRIPT>
		$('#${id}Remove').click(function() {
			$('#${id}').html('<SPAN CLASS="add-on"><I CLASS="icon-th"></I></SPAN>');
		});
	</SCRIPT>
</#macro>

<#include "myMacros_FoodInput.ftl">