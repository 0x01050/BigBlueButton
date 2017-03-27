<#-- GET_RECORDINGS FreeMarker XML template -->
<#compress>
<response>
    <#-- Where code is a 'SUCCESS' or 'FAILED' String -->
    <returncode>${returnCode}</returncode>
    <recordings>
        <#list recordings as r>
            <recording>
                <#if r.hasError()>
                    <error>
                        <recordID>${r.getMetadataXml()?html}</recordID>
                    </error>
                <#else>
                    <#include "include-recording.ftl">
                </#if>
            </recording>
        </#list>
    </recordings>
</response>
</#compress>