<#include "header.ftl">

	<input id="userId" type="hidden" value="1"/>

	<!-- Start of first page -->
	<div data-role="page" id="home">

		<div data-role="header" data-position="inline">
		     <h1>Autobetes</h1>
		     <a href="#settings" data-role="button" data-icon="gear" data-iconshadow="false"
		        data-direction="reverse" data-transition="flip"
		        data-iconpos="notext"  class="ui-btn-right"></a>
		</div>

		<div role="main" class="ui-content">
			<a id = "homeStartButton" href="#start" class="ui-btn ui-icon-plus ui-btn-icon-left">Start</a>
			<H3>Current activity:</H3>
			
			<a href="#history" class="ui-btn ui-btn-inline ui-icon-bullets ui-btn-icon-left">History</a>
		</div><!-- /content -->

	</div><!-- /page -->

	<div data-role="page" id="start">
		<div data-role="header">
			<a data-rel="back" class="ui-btn ui-shadow ui-corner-all ui-icon-arrow-l ui-btn-icon-left ui-btn-icon-notext ui-corner-all">Back</a>
			<H1><a href="#home">Home</a></H1>
			<a href="#newEvent" class="ui-btn ui-icon-plus ui-btn-icon-right ui-btn-icon-notext ui-corner-all"></a>
			<div data-role="navbar">
			    <ul>
			    	<li><a href="#" name="startEventTypeSelected">All</a></li>
			        <li><a href="#" name="startEventTypeSelected">Food</a></li>
			        <li><a href="#" name="startEventTypeSelected">Activity</a></li>
			    </ul>
			</div><!-- /navbar -->
		</div><!-- /header -->

		<div role="main" class="ui-content">
			<p id="startHelpText">Choose event to start:</p>
			<form id="searchEventsInputForm">
			    <input data-type="search" id="filterControlgroup-input">
			</form>
			<div id="event-list" data-role="controlgroup" data-filter="true" data-input="#filterControlgroup-input">
			</div>
		</div><!-- /content -->
	</div><!-- /page -->

	<div data-role="page" id="start2">
		<div data-role="header">
			<a data-rel="back" class="ui-btn ui-shadow ui-corner-all ui-icon-arrow-l ui-btn-icon-left ui-btn-icon-notext ui-corner-all">Back</a>
			<H1><a href="#home">Home</a></H1>
			<a href="#newEvent" class="ui-btn ui-icon-plus ui-btn-icon-right ui-btn-icon-notext ui-corner-all"></a>
		</div><!-- /header -->

		<div role="main" class="ui-content">
			<H3 id="startEventName"></H3>
			<!-- input name="mode7" id="mode7" type="text" data-role="datebox" data-icon="grid" data-options='{"mode":"timeflipbox", "useNewStyle":true, "overrideTimeFormat": 24}' class="ui-input-text ui-body-d ui-corner-all ui-icon-grid" readonly="readonly"-->
			
			<label for="mydate">Some Date</label>
			
			<input name="mydate" id="mydate" type="date" data-role="datebox"
			   data-options='{"mode": "timeflipbox"}'>
			
			<div class="ui-field-contain">
				<label for="slider-2">Amount</label>
				<input type="range" name="slider-2" id="slider-2" data-highlight="true" min="0" max="10" value="1" step=".1">
			</div>
			<A id="startEventButton" HREF="#" CLASS="ui-btn ui-shadow ui-corner-all">start</A>
		</div><!-- /content -->
	</div><!-- /page -->
	
	<div data-role="page" id="newEvent">
		<div data-role="header">
			<a data-rel="back" class="ui-btn ui-shadow ui-corner-all ui-icon-arrow-l ui-btn-icon-left ui-btn-icon-notext ui-corner-all">Back</a>
			<H1><a href="#home">Home</a></H1>
		</div><!-- /header -->

		<div role="main" class="ui-content">
			<div>
			    <fieldset id="eventType" data-role="controlgroup" data-type="horizontal" style="text-align: center">
			        <input type="radio" name="radio-choice-h-2" id="radio-choice-h-2a" value="Food" checked="checked">
			        <label for="radio-choice-h-2a">Food</label>
			        <input type="radio" name="radio-choice-h-2" id="radio-choice-h-2b" value="Activity">
			        <label for="radio-choice-h-2b">Activity</label>
			    </fieldset>
			</div>
		
			<input id="newEventName" type="text" data-clear-btn="true" name="text-7" id="text-7" value="" placeholder="Event name">
			<a id="addEvent" href="#" class="ui-btn ui-corner-all ui-shadow ui-btn-inline ui-icon-plus ui-btn-icon-left">Add</a>
		</div><!-- /content -->
	</div><!-- /page -->
	
	<div data-role="page" data-id="settingsHeader" id="settings">
		<div data-role="header">
			<a data-rel="back" class="ui-btn ui-shadow ui-corner-all ui-icon-arrow-l ui-btn-icon-left ui-btn-icon-notext ui-corner-all" data-transition="flip">Back</a>
			<H1><a href="#home">Home</a></H1>
			<div data-role="navbar">
			    <ul>
			        <li><a href="#" class="ui-btn-active">Settings</a></li>
			        <li><a href="#settingsEvents">Events</a></li>
			    </ul>
			</div><!-- /navbar -->
		</div><!-- /header -->

		<div role="main" class="ui-content">
			<P>Pump serial:</P>
			<input id="pumpSerial" type="text" data-clear-btn="true" placeholder="1234567">
			<a href="#editSettings" class="ui-btn ui-btn-inline ui-icon-check ui-btn-icon-left ui-corner-all">Save</a>
		</div><!-- /content -->
	</div><!-- /page -->

	<div data-role="page" data-id="settingsHeader" id="settingsEvents">
		<div data-role="header">
			<a data-rel="back" class="ui-btn ui-shadow ui-corner-all ui-icon-arrow-l ui-btn-icon-left ui-btn-icon-notext ui-corner-all" data-transition="flip">Back</a>
			<H1><a href="#home">Home</a></H1>
			<div data-role="navbar">
			    <ul>
			        <li><a href="#settings">Settings</a></li>
			        <li><a href="#" class="ui-btn-active">Events</a></li>
			    </ul>
			</div><!-- /navbar -->
		</div><!-- /header -->

		<div role="main" class="ui-content">
			<P>Events:</P>
		</div><!-- /content -->
	</div><!-- /page -->

	<div data-role="page" id="history">
		<div data-role="header">
			<a data-rel="back" class="ui-btn ui-shadow ui-corner-all ui-icon-arrow-l ui-btn-icon-left ui-btn-icon-notext ui-corner-all" data-transition="flip">Back</a>
			<H1><a href="#home">Home</a></H1>
		</div><!-- /header -->

		<div role="main" class="ui-content">
			<ul id="list" class="ui-listview" data-role="listview" data-icon="false" data-split-icon="delete">
				<li class="ui-li-has-alt ui-first-child">
	                <a href="#demo-mail" class="ui-btn">
	                    <h3>Avery Walker</h3>
	                    <p class="topic"><strong>Re: Dinner Tonight</strong></p>
	                    <p>Sure, let's plan on meeting at Highland Kitchen at 8:00 tonight. Can't wait! </p>
	                    <p class="ui-li-aside"><strong>4:48</strong>PM</p>
	                </a>
	                <a href="#" class="delete ui-btn ui-btn-icon-notext ui-icon-delete" title="Delete"></a>
	            </li>
	            <li class="ui-li-has-alt">
	                <a href="#demo-mail" class="ui-btn">
	                    <h3>Amazon.com</h3>
	                    <p class="topic"><strong>4-for-3 Books for Kids</strong></p>
	                    <p>As someone who has purchased children's books from our 4-for-3 Store, you may be interested in these featured books.</p>
	                    <p class="ui-li-aside"><strong>4:37</strong>PM</p>
	                </a>
	                <a href="#" class="delete ui-btn ui-btn-icon-notext ui-icon-delete" title="Delete"></a>
	            </li>
			</ul>
		</div><!-- /content -->
		
		<div style="display: none;" id="confirm-placeholder"><!-- placeholder for confirm --></div><!-- /popup -->
		<div class="ui-screen-hidden ui-popup-screen ui-overlay-inherit" id="confirm-screen"></div>
		<div class="ui-popup-container ui-popup-hidden ui-popup-truncate" id="confirm-popup">
			<div id="confirm" class="ui-content ui-popup ui-body-a ui-overlay-shadow ui-corner-all" data-role="popup" data-theme="a">
				<p id="question">Are you sure you want to delete:</p>
				<div class="ui-grid-a">
				    <div class="ui-block-a">
				        <a id="yes" class="ui-btn ui-corner-all ui-mini ui-btn-a" data-rel="back">Yes</a>
				    </div>
				    <div class="ui-block-b">
				        <a id="cancel" class="ui-btn ui-corner-all ui-mini ui-btn-a" data-rel="back">Cancel</a>
				    </div>
				</div>
			</div>
		</div>
