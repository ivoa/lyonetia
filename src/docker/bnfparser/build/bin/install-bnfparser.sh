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
# Build and install bnfparser²
# http://bnfparser2.sourceforge.net/
#

program=bnfparser2
version=0.2.0
project=${program}-${version:?}

tarname=${project:?}
tarfile=${tarname:?}.tar
zipfile=${tarfile:?}.gz

patches=$(pwd)/src/patch

#
# Create our temp directory.
tmpdir=${program:?}-build
mkdir "${tmpdir:?}"
pushd "${tmpdir:?}"

    #
    # Download and unpack the source code.
    wget -O "${zipfile:?}" \
        "https://sourceforge.net/projects/${program:?}/files/${program:?}/${project:?}/${zipfile:?}/download"

    tar -xvzf "${zipfile:?}"

    #
    # Apply our patches.
    pushd "${project:?}"

        for patchfile in ${patches:?}/*
        do
            echo "Applying patch [${patchfile:?}]"
            patch -p 0 -u < "${patchfile:?}"
        done

    popd

    #
    # Build and install the library.
    pushd "${project:?}"

        autoconf
        ./configure --prefix=/usr/local
        gmake
        gmake install

    popd
popd

#
# Delete our temp directory.
rm -rf "${tmpdir:?}"

#
# Fix the shared library path.
# http://xahlee.info/UnixResource_dir/_/ldpath.html
# https://codeyarns.com/2014/01/14/how-to-add-library-directory-to-ldconfig-cache/
ldconfig /usr/local/lib


