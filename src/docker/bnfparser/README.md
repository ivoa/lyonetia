# Docker container for bnfparser<sup>2</sup>

This patr of the project creates a [Docker](https://www.docker.com "Docker's Homepage") container for the [bnfparser<sup>2</sup>](http://bnfparser2.sourceforge.net/) parser.


## Running the container
The following command will start the container and give you a command line shell:

    docker run \
        --rm --tty \
        --interactive \
        "lyonetia/bnfparser" \
        bash

## Running the parser

By default, the command line shell will be started in the BNF src directory inside the container.

    pwd
        /bnfparser/src

This directory contains a **copy** of the BNF source files.

    ls
        adql.ebnf
        patch
        sample
        syntax
    
The following command will run the parser, using the **copy** of the `adql.ebnf` grammar packaged in the container:

    bnfcheck '<query>' adql.ebnf

The literal string `<query>` should match the [top level term](src/adql.ebnf#L47) defined in the grammar, which indicates which grammar element the input should match.

You can then enter test queries in the command line to see if they parse correctly:

    SELECT value
    SELECT value, value
    SELECT value AS alias
    SELECT value AS alias, value AS alias

Which, if everything is working correctly, should produce the following results:

    --------------------NEXT WORD--------------------
    SELECT value
    [1] passed
    --------------------NEXT WORD--------------------
    SELECT value, value
    [2] passed
    --------------------NEXT WORD--------------------
    SELECT value AS alias
    [3] passed
    --------------------NEXT WORD--------------------
    SELECT value AS alias, value AS alias
    [4] passed

Control 'C' will exit the parser.

    Ctrl^C

Typing `exit` at the command line to exit the container.

    exit

## Multi-line input

In order to test multi-line queries, you need to override the default input delimiter.

The following command will run the parser, using semicolon `;` (character 59) as the input delimiter:

    bnfcheck '<query>' --delimiter=59 adql.ebnf

You can now test multi-line queries, terminating each query with a semicolon.

    SELECT
        value
        ;

    SELECT
        value,
        value
        ;

    SELECT
        value AS alias
        ;

    SELECT
        value AS alias,
        value AS alias
        ;

## Working on the BNF
The commands given above will run the using a **copy** of the `adql.ebnf` grammar packaged in the container image.

If you want to work on the `adql.ebnf` grammar and commit the changes back to this GitHub project, then the easiest way is to use a `--volume` option to mount the `src` directory from the host over the `src` directory in the container.

    docker run \
        --rm --tty \
        --interactive \
        --volume "$(pwd)/src/docker/bnfparser/src:/bnfparser/src:ro" \
        "lyonetia/bnfparser" \
        bash

You can edit the `adql.ebnf` grammar on the host using your normal text editor, and the updated file will be visible from inside the container. Note that the `ro` suffix will prevent the container from writing to the `src` directory.

## Building the container
The following command will build the Docker container.

    docker build \
        --tag "lyonetia/bnfparser:latest" \
        --tag "lyonetia/bnfparser:${buildtag:?}" \
        src/docker/bnfparser

You should only need to do this if you are actually modifying the bnfparser<sup>2</sup> code or updating the contents of the container.
