	<BR>	
	<BR>
	<TABLE CLASS="table borderless">
		<TBODY>
		<TR><TD CLASS="span1"><P CLASS="orangeRight" STYLE="font-size:large;"><B>Voeding</B></P></TD>
			<TD CLASS="span7">
				<INPUT ID="foodInput" NAME="foodInput" TYPE="text" <#if !model.isLoggedIn() && !model.anyConsumedProduct()>STYLE="border: 5px solid #FF7F00" REL="tooltip" DATA-ORIGINAL-TITLE="Begin met typen!"</#if> CLASS="input typeahead span4" AUTOCOMPLETE="off" PLACEHOLDER="Kies een product">
				<SPAN CLASS="pull-right"><@datePicker "food" "${model.getFoodDateDashed()}" "true" "true" false /></SPAN>
			</TD>
		</TR>
		<TR><TD CLASS="span1"><P CLASS="orangeRight" STYLE="font-size:large;"><B>Hoeveel</B></P></TD>
			<TD><P CLASS="myGray">
					<INPUT ID="servings" NAME="servings" TYPE="text" CLASS="span1" AUTOCOMPLETE="off" PLACEHOLDER="1"> portie(s), &oacute;f
					<INPUT ID="weight" NAME="weight" TYPE="text" CLASS="span1" AUTOCOMPLETE="off" STYLE="background-color: #EEEEEE"> gram				
					<BUTTON ID="saveFood" CLASS="btn btn-large btn-info pull-right" DATA-TOGGLE="button" DATA-LOADING-TEXT="<I CLASS='icon-plus icon-white'></I> Bezig met toevoegen..." ONCLICK="__action.value='saveFood';$('#saveFood').button('loading');$('#saveFoodSubmit').click();"><B><I CLASS="icon-plus icon-white"></I> Toevoegen</B></BUTTON>
					<BUTTON ID="saveFoodSubmit" TYPE="submit" CLASS="btn hidden" ONCLICK="return true;">hidden submit</BUTTON>				
				</P>
			</TD>
		</TR>
		</TBODY>
	</TABLE>
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

		<#if !model.isLoggedIn() && !model.anyConsumedProduct()>
			$('#foodInput').keyup(function(e) {
				$(this).css('border','');
			});
		</#if>	
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
	<SCRIPT>
		function roundNDec(num, dec) {
    		var rounded = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
    		return rounded.toFixed(dec);
		}
		
		function AddBorderUpdateButton(id) {
			$('#updateButtonTD' + id).css('border', '5px solid #FF7F00');
			$('#updateButtonTD' + id).css('border-right', '');
			$('#cancelButtonTD' + id).css('border', '5px solid #FF7F00');
			$('#cancelButtonTD' + id).css('border-left', '');
			$('#updateButton' + id).removeClass('hide');
			$('#cancelIcon' + id).removeClass('hide');
			<#--$('#updateButton' + id).css('background-color', '#FF7F00');-->
		}
		function RemoveBorderUpdateButton(id) {
			$('#updateButtonTD' + id).css('border', '');
			$('#cancelButtonTD' + id).css('border', '');
			$('#updateButton' + id).addClass('hide');
			$('#cancelIcon' + id).addClass('hide');
			<#--$('#updateButton' + id).css('background-color', '');-->
		}

		function resetOtherConsumptions(idNoReset) {
			<#list model.getConsumedProductList() as cp><#if !cp.removed>
				if (idNoReset != ${cp_index}) {
					$('#serving${cp_index}').html(servingInitial${cp_index});
					$('#weight${cp_index}').html(weightInitial${cp_index});
				    
				    RemoveBorderUpdateButton(${cp_index});
				}
			</#if></#list>
		}
	</SCRIPT>
	
	<TABLE CLASS="table  table-hover">
		<THEAD>
		<TR><TH><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Product">Voeding</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Aantal porties dat je van deze voeding eet. Beweeg je muis over de voeding om de omvang van een portie te bekijken.">Porties</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Gewicht (gram) van je voeding">Gewicht</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Energie (kcal) in je voeding">Energie</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Eiwit (gram) in je voeding">Eiwit</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Koolhydraten (gram) in je voeding">Koolh.</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL="tooltip" DATA-ORIGINAL-TITLE="Vet (gram) in je voeding">Vet</SPAN></TH>
			<TH style="text-align: right;"><SPAN REL='tooltip' TITLE='Als je de hoeveelheid van een consumptie bewerkt dan verschijnt hieronder een icoontje waarmee je je wijziging kan opslaan'><I CLASS="icon-ok"></I></SPAN></TH>
			<TH style="text-align: right;"><SPAN REL='tooltip' TITLE='Als je de hoeveelheid van een consumptie bewerkt dan verschijnt hieronder een icoontje waarmee je je wijziging kan annuleren'><I CLASS="icon-remove"></I></SPAN></TH>
			<TH style="text-align: right;"><SPAN REL='tooltip' TITLE='Klik hieronder om de voeding per regel te verwijderen'><I CLASS="icon-trash"></I></SPAN></TH>
		</THEAD>
		<TBODY>
		<SCRIPT>
			var totalEnergy 	= 0;
			var totalProtein 	= 0;			
			var totalCarbs  	= 0;
			var totalFat		= 0;
		</SCRIPT>
		<#list model.getConsumedProductList() as cp><#if !cp.removed>
		<SCRIPT>
			var w = parseFloat( "${cp.getWeight()}".replace(',','') );
			var uw= parseFloat( "${cp.getProduct().getEenheidGewicht()}".replace(',','') );
			var s = w / uw;
			var e = parseFloat( "${cp.getNutrients().getEnergie()}".replace(',','') );
			var p = parseFloat( "${cp.getNutrients().getEiwit()}".replace(',','') );
			var c = parseFloat( "${cp.getNutrients().getKoolhydraat()}".replace(',','') );
			var f = parseFloat( "${cp.getNutrients().getVetTotaal()}".replace(',','') );
		</SCRIPT>
		<TR><TD><SPAN ID="product${cp_index}" REL="tooltip">${cp.getProduct().getName()}</SPAN></TD>
			<TD ID="serving${cp_index}" STYLE="text-align: right;" contentEditable="true"></TD>
			<TD ID="weight${cp_index}" STYLE="text-align: right;" contentEditable="true"></TD>
			<TD ID="energy${cp_index}" STYLE="text-align: right;"></TD>
			<TD ID="protein${cp_index}" STYLE="text-align: right;"></TD>
			<TD ID="carbs${cp_index}" STYLE="text-align: right;"></TD>
			<TD ID="fat${cp_index}" STYLE="text-align: right;"></TD>
			<TD ID="updateButtonTD${cp_index}" WIDTH="1%" STYLE="text-align: right;">
				<A ID="updateButton${cp_index}" CLASS="hide" HREF="#" ONCLICK="$('#updateButton${cp_index}').attr('href', 'molgenis.do?__target=ForumPlugin&__action=updateConsumption&cid=${cp_index}&servings=' + $('#serving${cp_index}').html() + '&weight=' + $('#weight${cp_index}').html()); $('#saveFood').data('loadingText','<I CLASS=\'icon-ok icon-white\'></I> Bezig met updaten...');$('#saveFood').removeClass('btn-info').addClass('btn-warning');$('#saveFood').button('loading');">
					<I CLASS="icon-ok"></I>
				</A>
				<SCRIPT>
					$('#updateButton${cp_index}').click(function(e) {
						e.stopPropagation();
					});
				</SCRIPT>
			</TD>
			<TD ID="cancelButtonTD${cp_index}" WIDTH="1%" STYLE="text-align: right;">
				<A ID="cancelIcon${cp_index}" CLASS="hide" HREF="#">
					<I CLASS="icon-remove"></I>
				</A>
			</TD>
			<TD WIDTH="1%" STYLE="text-align: right;">
				<A HREF="molgenis.do?__target=ForumPlugin&__action=deleteConsumption&cid=${cp_index}" ONCLICK="$('#saveFood').data('loadingText','<I CLASS=\'icon-trash icon-white\'></I> Bezig met verwijderen...');$('#saveFood').removeClass('btn-info').addClass('btn-danger');$('#saveFood').button('loading');">
					<I CLASS="icon-trash"></I>
				</A>
			</TD>
			<SCRIPT>
				$('#product${cp_index}').tooltip({title: "Een portie (${cp.getProduct().getEenheid()}) weegt " + roundNDec(uw,0) + " gram."});
				$('#serving${cp_index}').html(roundNDec(s,1));
				$('#weight${cp_index}').html(roundNDec(w,0));
				$('#energy${cp_index}').html(roundNDec(e,0));
				$('#protein${cp_index}').html(roundNDec(p,0));
				$('#carbs${cp_index}').html(roundNDec(c,0));
				$('#fat${cp_index}').html(roundNDec(f,0));
				
				totalEnergy += e;
				totalProtein += p;
				totalCarbs += c;
				totalFat += f;
				
				$('#serving${cp_index}').click(function (e) {
					AddBorderUpdateButton(${cp_index});
					$('#serving${cp_index}').html(servingInitial${cp_index});
					resetOtherConsumptions(${cp_index});
					e.stopPropagation();
				});
				$('#serving${cp_index}').keypress(function(e) {
				    if (e.keyCode == 13) { e.preventDefault(); }
				    if (e.keyCode == 27) { resetOtherConsumptions(-1); }
				});
				$('#serving${cp_index}').keyup(function (e) {
					if (e.keyCode == '13' || e.which == '13') {
						$('#updateButton${cp_index}').click();
						window.location = $('#updateButton${cp_index}').attr('href');
					} else if (!(e.keyCode == '9' || e.which == '9' || e.keyCode == '16' || e.which == '16')) {
						AddBorderUpdateButton(${cp_index});
						$('#weight${cp_index}').html('');
						resetOtherConsumptions(${cp_index});
					}
				});

				$('#weight${cp_index}').click(function (e) {
					AddBorderUpdateButton(${cp_index});
					$('#weight${cp_index}').html(weightInitial${cp_index});
					resetOtherConsumptions(${cp_index});
					e.stopPropagation();
				});
				$('#weight${cp_index}').keypress(function(e) {
				    if (e.keyCode == 13) { e.preventDefault(); }
				    if (e.keyCode == 27) { resetOtherConsumptions(-1); }
				});
				$('#weight${cp_index}').keyup(function (e) {
					if (e.keyCode == '13' || e.which == '13') {
						$('#updateButton${cp_index}').click();
						window.location = $('#updateButton${cp_index}').attr('href');
					} else if (!(e.keyCode == '9' || e.which == '9' || e.keyCode == '16' || e.which == '16')) {
						AddBorderUpdateButton(${cp_index});
						$('#serving${cp_index}').html('');
						resetOtherConsumptions(${cp_index});
					}
				});
				
				var servingInitial${cp_index} = roundNDec(s,1);
				var weightInitial${cp_index} = roundNDec(w,0);
				$(document).click(function() {
					resetOtherConsumptions(-1);
<#--					$('#serving${cp_index}').html(servingInitial${cp_index});
					$('#weight${cp_index}').html(weightInitial${cp_index});
				    $("#updateButton${cp_index}").hide();
-->
				});
			</SCRIPT>
		</TR>
		</#if></#list>
		<TR><TD><I><B>Totaal</B></I></TD>
			<TD></TD>
			<TD></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-info" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid energie (kcal) in je voeding"><DIV ID="totalEnergy"></DIV></SPAN></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-success" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid eiwit (gram) in je voeding"><DIV ID="totalProtein"></DIV></SPAN></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-warning" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid koolhydraten (gram) in je voeding"><DIV ID="totalCarbs"></DIV></SPAN></TD>
			<TD style="text-align: right;"><SPAN CLASS="label label-important" REL="tooltip" DATA-ORIGINAL-TITLE="Totale hoeveelheid vet (gram) in je voeding"><DIV ID="totalFat"></DIV></SPAN></TD>
			<TD></TD>
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
	<TABLE STYLE="float: left;">
		<#if model.anyConsumedProduct()>
			<TR><TD><SPAN CLASS="label label-warning"><B>Tip!</B></SPAN></TD><TD><SMALL>&nbsp;<@gray>Klik op Porties/Gewicht om hoeveelheden aan te passen.</@></SMALL></TD></TR>
		</#if>
		<TR>
			<TD><A HREF="molgenis.do?__target=ForumPlugin&__action=toHelpView"><SPAN CLASS="label label-default"><B>Help!</B></SPAN></A></TD>
			<TD><SMALL>&nbsp;
				<A HREF="molgenis.do?__target=ForumPlugin&__action=toHelpView&item=ProdNA">
					Ik kan een product niet vinden
				</A>
			</SMALL></TD>
		</TR>
	</TABLE>

	<@toTabLink "" "" "Naar overzicht" "molgenis.do?__target=ForumPlugin&__action=toFoodTabVitamin"/>
<#--SCRIPT>
	function hideShowDeleteButton()
	{
		if ($("#ForumPlugin input:checkbox:checked").length > 0)
		{
		    $('#deleteConsumption').removeClass('hide');
		}
		else
		{
		   $('#deleteConsumption').addClass('hide');
		}
	}
</SCRIPT-->
<SCRIPT>  
$(function (){
	$('.typeahead').typeahead({
		source: function(query, callback) {
			$.get('/xref/find?xrefEntity=lmd.Product&xrefField=id&xrefLabels=Naam&nillable=false&searchTerm=-' + query + '-', function(data) {
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
