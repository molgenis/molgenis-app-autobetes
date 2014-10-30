<DIV id="thanks" CLASS="modal hide fade">
	<DIV CLASS="modal-header">
		<H3 CLASS="myOrange">Hartelijk dank</H3>
	</DIV>
	<DIV CLASS="modal-body">
		<BR>
		Je feedback is verstuurd. Als je feedback daartoe aanleiding geeft en je je e-mailadres achtergelaten hebt dan kun je binnenkort een reactie verwachten.<BR>
	</DIV>	
</DIV>

<DIV id="contact" CLASS="modal hide fade">
	<FORM METHOD="post" ENCTYPE="multipart/form-data" NAME="FeedbackForm">
		<input type="hidden" name="__target" value="ForumPlugin">
		<!--needed in every form: to define the action. This can be set by the submit button-->
		<input type="hidden" name="__action" value="feedback">

		<DIV CLASS="modal-header">
			<BUTTON CLASS="close" data-dismiss="modal">&times;</BUTTON>
			<H3 CLASS="myOrange">Feedback en contact</H3>
		</DIV>
		<DIV CLASS="modal-body">
			<BR>
			<DIV CLASS="row">
				<DIV CLASS="span1 orangeRightBold">Onderwerp</DIV>
				<SELECT NAME="subject" CLASS="span4" STYLE="margin-left:30px">
					<OPTION>Compliment</OPTION>
					<OPTION>Suggestie</OPTION>
					<OPTION>Fout</OPTION>
					<OPTION>Anders...</OPTION>
				</SELECT>
			</DIV>
			<DIV CLASS="row">
				<DIV CLASS="span1 orangeRightBold">Afzender</DIV>
				<INPUT ID="email" NAME="email" CLASS="span4" STYLE="margin-left:30px" TYPE="text" PLACEHOLDER=<#if model.isLoggedIn()>"${model.getLmdUser().getEmail()}"<#else>"E-mail"</#if>>
			</DIV>
			<DIV CLASS="row">
				<DIV CLASS="span1 orangeRightBold">Je bericht</DIV>
				<TEXTAREA ID="message" NAME="message" CLASS="span4" STYLE="margin-left:30px" ROWS="3" PLACEHOLDER="Je bericht..."></TEXTAREA>
			</DIV>
	
		<#if !model.isLoggedIn()>
			<DIV CLASS="row">
				<DIV CLASS="span1 orangeRightBold">Controle</DIV>
				<INPUT NAME="verificationCode" ID="verificationCode" CLASS="span4" STYLE="margin-left:30px" TYPE="text" PLACEHOLDER="Hoeveel is 5 + 3? Bewijs dat je geen robot bent.">
			</DIV>
		</#if>
		</DIV>
		<DIV CLASS="modal-footer">
			<#-->BUTTON TYPE="submit" CLASS="btn btn-primary" DATA-DISMISS="modal" ONCLICK="document.forms.About.subject.value=$('#feedbackMessage').val();document.forms.About.__target.value='About';document.forms.About.__action.value='feedback';document.forms.About.submit();">Verstuur</BUTTON-->
			<BUTTON ID="feedbackSubmitButton" TYPE="submit" CLASS="btn btn-large btn-info" ONCLICK="$('#contact').modal('hide');$('#thanks').modal('show');">Verstuur</BUTTON>
			<BUTTON CLASS="btn" DATA-DISMISS="modal">Cancel</BUTTON>
		</DIV>
	</FORM>
</DIV>

<SCRIPT>
$('#email').keyup(function (e) {
	if (e.keyCode == '13' || e.which == '13') {
		$('#feedbackSubmitButton').click();
	};
});
$('#verificationCode').keyup(function (e) {
	if (e.keyCode == '13' || e.which == '13') {
		$('#feedbackSubmitButton').click();
	};
});
</SCRIPT>

<A DATA-TOGGLE="modal" ID="feedback" HREF="#contact">Feedback</A>
