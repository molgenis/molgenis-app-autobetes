<#include "headerOriginal.ftl">

<div class="container" style="margin-top: 20px;">
	<form id="upload-file-form" method="post" action="/menu/main/validate" enctype="multipart/form-data" onsubmit="parent.showSpinner(function(){$('.modal-body').html('Work in progress..');});  return true;">
	    <div class="row">
	        <div class="col-lg-6 col-sm-6 col-12">
				<div class="form-group">
				<h4>Step 1: Upload to Medtronic</h4>
				<span class="help-block">
					<p>Go to <a href="https://carelink.minimed.eu">https://carelink.minimed.eu</a> and log in.</p>
					<p>Upload your pump data.</p>
				</span>
				<h4>Step 2: Download to desktop</h4>
				<span class="help-block">
					<p>Select all data</p>
					<p>Save to your desktop</p>
				</span>
	            <h4>Step 3: Select file</h4>
	           
	            <div class="input-group">
	            
	                <span class="input-group-btn">
	                    <span class="btn btn-primary btn-file">
	                        Browse&hellip; <input type="file" name="csvFile">
	                    </span>
	                </span>
	                <input id="File text" type="text" class="form-control" readonly>
	            
	            
	            </div>
		
	            <span class="help-block">
	                Select the CSV you just downloaded.
	            </span>
			
				<h4>Step 4: Upload</h4>
				<button id="validateButton" type="submit" class="btn btn-default"><span class="glyphicon glyphicon-upload"></span> Upload</button>
	            <span class="help-block">
	                Upload
	            </span>
	        </div>        
		</div>
	</form>
</div>

<#include "footer.ftl">