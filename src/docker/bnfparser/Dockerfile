#
# Copyright (c) 2017, ROE (http://www.roe.ac.uk/)
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

FROM firethorn/fedora:2.1.8
MAINTAINER Dave Morris <docker-admin@metagrid.co.uk>

#
# Create our project directory.
RUN mkdir -p /bnfparser
WORKDIR /bnfparser

#
# Add our binary files.
COPY bin bin
RUN  chmod -R a+x bin/*

#
# Add our source files.
COPY src  src

#
# Install man, diff and patch.
RUN dnf install -y \
    man \
    diffutils \
    patch

#
# Install make.
RUN dnf install -y \
    make \
    automake

#
# Install C++ compiler.
RUN dnf install -y \
    gcc \
    gcc-c++

#
# Install the pcrecpp library.
# http://www.pcre.org
RUN dnf install -y \
    pcre \
    pcre-cpp \
    pcre-devel

#
# Build and install the GNU cgicc library.
# https://www.gnu.org/software/cgicc/index.html
RUN bin/install-cgicc.sh

#
# Download and install the SimpleOpt header.
# https://github.com/brofield/simpleopt
RUN bin/install-simpleopt.sh

# Build and install the bnfparser²
# http://bnfparser2.sourceforge.net/
RUN bin/install-bnfparser.sh

#
# Set the working directory.
WORKDIR /bnfparser/src












