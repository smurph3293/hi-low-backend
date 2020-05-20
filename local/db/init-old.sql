create table bet (
    id varchar(64) not null,
    xref varchar(64) not null,
    created_at timestamp not null default now(),
    creator_id varchar(64) not null,
    commissioner_id varchar(64),
    title varchar(64) not null,
    description varchar(512),
    conditions varchar(512) not null,
    punishment varchar(512) not null,
    conditions_deadline timestamp not null,
    punishment_deadline timestamp not null,
    result_xref varchar(64),
    is_complete boolean default false,
    version decimal(10, 1),
    primary key (id)
);
alter table bet add constraint bet_xref_con unique (xref);

create table hi_low_user (
    id varchar(64) not null,
    xref varchar(64) not null,
    created_at timestamp not null default now(),
    username varchar(64) not null,
    first_name varchar(64),
    last_name varchar(64),
    email varchar(64),
    cell_phone integer,
    primary key (id)
);
alter table hi_low_user add constraint hi_low_user_xref_con unique (xref);

create table user_bet_relation (
    hi_low_user_id varchar(64) not null,
    bet_id varchar(64) not null,
    foreign key (hi_low_user_id) references hi_low_user(id),
    foreign key (bet_id) references bet(id),
    unique (hi_low_user_id, bet_id)
);

create table comment (
    id varchar(64) not null,
    xref varchar(64) not null,
    hi_low_user_id varchar(64) not null references hi_low_user(id),
    comment varchar(512) not null,
    likes integer,
    primary key (id)
)