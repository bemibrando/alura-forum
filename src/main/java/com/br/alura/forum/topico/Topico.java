package br.com.alura.forum.topico;

import br.com.alura.forum.curso.Curso;
import br.com.alura.forum.resposta.Resposta;
import br.com.alura.forum.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topicos")
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Topico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String mensagem;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private StatusTopico statusTopico = StatusTopico.NAO_RESPONDIDO;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario autor;
    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;
    @OneToMany(mappedBy = "topico")
    private List<Resposta> respostas = new ArrayList<>();

    public Topico(TopicoRequest topicoRequest) {
        this.titulo = topicoRequest.titulo();
        this.mensagem = topicoRequest.mensagem();
    }

    public void alterar(AlteredTopic alteredTopic){
        if(alteredTopic.mensagem() != null && !alteredTopic.mensagem().trim().equals(""))
            this.mensagem = alteredTopic.mensagem();
        if(alteredTopic.titulo() != null && !alteredTopic.titulo().trim().equals(""))
            this.titulo = alteredTopic.titulo();
    }
}