<!-- 			<table data-role="table" id="event-history-table" data-mode="reflow" class="ui-responsive">
				<thead><tr>
				    <th data-priority="persist">Event</th>
				    <th data-priority="1">Start</th>
				    <th data-priority="2">Stop</th>
				</tr></thead>
				<tbody>
				</tbody>
			</table>
 -->		
		</div><!-- /content -->
	</div><!-- /page -->
<script>

function addNewRecord(url, data, callback){
	$.ajax({
		type : 'POST',
		url : url,
		data : JSON.stringify(data),
		async : false,
		contentType : 'application/json',
		success : function(data, textStatus, request){
			callback(data, textStatus, request);
		},
		error : function(request, textStatus, error){
			console.log(error);
		}
	});
}

function hrefToId(href){
	return href.substring(href.lastIndexOf('/') + 1); 
}

function getEvents(eventType) {
	console.log('>> getEvents()!!');
	var request  = null;
	if(eventType != null && eventType != undefined){
		request = {
			'q' : {
				'q' : [{
					'field' : 'eventType',
					'operator' : 'EQUALS',
					'value' : eventType
				}],
				'num' : 1000
			}
		};
	}
	restApi.getAsync('/api/v1/event', request, function(data) {
		console.log(JSON.stringify(data));

		if (0 === data.items.length) {
			$('#startHelpText').html('Event list empty. Please press + to add a new event.');
			$('#searchEventsInputForm').hide();
		} else {
			$('#startHelpText').html('Choose event to start:');
			$('#searchEventsInputForm').show();
		}
		
		$('#event-list').html('');
		$.each(data.items, function(i, event) {
			var eventButton = $('<A HREF="#" CLASS="ui-btn ui-shadow ui-corner-all">' + event.name + '</A>');
			$('#event-list').append(eventButton);
			eventButton.click(function(){
				window.location.href="#start2";
				$('#startEventName').html(event.name);
				$('#startEventButton').click(function(){
					var eventHistoryObject = {
						'owner' : $('#userId').val(),
						'event' : hrefToId(event.href)						
					};
					addNewRecord('/api/v1/EventHistory', eventHistoryObject, function() {
						window.location.href="#home";
						alert('Event added!');
					});
				});
			});
			//console.log(JSON.stringify(event.name));
		});
	});
}

