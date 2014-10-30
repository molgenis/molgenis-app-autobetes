<DIV ID="loginPanel" CLASS="collapse">
	<DIV CLASS="control-group pull-right">
		<INPUT NAME="email" TYPE="email" CLASS="input" PLACEHOLDER="E-mailadres">
	</DIV>
	<DIV CLASS="control-group pull-right">
		<INPUT NAME="password" ID="password" TYPE="password" CLASS="input" PLACEHOLDER="Wachtwoord"><BR>
	</DIV>
	<DIV CLASS="control-group pull-right">
		<BUTTON ID="login" CLASS="btn btn-info pull-right" DATA-TOGGLE="button" DATA-LOADING-TEXT="Bezig met inloggen..." ONCLICK="__action.value='login';$('#login').button('loading');$('#loginHidden').click();"><B>Log in</B></BUTTON>
		<BUTTON ID="loginHidden" TYPE="submit" CLASS="btn hidden" ONCLICK="return true;">hidden submit</BUTTON>
	</DIV>
	<DIV CLASS="control-group pull-right">
		<P CLASS="myGray" ALIGN="justify">				
			Nog geen account?
			<A HREF="molgenis.do?__target=ForumPlugin&amp;__action=toMyDataView">Aanmelden</A> is gratis en eenvoudig.<BR>
			<BR>				
			Wachtwoord vergeten? Wij kunnen het je <BUTTON ID="resendButton" TYPE="submit" CLASS="btn btn-link" DATA-TOGGLE="button" DATA-LOADING-TEXT="<B><SPAN CLASS='myOrange'>Bezig met toesturen...</SPAN></B>" ONCLICK="__action.value='resendPassword';return true;" STYLE="padding: 0px;">toesturen</BUTTON>. Vul eerst je emailadres hierboven in.
		</P>
	</DIV>
</DIV>