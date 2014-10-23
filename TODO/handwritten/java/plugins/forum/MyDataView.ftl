<link href="myres/validation/css/validation.css" rel="stylesheet" type="text/css" />
<#-->script src="myres/validation/js/jquery-1.7.1.min.js"></script-->
<script src="myres/validation/js/jquery.validate.min.js"></script>

<#include "Header.ftl">

<#--begin your plugin-->
<DIV CLASS="page-header" STYLE="margin-bottom: 10px;">
	<#if model.isLoggedIn()>
		<H1>Je gegevens</H1>
	<#else>
		<H1>Aanmelden</H1>
	</#if>
</DIV>
<A HREF="molgenis.do?__target=ForumPlugin&__action=toPreviousScreen">terug</A>
	<SCRIPT>
		function formatDate(e) {
	        var y = e.date.getFullYear(),
	            _m = e.date.getMonth() + 1,
	            m = (_m > 9 ? _m : '0'+_m),
	            _d = e.date.getDate(),
	            d = (_d > 9 ? _d : '0'+_d);
			return d + '-' + m + '-' + y;
		}
	</SCRIPT>
	
	<#macro dateHelpText>
		Klik eenmaal op maand/jaar (bovenin) om snel van maand te kunnen wisselen. Klik nogmaals om snel van jaartal te kunnen wisselen.
	</#macro>
	
<BR>
<BR>
<DIV CLASS="row">
<DIV CLASS="span6 form-horizontal">
	<TABLE CLASS="table table-hover table-condensed borderless span6">
		<TR <@popup "Wat is je voornaam?">"Uiteraard houden we je naam geheim en tonen hem niet aan andere gebruikers."</@>><TD CLASS="span3">Voornaam (prive)</TD><TD><INPUT TYPE="text" NAME="firstName" <#if model.isLoggedIn()>VALUE="${model.getLmdUser().getFirstName()}"</#if>></TD></TR>
		<TR <@popup "Wat is je achternaam?">"Uiteraard houden we je naam geheim en tonen hem niet aan andere gebruikers."</@>><TD>Achternaam (prive)</TD><TD><INPUT TYPE="text" NAME="lastName" <#if model.isLoggedIn()>VALUE="${model.getLmdUser().getLastName()}"</#if>></TD></TR>
		<TR <@popup "Wat is je e-mailadres?">"Je hebt een e-mailadres nodig om je account te kunnen activeren."</@>><TD>E-mailadres (prive)</TD><TD><INPUT TYPE="text" NAME="email" <#if model.isLoggedIn()>VALUE="${model.getLmdUser().getEmail()}" DISABLED</#if>></TD></TR>
		<TR <@popup "Wat wil je als wachtwoord?">"Gebruik ten minste 4 tekens."</@>><TD>Wachtwoord (prive)</TD><TD><INPUT TYPE="password" NAME="password" <#if model.isLoggedIn()>VALUE="${model.getLmdUser().getPassword()}"</#if>></TD></TR>
		<TR <@popup "Wanneer ben je geboren?">"Wat juiste voeding is hangt af van je leeftijd. Daarom willen we graag weten wanneer je geboren bent. <@dateHelpText/>"</@>><TD>Geboortedatum</TD><TD><@datePicker "bornDate" "${model.getMyDataBornDate()}" "false" "false" true /></TD></TR>
		<TR><TD>Geslacht</TD><TD>
			<LABEL CLASS="checkbox inline">
	  			<INPUT TYPE="radio" id="inlineCheckbox1" value="male" name="gender" <#if model.isLoggedIn() && (model.isLoggedIn() && model.getLmdUser().getGender() = "male")> CHECKED </#if>> man
			</LABEL>
			<LABEL CLASS="checkbox inline">
			  <INPUT TYPE="radio" id="inlineCheckbox2" value="female" name="gender" <#if !model.isLoggedIn() || model.getLmdUser().getGender() = "female"> CHECKED </#if>> vrouw
			</LABEL>
		</TD></TR>
		<TR <@popup "Wat wil je als forumnaam?">"Deze naam verschijnt bij je berichten en is zichtbaar voor andere gebruikers."</@>><TD>Naam op forum</TD><TD><DIV CLASS="control-group" id="forumNameDiv"><input type="text" id="forumName" name="forumName" <#if model.isLoggedIn()>VALUE="${model.getLmdUser().getForumName()}"</#if>><label for="forumName" generated="true" class="error" style="display: none"><B>Deze naam bestaat al! Kies een andere naam</B></label></DIV></TD></TR>
	</TABLE>
	<H3>Heb je diabetes?</H3>
	<TABLE CLASS="table table-hover table-condensed borderless span6">
		<TR <@popup "Heb je diabetes?">"Wanneer werd er bij jou diabetes geconstateerd? Heb je geen diabetes? Dan kun je dit veld leeg laten. <@dateHelpText/>"</@>><TD CLASS="span3">Diabetes</TD><TD CLASS="span2"><@datePicker "diabetesDate" "${model.getMyDataDiabetesDate()}" "false" "false" true /></TD><TD><@removeDate "diabetesDate" /></TD></TR>
		<TR <@popup "Heb je een insulinepomp?">"Wanneer kreeg jij je eerste pomp? Heb je geen pomp? Dan kun je dit veld leeg laten. <@dateHelpText/>"</@>><TD>Eerste pomp</TD><TD><@datePicker "pumpDate" "${model.getMyDataPumpDate()}"  "false" "false" true /></TD><TD><@removeDate "pumpDate" /></I></TD></TR>
		<TR <@popup "Heb je een glucosesensor">"Wanneer kreeg jij je eerste glucosesensor? Heb je geen sensor? Dan kun je dit veld leeg laten. <@dateHelpText/>"</@>><TD>Eerste sensor</TD><TD><@datePicker "sensorDate" "${model.getMyDataSensorDate()}" "false" "false" true /></TD><TD><@removeDate "sensorDate" /></I></TD></TR>
		<TR><TD COLSPAN="2">
			<BR>
			<BUTTON TYPE="submit" ID="submitButton" NAME="submitButton" CLASS="btn btn-large btn-warning" DATA-TOGGLE="button" DATA-LOADING-TEXT=<#if model.isLoggedIn()>"Bezig met bijwerken..."<#else>"Bezig met e-mail versturen..."</#if> ONCLICK="bornDate.value=$('#bornDate').html();diabetesDate.value=$('#diabetesDate').html();pumpDate.value=$('#pumpDate').html();sensorDate.value=$('#sensorDate').html();__action.value='updateAccount';"><B><#if model.isLoggedIn()>Bijwerken<#else>Aanmelden</#if></B></BUTTON><BR>
			<#if !model.isLoggedIn()>
				<@gray>Wij sturen je een e-mail met een activatielink. Dit kan even duren.</@>
			</#if>
		</TD></TR>
	</TABLE>
