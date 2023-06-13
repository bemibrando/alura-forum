package com.filiperobot.aluraforumapi.controller;

import com.filiperobot.aluraforumapi.domain.forum.resposta.DTO.*;
import com.filiperobot.aluraforumapi.domain.forum.resposta.Resposta;
import com.filiperobot.aluraforumapi.domain.forum.resposta.RespostaRepository;
import com.filiperobot.aluraforumapi.domain.forum.topico.TopicoRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/respostas")
@RequiredArgsConstructor
@Tag(name = "Respostas", description = "Endpoint para gerenciar respostas, criação, busca, listagem, atualização e remoção")
@SecurityRequirement(name = "TokenJWT")
@ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o token não seja valido ou não foi enviado",
        content = {@Content(schema = @Schema())})
public class RespostaController {

    private final RespostaRepository respostaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TopicoRepository topicoRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar resposta", description = "Cadastra uma resposta no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Retorna os dados da resposta cadastrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosResposta.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna um lista com as informações dos capos inválidos",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = DadosErrosValidacao.class)
                    ))
            })
    })
    public ResponseEntity<DadosResposta> cadastrar(@RequestBody @Valid DadosCadastroResposta dadosResposta,
                                                   UriComponentsBuilder uriBuilder) {
        var autor = usuarioRepository.findById(dadosResposta.autor()).orElseThrow(
                () -> new EntityNotFoundException("Autor não encontrado")
        );

        var topico = topicoRepository.findById(dadosResposta.topico()).orElseThrow(
                () -> new EntityNotFoundException("Topico não encontrado")
        );

        var dadosCompletoResposta = new DadosCompletoCadastroResposta(dadosResposta.mensagem(), autor, topico);

        Resposta resposta = respostaRepository.save(new Resposta(dadosCompletoResposta));

        var uri = uriBuilder.path("/respostas/{id}").buildAndExpand(resposta.getId()).toUri();

        return ResponseEntity.created(uri).body(new DadosResposta(resposta));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resposta", description = "Procura uma resposta com o ID informado no banco de dados.")
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id da resposta a ser encontrada no banco de dados")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os dados da resposta encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosResposta.class))
            })
    })
    public ResponseEntity<DadosResposta> buscar(@PathVariable Long id) {
        var resposta = respostaRepository.getReferenceById(id);

        return ResponseEntity.ok(new DadosResposta(resposta));
    }

    @GetMapping
    @Operation(summary = "Listar respostas", description = "Busca todas as respostas do banco de dados")
    @PageableAsQueryParam
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna uma paginação com todas as respostas encontrados",
                    useReturnTypeSchema = true)
    })
    public ResponseEntity<Page<DadosListagemResposta>> listarResposta(@ParameterObject Pageable pageable) {
        Page<DadosListagemResposta> listagemRespostas = respostaRepository.findAll(pageable).map(DadosListagemResposta::new);

        return ResponseEntity.ok(listagemRespostas);
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar resposta", description = "Atualiza os dados de uma resposta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os dados da resposta atualizada", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosListagemResposta.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna uma lista com as informações dos capos inválidos", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosErrosValidacao.class)))
            })
    })
    public ResponseEntity<DadosListagemResposta> atualizar(@RequestBody DadosAtualizarResposta respostaAtualizada) {
        var resposta = respostaRepository.getReferenceById(respostaAtualizada.id());

        resposta.atualizar(respostaAtualizada);

        return ResponseEntity.ok(new DadosListagemResposta(resposta));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Remover resposta", description = "Remove do banco de dados a resposta com o ID informado")
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id da resposta a ser removida")
    })
    @ApiResponse(responseCode = "204", description = "Caso a exclusão/remoção da resposta seja feita com sucesso",
            content = {@Content(schema = @Schema())})
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        respostaRepository.findById(id).ifPresentOrElse(
                respostaRepository::delete,
                () -> {
                    throw new IllegalArgumentException("Resposta não existe, não é possível deleta-la");
                }
        );

        return ResponseEntity.noContent().build();
    }
}

