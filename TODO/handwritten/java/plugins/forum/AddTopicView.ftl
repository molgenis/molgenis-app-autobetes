    <link href="myres/validation/css/validation.css" rel="stylesheet" type="text/css" />
	<script src="myres/validation/js/jquery-1.7.1.min.js"></script>
	<script src="myres/validation/js/jquery.validate.min.js"></script>
	
<#--begin your plugin-->
<#include "Header.ftl">
<DIV CLASS="row">
	<DIV CLASS="span8">
		<DIV CLASS="row">
			<DIV CLASS="span8">
				<DIV CLASS="page-header" STYLE="margin-bottom: 10px;">
		  			<H1>Forum <SMALL>over voeding en diabetes</SMALL></H1>
				</DIV>
      			<A HREF="molgenis.do?__target=ForumPlugin&__action=toPreviousScreen">terug</A><BR>
				<H2 CLASS="myOrange">Nieuw onderwerp</H2>
				<INPUT NAME="title" CLASS="input-xxlarge" TYPE="text" PLACEHOLDER="Titel" VALUE=""><BR>
				<TEXTAREA NAME="message" CLASS="input-xxlarge"  rows="10" PLACEHOLDER="Bericht..."></TEXTAREA><BR>
				<BUTTON ID="saveNewTopicButton" TYPE="submit" CLASS="btn btn-large btn-warning" DATA-TOGGLE="button" DATA-LOADING-TEXT="Bezig met opslaan..." ONCLICK="__action.value='saveNewTopic';return true;"><B>Opslaan</B></BUTTON>
			</DIV>
		</DIV>
	</DIV>
	<#include "RightColumn.ftl">
</DIV>
<SCRIPT>
$('#saveNewTopicButton').click(function() {
	$(this).button('loading');
});
</SCRIPT>
<#include "Footer.ftl">
<#--end of your plugin-->	
	<#-- ik denk dat dit wegkan omdat al infooter zit>/DIV>
</FORM-->

<SCRIPT>
$(document).ready(function(){

	 $('#${model.name}').validate(
	 {
		rules: {
			title: {
	      		required: true
	    	},
			message: {
	      		required: true
	    	}
	  	},
	  	
	  	messages: {
			title: {
				required: "Een titel is verplicht. Beschrijf kort wat u wilt bespreken."
			},
			message: {
				required: "Een bericht is verplicht. Bespreek het onderwerp in meer detail of geef uw mening over dit onderwerp."
			}
		},
	  	highlight: function(element) {
	    	$(element).closest('.control-group').removeClass('success').addClass('error');
	  	},
	  	success: function(element) {
	    	element.text('OK!').addClass('valid').closest('.control-group').removeClass('error').addClass('success');
	  	}
	});
}); // end document.ready
</SCRIPT>