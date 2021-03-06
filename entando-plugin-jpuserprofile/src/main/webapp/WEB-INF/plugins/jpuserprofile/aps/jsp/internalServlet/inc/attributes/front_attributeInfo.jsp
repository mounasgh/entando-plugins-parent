<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="/aps-core" prefix="wp" %>

<s:if test="#attribute.required || (#attribute.textAttribute && (#attribute.minLength != -1 || #attribute.maxLength != -1))">
&#32;<span class="monospace">&#32;[&#32;
<s:if test="#attribute.required">
<abbr title="<wp:i18n key="jpuserprofile_ENTITY_ATTRIBUTE_MANDATORY_FULL" />"><wp:i18n key="jpuserprofile_ENTITY_ATTRIBUTE_MANDATORY_SHORT" /></abbr>
</s:if>
<s:if test="#attribute.textAttribute">
<s:if test="#attribute.minLength != -1">
&#32;<abbr title="<wp:i18n key="jpuserprofile_ENTITY_ATTRIBUTE_MINLENGTH_FULL" />" ><wp:i18n key="jpuserprofile_ENTITY_ATTRIBUTE_MINLENGTH_SHORT" /></abbr>:&#32;<s:property value="#attribute.minLength" />
</s:if>
<s:if test="#attribute.maxLength != -1">
&#32;<abbr title="<wp:i18n key="jpuserprofile_ENTITY_ATTRIBUTE_MAXLENGTH_FULL" />" ><wp:i18n key="jpuserprofile_ENTITY_ATTRIBUTE_MAXLENGTH_SHORT" /></abbr>:&#32;<s:property value="#attribute.maxLength" />
</s:if>
</s:if>   
&#32;]</span>
</s:if>
