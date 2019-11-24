--------------------------------------------------------
--
-- Script file to create database file for XDoc plugin
-- 
-- Alesia
--
--------------------------------------------------------

DROP TABLE if exists XD_CONFIG CASCADE;
CREATE TABLE XD_CONFIG
(
   XD_COID           VARCHAR(19)     NOT NULL,
   XD_CONAME         VARCHAR(30),
   XD_COEXTENSION    VARCHAR(6),
   XD_CODESCRIPTION  VARCHAR(60),
   XD_CODATA         BINARY,
   XD_COQRCODE       VARCHAR(3000),
   XD_COPROPERTIES   VARCHAR(3000)
);
ALTER TABLE XD_CONFIG
   ADD CONSTRAINT PK_XD_CONFIG PRIMARY KEY (XD_COID);

DROP TABLE if exists XD_MERGED_DOC;
CREATE TABLE XD_MERGED_DOC
(
   XD_MDID           VARCHAR(20)   NOT NULL,
   XD_MDCERTIFICATE  VARCHAR(10)   NOT NULL,
   XD_MDDATE         DATE                NOT NULL,
   XD_MDREMITTENT    VARCHAR(60)   NOT NULL,
   XD_MDDOC_NAME     VARCHAR(30)   NOT NULL,
   XD_MDMERGED_DOC   BINARY                NOT NULL
);
ALTER TABLE XD_MERGED_DOC
   ADD CONSTRAINT PK_XD_MERGED_DOC PRIMARY KEY (XD_MDID);
