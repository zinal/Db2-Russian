#! /bin/sh

#oc policy add-role-to-user registry-editor <user_name>
#sudo sysctl -w kernel.unprivileged_userns_clone=1

podman pull ibmcom/db2

HOST=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')
podman tag ibmcom/db2 image-registry.openshift-image-registry.svc:5000/zen/db2
podman tag ibmcom/db2 "$HOST"/zen/db2

podman login -u $(oc whoami) -p $(oc whoami -t) --tls-verify=false $HOST
podman push "$HOST"/zen/db2 --tls-verify=false
