create table ai_conversation
(
    id              varchar(64)              default replace((gen_random_uuid())::text, '-'::text, ''::text) not null
        primary key,
    user_id         varchar(64)                                                                              not null,
    title           varchar(200),
    mode            varchar(32)              default 'academic'::character varying                           not null
        constraint chk_ai_conversation_mode
            check ((mode)::text = ANY ((ARRAY ['academic'::character varying, 'general'::character varying])::text[])),
    status          varchar(32)              default 'active'::character varying                             not null
        constraint chk_ai_conversation_status
            check ((status)::text = ANY
                   ((ARRAY ['active'::character varying, 'archived'::character varying, 'deleted'::character varying])::text[])),
    message_count   integer                  default 0                                                       not null,
    last_message_at timestamp with time zone,
    created_at      timestamp with time zone default now()                                                   not null,
    updated_at      timestamp with time zone default now()                                                   not null
);

alter table ai_conversation
    owner to postgres;

create index idx_ai_conv_user_time
    on ai_conversation (user_id asc, updated_at desc);

create table ai_message
(
    id                varchar(64)              default replace((gen_random_uuid())::text, '-'::text, ''::text) not null
        primary key,
    conversation_id   varchar(64)                                                                              not null
        references ai_conversation
            on delete cascade,
    user_id           varchar(64)                                                                              not null,
    role              varchar(20)                                                                              not null
        constraint chk_ai_message_role
            check ((role)::text = ANY
                   ((ARRAY ['user'::character varying, 'assistant'::character varying, 'system'::character varying])::text[])),
    content           text                                                                                     not null,
    provider          varchar(50),
    model             varchar(100),
    finish_reason     varchar(80),
    total_results     integer,
    displayed_results integer,
    csv_export_id     varchar(64),
    request_id        varchar(80),
    latency_ms        integer,
    created_at        timestamp with time zone default now()                                                   not null
);

alter table ai_message
    owner to postgres;

create index idx_ai_msg_conv_time
    on ai_message (conversation_id, created_at);

create index idx_ai_msg_user_time
    on ai_message (user_id asc, created_at desc);

create table ai_tool_call
(
    id              varchar(64)              default replace((gen_random_uuid())::text, '-'::text, ''::text) not null
        primary key,
    message_id      varchar(64)
                                                                                                             references ai_message
                                                                                                                 on delete set null,
    conversation_id varchar(64)                                                                              not null
        references ai_conversation
            on delete cascade,
    user_id         varchar(64)                                                                              not null,
    tool_name       varchar(100)                                                                             not null,
    arguments       jsonb,
    result_total    integer,
    result_preview  jsonb,
    success         boolean                  default true                                                    not null,
    error_code      varchar(80),
    error_message   text,
    latency_ms      integer,
    created_at      timestamp with time zone default now()                                                   not null
);

alter table ai_tool_call
    owner to postgres;

create index idx_ai_tool_msg
    on ai_tool_call (message_id);

create index idx_ai_tool_conv_time
    on ai_tool_call (conversation_id, created_at);

create index idx_ai_tool_user_time
    on ai_tool_call (user_id asc, created_at desc);

create table ai_conversation_summary
(
    conversation_id       varchar(64)                            not null
        primary key
        references ai_conversation
            on delete cascade,
    user_id               varchar(64)                            not null,
    summary               text                                   not null,
    covered_message_count integer                  default 0     not null,
    created_at            timestamp with time zone default now() not null,
    updated_at            timestamp with time zone default now() not null
);

alter table ai_conversation_summary
    owner to postgres;

create index idx_ai_summary_user
    on ai_conversation_summary (user_id);

create table ai_memory
(
    id                     varchar(64)              default replace((gen_random_uuid())::text, '-'::text, ''::text) not null
        primary key,
    user_id                varchar(64)                                                                              not null,
    memory_type            varchar(50)                                                                              not null,
    content                text                                                                                     not null,
    source_conversation_id varchar(64)
                                                                                                                    references ai_conversation
                                                                                                                        on delete set null,
    confidence             numeric(4, 3)            default 1.000                                                   not null
        constraint chk_ai_memory_confidence
            check ((confidence >= (0)::numeric) AND (confidence <= (1)::numeric)),
    enabled                boolean                  default true                                                    not null,
    created_at             timestamp with time zone default now()                                                   not null,
    updated_at             timestamp with time zone default now()                                                   not null
);

alter table ai_memory
    owner to postgres;

create index idx_ai_memory_user_type
    on ai_memory (user_id, memory_type, enabled);

