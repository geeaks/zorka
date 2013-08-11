
create table if not exists SYMBOLS (
  SID INTEGER PRIMARY KEY NOT NULL,
  NAME VARCHAR(1024) NOT NULL
);

insert into SYMBOLS (SID, NAME) values (0, '<INVALID>');

create table if not exists HOSTS (
  HOST_ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,
  HOST_NAME VARCHAR(128) NOT NULL,
  HOST_ADDR VARCHAR(128),
  HOST_PATH VARCHAR(128) NOT NULL
);

create table if not exists TRACES (
  HOST_ID INTEGER NOT NULL,
  DATA_OFFS BIGINT NOT NULL,
  TRACE_ID INTEGER NOT NULL,
  DATA_LEN INTEGER NOT NULL,
  CLOCK BIGINT NOT NULL,
  RFLAGS INTEGER NOT NULL,
  TFLAGS INTEGER NOT NULL,
  CALLS BIGINT NOT NULL,
  ERRORS BIGINT NOT NULL,
  RECORDS BIGINT NOT NULL,
  EXTIME BIGINT NOT NULL,
  OVERVIEW VARCHAR(256) NOT NULL
);

commit;
