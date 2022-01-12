#! /bin/sh
# #%L
# Discovery and Access Broker (DAB) Community Edition (CE)
# %%
# Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
# %%
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# #L%
###

set -e

NGINX_ROOT=/usr/share/nginx/html
INDEX_FILE=$NGINX_ROOT/index.html

replace_in_index () {
  if [ "$1" != "**None**" ]; then
    sed -i "s|/\*||g" $INDEX_FILE
    sed -i "s|\*/||g" $INDEX_FILE
    sed -i "s|$1|$2|g" $INDEX_FILE
  fi
}

replace_or_delete_in_index () {
  if [ -z "$2" ]; then
    sed -i "/$1/d" $INDEX_FILE
  else
    replace_in_index $1 $2
  fi
}

replace_in_index myApiKeyXXXX123456789 $API_KEY
replace_or_delete_in_index your-client-id $OAUTH_CLIENT_ID
replace_or_delete_in_index your-client-secret-if-required $OAUTH_CLIENT_SECRET
replace_or_delete_in_index your-realms $OAUTH_REALM
replace_or_delete_in_index your-app-name $OAUTH_APP_NAME
if [ "$OAUTH_ADDITIONAL_PARAMS" != "**None**" ]; then
    replace_in_index "additionalQueryStringParams: {}" "additionalQueryStringParams: {$OAUTH_ADDITIONAL_PARAMS}"
fi

if [[ -f $SWAGGER_JSON ]]; then
  cp $SWAGGER_JSON $NGINX_ROOT
  REL_PATH="/$(basename $SWAGGER_JSON)"
  sed -i "s|http://petstore.swagger.io/v2/swagger.json|$REL_PATH|g" $INDEX_FILE
  sed -i "s|http://example.com/api|$REL_PATH|g" $INDEX_FILE
else
  sed -i "s|http://petstore.swagger.io/v2/swagger.json|$API_URL|g" $INDEX_FILE
  sed -i "s|http://example.com/api|$API_URL|g" $INDEX_FILE
fi

if [[ -n "$VALIDATOR_URL" ]]; then
  sed -i "s|.*validatorUrl:.*$||g" $INDEX_FILE
  TMP_VU="$VALIDATOR_URL"
  [[ "$VALIDATOR_URL" != "null" && "$VALIDATOR_URL" != "undefined" ]] && TMP_VU="\"${VALIDATOR_URL}\""
  sed -i "s|\(url: .*,\)|\1\n    validatorUrl: ${TMP_VU},|g" $INDEX_FILE
  unset TMP_VU
fi

exec nginx -g 'daemon off;'
