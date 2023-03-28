do $$
    declare
        sql_text text;
    begin
        SELECT 'DROP TABLE ' || string_agg(format('%I.%I', schemaname, tablename), ', ') into sql_text
        FROM   pg_catalog.pg_tables t
        WHERE  schemaname = 'public';

        execute sql_text;
    end;
$$