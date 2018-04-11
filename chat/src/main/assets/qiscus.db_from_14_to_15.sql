/**
 * migration db v14 to v15
 */
CREATE INDEX comments_room_id_index ON comments (room_id);
CREATE INDEX room_members_distinct_id_index ON room_members (distinct_id);
CREATE INDEX rooms_unique_id_index ON rooms (unique_id);