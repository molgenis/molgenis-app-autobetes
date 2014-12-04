<#include "header.ftl">
<#if message?has_content><h1>${message}</h1></#if>
<h2>Welcome ${username}</h2>
  <p>The code: ${code}</p>
  <p>The token: ${token}</p>
<#include "footer.ftl">

<#-- #include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header/>
b
<@footer/-->