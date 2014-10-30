<#--begin your plugin-->
<#include "Header.ftl">
<INPUT TYPE="hidden" NAME="postId">
<DIV CLASS="row">
<DIV CLASS="span8">
	<DIV CLASS="row">
	<DIV CLASS="span8">
		<DIV CLASS="page-header" STYLE="margin-bottom: 10px;">
  			<H1>Forum <SMALL>over voeding en diabetes</SMALL></H1>
		</DIV>
		<A HREF="molgenis.do?__target=ForumPlugin&__action=toPreviousScreen">terug</A>
		<H2 CLASS="myOrange">${model.getCurrentTopic().getTitle()} <SMALL>(huidige onderwerp)</SMALL></H2>
		<TABLE CLASS="table fixedColumnSize table-condensed" FRAME="vsides" bordercolor="#EEEEEE" >
			<THEAD>
				<TR>
				  <TH STYLE="width: 17%"></TH>
				  <TH STYLE="width: 68%"></TH>
				  <TH STYLE="width: 15%"></TH>
				</TR>
			</THEAD>
			<TBODY>
				<#list model.getPostList() as post>
				<TR STYLE="background-color:#F8F8F8">
					<TD STYLE="vertical-align: bottom; padding-top:0px; padding-bottom:0px;"><A NAME="${post.getId()}"></A><SPAN CLASS="label label-<#if model.isMyPost(post.getId())>warning<#else>info</#if>"><I CLASS="icon-user icon-white"></I> ${post.getLmdUser_ForumName()}</SPAN></TD>
					<TD STYLE="vertical-align: bottom; padding-top:0px; padding-bottom:0px;">
						<FONT CLASS="myOrange"><I><B>${model.niceMessageDate(post.getCreated())}</B></I></FONT>
					</TD>
					<TD STYLE="padding-top:0px;padding-bottom:0px;">
  						<DIV STYLE="float:right;">
  							<#if model.isMyPost(post.getId())><BUTTON TYPE="submit" <@popup "Bewerken">"Klik hier als je dit bericht wilt bewerken"</@>CLASS="btn btn-link" STYLE="padding-top:0px;padding-bottom:0px;" ONCLICK="__action.value='editPost';postId.value='${post.getId()}';return true;"> <IMG SRC="myres/img/edit.png" WIDTH="20px" /></BUTTON></#if>
  							<BUTTON TYPE="submit" CLASS="btn btn-link" STYLE="padding-top:0px;padding-bottom:0px;" <#if model.isLoggedIn()>ONCLICK="__action.value='citePost';postId.value='${post.getId()}';return true;" <@popup "Citeren">"Klik hier als je dit bericht wilt citeren"</@> <#else> ONCLICK="return false;" <@blockingPopup/> </#if>> <IMG SRC="myres/img/quote.png" WIDTH="25px" /></BUTTON>
  						</DIV>
					</TD>
				</TR>
				<TR>
				<TD>
				<IMG SRC="http://www.gravatar.com/avatar/${model.getEmailOfPost(post.getId())}?s=80&d=http%3A%2F%2Ficons.iconarchive.com%2Ficons%2Fcustom-icon-design%2Fpretty-office-8%2F256%2FUser-blue-icon.png" STYLE="width:80px;"/>
				<SMALL>
				<@gray>
				<TABLE CLASS="table table-condensed borderless">
					<TR><TD STYLE="padding: 0px;"><@gray>Sensor</@></TD><TD STYLE="padding: 0px;">${model.getSensorDate(post.getId())}</TD></TR>
					<TR><TD STYLE="padding: 0px;"><@gray>Pomp</@></TD><TD STYLE="padding: 0px;">${model.getPumpDate(post.getId())}</TD></TR>
					<TR><TD STYLE="padding: 0px;"><@gray>Diabetes</@></TD><TD STYLE="padding: 0px;">${model.getDiabetesDate(post.getId())}</TD></TR>
					<TR><TD STYLE="padding: 0px;"><@gray>Geboren</@></TD><TD STYLE="padding: 0px;">${model.getBornDate(post.getId())}</TD></TR>
					<#--if model.isMyPost(post.getId())><TR><TD  STYLE="padding: 0px;" COLSPAN=2><@gray>(<A HREF="molgenis.do?__target=ForumPlugin&__action=toMyDataView">wijzigen</A>)</@></TD></TR></#if-->
				</TABLE>
				</@>
				</SMALL>
				</TD>
				<TD COLSPAN="2">
					${post.getMessage()}
					<BR>
					<BR>
					<#if post.getChangePercentage()??>
						<SMALL><@gray>
							<I>${model.getChangeAuthorOfPost(post.getId())} wijzigde dit bericht op ${model.getChangeDateOfPost(post.getId())} met ${post.getChangePercentage()}%: ${post.getChangeReason()}</I>
						</@></SMALL>
					</#if>
				</TD>
				</TR>
				</#list>
				<TR STYLE="background-color:#EEEEEE">
					<TD STYLE="vertical-align: bottom; padding-bottom: 0px;"><SPAN CLASS="label label-warning"><I CLASS="icon-user icon-white"></I> <#if model.isLoggedIn()>${model.getForumName()}<#else>jij</#if></SPAN></TD>
					<TD></TD>
					<TD></TD>
				</TR>
				<TR>
					<TD></TD>
					<TD COLSPAN="2">
						<P ALIGN="justify"><@gray>Je kunt een bericht citeren door op de aanhalingstekens bij dat bericht te klikken.</@></P>
						<TEXTAREA NAME="message" ID="message" ROWS="12" PLACEHOLDER="Bericht..." CLASS="input-xxlarge" <#if !model.isLoggedIn()>disabled</#if>><#if model.isLoggedIn() && model.isCitation()>${model.getPostCitation()}</#if></TEXTAREA><BR>
						<BUTTON id="addTopicButtonPopover" TYPE="submit" CLASS="btn btn-large btn-warning" <#if model.isLoggedIn()>ONCLICK="__action.value='saveNewPost';return true;"<#else> ONCLICK="return false;" <@blockingPopup/> </#if>><B>Verstuur</B></BUTTON>
						<BUTTON TYPE="submit" CLASS="btn" <#if model.isLoggedIn()>ONCLICK="__action.value='eraseMessage';return true;"<#else> ONCLICK="return false;" <@blockingPopup/> </#if>><B>Wissen</B></BUTTON>
					</TD>					
				</TR>
			</TBODY>
		</TABLE>
		<A HREF="molgenis.do?__target=ForumPlugin&__action=toPreviousScreen">terug</A>
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
	
