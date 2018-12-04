/*增加证件类型字段*/
ALTER TABLE t_user_info ADD COLUMN idType CHAR(2) AFTER userCardName;