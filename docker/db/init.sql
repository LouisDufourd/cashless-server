-- =========================================================
-- CLEANUP SECTION (DROP EXISTING TABLES)
-- =========================================================
drop table if exists token_session;
drop table if exists transaction_logs_archive;
drop table if exists cards_archive;
drop table if exists volunteers_archive;
drop table if exists users_archive;
drop table if exists articles_archive;
drop table if exists roles_archive;
drop table if exists civilities_archive;
drop table if exists stands_archive;

drop table if exists transaction_logs;
drop table if exists cards;
drop table if exists inventory;
drop table if exists volunteers;
drop table if exists users;
drop table if exists articles;
drop table if exists roles;
drop table if exists civilities;
drop table if exists stands;


-- =========================================================
-- TABLE CREATION SECTION (DATABASE STRUCTURE)
-- =========================================================

-- -------------------------
-- Token Session
-- -------------------------
create table token_session(
	id              serial4         not null    primary key,
    volunteer_id    int4            not null,
    refresh_jti     varchar(255)    not null    unique,
    revoked         boolean         not null    default false,
	created_at      timestamp       not null    default now(),
    expire_at       timestamp       not null
);


-- -------------------------
-- STANDS
-- -------------------------
create table stands(
	id              serial4         not null    primary key,
	name            varchar(45)     not null
);

-- -------------------------
-- CIVILITIES
-- -------------------------
create table civilities(
	id              serial4         not null    primary key,
	lastname        varchar(45)     not null,
	firstname       varchar(45)     not null,
	age             int4            not null,
	unique (firstname, lastname, age)
);

-- -------------------------
-- ROLES
-- -------------------------
create table roles(
	id              serial4         not null    primary key,
	name            varchar(45)     not null    unique
);

-- -------------------------
-- ARTICLES
-- -------------------------
create table articles(
	id              serial4         not null    primary key,
	name            varchar(45)     not null    unique
);

-- -------------------------
-- USERS
-- -------------------------
create table users(
	id              serial4         not null    primary key,
	username        varchar(45)     not null    unique,
	password        varchar(255)    not null
);

-- -------------------------
-- VOLUNTEERS
-- -------------------------
create table volunteers(
	id              serial4         not null    primary key,
	username        varchar(45)     not null    unique,
	password        varchar(255)    not null,
	fk_role_id      int4            not null    references roles(id),
	fk_civility_id  int4            not null    references civilities(id) 	on delete cascade,
	fk_stand_id     int4            null        references stands(id) 		on delete set null
);

-- -------------------------
-- INVENTORY
-- -------------------------
create table inventory(
	id              serial4         not null    primary key,
	quantity        int4            not null    default 0,
	price           numeric(10,2)   not null    default 0.00,
	fk_article_id   int4            not null    references articles(id)		on delete cascade,
	fk_stand_id     int4            not null    references stands(id)		on delete cascade,
	unique(fk_article_id, fk_stand_id)
);

-- -------------------------
-- CARDS
-- -------------------------
create table cards(
	id              serial4         not null    primary key,
	pin             int4            not null,
	balance         numeric(10,2)   not null    default 0.00,
	nfc             varchar(45)     not null    unique,
	fk_user_id      int4            null        references users(id)	on delete set null
);

-- -------------------------
-- TRANSACTION LOGS
-- -------------------------
create table transaction_logs(
	id              serial4         not null    primary key,
	date            timestamp       not null    default now(),
	amount          numeric(10,2)   not null,
	fk_user_id      int4            null        references users(id)	on delete set null,
	fk_card_id      int4            not null    references cards(id)	on delete cascade,
	fk_stand_id     int4            not null    references stands(id)	on delete cascade
);


-- =========================================================
-- ARCHIVE TABLE SECTION
-- =========================================================

-- -------------------------
-- STANDS ARCHIVE
-- -------------------------
create table stands_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	name            varchar(45)     not null
);

-- -------------------------
-- CIVILITIES ARCHIVE
-- -------------------------
create table civilities_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	lastname        varchar(45)     not null,
	firstname       varchar(45)     not null,
	age             int4            not null
);

