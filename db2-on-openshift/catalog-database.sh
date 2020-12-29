#! /bin/sh

db2 catalog tcpip node local5k remote localhost server 5000
db2 catalog db db as pubocp at node local5k

# *** with port forwarding running:
# db2 connect to pubocp user db2inst1 using "passw0rd"
