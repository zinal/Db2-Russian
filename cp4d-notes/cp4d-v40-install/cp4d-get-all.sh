#! /bin/sh

. ./cp4d-options.sh

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-cognos-analytics-prod-4.0.4.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-cde-2.0.2.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-dv-case-1.7.2.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-dmc-4.0.2.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-db2wh-4.0.3.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-hadoop-4.0.2.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-spss-1.0.2.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-wkc-4.0.2.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-wml-cpd-4.0.3.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-wsl-2.0.2.tgz \
  --outputdir ${OFFLINEDIR}

cloudctl case save \
  --case ${CASE_REPO_PATH}/ibm-wsl-runtimes-1.0.2.tgz \
  --outputdir ${OFFLINEDIR}
