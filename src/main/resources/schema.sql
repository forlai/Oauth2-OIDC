DROP TABLE IF EXISTS richieste;
 
CREATE TABLE euser (
	id INT AUTO_INCREMENT  PRIMARY KEY,
	sub varchar2(255),
	issuer varchar2(255),
	username varchar2(255) NOT NULL,
	password varchar2(2048),
	firstName varchar2(255),
	lastName varchar2(255),
	email varchar2(512)

);
 

insert into euser (username,password) values ('forlai', '$2a$10$40hOAmBnFZuQhdW.fruJkeCRoBWjQgv0i2OptXV54kLIwfR3RKlb6');
