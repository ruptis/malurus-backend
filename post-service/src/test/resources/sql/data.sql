ALTER SEQUENCE posts_id_seq RESTART WITH 1;
ALTER SEQUENCE likes_id_seq RESTART WITH 1;

INSERT INTO posts (creation_date, user_id, text)
VALUES (now(), 'dummy-id', 'some text');