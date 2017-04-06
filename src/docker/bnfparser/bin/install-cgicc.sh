#!/bin/bash -eu
# -e: Exit immediately if a command exits with a non-zero status.
# -u: Treat unset variables as an error when substituting.
#
#  Copyright (C) 2017 Royal Observatory, University of Edinburgh, UK
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# Install the GNU cgicc library (https://www.gnu.org/software/cgicc/index.html)
#

program=cgicc
version=3.2.16
project=${program}-${version:?}

tarname=${project:?}
tarfile=${tarname:?}.tar
zipfile=${tarfile:?}.gz

#
# Create our temp directory.
tmpdir=$(mktemp -d)
pushd "${tmpdir:?}"

    #
    # Download and unpack the source code.
    wget -O "${zipfile:?}" \
        "http://ftp.gnu.org/gnu/${program:?}/${zipfile:?}"

    tar -xvzf "${zipfile:?}"

    #
    # Build and install the library.
    pushd "${project:?}"

        ./configure
        make
        make install

    popd
popd

#
# Delete our temp directory.
rm -rf "${tmpdir:?}"

