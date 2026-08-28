CREATE TABLE "public"."ai_message"
(
    "id"               varchar(64) NOT NULL DEFAULT replace(uuid_generate_v4()::text, '-'::text, ''::text),
    "conversation_id"  varchar(64) NOT NULL,
    "user_id"          varchar(64) NOT NULL,
    "role"             varchar(32) NOT NULL,
    "content"          text        NOT NULL,
    "status"           varchar(32) NOT NULL,
    "message_type"     varchar(32) NOT NULL,
)