-- -------------------------
-- ROLES ARCHIVE
-- -------------------------
create table roles_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	name            varchar(45)     not null
);

-- -------------------------
-- ARTICLES ARCHIVE
-- -------------------------
create table articles_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	name            varchar(45)     not null
);

-- -------------------------
-- USERS ARCHIVE
-- -------------------------
create table users_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	username        varchar(45)     not null,
	password        varchar(255)    not null
);

-- -------------------------
-- VOLUNTEERS ARCHIVE
-- -------------------------
create table volunteers_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	username        varchar(45)     not null,
	password        varchar(255)    not null,
	fk_role_id      int4            not null,
	fk_civility_id  int4            not null,
	fk_stand_id     int4            null
);

-- -------------------------
-- CARDS ARCHIVE
-- -------------------------
create table cards_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	pin             int4            not null,
	balance         numeric(10,2)   not null,
	nfc             varchar(45)     not null,
	fk_user_id      int4            null
);

-- -------------------------
-- TRANSACTION LOGS ARCHIVE
-- -------------------------
create table transaction_logs_archive(
	archive_id      serial4         not null    primary key,
	archived_at     timestamp       not null    default now(),
	archive_reason  varchar(100)    not null    default 'manual_archive',
	original_id     int4            not null,
	date            timestamp       not null,
	amount          numeric(10,2)   not null,
	fk_user_id      int4            not null,
	fk_card_id      int4            not null,
	fk_stand_id     int4            not null
);

-- =========================================================
-- TRIGGER SECTION (COPY INSERTS, UPDATES AND DELETES TO ARCHIVES)
-- =========================================================

-- -------------------------
-- STANDS
-- -------------------------
create or replace function trg_stands_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into stands_archive(original_id, name, archived_at, archive_reason)
        values (old.id, old.name, now(), lower(tg_op));
        return old;
    else
        insert into stands_archive(original_id, name, archived_at, archive_reason)
        values (new.id, new.name, now(), lower(tg_op));
        return new;
    end if;
end;
$$;

create trigger stands_archive_trigger
after insert or update or delete on stands
for each row
execute function trg_stands_archive();


-- -------------------------
-- CIVILITIES
-- -------------------------
create or replace function trg_civilities_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into civilities_archive(original_id, lastname, firstname, age, archived_at, archive_reason)
        values (old.id, old.lastname, old.firstname, old.age, now(), lower(tg_op));
        return old;
    else
        insert into civilities_archive(original_id, lastname, firstname, age, archived_at, archive_reason)
        values (new.id, new.lastname, new.firstname, new.age, now(), lower(tg_op));
        return new;
    end if;
end;
$$;

create trigger civilities_archive_trigger
after insert or update or delete on civilities
for each row
execute function trg_civilities_archive();


-- -------------------------
-- ROLES
-- -------------------------
create or replace function trg_roles_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into roles_archive(original_id, name, archived_at, archive_reason)
        values (old.id, old.name, now(), lower(tg_op));
        return old;
    else
        insert into roles_archive(original_id, name, archived_at, archive_reason)
        values (new.id, new.name, now(), lower(tg_op));
        return new;
    end if;
end;
$$;

create trigger roles_archive_trigger
after insert or update or delete on roles
for each row
execute function trg_roles_archive();


-- -------------------------
-- ARTICLES
-- -------------------------
create or replace function trg_articles_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into articles_archive(original_id, name, archived_at, archive_reason)
        values (old.id, old.name, now(), lower(tg_op));
        return old;
    else
        insert into articles_archive(original_id, name, archived_at, archive_reason)
        values (new.id, new.name, now(), lower(tg_op));
        return new;
    end if;
end;
$$;

create trigger articles_archive_trigger
after insert or update or delete on articles
for each row
execute function trg_articles_archive();


-- -------------------------
-- USERS
-- -------------------------
create or replace function trg_users_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into users_archive(original_id, username, password, archived_at, archive_reason)
        values (old.id, old.username, old.password, now(), lower(tg_op));
        return old;
    else
        insert into users_archive(original_id, username, password, archived_at, archive_reason)
        values (new.id, new.username, new.password, now(), lower(tg_op));
        return new;
    end if;
end;
$$;

