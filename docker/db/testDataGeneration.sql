-- =========================================================
-- CLEAN START
-- =========================================================
truncate transaction_logs cascade;
truncate cards cascade;
truncate inventory cascade;
truncate token_session cascade;
truncate volunteers cascade;
truncate users cascade;
truncate articles cascade;
truncate civilities cascade;
truncate stands cascade;

-- =========================================================
-- STANDS
-- =========================================================
insert into stands (id, name) values
(1, 'Stand 1');

-- =========================================================
-- CIVILITIES
-- =========================================================
insert into civilities (id, lastname, firstname, age) values
(1, 'Doe', 'John', 30);

-- =========================================================
-- USERS
-- =========================================================
insert into users (id, username, password) values
(1, 'user_1', 'password123');

-- =========================================================
-- VOLUNTEERS
-- =========================================================
insert into volunteers (id, username, password, role, fk_civility_id, fk_stand_id) values
(1, 'volunteer_1', 'password123', 'ORGANIZER', 1, 1);

-- =========================================================
-- ARTICLE
-- =========================================================
insert into articles (id, name) values
(1, 'Article 1');

-- =========================================================
-- CARDS
-- =========================================================
insert into cards (id, pin, balance, nfc, fk_user_id) values
(1, 1234, 20.00, 'card-nfc', 1);

-- =========================================================
-- INVENTORY
-- =========================================================
insert into inventory (id, quantity, price, fk_article_id, fk_stand_id) values
(1, 10, 2.50, 1, 1);

-- =========================================================
-- TRANSACTION LOGS
-- =========================================================
insert into transaction_logs (id, date, amount, fk_user_id, fk_card_id, fk_stand_id) values
(1, now(), 5.00, 1, 1, 1);