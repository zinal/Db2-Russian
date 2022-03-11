CREATE TABLE DWH.H_TEST(
    ID BIGINT NOT NULL,
    ID_SYSTEM BIGINT NOT NULL,
    ID_ORIGINAL VARCHAR(64),
    id_hash integer not null generated always as (integer(mod(id, 10)))
) ORGANIZE BY COLUMN
DISTRIBUTE BY HASH (ID);

/*
CREATE TABLE DWH.H_TEST(
    ID BIGINT NOT NULL,
    ID_SYSTEM BIGINT NOT NULL,
    ID_ORIGINAL VARCHAR(64),
    id_hash integer not null generated always as (integer(mod(id, 10)))
) ORGANIZE BY COLUMN
DISTRIBUTE BY HASH (ID);
*/

alter table dwh.h_test add constraint h_test_pk primary key(id, id_hash) enforced;

create unique index h_test_pk on dwh.h_test(id, id_hash);
