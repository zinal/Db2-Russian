#! /bin/sh

. ./cp4d-options.sh

rm -rf ${OFFLINEDIR}/*

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-cp-datacore-2.0.5.tgz \
  --outputdir ${OFFLINEDIR} \
  --no-dependency

cloudctl case launch \
  --case ${OFFLINEDIR}/ibm-cp-datacore-2.0.5.tgz \
  --inventory cpdPlatformOperator \
  --action configure-creds-airgap \
  --args "--registry ${PRIVATE_REGISTRY} --user ${PRIVATE_REGISTRY_USER} --pass ${PRIVATE_REGISTRY_PASSWORD}"

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-cp-common-services-1.6.0.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-cpd-scheduling-1.2.3.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-cognos-analytics-prod-4.0.4.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-cde-2.0.2.tgz \
  --outputdir ${OFFLINEDIR}

