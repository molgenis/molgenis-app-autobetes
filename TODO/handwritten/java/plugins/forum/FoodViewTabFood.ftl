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
                                    <BUTTON ID="saveFood" CLASS="btn btn-large btn-info pull-right" DATA-TOGGLE="button" DATA-LOADING-TEXT="<I CLASS='icon-plus icon-white'></I> Bezig met toevoegen..." ONCLICK="__action.value='saveDish';$('#saveFood').button('loading');$('#saveFoodSubmit').click();"><B><I CLASS="icon-plus icon-white"></I> Toevoegen</B></BUTTON>
                                    <BUTTON ID="saveFoodSubmit" TYPE="submit" CLASS="btn hidden" ONCLICK="return true;">hidden submit</BUTTON>
                          
                            </P>
                    </TD>
            </TR>
            </TBODY>
    </TABLE>


	<@weightedFoodTable model.getCurrentConsumptionList() />
	<@servingsAndWeightBoxScript/>
	
	<BR>	
	<BR>
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