<#include "header.ftl">
<#if message?has_content>${message}</#if>
<h1>Please upload a file</h1>
        <form method="post" action="${context_url}/uploadCSV" enctype="multipart/form-data">
            <input type="file" name="file"/>
            <input type="submit"/>
        </form>
<#include "footer.ftl">
<#-- #include "header.ftl">
This is server content! remove
<#include "footer.ftl"-->

<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header/>
IMprove this text

<@footer/>
