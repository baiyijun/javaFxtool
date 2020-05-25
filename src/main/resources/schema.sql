drop table if exists CO_TXTANALYZE;
create table CO_TXTANALYZE
(
  typename       VARCHAR2(255),
  typecode       VARCHAR2(255),
  filetype       VARCHAR2(50),
  txtln          NUMBER(20),
  dstype         VARCHAR2(50),
  dslength       NUMBER(20),
  dsdataformat   VARCHAR2(255),
  dsflag         VARCHAR2(50),
  reobj          VARCHAR2(255),
  refield        VARCHAR2(255),
  refieldseq     NUMBER(20),
  refieldtype    VARCHAR2(255),
  refieldlength  VARCHAR2(255),
  refieldbegpos  NUMBER(20),
  refieldendpos  NUMBER(20),
  c1             VARCHAR2(255),
  c2             VARCHAR2(255),
  c3             VARCHAR2(255),
  c4             VARCHAR2(255),
  c5             VARCHAR2(255),
  c6             VARCHAR2(255),
  c7             VARCHAR2(255),
  c8             VARCHAR2(255),
  c9             VARCHAR2(255),
  c10            VARCHAR2(255),
  driectionflag  VARCHAR2(20),
  managercode    VARCHAR2(20),
  truncatemethod VARCHAR2(4)
);

drop table if exists CO_TRANCODECONFIG_TMP;
create table CO_TRANCODECONFIG_TMP
(
  glrbm         VARCHAR2(200),
  driectionflag VARCHAR2(200),
  dataset       VARCHAR2(200),
  bmly          VARCHAR2(200),
  c_1           VARCHAR2(200),
  c_2           VARCHAR2(200),
  c_3           VARCHAR2(200),
  c_4           VARCHAR2(200),
  c_5           VARCHAR2(200)
);

