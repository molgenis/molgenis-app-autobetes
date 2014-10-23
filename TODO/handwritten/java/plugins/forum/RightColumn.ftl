<DIV CLASS="span3 offset1">
	<DIV CLASS="row">
		<#include "Alert.ftl">
	</DIV>
	<#if model.isLoggedIn() || model.isMyDataView()>
	<#else>
	
		<#if model.anyConsumedProduct()>
			<DIV CLASS="row">
				<DIV CLASS="alert alert-block fade in">
				        <button type="button" class="close" data-dismiss="alert">x</button>
				        <strong>Je bent niet ingelogd!</strong>
						Als je inlogt dan slaan we je voeding op en geven we je beter advies.
						Aanmelden kan eenvoudig en snel.
				</DIV>
			</DIV>
		</#if>
	
		<DIV CLASS="row">
			<BR/>
			<#include "LoginPanel.ftl">
		</DIV>

		<SCRIPT>
			$('#login').click(function() {
				$(this).button('loading');
			});
			$('#resendButton').click(function() {
				$(this).button('loading');
			});
			document.getElementById('password').onkeypress = function(e)
			{
			    if (e.keyCode == 13)
			    {
			        document.getElementById('login').click();
			    }
			}
		</SCRIPT>
	</#if>
	<#if model.isMyDataView()>
		<DIV CLASS="row">
		<SPAN>
			<A HREF="http://nl.gravatar.com/" TARGET="_blank" STYLE="text-decoration: none;">
				<#--TODO: replace img with myres/img/User-blue-icon.png-->
				<IMG SRC="http://www.gravatar.com/avatar/${model.getMD5HashCurrentLmdUser()}?s=80&d=http%3A%2F%2Ficons.iconarchive.com%2Ficons%2Fcustom-icon-design%2Fpretty-office-8%2F256%2FUser-blue-icon.png" STYLE="width:80px"/>
			</A><BR>
			<P><@gray>Klik <A HREF="http://nl.gravatar.com/" TARGET="_blank">hier</A> om een (andere) afbeelding aan je e-mailadres te koppelen.</@></P>
		</SPAN>
		</DIV>
	<#else>	
		<DIV CLASS="row">
			<BR><BR><BR>
			<DIV ID="lmd-img">
				<#if model.isDiabetesView()>
					<IMG SRC="myres/img/lmd_logo_text_250px.png" STYLE="float:right" WIDTH="250">
				</#if>
			</DIV>
		</DIV>
	</#if>
</DIV>