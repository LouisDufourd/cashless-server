-- =========================================================
-- CLEAN START
-- =========================================================
truncate cards CASCADE;
truncate users CASCADE;
truncate articles CASCADE;
truncate civilities CASCADE;
truncate stands CASCADE;

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