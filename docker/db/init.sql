-- =========================================================
-- CLEANUP SECTION (DROP EXISTING TABLES)
-- =========================================================
drop table if exists    transaction_logs;
drop table if exists    cards;
drop table if exists    inventory;
drop table if exists    token_session;
drop table if exists    volunteers;
drop table if exists    users;
drop table if exists    articles;
drop type if exists     role;
drop table if exists    civilities;
drop table if exists    stands;


-- =========================================================
-- TABLE CREATION SECTION (DATABASE STRUCTURE)
-- =========================================================


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
    role            role            not null,
	fk_civility_id  int4            not null    references civilities(id) 	on delete cascade,
	fk_stand_id     int4            null        references stands(id) 		on delete set null
);

-- -------------------------
-- Token Session
-- -------------------------
create table token_session(
                              id              serial4         not null    primary key,
                              volunteer_id    int4            not null    references volunteers(id) on delete cascade,
                              refresh_jti     varchar(255)    not null    unique,
                              revoked         boolean         not null    default false,
                              created_at      timestamp       not null    default now(),
                              expire_at       timestamp       not null
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
insert into volunteers(username, password, role, fk_civility_id, fk_stand_id)
select
    'volunteer_' || c.id,
    'password123',
    (ARRAY['organiser','manager','seller','recharge'])[floor(random()*4+1)]::role,
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