#! /bin/sh

. ./cp4d-options.sh

cloudctl case launch \
  --case ${OFFLINEDIR}/ibm-cp-datacore-2.0.5.tgz \
  --inventory cpdPlatformOperator \
  --action mirror-images \
  --args "--registry ${PRIVATE_REGISTRY} --user ${PRIVATE_REGISTRY_USER} --pass ${PRIVATE_REGISTRY_PASSWORD} --inputDir ${OFFLINEDIR}"

