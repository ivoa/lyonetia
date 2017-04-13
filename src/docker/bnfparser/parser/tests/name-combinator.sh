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

cat_names=('catalog_name' '"catalog-name"')
sch_names=('schema_name' '"schema-name"')
tab_names=('table_name' '"table-name"')
col_names=('column_name' '"column-name"')

query_print()
    {
    local value_name=${1}
    local value_alias=${2}
    local table_name=${3}
    local table_alias=${4}

    echo ""

    if [ -n "${value_alias}" ]
    then
        echo "SELECT"
        echo "  ${value_name} AS ${value_alias}"
    else
        echo "SELECT"
        echo "  ${value_name}"
    fi

    if [ -n "${table_alias}" ]
    then
        echo "FROM"
        echo "  ${table_name} AS ${table_alias}"
    else
        echo "FROM"
        echo "  ${table_name}"
    fi

    echo ";"

    }

table_loop()
    {
    local value_name=${1}
    local value_alias=${2}
    local table_alias=${3}
    local tab_name
    local sch_name
    local cat_name

    for tab_name in ${tab_names[@]}
    do

        query_print "${value_name}" "${value_alias}" "${tab_name}" "${table_alias}"

        for sch_name in ${sch_names[@]}
        do

            query_print "${value_name}" "${value_alias}" "${sch_name}.${tab_name}" "${table_alias}"

            for cat_name in ${cat_names[@]}
            do

                query_print "${value_name}" "${value_alias}" "${cat_name}.${sch_name}.${tab_name}" "${table_alias}"

            done
        done
    done
    }

value_loop()
    {
    local value_alias=${1}
    local table_alias=${2}
    local col_name
    local tab_name
    local sch_name
    local cat_name
    
    for col_name in ${col_names[@]}
    do

        table_loop "${col_name}" "${value_alias}"

        for tab_name in ${tab_names[@]}
        do

            table_loop "${tab_name}.${col_name}" "${value_alias}" "${table_alias}"

            for sch_name in ${sch_names[@]}
            do

                table_loop "${sch_name}.${tab_name}.${col_name}" "${value_alias}" "${table_alias}"

                for cat_name in ${cat_names[@]}
                do

                    table_loop "${cat_name}.${sch_name}${tab_name}.${col_name}" "${value_alias}" "${table_alias}"

                done
            done
        done

        if [ -n "${table_alias}" ]
        then
            table_loop "${table_alias}.${col_name}" "${value_alias}" "${table_alias}"
        fi

    done
    }

select_loop()
    {
    value_loop '' ''
    value_loop 'value_alias' ''
    value_loop '"value-alias"' ''

    value_loop '' 'table_alias'
    value_loop 'value_alias' 'table_alias'
    value_loop '"value-alias"' 'table_alias'

    value_loop '' '"table-alias"'
    value_loop 'value_alias' '"table-alias"'
    value_loop '"value-alias"' '"table-alias"'

    }



