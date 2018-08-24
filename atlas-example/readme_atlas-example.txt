1. access to atlas web console

launch browser chrome and open ambari portal by link 'https://safebaolei.fenxibao.com/index.php', user/password is admin/admin

select atlas service by clicking in the leftmost service list, click 'quick links' on the central top and then open atlas portal in another chrome page.

the mapping from server name and ip address has been configured on the remote-login server, 

so, you can also manually open atlas page with link 'https://safebaolei.fenxibao.com/index.php', with account admin/admin



2. compile module 'atlas-example'

git pull the code, move to path ./shuyun.bigdata, and then launch maven compiling

mvn -pl ./atlas-example/ -am -DskipTests package

after successful compling, copy target jar files onto edge01 node

scp -P 2222 ./target/*.jar root@edge01.bigdata.shuyun.com:/tmp



3. execute the Java code

for adding lineage and entities, submit following command

java -Datlas.log.dir=/var/log/atlas -Datlas.log.file=quick_start.log -Datlas.home=/usr/hdp/2.6.5.0-292/atlas -Dlog4j.configuration=atlas-log4j.xml  -cp /usr/hdp/2.6.5.0-292/atlas/conf:/usr/hdp/current/atlas-server/server/webapp/atlas/WEB-INF/classes:/usr/hdp/current/atlas-server/server/webapp/atlas/WEB-INF/lib/*:/usr/hdp/2.6.5.0-292/atlas/libext/*:/tmp/atlas-example-1.0-SNAPSHOT.jar example.altas.LineageAddExample http://edge01.bigdata.shuyun.com:21000 


and you'll be required to input atlas administer account admin/admin


to delete the lineage and entities, type the following

java -Datlas.log.dir=/var/log/atlas -Datlas.log.file=quick_start.log -Datlas.home=/usr/hdp/2.6.5.0-292/atlas -Dlog4j.configuration=atlas-log4j.xml -cp /usr/hdp/2.6.5.0-292/atlas/conf:/usr/hdp/current/atlas-server/server/webapp/atlas/WEB-INF/classes:/usr/hdp/current/atlas-server/server/webapp/atlas/WEB-INF/lib/*:/usr/hdp/2.6.5.0-292/atlas/libext/*:/tmp/atlas-example-1.0-SNAPSHOT.jar example.altas.LineageDeleteExample http://edge01.bigdata.shuyun.com:21000 


and you'll be required to input atlas administer account admin/admin


4. check lineage on atlas portal

go back to atlas webpage, search type 'Table' and click any of tables listed in the right, you will find it's lineage and attributes, tags as well as the schema
