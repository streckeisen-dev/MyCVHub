--liquibase formatted sql

--changeset lstreckeisen:7
--validCheckSum: any
ALTER TABLE project_links ADD COLUMN display_name VARCHAR(60);

UPDATE project_links
SET display_name = case type
    when 'GITHUB'
        then 'GitHub'
    else (
        case type
            when 'DOCUMENT'
                then 'Document'
            else (
                case type
                    when 'WEBSITE'
                        then 'Website'
                    else 'Other'
                    end
                )
            end
        )
    end;

ALTER TABLE project_links ALTER COLUMN display_name SET NOT NULL;