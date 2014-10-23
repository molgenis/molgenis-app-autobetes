<A HREF="molgenis.do?__target=ForumPlugin&__action=toDishView&tab=overview" CLASS="pull-right"><I CLASS="icon-arrow-left"></I> terug naar overzicht</A>
<P>Hier kun je je gerechten aanmaken en bewerken.
</P>
<BR>
<DIV CLASS="control-group">
	<LABEL CLASS="control-label" FOR="dishName">Naam van je gerecht <SPAN CLASS="badge badge-info">1</SPAN></LABEL>
	<DIV CLASS="controls">
		<INPUT TYPE="text" ID="dishName" NAME="dishName" PLACEHOLDER="Bv. 'Mijn ontbijt'" VALUE="${model.getDishName()}">
	</DIV>
</DIV>

<DIV CLASS="control-group">	
	<LABEL CLASS="control-label" FOR="course">Kies een gang <SPAN CLASS="badge badge-info">2</SPAN></LABEL>
	<DIV CLASS="controls">
		<SELECT ID="course" NAME="course">
			<#list model.getDishCategoryDefaultList() as cat>
				<OPTION <#if cat = model.getDishCategory()>SELECTED="true"</#if>>${cat}</OPTION>
			</#list>
		</SELECT>
	</DIV>
</DIV>

<#-->DIV CLASS="control-group">	
	<LABEL CLASS="control-label" FOR="servings">Aantal porties in dit gerecht <SPAN CLASS="badge badge-info">3</SPAN></LABEL>
	<DIV CLASS="controls">
		<INPUT ID="servingsPerDish" NAME="servingsPerDish" TYPE="text" AUTOCOMPLETE="off" PLACEHOLDER="1">
	</DIV>
</DIV-->

<P>Stel je gerecht samen door de ingredi&euml;nten een voor een toe te voegen.</P>

<DIV CLASS="control-group">	
	<LABEL CLASS="control-label" FOR="foodInput">Ingredi&euml;nt <SPAN CLASS="badge badge-info">3</SPAN></LABEL>
	<DIV CLASS="controls">
		<INPUT TYPE="text" ID="foodInput" NAME="foodInput" CLASS="typeahead" PLACEHOLDER="Kies een product" AUTOCOMPLETE="off" >
	</DIV>
</DIV>

<DIV CLASS="control-group">	
	<LABEL CLASS="control-label" FOR="servings">Hoeveel <SPAN CLASS="badge badge-info">4</SPAN></LABEL>
	<DIV CLASS="controls">
		<@gray>
			<INPUT ID="servings" NAME="servings" TYPE="text" CLASS="span1" AUTOCOMPLETE="off" PLACEHOLDER="1"> portie(s), &oacute;f
			<INPUT ID="weight" NAME="weight" TYPE="text" CLASS="span1" AUTOCOMPLETE="off" STYLE="background-color: #EEEEEE"> gram
		</@>
	</DIV>
</DIV>

<DIV CLASS="control-group">
	<LABEL CLASS="control-label" FOR="servings">Toevoegen en opslaan <SPAN CLASS="badge badge-info">5</SPAN></LABEL>
	<DIV CLASS="controls">
		<BUTTON ID="saveDish" CLASS="btn btn-info" DATA-TOGGLE="button" DATA-LOADING-TEXT="<I CLASS='icon-plus icon-white'></I> Bezig met toevoegen..." ONCLICK="__action.value='saveDish';$('#saveDish').button('loading');$('#saveDishSubmit').click();"><B><I CLASS="icon-plus icon-white"></I> Ingredi&euml;nt toevoegen</B></BUTTON>
		<BUTTON ID="saveDishSubmit" TYPE="submit" CLASS="btn hidden" ONCLICK="return true;">hidden submit</BUTTON>				
	</DIV>
</DIV>
<BR/>
<BR/>
<P>Hieronder een overzicht van de samenstelling van je gerecht.
</P>

<@weightedFoodTable model.getCurrentDishWeightedFood() />
<@servingsAndWeightBoxScript/>

<input type="hidden" name="dishId" value="${model.getDishId()?c}">
<BUTTON CLASS="btn btn-danger" TYPE="submit" ONCLICK="__action.value='deleteDish';"><I CLASS="icon-trash icon-white"></I> <B>Gerecht verwijderen</B></BUTTON>
<BR/>
