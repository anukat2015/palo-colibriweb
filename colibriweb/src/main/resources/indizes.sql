CREATE INDEX activeTime ON offeraction USING btree (offer_id, active, clienttime, usertime);
CREATE INDEX maCalendar ON offeraction USING btree (offer_id, active, usertime, user_id, startdate); 
CREATE INDEX unit_participated ON unitclientparticipation USING btree (unit_id, participated);
CREATE INDEX unitdate ON unitclientparticipation USING btree (unit_id);
CREATE INDEX unit_participationstate ON unitclientparticipation USING btree (unit_id, participationstate);
CREATE INDEX idx_decursdates ON decurs USING btree (startdate, modificationdate);
-- the following is only possible for postgres 9.1 or higher
-- Install trigram module if you don't have installed already
CREATE EXTENSION pg_trgm;
 
-- Add trigram index (seems to take much longer to build if have btree 28 secs))
 CREATE INDEX idx_actionlabel_trgm_gist
   ON action USING gist (label gist_trgm_ops);
   
 CREATE INDEX idx_jobgroupname_trgm_gist
   ON jobgroup USING gist (name gist_trgm_ops);
   
  CREATE INDEX idx_substanceprescriptiondosage_trgm_gist
   ON substanceprescription USING gist (dosage1 gist_trgm_ops);
   
 CREATE INDEX idx_substanceprescriptiondosage2_trgm_gist
   ON substanceprescription USING gist (dosage2 gist_trgm_ops);
   
  CREATE INDEX idx_userlastname_trgm_gist
   ON opensmcuser USING gist (lastname gist_trgm_ops);
   
  CREATE INDEX idx_substanceprescriptiondosage4_trgm_gist
   ON substanceprescription USING gist (dosage4 gist_trgm_ops);
   
   CREATE INDEX idx_substanceprescriptiondosage3_trgm_gist
   ON substanceprescription USING gist (dosage3 gist_trgm_ops);
   
   CREATE INDEX idx_substanceprescriptionop_trgm_gist
   ON substanceprescription USING gist (op gist_trgm_ops);
   
   CREATE INDEX idx_substanceprescriptionfreetext_trgm_gist
   ON substanceprescription USING gist (dosagefreetext gist_trgm_ops);
   
   CREATE INDEX idx_userfirstname_trgm_gist
   ON opensmcuser USING gist (firstname gist_trgm_ops);
   
   CREATE INDEX idx_decursincident_trgm_gist
   ON decurs USING gist (incident gist_trgm_ops);
   
   CREATE INDEX idx_recipename_trgm_gist
   ON recipe USING gist (name gist_trgm_ops);
   
   CREATE INDEX idx_decursjournalentrytype_trgm_gist
   ON decursjournalentry USING gist (type gist_trgm_ops);
   
   CREATE INDEX idx_documenttypelabel_trgm_gist
   ON documenttype USING gist (label gist_trgm_ops);
   
   CREATE INDEX idx_offerlabel_trgm_gist
   ON offer USING gist (label gist_trgm_ops);
   
   CREATE INDEX idx_decursnote_trgm_gist
   ON decurs USING gist (note gist_trgm_ops);
   
   CREATE INDEX idx_documentname_trgm_gist
   ON document USING gist (name gist_trgm_ops);
   
   CREATE INDEX idx_substanceprescriptionopunit_trgm_gist
   ON substanceprescription USING gist (opunit gist_trgm_ops);
   
   CREATE INDEX idx_recipetypelabel_trgm_gist
   ON recipetype USING gist (label gist_trgm_ops);
   
   CREATE INDEX idx_actionattributelabel_trgm_gist
   ON actionattribute USING gist (label gist_trgm_ops);
   
   CREATE INDEX idx_recipevignettenumber_trgm_gist
   ON recipe USING gist (vignettenumber gist_trgm_ops);
   
   CREATE INDEX idx_userusername_trgm_gist
   ON opensmcuser USING gist (username gist_trgm_ops);
   
   CREATE INDEX idx_decursjournalentrynote_trgm_gist
   ON decursjournalentry USING gist (note gist_trgm_ops);