create table respostas(
    id bigint auto_increment,
    mensagem varchar(100) not null ,
    topico_id bigint not null,
    data_criacao datetime not null,
    usuario_id bigint not null,
    solucao boolean not null,
    foreign key (topico_id) references topicos(id),
    foreign key (usuario_id) references usuarios(id),
    primary key (id)
);