create index idx_ai_memory_user_time
    on ai_memory (user_id asc, updated_at desc);

create function digest(text, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function digest(text, text) owner to postgres;

create function digest(bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function digest(bytea, text) owner to postgres;

create function hmac(text, text, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function hmac(text, text, text) owner to postgres;

create function hmac(bytea, bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function hmac(bytea, bytea, text) owner to postgres;

create function crypt(text, text) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function crypt(text, text) owner to postgres;

create function gen_salt(text) returns text
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function gen_salt(text) owner to postgres;

create function gen_salt(text, integer) returns text
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function gen_salt(text, integer) owner to postgres;

create function encrypt(bytea, bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function encrypt(bytea, bytea, text) owner to postgres;

create function decrypt(bytea, bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function decrypt(bytea, bytea, text) owner to postgres;

create function encrypt_iv(bytea, bytea, bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function encrypt_iv(bytea, bytea, bytea, text) owner to postgres;

create function decrypt_iv(bytea, bytea, bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function decrypt_iv(bytea, bytea, bytea, text) owner to postgres;

create function gen_random_bytes(integer) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function gen_random_bytes(integer) owner to postgres;

create function gen_random_uuid() returns uuid
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function gen_random_uuid() owner to postgres;

create function pgp_sym_encrypt(text, text) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_encrypt(text, text) owner to postgres;

create function pgp_sym_encrypt_bytea(bytea, text) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_encrypt_bytea(bytea, text) owner to postgres;

create function pgp_sym_encrypt(text, text, text) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_encrypt(text, text, text) owner to postgres;

create function pgp_sym_encrypt_bytea(bytea, text, text) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_encrypt_bytea(bytea, text, text) owner to postgres;

create function pgp_sym_decrypt(bytea, text) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_decrypt(bytea, text) owner to postgres;

create function pgp_sym_decrypt_bytea(bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_decrypt_bytea(bytea, text) owner to postgres;

create function pgp_sym_decrypt(bytea, text, text) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_decrypt(bytea, text, text) owner to postgres;

create function pgp_sym_decrypt_bytea(bytea, text, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_sym_decrypt_bytea(bytea, text, text) owner to postgres;

create function pgp_pub_encrypt(text, bytea) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_encrypt(text, bytea) owner to postgres;

create function pgp_pub_encrypt_bytea(bytea, bytea) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_encrypt_bytea(bytea, bytea) owner to postgres;

create function pgp_pub_encrypt(text, bytea, text) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_encrypt(text, bytea, text) owner to postgres;

create function pgp_pub_encrypt_bytea(bytea, bytea, text) returns bytea
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_encrypt_bytea(bytea, bytea, text) owner to postgres;

create function pgp_pub_decrypt(bytea, bytea) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_decrypt(bytea, bytea) owner to postgres;

create function pgp_pub_decrypt_bytea(bytea, bytea) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_decrypt_bytea(bytea, bytea) owner to postgres;

create function pgp_pub_decrypt(bytea, bytea, text) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_decrypt(bytea, bytea, text) owner to postgres;

create function pgp_pub_decrypt_bytea(bytea, bytea, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_decrypt_bytea(bytea, bytea, text) owner to postgres;

create function pgp_pub_decrypt(bytea, bytea, text, text) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_decrypt(bytea, bytea, text, text) owner to postgres;

create function pgp_pub_decrypt_bytea(bytea, bytea, text, text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_pub_decrypt_bytea(bytea, bytea, text, text) owner to postgres;

create function pgp_key_id(bytea) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function pgp_key_id(bytea) owner to postgres;

create function armor(bytea) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function armor(bytea) owner to postgres;

create function armor(bytea, text[], text[]) returns text
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function armor(bytea, text[], text[]) owner to postgres;

create function dearmor(text) returns bytea
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;
$$;

alter function dearmor(text) owner to postgres;

create function pgp_armor_headers(text, out key text, out value text) returns setof setof record
    immutable
    strict
    parallel safe
    language c
as
$$
begin
-- missing source code
end;

$$;

alter function pgp_armor_headers(text, out text, out text) owner to postgres;

create function set_updated_at() returns trigger
    language plpgsql
as
$$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;

alter function set_updated_at() owner to postgres;

create trigger trg_ai_conversation_updated_at
    before update
    on ai_conversation
    for each row
execute procedure set_updated_at();

create trigger trg_ai_summary_updated_at
    before update
    on ai_conversation_summary
    for each row
execute procedure set_updated_at();

create trigger trg_ai_memory_updated_at
    before update
    on ai_memory
    for each row
execute procedure set_updated_at();
