<A HREF="molgenis.do?__target=ForumPlugin&__action=toDishView&tab=edit" CLASS="btn btn-warning pull-right" <#if !model.isLoggedIn()> <@popup "Log eerst in!">"Om een gerecht samen te stellen moet je ingelogd zijn. Meld je eerst aan."</@> ONCLICK="return false;" </#if>><I CLASS="icon-plus icon-white"></I> <B>Nieuw gerecht</B></A>
<P>Je kunt producten die je vaak samen eet, samenvoegen in een gerecht.</P>
<BR/><BR/><BR/>
<P>Hieronder een overzicht van je gerechten. Klik om een gerecht te bewerken of te verwijderen.</P>

<TABLE CLASS="table table-hover span7" STYLE="margin-left:0px;">
	<THEAD>
	<TR><TH><SPAN>Gerecht (portie)</SPAN></TH>
		<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Gewicht (gram) van je voeding">Gewicht</SPAN></TH>
		<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Energie (kcal) in je voeding">Energie</SPAN></TH>
		<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Eiwit (gram) in je voeding">Eiwit</SPAN></TH>
		<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Koolhydraten (gram) in je voeding">Koolh.</SPAN></TH>
		<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Vet (gram) in je voeding">Vet</SPAN></TH>
	</THEAD>
	<TBODY data-provides="rowlink">
	<#list model.getDishListAsWeightedFood() as wf>
		<TR>
			<TD><A HREF="molgenis.do?__target=ForumPlugin&__action=toDishView&tab=edit&dishId=${wf.getId()?c}">${wf.getFood().getName()}</A></TD>
			<TD ID="weight${wf_index}" STYLE="text-align: right;"></TD>
			<TD STYLE="text-align: right;"><SPAN CLASS="label label-info"><DIV ID="energy${wf_index}"></DIV></SPAN></TD>
			<TD STYLE="text-align: right;"><SPAN CLASS="label label-success"><DIV ID="protein${wf_index}"></DIV></SPAN></TD>
			<TD STYLE="text-align: right;"><SPAN CLASS="label label-warning"><DIV ID="carbs${wf_index}"></DIV></SPAN></TD>
			<TD STYLE="text-align: right;"><SPAN CLASS="label label-important"><DIV ID="fat${wf_index}"></DIV></SPAN></TD>

			<SCRIPT>
				var w = parseFloat( "${wf.getWeight()}".replace(',','') );
				var e = parseFloat( "${wf.getNutrientsTotal().getEnergie()}".replace(',','') );
				var p = parseFloat( "${wf.getNutrientsTotal().getEiwit()}".replace(',','') );
				var c = parseFloat( "${wf.getNutrientsTotal().getKoolhydraat()}".replace(',','') );
				var f = parseFloat( "${wf.getNutrientsTotal().getVetTotaal()}".replace(',','') );
				
				$('#weight${wf_index}').html(roundNDec(w,0));
				$('#energy${wf_index}').html(roundNDec(e,0));
				$('#protein${wf_index}').html(roundNDec(p,0));
				$('#carbs${wf_index}').html(roundNDec(c,0));
				$('#fat${wf_index}').html(roundNDec(f,0));
			</SCRIPT>
		</TR>
	</#list>
	</TBODY>
</TABLE>

<SCRIPT>  
$(function ()
	{ 	$('a').popover();
	});
</SCRIPT>