create trigger users_archive_trigger
after insert or update or delete on users
for each row
execute function trg_users_archive();


-- -------------------------
-- VOLUNTEERS
-- -------------------------
create or replace function trg_volunteers_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into volunteers_archive(
            original_id,
            username,
            password,
            fk_role_id,
            fk_civility_id,
            fk_stand_id,
            archived_at,
            archive_reason
        )
        values (
            old.id,
            old.username,
            old.password,
            old.fk_role_id,
            old.fk_civility_id,
            old.fk_stand_id,
            now(),
            lower(tg_op)
        );
        return old;
    else
        insert into volunteers_archive(
            original_id,
            username,
            password,
            fk_role_id,
            fk_civility_id,
            fk_stand_id,
            archived_at,
            archive_reason
        )
        values (
            new.id,
            new.username,
            new.password,
            new.fk_role_id,
            new.fk_civility_id,
            new.fk_stand_id,
            now(),
            lower(tg_op)
        );
        return new;
    end if;
end;
$$;

create trigger volunteers_archive_trigger
after insert or update or delete on volunteers
for each row
execute function trg_volunteers_archive();


-- -------------------------
-- CARDS
-- -------------------------
create or replace function trg_cards_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into cards_archive(
            original_id,
            pin,
            balance,
            nfc,
            fk_user_id,
            archived_at,
            archive_reason
        )
        values (
            old.id,
            old.pin,
            old.money,
            old.nfc,
            old.fk_user_id,
            now(),
            lower(tg_op)
        );
        return old;
    else
        insert into cards_archive(
            original_id,
            pin,
            balance,
            nfc,
            fk_user_id,
            archived_at,
            archive_reason
        )
        values (
            new.id,
            new.pin,
            new.balance,
            new.nfc,
            new.fk_user_id,
            now(),
            lower(tg_op)
        );
        return new;
    end if;
end;
$$;

create trigger cards_archive_trigger
after insert or update or delete on cards
for each row
execute function trg_cards_archive();


-- -------------------------
-- TRANSACTION LOGS
-- -------------------------
create or replace function trg_transaction_logs_archive()
returns trigger
language plpgsql
as
$$
begin
    if tg_op = 'DELETE' then
        insert into transaction_logs_archive(
            original_id,
            date,
            amount,
            fk_user_id,
            fk_card_id,
            fk_stand_id,
            archived_at,
            archive_reason
        )
        values (
            old.id,
            old.date,
            old.amount,
            old.fk_user_id,
            old.fk_card_id,
            old.fk_stand_id,
            now(),
            lower(tg_op)
        );
        return old;
    else
        insert into transaction_logs_archive(
            original_id,
            date,
            amount,
            fk_user_id,
            fk_card_id,
            fk_stand_id,
            archived_at,
            archive_reason
        )
        values (
            new.id,
            new.date,
            new.amount,
            new.fk_user_id,
            new.fk_card_id,
            new.fk_stand_id,
            now(),
            lower(tg_op)
        );
        return new;
    end if;
end;
$$;

create trigger transaction_logs_archive_trigger
after insert or update or delete on transaction_logs
for each row
execute function trg_transaction_logs_archive();


-- =========================================================
-- DATA GENERATION SECTION (SEED / TEST DATA)
-- =========================================================

-- -------------------------
-- STANDS
-- -------------------------
insert into stands(name)
select 'Stand ' || gs
from generate_series(1, 5) gs;

-- -------------------------
-- ROLES
-- -------------------------
insert into roles(name) values
('Admin'),
('Seller'),
('Manager');

-- -------------------------
-- ARTICLES
-- -------------------------
insert into articles(name)
select 'Article ' || gs
from generate_series(1, 10) gs;

-- -------------------------
-- CIVILITIES
-- -------------------------
insert into civilities(lastname, firstname, age)
select
    'Lastname_' || gs,
    'Firstname_' || gs,
    18 + gs
from generate_series(1, 20) gs;

-- -------------------------
-- USERS
-- -------------------------
insert into users(username, password)
select
    'user_' || gs,
    'password123'
from generate_series(1, 10) gs;

