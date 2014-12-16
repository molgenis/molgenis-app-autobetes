<#include "Header.ftl">
<DIV CLASS="row">
	<#include "Alert.ftl">
</DIV>
<DIV CLASS="row">
	<DIV CLASS="span3 pull-right">
		<#include "LoginPanel.ftl">
	</DIV>
</DIV>
<DIV CLASS="row">
		<div class="hero-unit">
	        <h1>Voeding is het nieuwe roken</h1>
	        <p ALIGN="justify">
	        	Roken is ongezond, dat weet iedereen.
	        	Maar wist je dat gezonde voeding nog belangrijker is?
	        	Vooral voor mensen met (pre-)diabetes.
	        	Hoe zit het met jouw voeding?
	        	Durf jij je voeding onder ogen te zien en wil je weten of je echt zo gezond eet als je denkt?
			</p>
			<DIV CLASS="jumbotron">
	        	<A HREF="molgenis.do?__target=ForumPlugin&amp;__action=toFoodView" CLASS="btn btn-success btn-large" STYLE="font-size: 21px;padding: 14px 24px;"><B>Check je voeding &raquo;</B></a>
			</DIV>
		</DIV>
</DIV>
<DIV CLASS="row span6" STYLE="float:none; margin:0 auto; ">
		<UL CLASS="thumbnails">
			<LI CLASS="span3">
				<DIV CLASS="thumbnail">
					<IMG STYLE="width: 260px; height: 173px;" SRC="myres/img/child-holding-cigarette.jpg" ALT="Rokend kind"/>
					<DIV CLASS="caption">
						<P ALIGN="justify">Vroeger vonden we het gewoon dat kinderen rookten...</P>
					</DIV>
				</DIV>
			</LI>
			<LI CLASS="span3">
				<DIV CLASS="thumbnail">
					<IMG STYLE="width: 260px; height: 173px;" SRC="myres/img/teenage-girl-eating-a-hamburger.jpeg" ALT="Vrouw eet hamburger" />
					<DIV CLASS="caption">
						<P ALIGN="justify">... nu vinden we wat we eten heel gewoon!</P>
					</DIV>
				</DIV>
			</LI>
		<UL>
	</UL>
</DIV>

<#include "Footer.ftl">
<SCRIPT>  
function equalHeight(group) {    
    tallest = 0;    
    group.each(function() {       
        thisHeight = $(this).height();       
        if(thisHeight > tallest) {          
            tallest = thisHeight;       
        }    
    });    
    group.each(function() { $(this).height(tallest); });
} 

$(document).ready(function() {   
    equalHeight($(".thumbnail")); 
});

$(function ()
	{ 	$('a').popover();
		$('button').popover({trigger:"hover"});
		$('tr').popover({placement:"right", trigger:"hover"});
	});
</SCRIPT>