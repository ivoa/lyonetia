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
# Create our build directory.
RUN mkdir /tmp/build
#VOLUME   /build
WORKDIR   /tmp/build

#
# Add our build scripts.
COPY build/bin bin
RUN  chmod -R a+rx bin/*

#
# Add our patch files.
COPY build/src src

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
# Create our parser directory.
RUN mkdir -p /bnfparser

#
# Add our parser source files.
COPY parser /bnfparser

#
# Set the working directory.
WORKDIR /bnfparser

#
# Remove our build directory.
#RUN rm -rf /tmp/build

