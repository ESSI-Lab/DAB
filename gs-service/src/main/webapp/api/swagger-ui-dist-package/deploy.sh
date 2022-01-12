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
# Deploy `swagger-ui-dist` to npm.

# Parameter Expansion: http://stackoverflow.com/questions/6393551/what-is-the-meaning-of-0-in-a-bash-script
cd "${0%/*}"

# Get UI version
UI_VERSION=$(node -p "require('../package.json').version")

# Replace our version placeholder with UI's version
sed -i "s|\$\$VERSION|$UI_VERSION|g" package.json

# Copy UI's dist files to our directory
cp ../dist/* .

if [ "$PUBLISH_DIST" = "true" ] || [ "$TRAVIS" = "true" ] ; then
  npm publish .
else
  npm pack .
fi

find . -not -name .npmignore -not -name .npmrc -not -name deploy.sh -not -name index.js -not -name package.json -not -name README.md -not -name *.tgz -delete
