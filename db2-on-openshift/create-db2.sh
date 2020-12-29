#!/bin/sh

oc create -f supp-db2-sa.yaml

oc adm policy add-scc-to-user privileged system:serviceaccount:zen:supp-db2-sa

oc create -f supp-db2-pvc.yaml

oc create -f supp-db2-depl.yaml

oc create -f supp-db2-svc.yaml

# End Of File