</DIV>

<#include "RightColumn.ftl">
</DIV><#-- Close row -->

<#include "Footer.ftl">
<#--end of your plugin-->
<SCRIPT>
function testForumName() {
	var forumNames = ${model.getForumNameList()};
	if (-1 < $.inArray($('#forumName').val(), forumNames) && $('#forumName').val() != "${model.getForumName()}")
	{
		$("#forumName").closest('.control-group').removeClass('success').addClass('error');
		$("#forumNameDiv label").show();
		return false;
	} else {
		$("#forumName").closest('.control-group').removeClass('error').addClass('success');
		$("#forumNameDiv label").hide();
		return true;
	}
};


$(function() { 
	$(':submit').click(function(e) {
    	if (testForumName())
    	{
    		return true;
    	} else 
    	{
    		e.preventDefault();
    		return false;
    	}
	});
});

$("#forumName").blur( testForumName );


$(document).ready(function(){
	 $('#${model.name}').validate(
	 {
		rules: {
			firstName: {
	      		minlength: 2,
	      		required: true
	    	},
			lastName: {
	      		minlength: 2,
	      		required: true
	    	},
	    	email: {
	      		required: true,
	      		email: true
		    },
		    password: {
	    	  	minlength: 4,
	      		required: true
	    	},
	    	forumName: {
	      		minlength: 2,
	      		maxlength: 15,
	      		required: true
	    	}
	  	},
	  	messages: {
			firstName: {
				required: "Wat is uw voornaam? Deze blijft geheim en wordt niet op het forum getoond.",
          	  	minlength: "Vul ten minste twee tekens in"  
			},
			lastName: {
				required: "Wat is uw achternaam? Deze blijft geheim en wordt niet op het forum getoond.",
          	  	minlength: "Vul ten minste twee tekens in"  
			},
			email: {
				required: "Een geldig e-mailadres is nodig om uw account te activeren. Deze blijft geheim en wordt niet op het forum getoond." 
			},
			password: {
				required: "Een wachtwoord is vereist",
				minlength: "Vul ten minste tekens in"
			},
			forumName: {
				required: "Onder welke naam wilt u op het forum verschijnen?",
          	  	minlength: "Vul ten minste twee tekens in",
          	  	maxlength: "Mag niet meer dan 15 tekens zijn"
			}
		},
	  	highlight: function(element) {
	    	$(element).closest('.control-group').removeClass('success').addClass('error');
	  	},
	  	success: function(element) {
	    	element.text('OK!').addClass('valid').closest('.control-group').removeClass('error').addClass('success');
	  	},
	  	submitHandler: function(form) {
   			//this runs when the form validated successfully
   			$('#submitButton').button('loading');
    		form.submit(); //submit it the form
  		}
	});
}); // end document.ready

$(function ()
{ 	
	$('tr').popover({placement:"right", trigger:"hover"});
});

</SCRIPT>