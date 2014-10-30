<SCRIPT>
	function roundNDec(num, dec) {
		var rounded = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
		return rounded.toFixed(dec);
	}
</SCRIPT>

<#macro weightedFoodTable weightedFood>
	<SCRIPT>
		function AddBorderUpdateButton(id) {
			$('#foodTable').removeClass('table-hover');
			$('#foodTableBody').addClass('tbodyDim');
			$('#row' + id).addClass('trHighlight');
			$('#updateButton' + id).fadeIn();
		}
		function RemoveBorderUpdateButton(id) {
			$('#row' + id).removeClass('trHighlight');
			$('#updateButton' + id).fadeOut();
		}

		function resetOtherConsumptions(idNoReset) {
			<#list weightedFood as wf>
				if (idNoReset != ${wf.getId()?c}) {
					$('#serving${wf.getId()?c}').html(servingInitial${wf.getId()?c});
					$('#weight${wf.getId()?c}').html(weightInitial${wf.getId()?c});
				    
				    RemoveBorderUpdateButton(${wf.getId()?c});
				}
			</#list>
			if (idNoReset == -1) {
				$('#foodTable').addClass('table-hover');
				$('#foodTableBody').removeClass('tbodyDim');
			}
		}
	</SCRIPT>
	
	<TABLE ID="foodTable" CLASS="table table-hover">
		<THEAD>
		<TR><TH><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Product">Voeding</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Aantal porties dat je van deze voeding eet. Beweeg je muis over de voeding om de omvang van een portie te bekijken.">Porties</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Gewicht (gram) van je voeding">Gewicht</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Energie (kcal) in je voeding">Energie</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Eiwit (gram) in je voeding">Eiwit</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Koolhydraten (gram) in je voeding">Koolh.</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Vet (gram) in je voeding">Vet</SPAN></TH>
			<TH style="text-align: right;"></TH>
			<TH style="text-align: right;"><SPAN REL='tooltip' TITLE='Klik hieronder om de voeding per regel te verwijderen'><I CLASS="icon-trash"></I></SPAN></TH>
		</THEAD>
		<TBODY ID="foodTableBody">
		<SCRIPT>
			var totalEnergy 	= 0;
			var totalProtein 	= 0;			
			var totalCarbs  	= 0;
			var totalFat		= 0;
		</SCRIPT>
		<#list weightedFood as wf>
		<SCRIPT>
			var w = parseFloat( "${wf.getWeight()}".replace(',','') );
			var uw= parseFloat( "${wf.getFood().getUnitWeight()}".replace(',','') );
			var s = w / uw;
			var e = parseFloat( "${wf.getNutrientsTotal().getEnergie()}".replace(',','') );
			var p = parseFloat( "${wf.getNutrientsTotal().getEiwit()}".replace(',','') );
			var c = parseFloat( "${wf.getNutrientsTotal().getKoolhydraat()}".replace(',','') );
			var f = parseFloat( "${wf.getNutrientsTotal().getVetTotaal()}".replace(',','') );
		</SCRIPT>
		<TR ID="row${wf.getId()?c}"><TD><SPAN ID="product${wf.getId()?c}" REL="tooltip">${wf.getFood().getName()}</SPAN></TD>
			<TD ID="serving${wf.getId()?c}" STYLE="text-align: right;" contentEditable="true"></TD>
			<TD ID="weight${wf.getId()?c}" STYLE="text-align: right;" contentEditable="true"></TD>
			<TD ID="energy${wf.getId()?c}" STYLE="text-align: right;"></TD>
			<TD ID="protein${wf.getId()?c}" STYLE="text-align: right;"></TD>
			<TD ID="carbs${wf.getId()?c}" STYLE="text-align: right;"></TD>
			<TD ID="fat${wf.getId()?c}" STYLE="text-align: right;"></TD>
			<TD WIDTH="1%" STYLE="white-space:nowrap;">
				<A ID="updateButton${wf.getId()?c}" STYLE="display: none;" CLASS="btn btn-warning btn-mini" HREF="#" ONCLICK="$('#updateButton${wf.getId()?c}').attr('href', 'molgenis.do?__target=ForumPlugin&__action=updateWeightedFood&foodType=' + foodType.value + '&id=${wf.getId()?c}&servings=' + $('#serving${wf.getId()?c}').html() + '&weight=' + $('#weight${wf.getId()?c}').html()); $('#saveDish').data('loadingText','<I CLASS=\'icon-ok icon-white\'></I> Bezig met updaten...');$('#saveDish').removeClass('btn-info').addClass('btn-warning');$('#saveDish').button('loading');">
					<B><I CLASS="icon-ok icon-white"></I>&nbsp;Wijzigen</B>
				</A>
				<SCRIPT>
					$('#updateButton${wf.getId()?c}').click(function(e) {
						e.stopPropagation();
					});
				</SCRIPT>
			</TD>
			<TD WIDTH="1%" STYLE="text-align: right;">
				<A ID="deleteButton${wf.getId()?c}" HREF="#" ONCLICK="$('#deleteButton${wf.getId()?c}').attr('href', 'molgenis.do?__target=ForumPlugin&__action=deleteWeightedFood&foodType=' + foodType.value + '&id=${wf.getId()?c}');">
					<I CLASS="icon-trash"></I>
				</A>
			</TD>
		</TR>
		<SCRIPT>
			$('#product${wf.getId()?c}').tooltip({title: "Een portie (${wf.getFood().getUnit()}) weegt " + roundNDec(uw,0) + " gram."});
			$('#serving${wf.getId()?c}').html(roundNDec(s,1));
			$('#weight${wf.getId()?c}').html(roundNDec(w,0));
			$('#energy${wf.getId()?c}').html(roundNDec(e,0));
			$('#protein${wf.getId()?c}').html(roundNDec(p,0));
			$('#carbs${wf.getId()?c}').html(roundNDec(c,0));
			$('#fat${wf.getId()?c}').html(roundNDec(f,0));
			
			totalEnergy += e;
			totalProtein += p;
			totalCarbs += c;
			totalFat += f;
			
			$('#serving${wf.getId()?c}').click(function (e) {
				AddBorderUpdateButton(${wf.getId()?c});
				$('#serving${wf.getId()?c}').html(servingInitial${wf.getId()?c});
				resetOtherConsumptions(${wf.getId()?c});
				e.stopPropagation();
			});
			$('#serving${wf.getId()?c}').keypress(function(e) {
			    if (e.keyCode == 13) { e.preventDefault(); }
			    if (e.keyCode == 27) { resetOtherConsumptions(-1); }
			});
			$('#serving${wf.getId()?c}').keyup(function (e) {
				if (e.keyCode == '13' || e.which == '13') {
					$('#updateButton${wf.getId()?c}').click();
					window.location = $('#updateButton${wf.getId()?c}').attr('href');
				} else if (!(e.keyCode == '9' || e.which == '9' || e.keyCode == '16' || e.which == '16')) {
					AddBorderUpdateButton(${wf.getId()?c});
					$('#weight${wf.getId()?c}').html('');
					resetOtherConsumptions(${wf.getId()?c});
				}
			});

			$('#weight${wf.getId()?c}').click(function (e) {
				AddBorderUpdateButton(${wf.getId()?c});
				$('#weight${wf.getId()?c}').html(weightInitial${wf.getId()?c});
				resetOtherConsumptions(${wf.getId()?c});
				e.stopPropagation();
			});
			$('#weight${wf.getId()?c}').keypress(function(e) {
			    if (e.keyCode == 13) { e.preventDefault(); }
			    if (e.keyCode == 27) { resetOtherConsumptions(-1); }
			});
			$('#weight${wf.getId()?c}').keyup(function (e) {
				if (e.keyCode == '13' || e.which == '13') {
					$('#updateButton${wf.getId()?c}').click();
					window.location = $('#updateButton${wf.getId()?c}').attr('href');
				} else if (!(e.keyCode == '9' || e.which == '9' || e.keyCode == '16' || e.which == '16')) {
					AddBorderUpdateButton(${wf.getId()?c});
					$('#serving${wf.getId()?c}').html('');
					resetOtherConsumptions(${wf.getId()?c});
				}
			});
			
			var servingInitial${wf.getId()?c} = roundNDec(s,1);
			var weightInitial${wf.getId()?c} = roundNDec(w,0);
			$(document).click(function() {
				resetOtherConsumptions(-1);
			});
		</SCRIPT>
		</#list>
		<TR><TD><I><B>Totaal</B></I></TD>
			<TD></TD>
			<TD></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-info" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid energie (kcal) in je voeding"><DIV ID="totalEnergy"></DIV></SPAN></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-success" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid eiwit (gram) in je voeding"><DIV ID="totalProtein"></DIV></SPAN></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-warning" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid koolhydraten (gram) in je voeding"><DIV ID="totalCarbs"></DIV></SPAN></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-important" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid vet (gram) in je voeding"><DIV ID="totalFat"></DIV></SPAN></TD>
			<TD></TD>
			<TD></TD>
		</TR>
		<SCRIPT>
			$('#totalEnergy').html(roundNDec(totalEnergy,0));
			$('#totalProtein').html(roundNDec(totalProtein,0));
			$('#totalCarbs').html(roundNDec(totalCarbs,0));
			$('#totalFat').html(roundNDec(totalFat,0));
		</SCRIPT>
		</TBODY>
	</TABLE>
	<SCRIPT>  
	$(function (){
		$('.typeahead').typeahead({
			source: function(query, callback) {
				$.get('/xref/find?xrefEntity=lmd.Food&xrefField=id&xrefLabels=nameDescriptive&nillable=false&searchTerm=-' + query + '-', function(data) {
					var items=[];
					$.each(data, function(i, val) {
						var txt = val.text.replace(/&#39;/g, "'");
						items.push(txt);
					});
					callback(items);
				});
			}
			, items: 50
			, minLength: 1
		});
	});
	</SCRIPT>
</#macro>

<#macro servingsAndWeightBoxScript>
	<SCRIPT>
		// make clear: input = either servings or weight
		$('#servings').focus(function(e) {
			$('#servings').css('background-color','');
			$('#weight').css('background-color','#EEEEEE');
			$('#servings').attr('placeholder','');
		});
		$('#servings').keyup(function(e) {
			if ("" == $(this).val()) {
				//$('#servings').attr('placeholder','1');
			} else {
				$('#weight').val('');
			}
		});
		$('#servings').blur(function(e) {
			if ("" == $(this).val() && "" == $('#weight').val()) {
				$('#servings').attr('placeholder','1');
			} else {
				$('#servings').attr('placeholder','');
				
				if ("" == $(this).val()) {
					$('#servings').css('background-color','#EEEEEE');
					$('#weight').css('background-color','');
				}
				if ("" == $('#weight').val()) {
					$('#servings').css('background-color','');
					$('#weight').css('background-color','#EEEEEE');
				}
			}
		});
		
		$('#weight').focus(function(e) {
			$('#servings').css('background-color','#EEEEEE');
			$('#weight').css('background-color','');
		});
		$('#weight').keyup(function(e) {
			if ("" == $(this).val()) {
				$('#servings').attr('placeholder','1');
			} else {
				$('#servings').attr('placeholder','');
				$('#servings').val('');
			}
		});
		$('#weight').blur(function(e) {
			if ("" == $(this).val()) {
				$('#servings').attr('placeholder','1');
				$('#servings').css('background-color','');
				$('#weight').css('background-color','#EEEEEE');
			}
		});
	</SCRIPT>
</#macro>