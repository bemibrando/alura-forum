package br.com.alura.forum.topico;


import br.com.alura.forum.curso.Curso;
import br.com.alura.forum.curso.CursoRepository;
import br.com.alura.forum.exceptionhandler.AttributeNotFound;
import br.com.alura.forum.exceptionhandler.EntityNotFound;
import br.com.alura.forum.exceptionhandler.NotAutorized;
import br.com.alura.forum.infra.security.TokenService;
import br.com.alura.forum.usuario.Usuario;
import br.com.alura.forum.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicoService {

    private final TopicoRepository topicoRepository;

    private final CursoRepository cursoRepository;

    private final UsuarioRepository usuarioRepository;

    private final TokenService tokenService;


    @Transactional
    public TopicoResponse saveTopico(TopicoRequest topicoRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email);
        Curso curso = cursoRepository.findCursoByNome(topicoRequest.curso()).orElseThrow();
        Topico topico = new Topico(topicoRequest);
        topico.setCurso(curso);
        topico.setAutor(usuario);
        topico = topicoRepository.save(topico);
        return new TopicoResponse(topico);
    }

    public Page<TopicoResponse> getTopicos(Pageable pageable) {
        return topicoRepository.findAll(pageable).map(TopicoResponse::new);
    }

    public TopicoResponse getTopicoById(Long id) {
        return topicoRepository.findById(id).map(TopicoResponse::new).orElseThrow(
                () -> new EntityNotFound("Tópico não encontrado!")
        );
    }


    public TopicoResponse updateTopicoById(Long id, AlteredTopic alteredTopict) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email);

        return topicoRepository.findById(id).map(topico -> {
                    if (topico.getAutor().equals(usuario)) {
                        if (alteredTopict.curso() != null) {
                            Curso curso = cursoRepository.findCursoByNome(alteredTopict.curso())
                                    .orElseThrow(() -> new AttributeNotFound("Curso não encontrado"));
                            topico.setCurso(curso);
                        }
                        topico.alterar(alteredTopict);
                        return new TopicoResponse(topico);
                    }
                    throw new NotAutorized("Acesso não autorizado!");
                }
        ).orElseThrow(() -> new EntityNotFound("Tópico não encontrado!"));

    }

    public void deleteById(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email);
        Topico topico = topicoRepository.findById(id).orElseThrow(() -> new EntityNotFound("Tópico não encontrado!"));
        if (topico.getAutor() == usuario) {
            topicoRepository.deleteById(id);
        } else
            throw new NotAutorized("Acesso não autorizado!");

    }
}
