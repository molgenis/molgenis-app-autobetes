#!/bin/sh
rm -r ~/.molgenis/$1/data/elasticsearch
/usr/local/bin/mysql -u root -e "drop database $1"
/usr/local/bin/mysql -u root -e "create database $1”