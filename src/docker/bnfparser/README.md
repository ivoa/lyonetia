[Docker](https://www.docker.com "Docker's Homepage") container for working with the [bnfparser<sup>2</sup>](https://bnfparser2.sourceforge.net/) BNF parser.


## Running the container
The following command will start the container and give you a command line shell.

    docker run \
        --rm --tty \
        --interactive \
        "lyonetia/bnfparser" \
        bash

## Running the parser

By default, the command line shell will be started in the BNF src directory inside the container.

    pwd

This directory contains a **copy** of the BNF source files.

    ls
    
Because *&lt;stuff I haven't fixed yet&gt;*, you need to set the load library path before running the BNF parser. 

    export LD_LIBRARY_PATH=/usr/local/lib

To run the parser 










## Building the container
The following command will build the Docker container.

    docker build \
        --tag "lyonetia/bnfparser:1.0" \
        --tag "lyonetia/bnfparser:latest" \
        src/docker/bnfparser
