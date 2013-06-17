echo register OIM_SERVER Plugin.zip username password
%JAVA_HOME%\bin\java.exe -cp UsernamePolicy.jar;wlfullclient.jar;oimclient.jar;spring.jar;log4j-1.2.8.jar;commons-logging.jar no.steria.tad.PluginDeploy register $1 $2 $3 $4