-- -------------------------
-- VOLUNTEERS
-- -------------------------
insert into volunteers(username, password, fk_role_id, fk_civility_id, fk_stand_id)
select
    'volunteer_' || c.id,
    'password123',
    (select id from roles order by random() limit 1),
    c.id,
    case
        when random() < 0.3 then null
        else (select id from stands order by random() limit 1)
    end
from civilities c
limit 15;

-- -------------------------
-- INVENTORY
-- -------------------------
insert into inventory(quantity, price, fk_article_id, fk_stand_id)
select
    floor(random() * 100)::int,
    round((random() * 20 + 1)::numeric, 2),
    a.id,
    s.id
from articles a
cross join stands s
where random() < 0.7;

-- -------------------------
-- CARDS
-- -------------------------
insert into cards(pin, balance, nfc, fk_user_id)
select
    floor(random() * 9000 + 1000)::int,
    round((random() * 100)::numeric, 2),
    md5(u.id::text || clock_timestamp()::text),
    u.id
from users u;

-- -------------------------
-- TRANSACTION LOGS
-- -------------------------
insert into transaction_logs(date, amount, fk_user_id, fk_card_id, fk_stand_id)
select
    now() - (random() * interval '30 days'),
    round((random() * 25 + 1)::numeric, 2),
    u.id,
    c.id,
    (select id from stands order by random() limit 1)
from users u
join cards c on c.fk_user_id = u.id
cross join generate_series(1, 3) gs;


-- =========================================================
-- ARCHIVE DATA GENERATION SECTION
-- =========================================================

-- -------------------------
-- STANDS ARCHIVE
-- -------------------------
insert into stands_archive(original_id, name, archived_at, archive_reason)
select
    id,
    name,
    now() - (random() * interval '90 days'),
    'seed_archive'
from stands
where random() < 0.4;

-- -------------------------
-- CIVILITIES ARCHIVE
-- -------------------------
insert into civilities_archive(original_id, lastname, firstname, age, archived_at, archive_reason)
select
    id,
    lastname,
    firstname,
    age,
    now() - (random() * interval '90 days'),
    'seed_archive'
from civilities
where random() < 0.4;

-- -------------------------
-- ROLES ARCHIVE
-- -------------------------
insert into roles_archive(original_id, name, archived_at, archive_reason)
select
    id,
    name,
    now() - (random() * interval '90 days'),
    'seed_archive'
from roles
where random() < 0.4;

-- -------------------------
-- ARTICLES ARCHIVE
-- -------------------------
insert into articles_archive(original_id, name, archived_at, archive_reason)
select
    id,
    name,
    now() - (random() * interval '90 days'),
    'seed_archive'
from articles
where random() < 0.4;

-- -------------------------
-- USERS ARCHIVE
-- -------------------------
insert into users_archive(original_id, username, password, archived_at, archive_reason)
select
    id,
    username,
    password,
    now() - (random() * interval '90 days'),
    'seed_archive'
from users
where random() < 0.4;

-- -------------------------
-- VOLUNTEERS ARCHIVE
-- -------------------------
insert into volunteers_archive(original_id, username, password, fk_role_id, fk_civility_id, fk_stand_id, archived_at, archive_reason)
select
    id,
    username,
    password,
    fk_role_id,
    fk_civility_id,
    fk_stand_id,
    now() - (random() * interval '90 days'),
    'seed_archive'
from volunteers
where random() < 0.4;


-- -------------------------
-- CARDS ARCHIVE
-- -------------------------
insert into cards_archive(original_id, pin, balance, nfc, fk_user_id, archived_at, archive_reason)
select
    id,
    pin,
    balance,
    nfc,
    fk_user_id,
    now() - (random() * interval '90 days'),
    'seed_archive'
from cards
where random() < 0.4;

-- -------------------------
-- TRANSACTION LOGS ARCHIVE
-- -------------------------
insert into transaction_logs_archive(original_id, date, amount, fk_user_id, fk_card_id, fk_stand_id, archived_at, archive_reason)
select
    id,
    date,
    amount,
    fk_user_id,
    fk_card_id,
    fk_stand_id,
    now() - (random() * interval '90 days'),
    'seed_archive'
from transaction_logs
where random() < 0.5;