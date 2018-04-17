/**
 * migration db v14 to v15
 */
CREATE INDEX comments_room_id_index ON comments (room_id);
CREATE INDEX comments_message_index ON comments (message);
CREATE INDEX room_members_distinct_id_index ON room_members (distinct_id);
CREATE INDEX rooms_unique_id_index ON rooms (unique_id);
ALTER TABLE rooms ADD COLUMN is_channel INTEGER DEFAULT 0;
ALTER TABLE rooms ADD COLUMN member_count INTEGER DEFAULT 0;