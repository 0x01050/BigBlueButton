/* Copy of the file found at http://www.java.com/js/deployJava.js - January 18, 2015 */
var deployJava=function(){var l={core:["id","class","title","style"],i18n:["lang","dir"],events:["onclick","ondblclick","onmousedown","onmouseup","onmouseover","onmousemove","onmouseout","onkeypress","onkeydown","onkeyup"],applet:["codebase","code","name","archive","object","width","height","alt","align","hspace","vspace"],object:["classid","codebase","codetype","data","type","archive","declare","standby","height","width","usemap","name","tabindex","align","border","hspace","vspace"]};var b=l.object.concat(l.core,l.i18n,l.events);var m=l.applet.concat(l.core);function g(o){if(!d.debug){return}if(console.log){console.log(o)}else{alert(o)}}function k(p,o){if(p==null||p.length==0){return true}var r=p.charAt(p.length-1);if(r!="+"&&r!="*"&&(p.indexOf("_")!=-1&&r!="_")){p=p+"*";r="*"}p=p.substring(0,p.length-1);if(p.length>0){var q=p.charAt(p.length-1);if(q=="."||q=="_"){p=p.substring(0,p.length-1)}}if(r=="*"){return(o.indexOf(p)==0)}else{if(r=="+"){return p<=o}}return false}function e(){var o="//java.com/js/webstart.png";try{return document.location.protocol.indexOf("http")!=-1?o:"http:"+o}catch(p){return"http:"+o}}function n(p){var o="http://java.com/dt-redirect";if(p==null||p.length==0){return o}if(p.charAt(0)=="&"){p=p.substring(1,p.length)}return o+"?"+p}function j(q,p){var o=q.length;for(var r=0;r<o;r++){if(q[r]===p){return true}}return false}function c(o){return j(m,o.toLowerCase())}function i(o){return j(b,o.toLowerCase())}function a(o){if("MSIE"!=deployJava.browserName){return true}if(deployJava.compareVersionToPattern(deployJava.getPlugin().version,["10","0","0"],false,true)){return true}if(o==null){return false}return !k("1.6.0_33+",o)}var d={debug:null,version:"20120801",firefoxJavaVersion:null,myInterval:null,preInstallJREList:null,returnPage:null,brand:null,locale:null,installType:null,EAInstallEnabled:false,EarlyAccessURL:null,oldMimeType:"application/npruntime-scriptable-plugin;DeploymentToolkit",mimeType:"application/java-deployment-toolkit",launchButtonPNG:e(),browserName:null,browserName2:null,getJREs:function(){var t=new Array();if(this.isPluginInstalled()){var r=this.getPlugin();var o=r.jvms;for(var q=0;q<o.getLength();q++){t[q]=o.get(q).version}}else{var p=this.getBrowser();if(p=="MSIE"){if(this.testUsingActiveX("1.7.0")){t[0]="1.7.0"}else{if(this.testUsingActiveX("1.6.0")){t[0]="1.6.0"}else{if(this.testUsingActiveX("1.5.0")){t[0]="1.5.0"}else{if(this.testUsingActiveX("1.4.2")){t[0]="1.4.2"}else{if(this.testForMSVM()){t[0]="1.1"}}}}}}else{if(p=="Netscape Family"){this.getJPIVersionUsingMimeType();if(this.firefoxJavaVersion!=null){t[0]=this.firefoxJavaVersion}else{if(this.testUsingMimeTypes("1.7")){t[0]="1.7.0"}else{if(this.testUsingMimeTypes("1.6")){t[0]="1.6.0"}else{if(this.testUsingMimeTypes("1.5")){t[0]="1.5.0"}else{if(this.testUsingMimeTypes("1.4.2")){t[0]="1.4.2"}else{if(this.browserName2=="Safari"){if(this.testUsingPluginsArray("1.7.0")){t[0]="1.7.0"}else{if(this.testUsingPluginsArray("1.6")){t[0]="1.6.0"}else{if(this.testUsingPluginsArray("1.5")){t[0]="1.5.0"}else{if(this.testUsingPluginsArray("1.4.2")){t[0]="1.4.2"}}}}}}}}}}}}}if(this.debug){for(var q=0;q<t.length;++q){g("[getJREs()] We claim to have detected Java SE "+t[q])}}return t},installJRE:function(r,p){var o=false;if(this.isPluginInstalled()&&this.isAutoInstallEnabled(r)){var q=false;if(this.isCallbackSupported()){q=this.getPlugin().installJRE(r,p)}else{q=this.getPlugin().installJRE(r)}if(q){this.refresh();if(this.returnPage!=null){document.location=this.returnPage}}return q}else{return this.installLatestJRE()}},isAutoInstallEnabled:function(o){if(!this.isPluginInstalled()){return false}if(typeof o=="undefined"){o=null}return a(o)},isCallbackSupported:function(){return this.isPluginInstalled()&&this.compareVersionToPattern(this.getPlugin().version,["10","2","0"],false,true)},installLatestJRE:function(q){if(this.isPluginInstalled()&&this.isAutoInstallEnabled()){var r=false;if(this.isCallbackSupported()){r=this.getPlugin().installLatestJRE(q)}else{r=this.getPlugin().installLatestJRE()}if(r){this.refresh();if(this.returnPage!=null){document.location=this.returnPage}}return r}else{var p=this.getBrowser();var o=navigator.platform.toLowerCase();if((this.EAInstallEnabled=="true")&&(o.indexOf("win")!=-1)&&(this.EarlyAccessURL!=null)){this.preInstallJREList=this.getJREs();if(this.returnPage!=null){this.myInterval=setInterval("deployJava.poll()",3000)}location.href=this.EarlyAccessURL;return false}else{if(p=="MSIE"){return this.IEInstall()}else{if((p=="Netscape Family")&&(o.indexOf("win32")!=-1)){return this.FFInstall()}else{location.href=n(((this.returnPage!=null)?("&returnPage="+this.returnPage):"")+((this.locale!=null)?("&locale="+this.locale):"")+((this.brand!=null)?("&brand="+this.brand):""))}}return false}}},runApplet:function(p,u,r){if(r=="undefined"||r==null){r="1.1"}var t="^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?$";var o=r.match(t);if(this.returnPage==null){this.returnPage=document.location}if(o!=null){var q=this.getBrowser();if(q!="?"){if(this.versionCheck(r+"+")){this.writeAppletTag(p,u)}else{if(this.installJRE(r+"+")){this.refresh();location.href=document.location;this.writeAppletTag(p,u)}}}else{this.writeAppletTag(p,u)}}else{g("[runApplet()] Invalid minimumVersion argument to runApplet():"+r)}},writeAppletTag:function(r,w){var o="<"+"applet ";var q="";var t="<"+"/"+"applet"+">";var x=true;if(null==w||typeof w!="object"){w=new Object()}for(var p in r){if(!c(p)){w[p]=r[p]}else{o+=(" "+p+'="'+r[p]+'"');if(p=="code"){x=false}}}var v=false;for(var u in w){if(u=="codebase_lookup"){v=true}if(u=="object"||u=="java_object"||u=="java_code"){x=false}q+='<param name="'+u+'" value="'+w[u]+'"/>'}if(!v){q+='<param name="codebase_lookup" value="false"/>'}if(x){o+=(' code="dummy"')}o+=">";document.write(o+"\n"+q+"\n"+t)},versionCheck:function(p){var v=0;var x="^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?(\\*|\\+)?$";var y=p.match(x);if(y!=null){var r=false;var u=false;var q=new Array();for(var t=1;t<y.length;++t){if((typeof y[t]=="string")&&(y[t]!="")){q[v]=y[t];v++}}if(q[q.length-1]=="+"){u=true;r=false;q.length--}else{if(q[q.length-1]=="*"){u=false;r=true;q.length--}else{if(q.length<4){u=false;r=true}}}var w=this.getJREs();for(var t=0;t<w.length;++t){if(this.compareVersionToPattern(w[t],q,r,u)){return true}}return false}else{var o="Invalid versionPattern passed to versionCheck: "+p;g("[versionCheck()] "+o);alert(o);return false}},isWebStartInstalled:function(r){var q=this.getBrowser();if(q=="?"){return true}if(r=="undefined"||r==null){r="1.4.2"}var p=false;var t="^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?$";var o=r.match(t);if(o!=null){p=this.versionCheck(r+"+")}else{g("[isWebStartInstaller()] Invalid minimumVersion argument to isWebStartInstalled(): "+r);p=this.versionCheck("1.4.2+")}return p},getJPIVersionUsingMimeType:function(){for(var p=0;p<navigator.mimeTypes.length;++p){var q=navigator.mimeTypes[p].type;var o=q.match(/^application\/x-java-applet;jpi-version=(.*)$/);if(o!=null){this.firefoxJavaVersion=o[1];if("Opera"!=this.browserName2){break}}}},launchWebStartApplication:function(r){var o=navigator.userAgent.toLowerCase();this.getJPIVersionUsingMimeType();if(this.isWebStartInstalled("1.7.0")==false){if((this.installJRE("1.7.0+")==false)||((this.isWebStartInstalled("1.7.0")==false))){return false}}var u=null;if(document.documentURI){u=document.documentURI}if(u==null){u=document.URL}var p=this.getBrowser();var q;if(p=="MSIE"){q="<"+'object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" '+'width="0" height="0">'+"<"+'PARAM name="launchjnlp" value="'+r+'"'+">"+"<"+'PARAM name="docbase" value="'+u+'"'+">"+"<"+"/"+"object"+">"}else{if(p=="Netscape Family"){q="<"+'embed type="application/x-java-applet;jpi-version='+this.firefoxJavaVersion+'" '+'width="0" height="0" '+'launchjnlp="'+r+'"'+'docbase="'+u+'"'+" />"}}if(document.body=="undefined"||document.body==null){document.write(q);document.location=u}else{var t=document.createElement("div");t.id="div1";t.style.position="relative";t.style.left="-10000px";t.style.margin="0px auto";t.className="dynamicDiv";t.innerHTML=q;document.body.appendChild(t)}},createWebStartLaunchButtonEx:function(q,p){if(this.returnPage==null){this.returnPage=q}var o="javascript:deployJava.launchWebStartApplication('"+q+"');";document.write("<"+'a href="'+o+"\" onMouseOver=\"window.status=''; "+'return true;"><'+"img "+'src="'+this.launchButtonPNG+'" '+'border="0" /><'+"/"+"a"+">")},createWebStartLaunchButton:function(q,p){if(this.returnPage==null){this.returnPage=q}var o="javascript:"+"if (!deployJava.isWebStartInstalled(&quot;"+p+"&quot;)) {"+"if (deployJava.installLatestJRE()) {"+"if (deployJava.launch(&quot;"+q+"&quot;)) {}"+"}"+"} else {"+"if (deployJava.launch(&quot;"+q+"&quot;)) {}"+"}";document.write("<"+'a href="'+o+"\" onMouseOver=\"window.status=''; "+'return true;"><'+"img "+'src="'+this.launchButtonPNG+'" '+'border="0" /><'+"/"+"a"+">")},launch:function(o){document.location=o;return true},isPluginInstalled:function(){var o=this.getPlugin();if(o&&o.jvms){return true}else{return false}},isAutoUpdateEnabled:function(){if(this.isPluginInstalled()){return this.getPlugin().isAutoUpdateEnabled()}return false},setAutoUpdateEnabled:function(){if(this.isPluginInstalled()){return this.getPlugin().setAutoUpdateEnabled()}return false},setInstallerType:function(o){this.installType=o;if(this.isPluginInstalled()){return this.getPlugin().setInstallerType(o)}return false},setAdditionalPackages:function(o){if(this.isPluginInstalled()){return this.getPlugin().setAdditionalPackages(o)}return false},setEarlyAccess:function(o){this.EAInstallEnabled=o},isPlugin2:function(){if(this.isPluginInstalled()){if(this.versionCheck("1.6.0_10+")){try{return this.getPlugin().isPlugin2()}catch(o){}}}return false},allowPlugin:function(){this.getBrowser();var o=("Safari"!=this.browserName2&&"Opera"!=this.browserName2);return o},getPlugin:function(){this.refresh();var o=null;if(this.allowPlugin()){o=document.getElementById("deployJavaPlugin")}return o},compareVersionToPattern:function(v,p,r,t){if(v==undefined||p==undefined){return false}var w="^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?$";var x=v.match(w);if(x!=null){var u=0;var y=new Array();for(var q=1;q<x.length;++q){if((typeof x[q]=="string")&&(x[q]!="")){y[u]=x[q];u++}}var o=Math.min(y.length,p.length);if(t){for(var q=0;q<o;++q){if(y[q]<p[q]){return false}else{if(y[q]>p[q]){return true}}}return true}else{for(var q=0;q<o;++q){if(y[q]!=p[q]){return false}}if(r){return true}else{return(y.length==p.length)}}}else{return false}},getBrowser:function(){if(this.browserName==null){var o=navigator.userAgent.toLowerCase();g("[getBrowser()] navigator.userAgent.toLowerCase() -> "+o);if((o.indexOf("msie")!=-1)&&(o.indexOf("opera")==-1)){this.browserName="MSIE";this.browserName2="MSIE"}else{if(o.indexOf("trident")!=-1||o.indexOf("Trident")!=-1){this.browserName="MSIE";this.browserName2="MSIE"}else{if(o.indexOf("iphone")!=-1){this.browserName="Netscape Family";this.browserName2="iPhone"}else{if((o.indexOf("firefox")!=-1)&&(o.indexOf("opera")==-1)){this.browserName="Netscape Family";this.browserName2="Firefox"}else{if(o.indexOf("chrome")!=-1){this.browserName="Netscape Family";this.browserName2="Chrome"}else{if(o.indexOf("safari")!=-1){this.browserName="Netscape Family";this.browserName2="Safari"}else{if((o.indexOf("mozilla")!=-1)&&(o.indexOf("opera")==-1)){this.browserName="Netscape Family";this.browserName2="Other"}else{if(o.indexOf("opera")!=-1){this.browserName="Netscape Family";this.browserName2="Opera"}else{this.browserName="?";this.browserName2="unknown"}}}}}}}}g("[getBrowser()] Detected browser name:"+this.browserName+", "+this.browserName2)}return this.browserName},testUsingActiveX:function(o){var q="JavaWebStart.isInstalled."+o+".0";if(typeof ActiveXObject=="undefined"||!ActiveXObject){g("[testUsingActiveX()] Browser claims to be IE, but no ActiveXObject object?");return false}try{return(new ActiveXObject(q)!=null)}catch(p){return false}},testForMSVM:function(){var p="{08B0E5C0-4FCB-11CF-AAA5-00401C608500}";if(typeof oClientCaps!="undefined"){var o=oClientCaps.getComponentVersion(p,"ComponentID");if((o=="")||(o=="5,0,5000,0")){return false}else{return true}}else{return false}},testUsingMimeTypes:function(p){if(!navigator.mimeTypes){g("[testUsingMimeTypes()] Browser claims to be Netscape family, but no mimeTypes[] array?");return false}for(var q=0;q<navigator.mimeTypes.length;++q){s=navigator.mimeTypes[q].type;var o=s.match(/^application\/x-java-applet\x3Bversion=(1\.8|1\.7|1\.6|1\.5|1\.4\.2)$/);if(o!=null){if(this.compareVersions(o[1],p)){return true}}}return false},testUsingPluginsArray:function(p){if((!navigator.plugins)||(!navigator.plugins.length)){return false}var o=navigator.platform.toLowerCase();for(var q=0;q<navigator.plugins.length;++q){s=navigator.plugins[q].description;if(s.search(/^Java Switchable Plug-in (Cocoa)/)!=-1){if(this.compareVersions("1.5.0",p)){return true}}else{if(s.search(/^Java/)!=-1){if(o.indexOf("win")!=-1){if(this.compareVersions("1.5.0",p)||this.compareVersions("1.6.0",p)){return true}}}}}if(this.compareVersions("1.5.0",p)){return true}return false},IEInstall:function(){location.href=n(((this.returnPage!=null)?("&returnPage="+this.returnPage):"")+((this.locale!=null)?("&locale="+this.locale):"")+((this.brand!=null)?("&brand="+this.brand):""));return false},done:function(p,o){},FFInstall:function(){location.href=n(((this.returnPage!=null)?("&returnPage="+this.returnPage):"")+((this.locale!=null)?("&locale="+this.locale):"")+((this.brand!=null)?("&brand="+this.brand):"")+((this.installType!=null)?("&type="+this.installType):""));return false},compareVersions:function(r,t){var p=r.split(".");var o=t.split(".");for(var q=0;q<p.length;++q){p[q]=Number(p[q])}for(var q=0;q<o.length;++q){o[q]=Number(o[q])}if(p.length==2){p[2]=0}if(p[0]>o[0]){return true}if(p[0]<o[0]){return false}if(p[1]>o[1]){return true}if(p[1]<o[1]){return false}if(p[2]>o[2]){return true}if(p[2]<o[2]){return false}return true},enableAlerts:function(){this.browserName=null;this.debug=true},poll:function(){this.refresh();var o=this.getJREs();if((this.preInstallJREList.length==0)&&(o.length!=0)){clearInterval(this.myInterval);if(this.returnPage!=null){location.href=this.returnPage}}if((this.preInstallJREList.length!=0)&&(o.length!=0)&&(this.preInstallJREList[0]!=o[0])){clearInterval(this.myInterval);if(this.returnPage!=null){location.href=this.returnPage}}},writePluginTag:function(){var o=this.getBrowser();if(o=="MSIE"){document.write("<"+'object classid="clsid:CAFEEFAC-DEC7-0000-0001-ABCDEFFEDCBA" '+'id="deployJavaPlugin" width="0" height="0">'+"<"+"/"+"object"+">")}else{if(o=="Netscape Family"&&this.allowPlugin()){this.writeEmbedTag()}}},refresh:function(){navigator.plugins.refresh(false);var o=this.getBrowser();if(o=="Netscape Family"&&this.allowPlugin()){var p=document.getElementById("deployJavaPlugin");if(p==null){this.writeEmbedTag()}}},writeEmbedTag:function(){var o=false;if(navigator.mimeTypes!=null){for(var p=0;p<navigator.mimeTypes.length;p++){if(navigator.mimeTypes[p].type==this.mimeType){if(navigator.mimeTypes[p].enabledPlugin){document.write("<"+'embed id="deployJavaPlugin" type="'+this.mimeType+'" hidden="true" />');o=true}}}if(!o){for(var p=0;p<navigator.mimeTypes.length;p++){if(navigator.mimeTypes[p].type==this.oldMimeType){if(navigator.mimeTypes[p].enabledPlugin){document.write("<"+'embed id="deployJavaPlugin" type="'+this.oldMimeType+'" hidden="true" />')}}}}}}};d.writePluginTag();if(d.locale==null){var h=null;if(h==null){try{h=navigator.userLanguage}catch(f){}}if(h==null){try{h=navigator.systemLanguage}catch(f){}}if(h==null){try{h=navigator.language}catch(f){}}if(h!=null){h.replace("-","_");d.locale=h}}return d}();