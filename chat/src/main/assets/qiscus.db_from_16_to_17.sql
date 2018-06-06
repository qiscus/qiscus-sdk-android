/**
 * migration db v15 to v16
 */

ALTER TABLE comments ADD COLUMN encrypted INTEGER DEFAULT 0;
UPDATE comments SET encrypted = 0;
UPDATE comments SET encrypted = 0 WHERE message = "Encrypted" or message = "Terenkripsi";