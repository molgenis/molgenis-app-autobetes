<#assign alert=model.getAlert()>
<#if alert.show()>	
<DIV CLASS="alert alert-block <#if alert.isError()>alert-error<#elseif alert.isSuccess()>alert-success</#if> fade in">
        <button type="button" class="close" data-dismiss="alert">x</button>
        <strong>${alert.getTitle()}</strong> ${alert.getBody()} 
</div>
</#if>