<#if model.isCitation()>
	$('html, body').animate({scrollTop: $(document).height()}, 'slow');

    var temp;
    temp=$('#message').val();
    $('#message').val('');
    $('#message').focus();
    $('#message').val(temp);
</#if>
</SCRIPT>

<#if model.isLoggedIn() && model.isEditing()>
	<DIV id="editPostModal" CLASS="modal hide fade">
		<FORM METHOD="post" ENCTYPE="multipart/form-data" NAME="editPostForm" ACTION="">
			<input type="hidden" name="__target" value="ForumPlugin">
			<!--needed in every form: to define the action. This can be set by the submit button-->
			<input type="hidden" name="__action" value="editPostMessage">
	
			<DIV CLASS="modal-header">
				<BUTTON CLASS="close" data-dismiss="modal">&times;</BUTTON>
				<H3 CLASS="myOrange">${model.getCurrentTopic().getTitle()}</H3>
			</DIV>
			<DIV CLASS="modal-body">
				<BR>
				<DIV CLASS="row">
					<DIV CLASS="span1 orangeRightBold">Je bericht</DIV>
					<TEXTAREA CLASS="span4" NAME="message" STYLE="margin-left:30px" ROWS="12" PLACEHOLDER="Je bericht...">${model.getEditMessage()}</TEXTAREA>
				</DIV>
				<DIV CLASS="row">
					<DIV CLASS="span4 offset1" ALIGN="left">
						<FONT COLOR="#AAA">Geef kort aan waarom je je bericht wijzigt</FONT>
					</DIV>
				</DIV>
				<DIV CLASS="row">
					<DIV CLASS="span1 orangeRightBold">Reden</DIV>
					<INPUT CLASS="span4" STYLE="margin-left:30px" TYPE="text" NAME="reason" PLACEHOLDER="Bijv.: spelfout"><BR>
				</DIV>		
			</DIV>
			<DIV CLASS="modal-body"></DIV>
			<DIV CLASS="modal-footer">
				<BUTTON TYPE="submit" CLASS="btn btn-large btn-info" ONCLICK="$('#editPostModal').modal('hide');">Wijzigen</BUTTON>
				<BUTTON TYPE="submit" CLASS="btn" DATA-DISMISS="modal" ONCLICK="__action.value='eraseMessage';return true;">Cancel</BUTTON>
			</DIV>
		</FORM>
	</DIV>
	<#-- show the modal -->
	<SCRIPT>
		$("#editPostModal").modal('show');
	</SCRIPT>
</#if>

<SCRIPT>
	$(function() {
	     $(".goto").click(function() {
	      var target = $(this).attr("href")
	      target = target.substring(1,target.length);
	      $(window).scrollTop($('a[name="'+target+'"]').offset().top - 42); 
	      return false; 
	    });
	});
</SCRIPT>