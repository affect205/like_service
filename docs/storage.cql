create keyspace store with replication={'class': 'SimpleStrategy', 'replication_factor': 1};
create table likes (player_id varchar primary key, total counter);
select * from store.likes;
update store.likes set total += 1 where player_id = '1';
drop table store.likes;