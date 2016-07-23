#!/usr/bin/env bash

docker-compose --file travis/docker-compose.yml up -d
docker exec $(docker-compose --file travis/docker-compose.yml ps -q cubrid) /bin/bash -c 'mkdir -p ~/CUBRID_databases/demodb && cd $_ && cubrid createdb --db-volume-size=100M --log-volume-size=100M demodb en_US.iso88591 && cubrid loaddb -u dba -s $CUBRID/demo/demodb_schema -d $CUBRID/demo/demodb_objects demodb'
docker exec -d $(docker-compose --file travis/docker-compose.yml ps -q cubrid) /bin/bash -c 'cubrid server start demodb'
docker exec $(docker-compose --file travis/docker-compose.yml ps -q db2) runuser -l db2inst1 -c "db2 create db sample"
docker exec $(docker-compose --file travis/docker-compose.yml ps -q firebird) /usr/local/firebird/bin/isql -u sysdba -p masterkey -i /docker-entrypoint-initdb.d/firebird.sql
docker ps
