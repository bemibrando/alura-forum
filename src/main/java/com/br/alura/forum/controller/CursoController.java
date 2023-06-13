package com.filiperobot.aluraforumapi.controller;

import com.filiperobot.aluraforumapi.domain.course.*;
import com.filiperobot.aluraforumapi.domain.course.DTO.DadosCadastroCurso;
import com.filiperobot.aluraforumapi.domain.course.DTO.DadosCursoAtualizar;
import com.filiperobot.aluraforumapi.domain.course.DTO.DadosCursoCompleto;
import com.filiperobot.aluraforumapi.domain.course.DTO.DadosListagemCurso;
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
@RequestMapping("/cursos")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Endpoint para gerenciar cursos, criação, busca, listagem, atualização e remoção")
@SecurityRequirement(name = "TokenJWT")
@ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o token não seja valido ou não foi enviado",
        content = {@Content(schema = @Schema())})
public class CursoController {

    private final CursoRepository cursoRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar um curso", description = "Cadastra um curso no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Retorna os dados do curso cadastrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosCursoCompleto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna um lista com as informações dos capos inválidos", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosErrosValidacao.class)))
            })
    })
    public ResponseEntity<DadosCursoCompleto> cadastrar(
            @RequestBody @Valid DadosCadastroCurso dadosCurso, UriComponentsBuilder uriBuilder) {
        var curso = cursoRepository.save(new Curso(dadosCurso));

        var uri = uriBuilder.path("/cursos/{id}").buildAndExpand(curso.getId()).toUri();

        return ResponseEntity.created(uri).body(new DadosCursoCompleto(curso));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar curso", description = "Procura um curso com o ID informado no banco de dados.")
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id do curso a ser encontrado no banco de dados")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os dados do curso encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosCursoCompleto.class))
            })
    })
    public ResponseEntity<DadosCursoCompleto> curso(@PathVariable Long id) {
        var curso = cursoRepository.getReferenceById(id);
        return ResponseEntity.ok(new DadosCursoCompleto(curso));
    }

    @GetMapping
    @Operation(summary = "Listar cursos", description = "Busca todos os cursos do banco de dados")
    @PageableAsQueryParam
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna uma paginação com todos os cursos encontrados",
                    useReturnTypeSchema = true)
    })
    public ResponseEntity<Page<DadosListagemCurso>> listaCursos(@ParameterObject Pageable pageable) {
        Page<DadosListagemCurso> cursos = cursoRepository.findAll(pageable).map(DadosListagemCurso::new);

        return ResponseEntity.ok(cursos);
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar curso", description = "Atualiza os dados de um curso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os dados do curso atualizado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosCursoCompleto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna uma lista com as informações dos capos inválidos", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosErrosValidacao.class)))
            })
    })
    public ResponseEntity<DadosCursoCompleto> atualizar(@RequestBody @Valid DadosCursoAtualizar dadosCursoAtualizacao) {
        var curso = cursoRepository.getReferenceById(dadosCursoAtualizacao.id());

        curso.atualizar(dadosCursoAtualizacao);

        return ResponseEntity.ok(new DadosCursoCompleto(curso));
    }

    @DeleteMapping("{id}")
    @Transactional
    @Operation(summary = "Remover curso", description = "Remove do banco de dados o curso com o ID informado")
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id do curso a ser removido")
    })
    @ApiResponse(responseCode = "204", description = "Caso a exclusão/remoção do curso seja feita com sucesso",
            content = {@Content(schema = @Schema())})
    public ResponseEntity<Void> remover(@PathVariable Long id){
        cursoRepository.findById(id).ifPresentOrElse(
                cursoRepository::delete,
                ()  -> {
                    throw new IllegalArgumentException("Curso não existe, não é possível deleta-lo");
                }
        );

        return ResponseEntity.noContent().build();
    }
}
