<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header/>

<#if message?has_content><h1>${message}</h1></#if>

<a href="autobetes://"  style="font-size: 200%;">Click here to go back to the autobetes app!</a>

<@footer/>

1. signed in? False -> Sign in or Register
2. activated? False -> Go to email to activate ur account OR Button voor Logout
3. Moves installed? Button voor Logout
4. Moves connected? Button voor Logout