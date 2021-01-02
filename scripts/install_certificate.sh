#!/usr/bin/env sh

KEYSTORE_FILE=dest_keystore
KEYSTORE_PASS=changeit


import_cert() {
  local HOST=$1
  local PORT=$2

  # get the SSL certificate
  openssl s_client -connect ${HOST}:${PORT} </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ${HOST}.cert

  # delete the old alias and then import the new one
  keytool -delete -keystore ${KEYSTORE_FILE} -storepass ${KEYSTORE_PASS} -alias ${HOST} &> /dev/null

  # create a keystore and import certificate
  keytool -import -noprompt -trustcacerts \
      -alias ${HOST} -file ${HOST}.cert \
      -keystore ${KEYSTORE_FILE} -storepass ${KEYSTORE_PASS}

  rm ${HOST}.cert
}

import_cert amxremit.com 443
import_cert www.amxremit.com 443
import_cert jaxd1-kwt.amxremit.com 443 # google
import_cert graph.facebook.com 443 