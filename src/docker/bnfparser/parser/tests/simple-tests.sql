
    SELECT value_name FROM table_name ;

    SELECT value_name AS alias FROM table_name ;

    SELECT value_name AS alias FROM table_name AS table_alias;

    SELECT value_name AS alias, value_name AS alias FROM table_name ;

    SELECT value_name AS alias, value_name AS alias FROM table_name AS alias;

    SELECT value_name AS alias, value_name AS alias FROM table_name, table_name ;

    SELECT value_name AS alias, value_name AS alias FROM table_name AS alias, table_name AS alias;


    SELECT
        value_name
    FROM
        table_name 
        ;

    SELECT
        value_name,
        value_name
    FROM
        table_name,
        table_name 
        ;

    SELECT
        value_name AS alias
    FROM
        table_name AS alias
        ;

    SELECT
        value_name AS alias,
        value_name AS alias
    FROM
        table_name AS alias,
        table_name AS alias
        ;


    SELECT
        value_name_one
    FROM
        table_name_one 
        ;

    SELECT
        value_name_one,
        value_name_two
    FROM
        table_name_one,
        table_name_two
        ;

    SELECT
        value_name,
        value_name_123
    FROM
        table_name,
        table_name_123 
        ;


    SELECT
        "value_name-one"
    FROM
        "table_name-one"
        ;

    SELECT
        "value_name-one",
        "value_name+two"
    FROM
        "table_name-one",
        "table_name+two"
        ;

    SELECT
        "value_name[123]",
        "value_name(123)"
    FROM
        "table_name[123]",
        "table_name(123)"
        ;



