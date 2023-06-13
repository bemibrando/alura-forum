create table topicos(
    id bigint auto_increment,
    titulo varchar(100) not null,
    mensagem varchar(100) not null,
    data_criacao datetime not null ,
    status_topico varchar(100) not null,
    usuario_id bigint not null,
    curso_id bigint not null,
    foreign key(usuario_id) references usuarios(id),
    foreign key(curso_id) references cursos(id),
    primary key (id)
);