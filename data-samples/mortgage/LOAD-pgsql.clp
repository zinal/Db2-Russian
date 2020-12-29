psql -h localhost -U pguser -W db

\copy "MORTGAGE"."MORTGAGE_APPLICANT" from 'MORTGAGE.MORTGAGE_APPLICANT.del' with (format CSV, delimiter E'\t');
\copy "MORTGAGE"."MORTGAGE_CUSTOMER" from 'MORTGAGE.MORTGAGE_CUSTOMER.del' with (format CSV, delimiter E'\t');
\copy "MORTGAGE"."MORTGAGE_DEFAULT" from 'MORTGAGE.MORTGAGE_DEFAULT.del' with (format CSV, delimiter E'\t');
\copy "MORTGAGE"."MORTGAGE_PROPERTY" from 'MORTGAGE.MORTGAGE_PROPERTY.del' with (format CSV, delimiter E'\t');
