create table usuarios(
    id bigint auto_increment,
    nome varchar(100) not null,
    email varchar(100) not null,
    senha varchar(100) not null,
    primary key(id)
);