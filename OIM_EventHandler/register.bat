"c:\Program Files\Java\jdk1.7.0_13\bin\jar.exe" cvf deploy\lib\UsernamePolicy.jar -C bin .
cd c:\oracle\deploy
"C:\Program Files\7-Zip\7z.exe" a ..\UsernamePolicyPlugin.zip .
cd c:\oracle
"c:\Program Files\Java\jdk1.7.0_13\bin\java.exe" -cp bin;lib\wlfullclient.jar;lib\oimclient.jar;lib\spring.jar;lib\log4j-1.2.8.jar;lib\commons-logging.jar no.steria.tad.PluginDeploy register UsernamePolicyPlugin.zip
