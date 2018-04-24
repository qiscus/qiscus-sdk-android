/**
 * migration db v15 to v16
 */

ALTER TABLE comments ADD COLUMN hard_deleted INTEGER DEFAULT 0;
UPDATE comments SET hard_deleted = 0;