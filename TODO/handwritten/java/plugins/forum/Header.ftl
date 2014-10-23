<#include "myMacros.ftl" />

<#---- Facebook ---->
<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/nl_NL/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>

<DIV ID="wrap">
	<FORM CLASS="form-horizontal" METHOD="post" ENCTYPE="multipart/form-data" NAME="${model.name}" ID="${model.name}">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${model.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action">
	
	<DIV CLASS="navbar navbar-fixed-top">
		<DIV CLASS="navbar-inner">
			<DIV CLASS="container">
				<A HREF="molgenis.do?__target=ForumPlugin&amp;__action=toVoedingsdagboekView">
					<IMG CLASS="brand" STYLE="height: 35px; padding-top:2px; padding-bottom:2px;" SRC="myres/img/lmd_logo_small.png" ALT="Klein logo" />
				</A>

  <ul class="nav">
    <LI<#if model.isFoodView()> CLASS="active"</#if>><A HREF="molgenis.do?__target=ForumPlugin&amp;__action=toFoodView">Voeding</A></LI>
    <LI<#if model.isForumView()> CLASS="active"</#if>><A HREF="molgenis.do?__target=ForumPlugin&amp;__action=toTopicView">Forum</A></LI>
    <LI<#if model.isHelpView()> CLASS="active"</#if>><A HREF="molgenis.do?__target=ForumPlugin&amp;__action=toHelpView">Help</A></LI>
  </ul>

	<ul class="nav pull-right">
		<#if model.isLoggedIn()>
		    <li class="dropdown">
		      <a href="#" class="dropdown-toggle" data-toggle="dropdown"><IMG SRC="http://www.gravatar.com/avatar/${model.getMD5HashCurrentLmdUser()}?s=18&d=http%3A%2F%2Ficons.iconarchive.com%2Ficons%2Fcustom-icon-design%2Fpretty-office-8%2F256%2FUser-blue-icon.png" STYLE="width: 18px; max-height: 40px; overflow: visible;padding-top: 0; padding-bottom: 0;"/> ${model.getUserName()} <B CLASS="caret"></B></A>
		      <ul class="dropdown-menu">
		        <li><a href="molgenis.do?__target=ForumPlugin&amp;__action=toMyDataView"><i class="icon-cog"></i> Mijn gegevens</a></li>
		        <li><a href="molgenis.do?__target=ForumPlugin&amp;__action=logout"><i class="icon-off"></i> Uitloggen</a></li>
		      </ul>
		    </li>
	    <#else>
	    	<#if !model.isMyDataView()>
		    	<LI><A HREF="#" ONCLICK="$('#loginPanel').collapse('toggle');"><i class="icon-user"></i> <@blue>Inloggen</@></a></li>
		    	<LI><A HREF="molgenis.do?__target=ForumPlugin&amp;__action=toMyDataView"><@blue>aanmelden</@></A></LI>
		    </#if>
		</#if>
	</ul>

	          </div>
	        </div>
	      </div>
	
	<DIV CLASS="container">
