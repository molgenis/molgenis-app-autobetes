echo "cp mysql-connector-java-5.1.28.jar apache-tomcat-7.0.55/lib/"
echo "cp setenv.sh apache-tomcat/bin/"
echo "create .molgenis/ folder to server"
echo "cleanup.sh"
#dit heb ik staan in ~/bin
#en die dan op het pad
#en het scriptje ge chmod a+x"

# Strange, but we have to create / fill table on server before deploy:
#/usr/bin/mysql -umolgenis -pmolgenis -e "USE autobetes; CREATE TABLE \`SEQUENCE\` (   \`SEQ_NAME\` varchar(50) NOT NULL,   \`SEQ_COUNT\` decimal(38,0) DEFAULT NULL,   PRIMARY KEY (\`SEQ_NAME\`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;"
#/usr/bin/mysql -umolgenis -pmolgenis -e "USE autobetes; INSERT INTO SEQUENCE VALUES('SEQ_GEN',1);"