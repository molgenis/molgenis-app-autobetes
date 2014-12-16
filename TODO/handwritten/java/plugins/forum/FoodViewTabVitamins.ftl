<#--BR>
<DIV CLASS="btn-toolbar pagination-centered">
	<DIV CLASS="btn-group" DATA-TOGGLE="buttons-radio">
	  <BUTTON ID="vitaminPlotButton1" TYPE="button" CLASS="btn btn-default active">Je vitaminen</BUTTON>
	  <BUTTON ID="vitaminPlotButton2" TYPE="button" CLASS="btn btn-default">aangevuld</BUTTON>
	</DIV>
</DIV-->
<BR>
<BR>
<SPAN CLASS="pull-right"><@datePicker "food" "${model.getFoodDateDashed()}" "true" "true" false /></SPAN>
<SCRIPT>
	$(function (){
		$('#foodDP').datepicker();
		$('#foodDP').datepicker().on('changeDate', function(e){
			$('#saveFood').button('loading');
			$('#saveFood').html("Bezig met aanpassen datum...");
	        var y = e.date.getFullYear(),
	            _m = e.date.getMonth() + 1,
	            m = (_m > 9 ? _m : '0'+_m),
	            _d = e.date.getDate(),
	            d = (_d > 9 ? _d : '0'+_d);
			window.location = 'molgenis.do?__target=ForumPlugin&__action=gotoDate&date=' + d + '-' + m + '-' + y;
		});
	});
</SCRIPT>
<IMG ID="vitaminPlot" SRC="${model.getVitaminePlotPath()}" ALT="Overzicht micronutrienten in je voeding"/>

<BR><BR>
<@gray>
<P ALIGN="justify">
Dit figuur toont de vitaminen en mineralen in je voeding op dagbasis.
Per nutri‘nt geeft een balkje aan of je te veel dan wel te weinig van deze stof nuttigt.
De aanbevolen hoeveelheden hangen af van onder meer je geslacht en leeftijd.
<#if !model.isLoggedIn()>
</#if>
Het figuur gaat uit van een <B><I>${model.getWebGender()}</I></B> van <B><I>${model.getAge()} jaar</I></B>.
Je kunt dit <a href="molgenis.do?__target=ForumPlugin&amp;__action=toMyDataView">aanpassen</a>.
</P>
<#--P ALIGN="justify">
Met de knop "<A ID="vitaminPlotLink" HREF="#">aangevuld</A>" kun je je voeding aanvullen met wat andere mensen gemiddeld eten.
Dit is handig als je niet al je voeding ingevoerd hebt en toch een indruk wil hebben van je dagtotalen.
Hiervoor gebruiken we voedingsgegevens uit een wetenschappelijk experiment waarbij het effect van gluten op de gezondheid getest werd. 
</P-->
</@>
<@toTabLink "Naar dagboek" "molgenis.do?__target=ForumPlugin&__action=toFoodTabFood" "Naar advies" "molgenis.do?__target=ForumPlugin&__action=toFoodTabAdvice"/>

<#--SCRIPT>
$('#vitaminPlotButton1').on({
    'click': function(){
        $('#vitaminPlot').attr('src','${model.getVitaminePlotPath()}');
    }
});
$('#vitaminPlotButton2').on({
    'click': function(){
        $('#vitaminPlot').attr('src','${model.getVitaminePlusPlotPath()}');
    }
});
$('#vitaminPlotLink').on({
    'click': function(){
        $('#vitaminPlot').attr('src','${model.getVitaminePlusPlotPath()}');
        $('#vitaminPlotButton1').removeClass('active');
        $('#vitaminPlotButton2').addClass('active');
    }
});
</SCRIPT-->