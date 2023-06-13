package com.filiperobot.aluraforumapi.controller;

import com.filiperobot.aluraforumapi.domain.course.CursoRepository;
import com.filiperobot.aluraforumapi.domain.forum.topico.*;
import com.filiperobot.aluraforumapi.domain.forum.topico.DTO.*;
import com.filiperobot.aluraforumapi.domain.user.UsuarioRepository;
import com.filiperobot.aluraforumapi.infra.documentation.PageableAsQueryParam;
import com.filiperobot.aluraforumapi.infra.exceptions.DTO.DadosErrosValidacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/topicos")
@RequiredArgsConstructor
@Tag(name = "Tópicos", description = "Endpoint para gerenciar tópicos, criação, busca, listagem, atualização e remoção")
@SecurityRequirement(name = "TokenJWT")
@ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o token não seja valido ou não foi enviado",
        content = {@Content(schema = @Schema())})
public class ForumController {

    private final TopicoRepository topicoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar tópico", description = "Cadastra um tópico no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Retorna os dados do tópico cadastrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosTopicoCompleto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna um lista com as informações dos capos inválidos",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = DadosErrosValidacao.class)
                    ))
            })
    })
    public ResponseEntity<DadosTopicoCompleto> criarTopico(
            @RequestBody @Valid DadosCadastroTopico dadosNovoTopico,
            UriComponentsBuilder uriBuilder) {
        var usuario = usuarioRepository.findById(dadosNovoTopico.autor()).orElseThrow(
                () -> new EntityNotFoundException("Autor não encontrado")
        );

        var curso = cursoRepository.findById(dadosNovoTopico.curso()).orElseThrow(
                () -> new EntityNotFoundException("Curso não encontrado")
        );

        var dadosCadastroTopico = new DadosCompletoCadastroTopico(
                dadosNovoTopico.titulo(),
                dadosNovoTopico.mensagem(),
                usuario, curso
        );

        var topico = topicoRepository.save(new Topico(dadosCadastroTopico));

        var uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();

        return ResponseEntity.created(uri).body(new DadosTopicoCompleto(topico));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar tópico", description = "Procura um tópico com o ID informado no banco de dados.")
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id do tópico a ser encontrado no banco de dados")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os dados do tópico encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosTopicoCompleto.class))
            })
    })
    public ResponseEntity<DadosTopicoCompleto> topico(@PathVariable Long id) {
        var topico = topicoRepository.getReferenceById(id);

        return ResponseEntity.ok(new DadosTopicoCompleto(topico));
    }

    @GetMapping
    @Operation(summary = "Listar tópicos", description = "Busca todos os tópicos do banco de dados")
    @PageableAsQueryParam
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna uma paginação com todos os cursos encontrados",
                    useReturnTypeSchema = true)
    })
    public ResponseEntity<Page<DadosListagemTopico>> listarTopicos(
            @ParameterObject
            @PageableDefault(sort = {"dataCriacao"}, direction = Sort.Direction.ASC)
            Pageable pageable) {

        Page<DadosListagemTopico> listagemTopicos = topicoRepository.findAll(pageable).map(DadosListagemTopico::new);

        return ResponseEntity.ok(listagemTopicos);
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar tópico", description = "Atualiza os dados de um tópico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os dados do tópico atualizado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosListagemTopico.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna uma lista com as informações dos capos inválidos", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosErrosValidacao.class)))
            })
    })
    public ResponseEntity<DadosListagemTopico> atualizar(@RequestBody @Valid DadosAtualizarTopico dadosTopicoAtualizacao) {
        var topico = topicoRepository.getReferenceById(dadosTopicoAtualizacao.id());

        topico.atualizar(dadosTopicoAtualizacao);

        return ResponseEntity.ok(new DadosListagemTopico(topico));
    }

    @DeleteMapping("{id}")
    @Transactional
    @Operation(summary = "Remover tópico", description = "Remove do banco de dados o tópico com o ID informado")
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id do tópico a ser removido")
    })
    @ApiResponse(responseCode = "204", description = "Caso a exclusão/remoção do tópico seja feita com sucesso",
            content = {@Content(schema = @Schema())})
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        topicoRepository.findById(id).ifPresentOrElse(
                topicoRepository::delete,
                ()  -> {
                    throw new IllegalArgumentException("Tópico não existe, não é possível deleta-lo");
                }
        );

        return ResponseEntity.noContent().build();
    }
}