function selectTabMenu(){
	var selectedTabIndex = $(document).data('selectedTabIndex');
	var index = selectedTabIndex === undefined ? 0 : selectedTabIndex.index;
	var eventType = selectedTabIndex === undefined ? null : selectedTabIndex.eventType;
	$('[name=startEventTypeSelected]').removeClass('ui-btn-active');
	$('[name=startEventTypeSelected]:eq(' + index + ')').addClass('ui-btn-active');
	getEvents(eventType);
}

$('[name=startEventTypeSelected]').click(function(){
	var eventType = $(this).html() === 'All' ? null : $(this).html();
	var index = 0
	if(eventType === 'Food') index = 1;
	else if(eventType === 'Activity') index = 2;
	$(document).data('selectedTabIndex', {
		'index' : index,
		'eventType' : eventType
	});
	getEvents(eventType);
});

$(document).on('pageshow', '#start', function(){ 
	selectTabMenu();
});

$('#homeStartButton').click(function(){
	$(document).removeData('selectedTabIndex');
	selectTabMenu();
});

$('#addEvent').click(function(){
	var eventRecord = {
		'name' : $('#newEventName').val(),
		'owner' : $('#userId').val(),
		'eventType' : $('[name="radio-choice-h-2"]:checked').val()
	};
	addNewRecord('/api/v1/event', eventRecord, function(){
		window.location.href="#start";
	});
	/* $.ajax({
		type : 'POST',
		url : '/api/v1/event',
		data : JSON.stringify(eventRecord),
		async : false,
		contentType : 'application/json',
		success : function(data, textStatus, request){
			console.log(textStatus);
			window.location.href="#start";
		},
		error : function(request, textStatus, error){
			console.log(error);
		}
	}); */
});
</script>


<#include "footer.ftl">

<#-- #include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header/>
b
<@footer/-->