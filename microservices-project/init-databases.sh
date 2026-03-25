#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE DATABASE productdb;
  CREATE DATABASE inventorydb;
  CREATE DATABASE orderdb;
EOSQL
